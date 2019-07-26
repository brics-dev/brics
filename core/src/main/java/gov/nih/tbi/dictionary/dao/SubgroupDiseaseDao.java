
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.SubgroupDisease;

import java.util.List;

public interface SubgroupDiseaseDao extends GenericDao<SubgroupDisease, Long>
{

    /**
     * Returns a list of subgroups by a single disease
     * 
     * @param disease
     * @return
     */
    public List<Subgroup> getByDisease(Disease disease);

    public Disease getDiseaseBySubGroup(Subgroup subgroup);

}
