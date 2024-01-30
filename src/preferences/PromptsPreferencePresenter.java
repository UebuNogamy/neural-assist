package preferences;

import java.util.Arrays;

import org.eclipse.jface.preference.IPreferenceStore;

import prompt.Prompts;


public class PromptsPreferencePresenter
{
    private PromptsPreferencePage view;
    private IPreferenceStore preferenceStore;
    
    public PromptsPreferencePresenter(IPreferenceStore preferenceStore)
    {
        this.preferenceStore = preferenceStore;
    }
    
    public void registerView(PromptsPreferencePage view)
    {
        this.view = view;
        initializeView();
    }
    
    private void initializeView()
    {
        String[] prompts = Arrays.stream(Prompts.values()).map(Prompts::getDescription).toArray(String[]::new);
        view.setPrompts(prompts);
    }
    
    public void setSelectedPrompt(int index)
    {
        if (index < 0)
        {
            view.setCurrentPrompt("");
        }
        else
        {
            String prompt = preferenceStore.getString(getPreferenceName(index));
            view.setCurrentPrompt(prompt);
        }
    }

    private String getPreferenceName(int index)
    {
        return Prompts.values()[index].preferenceName();
    }

    public void savePrompt(int selectedIndex, String text)
    {
        preferenceStore.setValue(getPreferenceName(selectedIndex), text);
    }

    public void resetPrompt(int selectedIndex)
    {
        String propertyName = getPreferenceName(selectedIndex);
        String defaultValue = preferenceStore.getDefaultString(propertyName);
        preferenceStore.setValue(propertyName, defaultValue);
        view.setCurrentPrompt(defaultValue);
    }

}
