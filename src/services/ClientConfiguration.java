package services;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;


import activator.Activator;
import preferences.PreferenceConstants;

@Creatable
@Singleton
public class ClientConfiguration 
{

    public String getApiBase()
    {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return prefernceStore.getString(PreferenceConstants.API_BASE);
    }
    
    public String getApiEndPoint()
    {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return prefernceStore.getString(PreferenceConstants.API_END_POINT);
    }

//    public String getApiKey()
//    {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return prefernceStore.getString(PreferenceConstants.API_KEY);
//    }
//
//    public String getModelName()
//    {
//        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
//        return prefernceStore.getString(PreferenceConstants.MODEL_NAME);
//    }

    public String getApiUrl()
    {
    	if (getApiEndPoint().startsWith("/"))
    	{
    		return getApiBase() + getApiEndPoint();
    	}
    	else
    	{
    		return getApiBase() + "/" + getApiEndPoint();
    	}
    }
    
    public int getConnectionTimoutSeconds()
    {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Integer.parseInt(prefernceStore.getString(PreferenceConstants.CONNECTION_TIMEOUT_SECONDS));
        
    }
    
    public int getRequestTimoutSeconds()
    {
        IPreferenceStore prefernceStore = Activator.getDefault().getPreferenceStore();
        return Integer.parseInt(prefernceStore.getString(PreferenceConstants.REQUEST_TIMEOUT_SECONDS));
        
    }
    
}
