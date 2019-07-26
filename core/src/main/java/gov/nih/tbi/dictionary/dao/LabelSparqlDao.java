
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericSparqlDao;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;

import java.util.List;

public interface LabelSparqlDao extends GenericSparqlDao<Keyword>
{

    /**
     * Returns a keyword object by the given name. This keyword object contains a name, uri, and count. Returns null if
     * keyword does not exist
     * 
     * @param name
     * @return
     */
    public Keyword getByName(String name);

    /**
     * Returns a list of keywords that contain the given string. Search is case insensitive.
     * 
     * @param searchKey
     * @return
     */
    public List<Keyword> search(String searchKey);
}
