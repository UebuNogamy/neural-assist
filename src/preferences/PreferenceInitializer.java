package preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import activator.Activator;
import prompt.PromptLoader;
import prompt.Prompts;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.API_BASE, "http://sng-oatp-01:8090");
        store.setDefault(PreferenceConstants.API_END_POINT, "/v1/chat/completions");
//        store.setDefault(PreferenceConstants.API_KEY, "");
//        store.setDefault(PreferenceConstants.MODEL_NAME, "gpt-4");
        store.setDefault(PreferenceConstants.CONNECTION_TIMEOUT_SECONDS, 10);
        store.setDefault(PreferenceConstants.REQUEST_TIMEOUT_SECONDS, 30);
        
        PromptLoader promptLoader = new PromptLoader();
        for (Prompts prompt : Prompts.values())
        {
            store.setDefault(prompt.preferenceName(), promptLoader.getRawPrompt(prompt.getFileName()));
        }
    }
}
