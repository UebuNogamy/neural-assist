package commands;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

@Creatable
public class CallHierarchyHelper
{
    public void getCallHierarchy(ICompilationUnit selectedElement)
    {
        if (selectedElement.getElementType() == IJavaElement.METHOD)
        {
            IMethod selectedMethod = (IMethod) selectedElement;
            CallHierarchy callHierarchy = CallHierarchy.getDefault();
            MethodWrapper[] callers = callHierarchy.getCallerRoots(new IMethod[] {selectedMethod});
            traverseCallHierarchy(callers, 0);
        }        
    }
    
    private void traverseCallHierarchy(MethodWrapper[] callers, int level)
    {
        for (MethodWrapper caller : callers)
        {
            IMethod method = (IMethod) caller.getMember();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < level; i++) sb.append("  ");
            System.out.println(sb.toString() + method.getElementName());

            // Recurse into the callers of the current method
            traverseCallHierarchy(caller.getCalls(new NullProgressMonitor()), level + 1);
        }
    }
}
