package com.github.gradusnikov.eclipse.assistai.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.github.gradusnikov.eclipse.assistai.Activator;

public class AssistAICodeRefactorHandler
{


    @Inject
    private JobFactory jobFactory;
    
    public AssistAICodeRefactorHandler()
    {
    }



    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s)
    {
        // Get the active editor
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart activeEditor = activePage.getActiveEditor();

        // Check if it is a text editor
        if (activeEditor instanceof ITextEditor)
        {
            ITextEditor textEditor = (ITextEditor) activeEditor;

            // Retrieve the document and text selection
            ITextSelection textSelection = (ITextSelection) textEditor.getSelectionProvider().getSelection();

            Activator.getDefault().getLog().info("Text selection:\n" + textSelection);

            IDocument document  = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
            String documentText = document.get();
            String selectedText = textSelection.getText();
            String fileName     = activeEditor.getEditorInput().getName();
            
            Activator.getDefault().getLog().info("Selected Text:\n" + textSelection.getText());
            
            // get java elements
            String selectedJavaElement = "";
            String selectedJavaType = "";
            
            IJavaElement compilationUnit = JavaUI.getEditorInputJavaElement(textEditor.getEditorInput());
            if (compilationUnit instanceof ICompilationUnit)
            {
                IJavaElement selectedElement;
                try
                {
                    selectedElement = ((ICompilationUnit) compilationUnit).getElementAt( textSelection.getOffset( ));
                    if (selectedElement != null )
                    {
                        switch ( selectedElement.getElementType() )
                        {
                            case IJavaElement.METHOD:
                                selectedJavaElement = selectedElement.toString();
                                selectedJavaType = "method";
                                break;
                            case IJavaElement.TYPE:
                                selectedJavaElement = selectedElement.toString();
                                
                                selectedJavaType = "type";
                                break;
                            case IJavaElement.FIELD:
                                selectedJavaElement = selectedElement.toString();
                                selectedJavaType = "field";
                                break;
                            case IJavaElement.LOCAL_VARIABLE:
                                selectedJavaElement = selectedElement.toString();
                                selectedJavaType = "variable";
                                break;
                        }
                    }
                }
                catch (JavaModelException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Job job = jobFactory.createRefactorJob(documentText, selectedText, fileName);
//            Job job = jobFactory.createJavaDocJob(documentText, selectedJavaElement, selectedJavaType);
            job.schedule();
        }
    }
}
