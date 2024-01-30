package handlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import model.ChatMessage;
import part.SaigaPresenter;
import prompt.ChatMessageFactory;
import prompt.Prompts;

@SuppressWarnings("restriction")
public class NeuralAssistGenerateGitCommentHandler
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
        // Get the active editor
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart activeEditor = activePage.getActiveEditor();

        try
        {
            // Obtain the repository from the active editor's project
            RepositoryMapping mapping = RepositoryMapping.getMapping(activeEditor.getEditorInput().getAdapter(IResource.class));
            Repository repository = mapping.getRepository();

            // Obtain the Git object for the repository
            try (Git git = new Git(repository))
            {
                // Get the staged changes
                ObjectId head = repository.resolve("HEAD");
                if (Objects.isNull(head))
                {
                    // TODO: Handle the initial commit scenario
                    logger.info("Initial commit: No previous commits found.");
                }
                else
                {
                    AbstractTreeIterator headTree  = prepareTreeParser(repository, head);
                    AbstractTreeIterator indexTree = prepareIndexTreeParser(repository);
                    List stagedChanges = git.diff().setOldTree(headTree).setNewTree(indexTree).call();
                    
                    String patch = printChanges(git.getRepository(), stagedChanges);
                    
                    ChatMessage message = chatMessageFactory.createGenerateGitCommitCommentJob(patch);
                    viewPresenter.onSendPredefinedPrompt(Prompts.GIT_COMMENT, message);
                }
                    
            }
        }
        catch (Exception e)
        {
        	logger.error(e, e.getMessage());
        }
    }

    // Helper method to prepare the tree parser
    private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException
    {
        try (RevWalk walk = new RevWalk(repository))
        {
            RevCommit commit = walk.parseCommit(objectId);
            ObjectId treeId  = commit.getTree().getId();

            try (ObjectReader reader = repository.newObjectReader())
            {
                return new CanonicalTreeParser(null, reader, treeId);
            }
        }
    }

    // Helper method to prepare the index tree parser
    private static AbstractTreeIterator prepareIndexTreeParser(Repository repository) throws IOException
    {
        try (ObjectInserter inserter = repository.newObjectInserter(); 
              ObjectReader reader = repository.newObjectReader())
        {
            ObjectId treeId = repository.readDirCache().writeTree(inserter);
            return new CanonicalTreeParser(null, reader, treeId);
        }
    }

    private String printChanges(Repository repository, List<DiffEntry> stagedChanges) throws IOException
    {
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); 
             DiffFormatter formatter = new DiffFormatter(out))
        {
            formatter.setRepository(repository);
            formatter.setDiffComparator(RawTextComparator.DEFAULT);
            formatter.setDetectRenames(true);
            
            for (DiffEntry diff : stagedChanges)
            {
                // Print the patch to the output stream
                formatter.format(diff);
            }
            String patch = out.toString("UTF-8");
            return patch;
        }
    }
}
