package handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.egit.core.Activator;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import model.ChatMessage;
import part.SaigaPresenter;
import prompt.ChatMessageFactory;
import prompt.Prompts;

public class NeuralAssistFixErrorsHandler
{
    @Inject
    private Logger logger;
    @Inject
    private ChatMessageFactory chatMessageFactory;
    @Inject
    private SaigaPresenter viewPresenter;
    
    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s)
    {
        String activeFile = "";
        String filePath = "";
        String ext = "";
        String fileContents = "";
        String errorMessages = "";
        
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = workspaceRoot.getProjects();

        // Get the active workbench window
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        // Get the active editor's input file
        IEditorPart activeEditor = workbenchWindow.getActivePage().getActiveEditor();
        
        if (activeEditor instanceof ITextEditor)
        {
            ITextEditor textEditor = (ITextEditor) activeEditor;
            activeFile = textEditor.getEditorInput().getName();
            // Read the content from the file
            // this fixes skipped empty lines issue
            IFile file = (IFile) textEditor.getEditorInput().getAdapter(IFile.class);
            try  
            {
                fileContents = new String(Files.readAllBytes(file.getLocation().toFile().toPath()), StandardCharsets.UTF_8);
            } 
            catch (IOException e) 
            {
                throw new RuntimeException(e);
            }
            filePath     = file.getProjectRelativePath().toString(); // use project relative path
            ext          = activeFile.substring(activeFile.lastIndexOf(".")+1);
        }
        for (IProject project : projects)
        {
            try
            {
                // Check if the project is open and has a Java nature
                if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID))
                {
                    // Get the markers for the project
                    IMarker[] markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

                    // Iterate through the markers and access the compile errors
                    for (IMarker marker : markers)
                    {
                        int severity = marker.getAttribute(IMarker.SEVERITY, -1);

                        if (severity == IMarker.SEVERITY_ERROR)
                        {
                            // This marker represents a compile error
                            String errorMessage = marker.getAttribute(IMarker.MESSAGE, "");
                            int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
                            String fileName = marker.getResource().getName();
                            // Check if the error is related to the active workbench
                            if (activeFile != null && activeFile.equals(fileName)) 
                            {
                                // Process the compile error (e.g., display it or store it for further analysis)
                                errorMessages += "Error: " + errorMessage + " at line " + lineNumber + " in file " + fileName;
                            }
                        }
                    }
                }
            }
            catch (CoreException e)
            {
            	logger.error(e, e.getMessage());
            }
        }
        if (!errorMessages.isEmpty())
        {
            Context context = new Context(filePath, fileContents, errorMessages, "", "", ext);
            ChatMessage message = chatMessageFactory.createUserChatMessage(Prompts.FIX_ERRORS, context);
            viewPresenter.onSendPredefinedPrompt(Prompts.FIX_ERRORS, message);
        }
    }
}
