
package gov.nih.tbi.repository.model;

import gov.nih.tbi.repository.model.hibernate.DataStoreBinaryInfo;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * An abstract class with the minimum fields required for a DataStoreInfo
 * 
 * @author mvalei
 * 
 */
@MappedSuperclass
@XmlType(namespace = "http://tbi.nih.gov/RepositorySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractDataStoreInfo
{

    /**********************************************************************/

    public abstract Long getId();

    public abstract void setId(Long id);

    public abstract boolean isTabular();

    public abstract void setTabular(boolean tabular);

    public abstract boolean isFederated();

    public abstract void setFederated(boolean federated);

    public abstract DataStoreBinaryInfo getBinaryDataStore();

    public abstract void setBinaryDataStore(DataStoreBinaryInfo binaryDataStore);

    public abstract boolean isArchived();

    public abstract void setArchived(boolean archived);

    public abstract long getDataStructureId();

    public abstract void setDataStructureId(long dataStructureId);
}
