
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;

public class AliasAction extends DataElementAction
{

    private static final long serialVersionUID = -1295379706844012245L;

    /*****************************************************************/

    Alias currentAlias;

    String formType;

    String aliasName;

    /*****************************************************************/

    public String getAliasName()
    {

        return aliasName;
    }

    public void setAliasName(String aliasName)
    {

        this.aliasName = aliasName;
    }

    public Alias getCurrentAlias()
    {

        return currentAlias;
    }

    public void setCurrentAlias(Alias currentAlias)
    {

        this.currentAlias = currentAlias;
    }

    public DataElement getCurrentDataElement()
    {

        return getSessionDataElement().getDataElement();
    }

    public String getFormType()
    {

        return formType;
    }

    public void setFormType(String formType)
    {

        this.formType = formType;
    }

    /*****************************************************************/

    /**
     * Creates a new alias and add it to session data element.
     * 
     * @return String
     */
    public String create()
    {

        // create a new alias and set its name
        currentAlias = new Alias();
        if (aliasName != null)
        {
            currentAlias.setName(aliasName);
        }

        // Adds alias to session if its valid
        if (dictionaryManager.validateAlias(null, currentAlias, getCurrentDataElement()))
        {
            currentAlias.setDataElement(getCurrentDataElement().getStructuralObject());
            getCurrentDataElement().getAliasList().add(currentAlias);
        }

        return PortalConstants.ACTION_INPUT;
    }

    /**
     * Action for removing an existing alias.
     * 
     * @return String
     */
    public String remove()
    {

        getSessionDataElement().removeAliasByName(aliasName);

        return PortalConstants.ACTION_INPUT;
    }

    public String input()
    {

        return PortalConstants.ACTION_DISPLAY;
    }
}
