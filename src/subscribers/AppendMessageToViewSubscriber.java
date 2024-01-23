package subscribers;

import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.egit.core.Activator;

import model.ChatMessage;
import model.Incoming;
import part.SaigaPresenter;

@Creatable
@Singleton
public class AppendMessageToViewSubscriber implements Flow.Subscriber<Incoming>
{
    @Inject
    private ILog logger;
    
    private Flow.Subscription subscription;
    
    private ChatMessage message;
    private SaigaPresenter presenter;
    
    public AppendMessageToViewSubscriber( )
    {
    }
    
    public void setPresenter(SaigaPresenter presenter)
    {
        this.presenter = presenter;
    }

    @Override
    public void onSubscribe(Subscription subscription)
    {
        Objects.requireNonNull( presenter );
        this.subscription = subscription;
        message = presenter.beginMessageFromAssistant();
        subscription.request(1);
    }

    @Override
    public void onNext(Incoming item)
    {
        Objects.requireNonNull( presenter );
        Objects.requireNonNull( message );
        Objects.requireNonNull( subscription );
        message.append(item.getPayload());
        presenter.updateMessageFromAssistant( message );
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable)
    {
        message = null;
        logger.log(new Status(IStatus.ERROR, Activator.getPluginId(), throwable.getMessage(), throwable));
    }

    @Override
    public void onComplete()
    {
        Objects.requireNonNull( presenter );
        message = null;
        subscription = null;
        presenter.endMessageFromAssistant();
        subscription.request(1);
    }
    

}
