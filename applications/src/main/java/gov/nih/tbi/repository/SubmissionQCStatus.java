package gov.nih.tbi.repository;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import gov.nih.tbi.ApplicationsConstants;

public enum SubmissionQCStatus {
		PASSED(0L, "Passed", "Upload", "QC Passed"),
		EMPTY_STUDY(1L, "Empty_Study", "Empty Study", "A study must be selected, if no studies appear in the drop down menu, please return to the organization website and create one."),
		EMPTY_TICKET_NAME(2L, "Empty_Ticket_Name", "Upload Error", "You must select a file to upload."),
		INVALID_TICKET_NAME(3L, "Invalid_Ticket_Name", "Invalid Ticket Name", "A Submission Ticket was not detected. Please upload the Submission Ticket."),
		NULL_TICKET(4L, "Null_Ticket", "Invalid Ticket", "Invalid Submission Ticket."),
		INVALID_TICKET_FORMAT(5L, "Invalid_Ticket_Format", "Invalid Ticket Format", "Invalid fomrat of Submission Ticket, failed to parse and covert to a dataset for submission."),
		EMPTY_DATASET(6L, "Empty_Dataset", "Dataset Error", "You must enter a name for the Datase."),
		INVALID_DATASET_LENGTH(7L, "Invalid_DatasetNameLength", "Dataset Error", "Dataset name must not exceed " + ApplicationsConstants.MAX_DATASET_NAME_LENGTH + " characters."),
		DUPLICATE_DATASET(8L, "Duplicate_Dataset", "Dataset Error", "Dataset name must be unique"),
		ILLEGAL_MODIFICATION(9L, "Illegal_Modification", "Illegal Modification Detected", "Submission Package has been illegally modified."),
		INVALID_ENV(10L, "Invalid_Env", "Environment Mismatch", "The environment this ticket was created in does not match the upload manager's environment."),
		INVALID_VERSION(11L, "Version_Mismatch", "Version Mismatch", "The deployment version this ticket was created in does not match the upload manager's version.");

		private Long id;
		private String name;
		private String category;
		private String message;

		private static final Map<Long, SubmissionQCStatus> idLookup = new HashMap<Long, SubmissionQCStatus>();
		private static final Map<String, SubmissionQCStatus> nameLookup = new HashMap<String, SubmissionQCStatus>();

		static {
			for (SubmissionQCStatus s : EnumSet.allOf(SubmissionQCStatus.class)) {
				idLookup.put(s.getId(), s);
				nameLookup.put(s.getName().toLowerCase(), s);
			}
		}

		SubmissionQCStatus(Long _id, String _name, String _category, String _message) {
			this.id = _id;
			this.name = _name;
			this.category  = _category;
			this.message = _message;
		}

		public Long getId() {

			return id;
		}

		public String getName() {

			return name;
		}

		public String getMessage() {

			return message;
		}


		public String getCategory() {

			return category;
		}
		
		public static SubmissionQCStatus getById(Long id) {

			return idLookup.get(id);
		}

		public static SubmissionQCStatus getByName(String name) {
			if (name != null) {
				name = name.toLowerCase();
			}
			return nameLookup.get(name);
		}
	}
