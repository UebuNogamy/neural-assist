package part;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.patch.ApplyPatchOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.egit.core.Activator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * A helper class for displaying the apply patch wizard dialog in the Eclipse IDE.
 * Allows users to apply a patch to a specified target path.
 */
@Creatable
@Singleton
public class ApplyPatchWizardHelper
{
    @Inject
    private Logger logger;
    
    /**
     * Displays the apply patch wizard dialog to the user, allowing them to apply a patch to a specified target path.
     *
     * @param patch The patch content as a string.
     * @param targetPath The target path where the patch will be applied.
     */
    public void showApplyPatchWizardDialog(String patch, String targetPath) {
        
        // Create an InputStream from the patch string
        try (ByteArrayInputStream patchInputStream = new ByteArrayInputStream(patch.getBytes(StandardCharsets.UTF_8)))
        {
            // Create an IStorage object to wrap the InputStream
            PatchStorage patchStorage = new PatchStorage(patch);
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPart part = window.getActivePage().getActivePart();
            // TODO: for the moment assume the target is associated with the project of currently opened editor
            // which may not be true
            IProject target = getProjectOfCurrentlyOpenedEditor();
            
            ApplyPatchOperation operation = new ApplyPatchOperation(part, patchStorage, target, new CompareConfiguration());
            // Create and open the WizardDialog
            operation.openWizard();
        } 
        catch (Exception e) 
        {
            logger.error(e, e.getLocalizedMessage());
        }
    }
    

    /**
     * Returns the {@link IProject} of the project associated with the currently opened file in the text editor.
     *
     * @return The {@link IProject} of the project, or null if the active editor is not a text editor.
     */
    private IProject getProjectOfCurrentlyOpenedEditor() 
    {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = window.getActivePage();
        IEditorPart activeEditor = activePage.getActiveEditor();
        if (activeEditor instanceof ITextEditor)
        {
            ITextEditor textEditor = (ITextEditor) activeEditor;
            IFile file = textEditor.getEditorInput().getAdapter(IFile.class);
            IProject project = file.getProject();
            return project;
        }
        else
        {
            return null;
        }
    }
    private static class PatchStorage implements IStorage
    {
        private final String patch;
        public PatchStorage(String patch)
        {
            this.patch = patch;
        }
        @Override
        public <T> T getAdapter(Class<T> arg0)
        {
            return null;
        }

        @Override
        public InputStream getContents() throws CoreException
        {
            return new ByteArrayInputStream(patch.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public IPath getFullPath()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return "patch";
        }

        @Override
        public boolean isReadOnly()
        {
            return true;
        }
        
    }
    
}
