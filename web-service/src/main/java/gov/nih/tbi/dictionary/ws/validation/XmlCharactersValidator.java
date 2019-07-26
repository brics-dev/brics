
package gov.nih.tbi.dictionary.ws.validation;

import gov.nih.tbi.dictionary.model.hibernate.MapElement;

/**
 * Implementation of cell validator that validates cell for XML
 * 
 * @author Francis Chen
 * 
 */
public class XmlCharactersValidator extends CellValidator
{

    public XmlCharactersValidator()
    {

        super();
    }

    /**
     * Makes sure each character in the cell are valid for XML
     */
    @Override
    public boolean validate(MapElement iElement, String data)
    {

        char current; // Used to reference the current character.

        if (data == null || ("".equals(data)))
        {
            return true;
        }

        for (int i = 0; i < data.length(); i++)
        {
            current = data.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if (!(current == 0x9 || current == 0xA || current == 0xD || (current >= 0x20 && current <= 0xD7FF)
                    || (current >= 0xE000 && current <= 0xFFFD) || (current >= 0x100000 && current <= 0x10FFFF)))
            {
                return false;
            }
        }

        return true;
    }
}
