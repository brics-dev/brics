
	
package gov.nih.tbi.dictionary.validators;
 
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;

public class JAXBValidationEventCollector extends ValidationEventCollector {

    @Override
    public boolean handleEvent(ValidationEvent event) {
        super.handleEvent(event);
        return true;
    }

}