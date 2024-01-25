package part;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.egit.core.Activator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;

import jobs.NeuralAssistJobConstants;
import jobs.SendConversationJob;
import model.ChatMessage;
import model.Conversation;
import prompt.ChatMessageFactory;
import prompt.Prompts;
import subscribers.AppendMessageToViewSubscriber;

@Creatable
@Singleton
public class SaigaPresenter
{
    @Inject
    private Logger logger;
    
    @Inject
    private PartAccessor partAccessor;
    
    @Inject
    private Conversation conversation;
    
    @Inject
    private ChatMessageFactory chatMessageFactory;        

    @Inject
    private IJobManager jobManager;
    
    @Inject
    private Provider<SendConversationJob> sendConversationJobProvider;
    
    @Inject
    private AppendMessageToViewSubscriber appendMessageToViewSubscriber;
    
    @Inject
    private ApplyPatchWizardHelper applyPatchWizzardHelper;
    
    @PostConstruct
    public void init()
    {
        appendMessageToViewSubscriber.setPresenter( this );
    }
    
    public void onClear()
    {
        onStop();
        conversation.clear();
        partAccessor.findMessageView().ifPresent( SaigaViewPart::clearChatView );
    }
    public void on( String text )
    {
        
    }

    public void onSendUserMessage( String text )
    {
        logger.info("Send user message");
        ChatMessage message = chatMessageFactory.createUserChatMessage( () -> text );
        conversation.add( message );
        partAccessor.findMessageView().ifPresent( part -> { 
            part.clearUserInput(); 
            part.appendMessage( message.getId(), message.getRole() );
            part.setMessageHtml( message.getId(), message.getContent() );
        });
        sendConversationJobProvider.get().schedule();
    }


    public ChatMessage beginMessageFromAssistant()
    {   
        ChatMessage message = chatMessageFactory.createAssistantChatMessage("");
        conversation.add(message);
        partAccessor.findMessageView().ifPresent(messageView -> {
                messageView.appendMessage(message.getId(), message.getRole());
                messageView.setInputEnabled( false );
            });
        return message;
    }


    public void updateMessageFromAssistant( ChatMessage message )
    {
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.setMessageHtml( message.getId(), message.getContent() );   
        });
    }


    public void endMessageFromAssistant()
    {
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.setInputEnabled( true );
        });
    }
    /**
     * Cancels all running Saiga jobs
     */
    public void onStop()
    {
        var jobs = jobManager.find( null );
        Arrays.stream( jobs )
              .filter( job -> job.getName().startsWith( NeuralAssistJobConstants.JOB_PREFIX ) )
              .forEach( Job::cancel );
        
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.setInputEnabled( true );
        });
    }
    /**
     * Copies the given code block to the system clipboard.
     *
     * @param codeBlock The code block to be copied to the clipboard.
     */
    public void onCopyCode( String codeBlock )
    {
        var clipboard    = new Clipboard(PlatformUI.getWorkbench().getDisplay());
        var textTransfer = TextTransfer.getInstance();
        clipboard.setContents(new Object[] { codeBlock }, new Transfer[] { textTransfer });
        clipboard.dispose();
    }

    public void onApplyPatch( String codeBlock )
    {
        applyPatchWizzardHelper.showApplyPatchWizardDialog( codeBlock, null );
        
    }

    public void onSendPredefinedPrompt( Prompts type, ChatMessage message )
    {
        conversation.add( message );
        
        // update view
        partAccessor.findMessageView().ifPresent(messageView -> {
            messageView.appendMessage( message.getId(), message.getRole() );
            messageView.setMessageHtml( message.getId(), type.getDescription() );
        });        
        
        // schedule message
        sendConversationJobProvider.get().schedule();
    }
}
