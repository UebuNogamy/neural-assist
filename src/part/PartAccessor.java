package part;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * Provides access to the SaigaViewPart within the application model.
 * This class is responsible for locating and retrieving the SaigaViewPart using the element ID.
 */
@Creatable
@Singleton
public class PartAccessor
{
    @Inject
    private MApplication application;
    @Inject
    private EModelService modelService; 
    
    /**
     * Finds the SaigaViewPart in the application model by its element ID.
     *
     * @return an Optional containing the SaigaViewPart if found, otherwise an empty Optional
     */
    public Optional<SaigaViewPart> findMessageView() 
    {
        // Find the MessageView by element ID in the application model
    	return modelService.findElements(application, MPart.class, EModelService.IN_ACTIVE_PERSPECTIVE, element -> element.getElementId().equalsIgnoreCase("neural-assist.partdescriptor.chatgptview"))
	    	.stream()
	        .findFirst()
	        .map( mpart -> mpart.getObject() )
	        .map( SaigaViewPart.class::cast );
    }

    
}
