package services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.egit.core.Activator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import commands.FunctionExecutorProvider;
import model.ChatMessage;
import model.Conversation;
import model.Incoming;
import model.Type;
import prompt.PromptLoader;

/**
 * A Java HTTP client for streaming requests to API.
 * This class allows subscribing to responses received from the API and processes the chat completions.
 */
@Creatable
public class StreamJavaHttpClient
{
    private SubmissionPublisher<Incoming> publisher;
    
    private Supplier<Boolean> isCancelled = () -> false;
    
    @Inject
    private Logger logger;
    
    @Inject
    private PromptLoader promptLoader;
    
    @Inject
    private ClientConfiguration configuration;
    
    @Inject
    private FunctionExecutorProvider functionExecutor;
    
    public StreamJavaHttpClient()
    {
        publisher = new SubmissionPublisher<>();

    }
    
    public void setCancelProvider(Supplier<Boolean> isCancelled)
    {
        this.isCancelled = isCancelled;
    }
    
    /**
     * Subscribes a given Flow.Subscriber to receive String data from API responses.
     * @param subscriber the Flow.Subscriber to be subscribed to the publisher
     */
    public synchronized void subscribe(Flow.Subscriber<Incoming> subscriber)
    {
        publisher.subscribe(subscriber);
    }
    /**
     * Returns the JSON request body as a String for the given prompt.
     * @param prompt the user input to be included in the request body
     * @return the JSON request body as a String
     */
    private String getRequestBody(Conversation prompt)
    {
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            LinkedHashMap<String, Object> requestBody = new LinkedHashMap<>();
            ArrayList<Map<String, Object>> messages = new ArrayList<>();
    
            LinkedHashMap<String, Object> systemMessage = new LinkedHashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", promptLoader.createPromptText("system-prompt.txt"));
            messages.add(systemMessage);
    
            for (ChatMessage message : prompt.messages())
            {
            	LinkedHashMap<String,Object> userMessage = new LinkedHashMap<>();
                userMessage.put("role", message.getRole());
                if (Objects.nonNull(message.getContent()))
                {
                    userMessage.put("content", message.getContent());
                }
                if (Objects.nonNull(message.getFunctionCall()))
                {
                	LinkedHashMap<String, Object> functionCall = new LinkedHashMap<>();
                    functionCall.put("name", message.getFunctionCall().getName());
                    functionCall.put("arguments", objectMapper.writeValueAsString(message.getFunctionCall().getArguments()));
                    
                    userMessage.put("function_call", functionCall);
                }
                if (Objects.nonNull(message.getName()))
                {
                    userMessage.put("name", message.getName());
                }
                messages.add(userMessage);
            }
//            requestBody.put("model", configuration.getModelName());
            requestBody.put("functions", AnnotationToJsonConverter.convertDeclaredFunctionsToJson(functionExecutor.get().getFunctions()));
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.1);
            requestBody.put("stream", true);
            requestBody.put("max_tokens", 3000);
            requestBody.put("top_k", 30);
            requestBody.put("top_p", 0.9);
            requestBody.put("repeat_penalty", 1.1);
    
            String jsonString;
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody);
            return jsonString;
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Creates and returns a Runnable that will execute the HTTP request to API
     * with the given conversation prompt and process the responses.
     * <p>
     * Note: this method does not block and the returned Runnable should be executed
     * to perform the actual HTTP request and processing.
     *
     * @param prompt the conversation to be sent to the API
     * @return a Runnable that performs the HTTP request and processes the responses
     */
    public Runnable run(Conversation prompt) 
    {
    	return () ->  {
    		HttpClient client = HttpClient.newBuilder()
    		                              .connectTimeout(Duration.ofSeconds(configuration.getConnectionTimoutSeconds()))
    		                              .build();
    		
    		String requestBody = getRequestBody(prompt);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(configuration.getApiUrl()))
                    .timeout(Duration.ofSeconds(configuration.getRequestTimoutSeconds() * 4))
//    				.header("Authorization", "Bearer " + configuration.getApiKey())
    				.header("Accept", "text/event-stream")
    				.header("Content-Type", "application/json")
    				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
    				.build();
    		
    		logger.info("Sending request to Saiga.\n\n" + requestBody);
    		
    		try
    		{
    			HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
    			
    			if (response.statusCode() != 200)
    			{
    			    logger.error("Request failed with status code: " + response.statusCode() + " and response body: " + response.body());
    			}
    			try (var inputStream = response.body();
    			     InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    			     BufferedReader reader = new BufferedReader(inputStreamReader)) 
    			{
    				String line;
    				while ((line = reader.readLine()) != null && !isCancelled.get())
    				{
    					if (line.startsWith("data:"))
    					{
    					    String data = line.substring(5).trim();
    						if ("[DONE]".equals(data))
    						{
    							break;
    						} 
    						else
    						{
    						    ObjectMapper mapper = new ObjectMapper();
    						    JsonNode choice = mapper.readTree(data).get("choices").get(0);
    						    JsonNode node =  choice.get("delta");
    							if (node.has("content"))
    							{
    							    String content = node.get("content").asText();
    							    if (!"null".equals(content))
    							    {
    							        publisher.submit(new Incoming(Type.CONTENT, content));
    							    }
    							}
    							if (node.has("function_call"))
    							{
    							    JsonNode functionNode = node.get("function_call");
    							    if (functionNode.has("name"))
    							    {
    							        publisher.submit(new Incoming(Type.FUNCTION_CALL, String.format("\"function_call\" : { \n \"name\": \"%s\",\n \"arguments\" :", functionNode.get("name").asText())));
    							    }
    							    if (functionNode.has("arguments"))
    							    {
    							        publisher.submit(new Incoming(Type.FUNCTION_CALL, node.get("function_call").get("arguments").asText()));
    							    }
    							}
    						}
    					}
    				}
    			}
    			if (isCancelled.get())
    			{
    				publisher.closeExceptionally(new CancellationException());
    			}
    		}
    		catch (Exception e)
    		{
    			logger.error(e, e.getMessage());
    			publisher.closeExceptionally(e);
    		} 
    		finally
    		{
    			publisher.close();
    		}
    	};
    }
}