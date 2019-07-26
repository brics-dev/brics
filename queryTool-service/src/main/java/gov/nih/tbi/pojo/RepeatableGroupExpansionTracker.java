
package gov.nih.tbi.pojo;

import gov.nih.tbi.repository.model.DataTableColumn;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class RepeatableGroupExpansionTracker implements Serializable {
    private static final long serialVersionUID = -5894278287480092386L;

    private String submissionId;
    private DataTableColumn column;
    
	public RepeatableGroupExpansionTracker() {}

    public RepeatableGroupExpansionTracker(String rowUri, DataTableColumn column)
    {

        this.submissionId = rowUri;
        this.column = column;
    }

    public String getSubmissionId()
    {

        return submissionId;
    }

    public void setSubmissionId(String submissionId)
    {

        this.submissionId = submissionId;
    }

    public DataTableColumn getColumn()
    {

        return column;
    }

    public void setColumn(DataTableColumn column)
    {

        this.column = column;
    }

	@Override()
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }

		if (this == obj) {
			return true;
        }

		if (obj instanceof RepeatableGroupExpansionTracker) {
			RepeatableGroupExpansionTracker rget = (RepeatableGroupExpansionTracker) obj;

			return (this.column == rget.column || (this.column != null && this.column.equals(rget.column)))
					&& (this.submissionId == rget.submissionId || (this.submissionId != null && this.submissionId
							.equals(rget.submissionId)));
        }

		return false;
    }

	@Override()
    public int hashCode()
    {

        int hashCode = 1;
        hashCode = 31 * hashCode + (submissionId == null ? 0 : submissionId.hashCode());
        hashCode = 31 * hashCode + (column == null ? 0 : column.hashCode());
        return hashCode;
    }

}
