
package services;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import commands.Function;
import commands.FunctionExecutor;
import commands.FunctionParam;

public class AnnotationToJsonConverter
{
    public static JsonNode convertDeclaredFunctionsToJson(Method ... methods)
    {
    	ArrayList<Function> functionDetailsList = new ArrayList<>();
        
        for (Method method : methods)
        {
            Function functionAnnotation = method.getAnnotation(Function.class);
            if (functionAnnotation != null)
            {
                LinkedHashMap<String, Property> properties = new LinkedHashMap<>();
                ArrayList<String> required   = new ArrayList<>();
                
                for (Parameter param : method.getParameters())
                {
                    FunctionParam functionParamAnnotation = param.getAnnotation(FunctionParam.class);
                    if (functionParamAnnotation != null)
                    {
                        String name = FunctionExecutor.toParamName(param); 
                        properties.put(name, 
                                        new Property(functionParamAnnotation.type(),
                                                      functionParamAnnotation.description()) 
                                       );
                        if (functionParamAnnotation.required())
                        {
                            required.add(name);
                        }
                    }
                }
                
                Parameters parameters = new Parameters(functionAnnotation.type(), properties, required);
                String name = FunctionExecutor.toFunctionName(method);
                functionDetailsList.add(new Function(name, 
                                                       functionAnnotation.description(), 
                                                       parameters));
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(functionDetailsList);

    }

    /**
     * Converts methods of the class that have {@link Function} annotations into
     * a JSON structure.
     * <p>
     * See <a href=
     * "https://platform.openai.com/docs/guides/gpt/function-calling">API
     * docs</a> for details.
     *
     * 
     * @param clazz
     *            a {@link Class} where we expect to find {@link Function}
     *            annotated methods.
     * @return a {@link JsonNode} in API format
     */
    public static JsonNode convertDeclaredFunctionsToJson(Class<?> clazz)
    {
        return convertDeclaredFunctionsToJson(clazz.getDeclaredMethods());
    }

}
