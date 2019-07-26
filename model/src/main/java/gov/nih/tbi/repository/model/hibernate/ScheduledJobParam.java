package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.JobParameterKey;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by amakar on 9/7/2016.
 */
@Entity
@Table(name = "SCHEDULED_JOB_PARAM")
@XmlRootElement(name = "scheduledJobParam")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScheduledJobParam implements Serializable {


    private static final long serialVersionUID = -7657807212329921234L;

    @EmbeddedId
    private ScheduledJobParamPK key;

    @MapsId("jobID") //references EmbeddedId's property
    @JoinColumn(name = "SCHEDULED_JOB_ID", referencedColumnName = "ID")
    @ManyToOne
    private ScheduledJob job;

    @Column(name = "PARAM_VAL")
    private String paramVal;

    public ScheduledJobParam() {

        //System.out.println("In ScheduledJobParam()");
    }

    public ScheduledJobParam(ScheduledJob _job, JobParameterKey _key, String _paramVal) {

        this.key = new ScheduledJobParamPK(_job, _key);
        this.job = _job;
        this.paramVal = _paramVal;
    }

    public ScheduledJobParamPK getKey() {
        return this.key;
    }

    public ScheduledJob getScheduledJob() {
        return this.job;
    }

    public String getValue() {
        return this.paramVal;
    }

    @Override
    public boolean equals(Object obj) {

        //System.out.println("In ScheduledJobParam.equals");

        boolean result = false;

        if(obj instanceof ScheduledJobParam) {

            ScheduledJobParam objAsParam = (ScheduledJobParam)obj;

            ScheduledJobParamPK objKey = objAsParam.getKey();

            result =  objKey.equals(this.key);
        }

        return result;
    }

    /* @Override
    public int hashCode() {
        int result =  5;

        //String paramName = this.getKey().getName();

        int keyCode = this.key.hashCode();

        result = 37 * result + keyCode;

        int valCode = this.paramVal.hashCode();

        result = 37 * result + valCode;

        return result;
    } */

    @Override
    public String toString() {
        //System.out.println("In ScheduledJobParam.toString");
        return super.toString();
    }
}
