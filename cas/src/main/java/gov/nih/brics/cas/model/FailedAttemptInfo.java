package gov.nih.brics.cas.model;

import java.util.Date;

public class FailedAttemptInfo {
	private int failedIntervalCount;
	private int failedConsecutiveCount;
	private Date failedIntervalStart;

	public int getFailedIntervalCount() {
		return failedIntervalCount;
	}

	public void setFailedIntervalCount(int failedIntervalCount) {
		this.failedIntervalCount = failedIntervalCount;
	}

	public int getFailedConsecutiveCount() {
		return failedConsecutiveCount;
	}

	public void setFailedConsecutiveCount(int failedConsecutiveCount) {
		this.failedConsecutiveCount = failedConsecutiveCount;
	}

	public Date getFailedIntervalStart() {
		return failedIntervalStart;
	}

	public void setFailedIntervalStart(Date failedIntervalStart) {
		this.failedIntervalStart = failedIntervalStart;
	}
}
