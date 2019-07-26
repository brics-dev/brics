package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.DataSource;

/**
 * Model for Visualization Access Record. Contains information of user downloading data from repository or QT.
 * 
 * @author Ryan Stewart
 * 
 */
@Entity
@Table(name = "ACCESS_RECORD")
@XmlRootElement(name = "accessRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class VisualizationAccessRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9006370987863274609L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCESS_RECORD_SEQ")
	@SequenceGenerator(name = "ACCESS_RECORD_SEQ", sequenceName = "ACCESS_RECORD_SEQ",
			allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "DATASET_ID")
	private Dataset dataset;

	/**
	 * User who downloaded the data described by this entity.
	 */
	@ManyToOne
	@JoinColumn(name = "ACCOUNT_ID")
	private Account account;

	/**
	 * Date the data was added to the user queue
	 */
	@Column(name = "QUEUE_DATE")
	private Date queueDate;

	/**
	 * The number of records from the dataset that were downloaded.
	 */
	@Column(name = "RECORD_COUNT")
	private Long recordCount;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "DATA_SOURCE")
	private DataSource dataSource;

	/**
	 * This no arg constructor is required for hibernate to create AccessRecord objects through
	 * reflection.
	 */
	public VisualizationAccessRecord() {

	}

	public VisualizationAccessRecord(Dataset dataset, Account account, Date queueDate, Long recordCount,
			DataSource dataSource) {

		this.setDataset(dataset);
		this.setAccount(account);
		this.setQueueDate(queueDate);
		this.setRecordCount(recordCount);
		this.setDataSource(dataSource);
	}

	public Long getId() {

		return id;
	}

	/**
	 * @return the dataset
	 */
	public Dataset getDataset() {

		return dataset;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(Dataset dataset) {

		this.dataset = dataset;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {

		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {

		this.account = account;
	}

	/**
	 * @return the queueDate
	 */
	public Date getQueueDate() {

		return queueDate;
	}

	/**
	 * @param queueDate the queueDate to set
	 */
	public void setQueueDate(Date queueDate) {

		this.queueDate = queueDate;
	}

	/**
	 * @return the recordCount
	 */
	public Long getRecordCount() {

		return recordCount;
	}

	/**
	 * @param recordCount the recordCount to set
	 */
	public void setRecordCount(Long recordCount) {

		this.recordCount = recordCount;
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public String toString() {

		return "Access Record [id=" + getId() + ", dataset id="
				+ (getDataset() == null ? "null" : getDataset().getId()) + ", date="
				+ getQueueDate() + ", count=" + getRecordCount() + "]";
	}

}
