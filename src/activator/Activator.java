package activator;


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import preferences.PromptsPreferencePresenter;

public class Activator extends AbstractUIPlugin 
{
    private static Activator plugin = null;
    
    @Override
    public void start(BundleContext context) throws Exception 
    {
        super.start(context);
        plugin = this;
    }
    
    public static Activator getDefault() 
    {
        return plugin;
    }
    
    // rest of the class code goes here
    public PromptsPreferencePresenter getPromptsPreferncePresenter()
    {
        PromptsPreferencePresenter presenter = new PromptsPreferencePresenter( getDefault().getPreferenceStore() );
        return presenter;
    }
}
