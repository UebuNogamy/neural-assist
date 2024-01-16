package jobs;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.egit.core.Activator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import commands.FunctionExecutorProvider;
import model.ChatMessage;
import model.Conversation;
import model.FunctionCall;

@Creatable
public class ExecuteFunctionCallJob extends Job
{
    @Inject
    private ILog logger;

    @Inject
    private Provider<SendConversationJob> sendConversationJobProvider;
    
    @Inject
    private FunctionExecutorProvider functionExecutorProvider;
    @Inject
    private Conversation conversation;

    private FunctionCall functionCall;

    
    public ExecuteFunctionCallJob()
    {
        super( AssistAIJobConstants.JOB_PREFIX + " execute function call");
        
    }
    
    @Override
    protected IStatus run( IProgressMonitor monitor )
    {
        Objects.requireNonNull( functionCall );
        
        try
        {
            // 1. execute the callback
            var future = executeFunctionCall( functionCall );
            return future.get();
        }
        catch ( InterruptedException | ExecutionException e )
        {
            return new Status(IStatus.ERROR, Activator.getPluginId(), e.getMessage(), e);
        }
    }

    public void setFunctionCall( FunctionCall functionCall )
    {
        this.functionCall = functionCall;
    }
    private CompletableFuture<IStatus> executeFunctionCall( FunctionCall functionCall )
    {
    	logger.log(new Status(IStatus.INFO, Activator.getPluginId(), "Executing function call: " + functionCall));
        var functionExecutor = functionExecutorProvider.get();
        return functionExecutor.call( functionCall.getName(), functionCall.getArguments() )
        .exceptionally( th -> {
            Status status = new Status(IStatus.ERROR, Activator.getPluginId(), th.getMessage(), th);
        	logger.log(status);
            return status; 
            })
        .thenApply( result -> {
            logger.log(new Status(IStatus.INFO, Activator.getPluginId(), "Finished function call: " + functionCall));
            ChatMessage resultMessage = new ChatMessage( UUID.randomUUID().toString(), functionCall.getName(), "function" );
            String resultJson;
            try
            {
                // 2. append function_call request to the conversation
                ChatMessage message =  new ChatMessage( UUID.randomUUID().toString(), "assistant");
                message.setFunctionCall( functionCall );
                conversation.add( message );
                // 3. append function_call result to the conversation
                ObjectMapper mapper = new ObjectMapper();
                resultJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( result );
                resultMessage.setContent( resultJson );
                conversation.add( resultMessage );
                // 4. and push the conversation to the LLM
                SendConversationJob job = sendConversationJobProvider.get();
                job.schedule();
                return Status.OK_STATUS;
            }
            catch ( JsonProcessingException e )
            {
            	Status status = new Status(IStatus.ERROR, Activator.getPluginId(), e.getMessage(), e);
            	logger.log(status);
                return status; 
            }
        } );
    }
}
