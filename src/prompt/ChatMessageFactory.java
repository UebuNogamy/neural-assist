package prompt;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;

import activator.Activator;
import handlers.Context;
import model.ChatMessage;


@Creatable
@Singleton
public class ChatMessageFactory
{
	private IPreferenceStore preferenceStore;
    
	@Inject
    private PromptLoader promptLoader;

    public ChatMessageFactory()
    {
        
    }
    @PostConstruct
    public void init()
    {
        preferenceStore = Activator.getDefault().getPreferenceStore();
    }

    public ChatMessage createAssistantChatMessage(String text)
    {
        ChatMessage message = new ChatMessage(UUID.randomUUID().toString(), "assistant");
        message.setContent(text);
        return message;

    }
    
    public ChatMessage createUserChatMessage(Prompts type, Context context)
    {
        Supplier<String> promptSupplier;
        switch (type)
        {
            case DOCUMENT:
                promptSupplier = javaDocPromptSupplier(context);
                break;
            case TEST_CASE:
                promptSupplier = unitTestSupplier(context);
                break;
            case REFACTOR:
                promptSupplier = refactorPromptSupplier(context);
                break;
            case DISCUSS:
                promptSupplier = discussCodePromptSupplier(context);
                break;
            case FIX_ERRORS:
                promptSupplier = fixErrorsPromptSupplier(context);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return createUserChatMessage(promptSupplier);
    }
    
    private Supplier<String> fixErrorsPromptSupplier(Context context)
    {
        return () -> promptLoader.updatePromptText(preferenceStore.getString(Prompts.FIX_ERRORS.preferenceName()), 
                "${documentText}", context.getFileContents(),
                "${fileName}", context.getFileName(),
                "${lang}", context.getLang(),
                "${errors}", context.getSelectedContent()
               );
    }

    private Supplier<String> discussCodePromptSupplier(Context context)
    {
        return () -> promptLoader.updatePromptText(preferenceStore.getString(Prompts.DISCUSS.preferenceName()), 
                "${documentText}", context.getFileContents(),
                "${fileName}", context.getFileName(),
                "${lang}", context.getLang()
               );
    }

    private Supplier<String> javaDocPromptSupplier(Context context)
    {
        return () -> promptLoader.updatePromptText(preferenceStore.getString(Prompts.DOCUMENT.preferenceName()), 
                    "${documentText}", context.getFileContents(),
                    "${javaType}", context.getSelectedItemType(),
                    "${name}", context.getSelectedItem(),
                    "${lang}", context.getLang()
                   );
    }
    private Supplier<String> refactorPromptSupplier(Context context)
    {
        return () -> promptLoader.updatePromptText(preferenceStore.getString(Prompts.REFACTOR.preferenceName()), 
                "${documentText}", context.getFileContents(),
                "${selectedText}", context.getSelectedContent(),
                "${fileName}", context.getFileName(),
                "${lang}", context.getLang()
               );
    }
    private Supplier<String> unitTestSupplier(Context context)
    {
        return () -> promptLoader.updatePromptText(preferenceStore.getString(Prompts.TEST_CASE.preferenceName()), 
                "${documentText}", context.getFileContents(),
                "${javaType}", context.getSelectedItemType(),
                "${name}", context.getSelectedItem(),
                "${lang}", context.getLang()
               );
    }

    
    public ChatMessage createGenerateGitCommitCommentJob(String patch)
    {
        Supplier<String> promptSupplier  =  () -> promptLoader.updatePromptText(preferenceStore.getString(Prompts.GIT_COMMENT.preferenceName()), 
                "${content}", patch);
        
        return createUserChatMessage(promptSupplier);
    }
    
    public ChatMessage createUserChatMessage(Supplier<String> promptSupplier)
    {
        ChatMessage message = new ChatMessage(UUID.randomUUID().toString(), "user");
        message.setContent(promptSupplier.get());
        return message;        
    }



}
