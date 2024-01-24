package jobs;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.egit.core.Activator;

import model.Conversation;
import subscribers.HttpClientProvider;

@Creatable
public class SendConversationJob extends Job
{
    @Inject
    private Logger logger;
    
    @Inject
    private HttpClientProvider clientProvider;
    
    @Inject
    private Conversation conversation;
    
    public SendConversationJob()
    {
        super( NeuralAssistJobConstants.JOB_PREFIX + " ask ChatGPT for help");
        
    }
    @Override
    protected IStatus run(IProgressMonitor progressMonitor) 
    {
        var client = clientProvider.get();
        client.setCancelProvider( () -> progressMonitor.isCanceled() ); 
        
        try 
        {
            var future = CompletableFuture.runAsync( client.run(conversation) )
                    .thenApply( v -> Status.OK_STATUS )
                    .exceptionally(e -> new Status(IStatus.ERROR, Activator.getPluginId(), "Unable to run the task: " + e.getMessage(), e));
            return future.get();
        } 
        catch ( Exception e ) 
        {
            return new Status(IStatus.ERROR, Activator.getPluginId(), e.getMessage(), e);
        }
    }
}
