package gov.nih.brics.cas.model;

import java.util.Date;

public class LockInfo {
	private boolean isLocked;
	private Date lockedUntil;


	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public Date getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(Date lockedUntil) {
		this.lockedUntil = lockedUntil;
	}
}
