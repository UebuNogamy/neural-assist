package subscribers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import javax.inject.Inject;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.egit.core.Activator;

import model.Incoming;

@Creatable
public class PrintToFileMessageSubscriber implements Flow.Subscriber<Incoming>
{
    private Flow.Subscription subscription;
    
    @Inject
    private ILog logger;
    
    private Path getFile()
    {
        Path path = Paths.get( System.getProperty( "user.home" ), "assitai.log" );
        return path;
    }
    
    private void write( String str )
    {
        try
        {
            Files.writeString( getFile(), str, StandardOpenOption.CREATE, StandardOpenOption.APPEND );
        }
        catch ( IOException e )
        {
        	logger.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }

    }
    @Override
    public void onSubscribe(Subscription subscription)
    {
        logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Opening a log file: " + getFile()));
        this.subscription = subscription;
        write( "\n>--- BEGIN MESSAGE ---\n" );
        subscription.request(1);
    }
    @Override
    public void onNext(Incoming item)
    {
        write( item.getPayload() );
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable)
    {
    }

    @Override
    public void onComplete()
    {
        write( "\n--- END MESSAGE ---\n" );
        subscription.request(1);
    }
}
