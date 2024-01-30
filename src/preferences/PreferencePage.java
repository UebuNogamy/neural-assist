package preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import activator.Activator;


public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    public PreferencePage()
    {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("API settings");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    public void createFieldEditors()
    {
        addField(new StringFieldEditor(PreferenceConstants.API_BASE, "&API Base:", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.API_END_POINT, "&API End Point:", getFieldEditorParent()));
//        addField(new StringFieldEditor(PreferenceConstants.API_KEY, "&API Key:", getFieldEditorParent()));
//        addField(new StringFieldEditor(PreferenceConstants.MODEL_NAME, "&Model Name", getFieldEditorParent()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

}