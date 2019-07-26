package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.*;

import gov.nih.tbi.commons.model.JobParameterKey;

/**
 * Created by amakar on 9/7/2016.
 */

@Embeddable
public class ScheduledJobParamPK implements Serializable {


    private static final long serialVersionUID = -5359168530134611537L;

    //@ManyToOne
    //@JoinColumn(name = "SCHEDULED_JOB_ID")
    //private ScheduledJob job;

    @Column(name = "SCHEDULED_JOB_ID")
    private long jobID;

    @Column(name = "PARAM_NAME")
    @Enumerated(EnumType.STRING)
    private JobParameterKey key;

   // @Transient
   // private long jobID;

    public ScheduledJobParamPK() {
        //System.out.println("In ScheduledJobParamPK()");
    }

    public ScheduledJobParamPK(ScheduledJob _job, JobParameterKey _key) {
        //this.job = _job;
        this.key = _key;

        this.jobID = _job.getID();
    }

    /* public ScheduledJob getScheduledJob() {
        return this.job;
    }

    public void setScheduledJob(ScheduledJob _val) {
        this.job = _val;
    } */

    public long getJobID() {
        return jobID;
    }

    public void setJobID(long jobID) {
        this.jobID = jobID;
    }

    public JobParameterKey getKey() {
        return this.key;
    }

    public void setName(JobParameterKey _val) {
        this.key = _val;
    }

    @Override
    public boolean equals(Object obj) {

        System.out.println("In ScheduledJobParamPK.equals");

        boolean result = false;

        if(obj instanceof ScheduledJobParamPK) {

            ScheduledJobParamPK objAsPK = (ScheduledJobParamPK)obj;

            //ScheduledJob objSchedJob = objAsPK.getScheduledJob();

            long objJobID = objAsPK.getJobID();
            //ScheduledJob thisSchedJob = this.getScheduledJob();
            long thisJobID = this.jobID;

            if(objJobID == thisJobID) {

                JobParameterKey objKey = objAsPK.getKey();
                JobParameterKey thisKey = this.getKey();

                if(objKey.equals(thisKey)) {
                    result = true;
                }
            }
        }

        return result;
    }

    @Override
    public int hashCode() {

        int result = 5;

        //ScheduledJob thisSchedJob = this.getScheduledJob();
        //long thisJobID = this.jobID;

        int jobCode = (int)(this.jobID ^ (this.jobID >>> 32));

        result = 37 * result + jobCode;

        //String thisName = thisname;

        int nameCode = this.key.hashCode();

        result = 37 * result + nameCode;



        return result;
    }

    @Override
    public String toString() {
        //System.out.println("In ScheduledJobParamPK.toString");
        return super.toString();
    }
}
