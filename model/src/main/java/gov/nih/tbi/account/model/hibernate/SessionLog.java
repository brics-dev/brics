package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import gov.nih.tbi.commons.model.hibernate.User;

/**
 * Model for storing session login/logout logs.
 * These are created by CAS and accessed here.
 * 
 * @author Joshua Park
 *
 */
@Entity
@Table(name = "SESSION_LOG")
public class SessionLog implements Serializable {
	private static final long serialVersionUID = -781170829770742134L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SESSIONLOG_SEQ")
	@SequenceGenerator(name = "SESSIONLOG_SEQ", sequenceName = "SESSIONLOG_SEQ", allocationSize = 1)
	private Long id;
	
	/**
	 * Ticket Granting Ticket from CAS
	 */
	@Id
	@Column(name = "TGT")
	private String tgt;
	
	/**
	 * Username performing the action
	 */
	@Column(name = "USERNAME")
	private String username;
	
	/**
	 * Time user entered the system
	 */
	@Column(name = "TIME_IN")
	private Date timeIn;
	
	/**
	 * Time user exited the system
	 */
	@Column(name = "TIME_OUT")
	private Date timeOut;
	
	/**
	 * User's full name (last, first)
	 */
	@Column(name = "FULL_NAME")
	private String fullName;
	
	/**
	 * User's email address
	 */
	@Column(name = "EMAIL")
	private String email;
	
	@ManyToOne
	@JoinColumn(name = "ACCOUNT_ID")
	private Account account;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSessionStatus() {
		return (timeOut == null) ? "ACTIVE" : "EXPIRED";
	}
	
	public boolean equals(Object otherSession) {
		
		if (!(otherSession instanceof SessionLog)) {
			return false;
		}
		
		SessionLog other = (SessionLog) otherSession;
		
		return tgt.equals(other.getTgt()) 
				&& username.equals(other.getUsername()) 
				&& timeIn.equals(other.getTimeIn())
				&& timeOut.equals(other.getTimeOut());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tgt == null) ? 0 : tgt.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((timeIn == null) ? 0 : timeIn.hashCode());
		return result;
	}
	
	public String toString() {
		return "SessionLog [user: " + username + "(" + fullName + "<" + email + ">), timeIn: " + timeIn + ", timeOut: " + timeOut + ", tgt: " + tgt + "]";
	}
	
	public String getTgt() {
		return tgt;
	}
	public void setTgt(String tgt) {
		this.tgt = tgt;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Date getTimeIn() {
		return timeIn;
	}
	public void setTimeIn(Date timeIn) {
		this.timeIn = timeIn;
	}
	public Date getTimeOut() {
		return timeOut;
	}
	public void setTimeOut(Date timeOut) {
		this.timeOut = timeOut;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
}
