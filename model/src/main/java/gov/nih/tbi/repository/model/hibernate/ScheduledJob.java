package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.JobType;
import gov.nih.tbi.commons.model.JobStatus;
import gov.nih.tbi.commons.model.JobParameterKey;
import gov.nih.tbi.commons.model.hibernate.User;

//import javax.persistence.*;
import javax.persistence.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
//import java.util.Set;
import java.util.Map;
//import java.util.HashSet;
import java.util.List;

/**
 * Created by amakar on 9/7/2016.
 */


@NamedQueries(
        {
                @NamedQuery(
                        name = "ScheduledJob.searchByTypeAndStatus",
                        query = "from ScheduledJob j where j.typeStr = :typeStr " +
                                "and j.statusInt = :statusInt order by id"
                )
        }
)
@Entity
@Table(name = "SCHEDULED_JOB")
@XmlRootElement(name = "scheduledJob")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScheduledJob implements Serializable {

    private static final long serialVersionUID = -5851483390665496974L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCHEDULED_JOB_SEQ")
    @SequenceGenerator(name = "SCHEDULED_JOB_SEQ", sequenceName = "SCHEDULED_JOB_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "JOB_TYPE")
    private String typeStr;

    @Column(name = "STATUS")
    private int statusInt;

    @Column(name = "DO_ABORT")
    private boolean doAbort;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "START_TIME")
    private Date startTime;

    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    @Column(name = "END_TIME")
    private Date endTime;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "job", targetEntity = ScheduledJobParam.class, orphanRemoval = true)
    @XmlTransient
    @MapKey (name="key.key")
    //private Set<ScheduledJobParam> paramSet;
    private Map<JobParameterKey, ScheduledJobParam> params;

    @ManyToOne
    @JoinColumn(name = "PARENT_JOB_ID")
    private ScheduledJob parentJob;

    //@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parentJob", targetEntity = ScheduledJob.class, orphanRemoval = false)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parentJob", targetEntity = ScheduledJob.class, orphanRemoval = false)
    @OrderBy("id")
    @XmlTransient
    private List<ScheduledJob> subTasks;

    //@Transient
    //private Map<String, ScheduledJobParam> params;

    public ScheduledJob() {

        //System.out.println("In ScheduledJob()");
    }

    public ScheduledJob(JobType _type, User _user) {

        this.user = _user;
        this.setJobType(_type);
        this.setJobStatus(JobStatus.NEW);
        this.createTime = new Date();
    }

    public Long getID() {
        return this.id;
    }


    public JobType getJobType() {
        return JobType.getFromDatabaseID(this.typeStr);
    }

    public void setJobType(JobType _type) {
        this.typeStr = _type.getId();
    }

    public JobStatus getJobStatus() {
        return JobStatus.getFromDatabaseID(this.statusInt);
    }

    public void setJobStatus(JobStatus _status) {
        this.statusInt = _status.getId();
    }

    /* @PostLoad
    public void indexParameters() {

        this.params = new HashMap<>();

        for(ScheduledJobParam param : this.paramSet) {

            String paramName = param.getKey().getName();
            this.params.put(paramName, param);
        }
    } */

    private void addParameter(ScheduledJobParam _param) {



        if(this.params == null) {
            this.params = new HashMap<>();
        }

        JobParameterKey paramKey = _param.getKey().getKey();

        this.params.put(paramKey, _param);

        //this.paramSet.add(_param);
    }

    public void addParameter(JobParameterKey _paramKey, String _paramVal) {

        ScheduledJobParam param = new ScheduledJobParam(this, _paramKey, _paramVal);

        this.addParameter(param);
    }

    public void addParameter(JobParameterKey _paramKey, Long _paramVal) {

        String paramValStr = String.valueOf(_paramVal);

        this.addParameter(_paramKey, paramValStr);
    }

    /* public Set<ScheduledJobParam> getParameters() {
        return this.paramSet;
    }

    public void setParameters(Set<ScheduledJobParam> _set) {
        this.paramSet = _set;
    } */

    public String getParameterValue(JobParameterKey _paramKey) {

        String result = null;
        ScheduledJobParam param = this.params.get(_paramKey);

        if(param != null) {
            result = param.getValue();
        }

        return result;
    }

    public Long getLongParameterValue(JobParameterKey _paramKey) {

        Long result = null;

        String strVal = this.getParameterValue(_paramKey);

        if(strVal != null) {
            result = Long.valueOf(strVal);
        }

        return result;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date _val) {
        this.startTime = _val;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date _val) {
        this.updateTime = _val;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setStatus(JobStatus _val) {

        this.statusInt = _val.getId();
    }

    public JobStatus getStatus() {

        JobStatus result = JobStatus.getFromDatabaseID(this.statusInt);

        return result;
    }

    public ScheduledJob getParentJob() {
        return parentJob;
    }

    public void setParentJob(ScheduledJob _val) {
        this.parentJob = _val;
    }

    public List<ScheduledJob> getSubtasks() {
        return this.subTasks;
    }

    public boolean getAbortRequested() {
        return doAbort;
    }

    public void setAbortRequested(boolean _val) {
        this.doAbort = _val;
    }

    /* @Override
    public boolean equals(Object _obj) {
        //System.out.println("In ScheduledJob.equals");
        boolean result = super.equals(_obj);
        return result;
    }

    @Override
    public int hashCode() {
        int result =  5;

        int typeCode = this.typeStr.hashCode();

        result = 37 * result + typeCode;

        int dateCode = this.createTime.hashCode();

        result = 37 * result + dateCode;

        return result;
    } */

    @Override
    public String toString() {
        //System.out.println("In ScheduledJob.toString");
        return super.toString();
    }
}
