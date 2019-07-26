package main.java.dataimport.model;

import java.util.List;

public class SubmissionMetaData {
	// The account ID that indicates that this submission was submitted by a PSR user.
	private final static Long SUBJECT_USER_ID = -1L;
	private String userEmail;
	private String datasetName;
	private String studyPrefixedId;
	private String submissionLocation;
	private String submissionFileLocation;
	private Long administeredFormId;
	private Long lockedByAccountId;
	private String lockedDate;
	private boolean isSubjectSubmitted;

	private final static int USER_EMAIL_INDEX = 0;
	private final static int DATASET_NAME_INDEX = 1;
	private final static int STUDY_PREFIXED_ID_INDEX = 2;
	private final static int SUBMISSION_LOCATION_INDEX = 3;
	private final static int SUBMISSION_FILE_LOCATION_INDEX = 4;
	private final static int ADMINISTERED_FORM_ID_START_INDEX = 5;
	private final static int LOCKED_DATE_INDEX = 6;
	private final static int LOCKED_BY_ID_INDEX = 7;


	public SubmissionMetaData(List<String> metaDataLine) {
		this.userEmail = metaDataLine.get(USER_EMAIL_INDEX);
		this.datasetName = metaDataLine.get(DATASET_NAME_INDEX);
		this.studyPrefixedId = metaDataLine.get(STUDY_PREFIXED_ID_INDEX);
		this.submissionLocation = metaDataLine.get(SUBMISSION_LOCATION_INDEX);
		this.submissionFileLocation = metaDataLine.get(SUBMISSION_FILE_LOCATION_INDEX);

		if (metaDataLine.size() > 5) {
			try {
				this.lockedByAccountId = Long.valueOf(metaDataLine.get(LOCKED_BY_ID_INDEX));
			} catch (NumberFormatException e) {
				this.lockedByAccountId = null;
			}

			this.lockedDate = metaDataLine.get(LOCKED_DATE_INDEX);

			try {
				this.administeredFormId = Long.valueOf(metaDataLine.get(ADMINISTERED_FORM_ID_START_INDEX));
			} catch (NumberFormatException e) {
				this.administeredFormId = null;
			}

			if (SUBJECT_USER_ID.equals(this.lockedByAccountId)) {
				isSubjectSubmitted = true;
			} else {
				isSubjectSubmitted = false;
			}
		}
	}

	public SubmissionMetaData(String userEmail, String datasetName, String studyPrefixedId, String submissionLocation,
			String submissionFileLocation, Long administeredFormId, String lockedDate, Long lockedByAccountId) {
		super();
		this.userEmail = userEmail;
		this.datasetName = datasetName;
		this.studyPrefixedId = studyPrefixedId;
		this.submissionLocation = submissionLocation;
		this.submissionFileLocation = submissionFileLocation;
		this.administeredFormId = administeredFormId;
		this.lockedByAccountId = lockedByAccountId;
		this.lockedDate = lockedDate;

		if (SUBJECT_USER_ID.equals(this.lockedByAccountId)) {
			isSubjectSubmitted = true;
		} else {
			isSubjectSubmitted = false;
		}
	}

	public Long getLockedByAccountId() {
		return lockedByAccountId;
	}

	public void setLockedByAccountId(Long lockedByAccountId) {
		this.lockedByAccountId = lockedByAccountId;
	}

	public String getLockedDate() {
		return lockedDate;
	}

	public void setLockedDate(String lockedDate) {
		this.lockedDate = lockedDate;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getStudyPrefixedId() {
		return studyPrefixedId;
	}

	public void setStudyPrefixedId(String studyPrefixedId) {
		this.studyPrefixedId = studyPrefixedId;
	}

	public String getSubmissionLocation() {
		return submissionLocation;
	}

	public void setSubmissionLocation(String submissionLocation) {
		this.submissionLocation = submissionLocation;
	}

	public String getSubmissionFileLocation() {
		return submissionFileLocation;
	}

	public void setSubmissionFileLocation(String submissionFileLocation) {
		this.submissionFileLocation = submissionFileLocation;
	}

	public Long getAdministeredFormId() {
		return administeredFormId;
	}

	public void setAdministeredFormId(Long administeredFormId) {
		this.administeredFormId = administeredFormId;
	}

	public boolean isSubjectSubmitted() {
		return isSubjectSubmitted;
	}

	public void setSubjectSubmitted(boolean isSubjectSubmitted) {
		this.isSubjectSubmitted = isSubjectSubmitted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((administeredFormId == null) ? 0 : administeredFormId.hashCode());
		result = prime * result + ((datasetName == null) ? 0 : datasetName.hashCode());
		result = prime * result + ((lockedByAccountId == null) ? 0 : lockedByAccountId.hashCode());
		result = prime * result + ((lockedDate == null) ? 0 : lockedDate.hashCode());
		result = prime * result + ((studyPrefixedId == null) ? 0 : studyPrefixedId.hashCode());
		result = prime * result + ((submissionFileLocation == null) ? 0 : submissionFileLocation.hashCode());
		result = prime * result + ((submissionLocation == null) ? 0 : submissionLocation.hashCode());
		result = prime * result + ((userEmail == null) ? 0 : userEmail.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubmissionMetaData other = (SubmissionMetaData) obj;
		if (administeredFormId == null) {
			if (other.administeredFormId != null)
				return false;
		} else if (!administeredFormId.equals(other.administeredFormId))
			return false;
		if (datasetName == null) {
			if (other.datasetName != null)
				return false;
		} else if (!datasetName.equals(other.datasetName))
			return false;
		if (lockedByAccountId == null) {
			if (other.lockedByAccountId != null)
				return false;
		} else if (!lockedByAccountId.equals(other.lockedByAccountId))
			return false;
		if (lockedDate == null) {
			if (other.lockedDate != null)
				return false;
		} else if (!lockedDate.equals(other.lockedDate))
			return false;
		if (studyPrefixedId == null) {
			if (other.studyPrefixedId != null)
				return false;
		} else if (!studyPrefixedId.equals(other.studyPrefixedId))
			return false;
		if (submissionFileLocation == null) {
			if (other.submissionFileLocation != null)
				return false;
		} else if (!submissionFileLocation.equals(other.submissionFileLocation))
			return false;
		if (submissionLocation == null) {
			if (other.submissionLocation != null)
				return false;
		} else if (!submissionLocation.equals(other.submissionLocation))
			return false;
		if (userEmail == null) {
			if (other.userEmail != null)
				return false;
		} else if (!userEmail.equals(other.userEmail))
			return false;
		return true;
	}
}
