package subscribers;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.egit.core.Activator;

import com.fasterxml.jackson.databind.ObjectMapper;

import jobs.ExecuteFunctionCallJob;
import model.FunctionCall;
import model.Incoming;
import model.Type;

@Creatable
public class FunctionCallSubscriber implements Flow.Subscriber<Incoming>
{
    @Inject
    private Logger logger;
    @Inject
    private Provider<ExecuteFunctionCallJob> executeFunctionCallJobProvider;
    
    private Subscription subscription;
    private final StringBuffer jsonBuffer;
    
    public FunctionCallSubscriber()
    {
        jsonBuffer = new StringBuffer();
    }
    
    @Override
    public void onSubscribe(Subscription subscription)
    {
        this.subscription = subscription;
        jsonBuffer.setLength(0);
        subscription.request(1);

    }

    @Override
    public void onNext(Incoming item)
    {
        if (Type.FUNCTION_CALL == item.getType())
        {
            jsonBuffer.append(item.getPayload());
        }
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable)
    {
        jsonBuffer.setLength(0);
    }

    @Override
    public void onComplete()
    {
        String json = jsonBuffer.toString();
        json += "}";

        if (!json.startsWith("\"function_call\""))
        {
            subscription.request(1);
            return;
        }
        try
        {
            // 1. append assistant request to call a function to the conversation
            ObjectMapper mapper = new ObjectMapper();
            // -- convert JSON to FuncationCall object
            FunctionCall functionCall = mapper.readValue(json.replace("\"function_call\" : ",""), FunctionCall.class);
            
            ExecuteFunctionCallJob job = executeFunctionCallJobProvider.get();
            job.setFunctionCall(functionCall);
            job.schedule();
        }
        catch (Exception e)
        {
        	logger.error(e, e.getMessage());
        }
        subscription.request(1);
    }


    

}
