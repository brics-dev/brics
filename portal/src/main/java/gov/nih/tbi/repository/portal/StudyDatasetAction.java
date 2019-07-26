package gov.nih.tbi.repository.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtFilterDescription;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.taglib.datatableDecorators.DatasetIdtListDecorator;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.Logger;


public class StudyDatasetAction extends BaseRepositoryAction {

	private static final long serialVersionUID = -219734745265403134L;
	private static Logger logger = Logger.getLogger(StudyDatasetAction.class);

	// This boolean is used to differentiate the modes the dataset table can employ. True = edit mode (the table on the
	// manage dataset) , False = view mode (the table in view study)
	public boolean editMode;

	public List<Dataset> currentDatasetList;

	public static final String COLUMN_DATA_DATASETID = "prefixId";
	public static final String COLUMN_DATA_NAME = "name";
	public static final String COLUMN_DATA_SUBMITDATE = "submitDate";
	public static final String COLUMN_DATA_TYPE = "type";
	public static final String COLUMN_DATA_STATUS = "status";
	public static final String COLUMN_DATA_RECORDCOUNT = "recordCount";


	/******************* Edit Dataset *************************************/

	private Long selectedDataset;
	private Long datasetStatusSelect;
	private List<String> selectedDatasetIds;

	/**
	 * filterErroredDatasets and includeErroredDatasets are action methods for the dataset table on the view study page
	 */

	public String filterErroredDatasets() {

		// Edit mode is set to true in editStudyDataset.jsp
		currentDatasetList = getFilteredStudyDatasets();
		return PortalConstants.ACTION_DATASET_TABLE;
	}

	public String includeErroredDatasets() {
		// add search, pagination
		// filter shared/private vs all (shared/private default)

		// Edit mode is set to true in editStudyDataset.jsp
		currentDatasetList = getStudyDatesets();
		return PortalConstants.ACTION_DATASET_TABLE;
	}

	public String datasetDatatable() {
		// datasets are already in session, so just need to modify the list as needed
		List<Dataset> studyDatasets = getStudyDatesets();

		try {
			IdtInterface idt = new Struts2IdtInterface();
			IdtRequest request = idt.getRequest();

			// operate on a copy of the studyDatasets list, not the original
			ArrayList<Dataset> outputDataset = new ArrayList<Dataset>(studyDatasets);
			idt.setTotalRecordCount(studyDatasets.size());

			// search, sort, filter, paginate
			outputDataset = searchDtDatasets(outputDataset, request);
			outputDataset = filterDtDatasets(outputDataset, request.getFilters());
			idt.setFilteredRecordCount(outputDataset.size());
			outputDataset = orderDtDatasets(outputDataset, request.getOrderColumn());
			outputDataset = paginateDtDatasets(outputDataset, request);

			idt.setList(outputDataset);
			idt.decorate(new DatasetIdtListDecorator());

			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;

		// List<Dataset> datasets = getStudyDatesets();
		// List<Dataset> setValues = new ArrayList<Dataset>();
		// for (Dataset dtst : datasets) {
		// if (dtst.getDatasetStatus().equals(DatasetStatus.PRIVATE)
		// || dtst.getDatasetStatus().equals(DatasetStatus.SHARED)) { // Uploading, loading, deleted, Archived
		// setValues.add(dtst);
		// }
		// }
		// return setValues;
	}

	// TODO: remove viewDatasetTable and viewErrorDatasetTable once the old dataset table is removed
	/**
	 * viewDatasetTable and viewErrorDatasetTable are action methods for the dataset table for the edit study workflow
	 * on the manage datasets page
	 */
	public String viewDatasetTable() {
		return PortalConstants.ACTION_DATASET_TABLE;
	}

	public String viewErrorDatasetTable() {

		setEditMode(true);
		currentDatasetList = getStudyDatesets();
		return PortalConstants.ACTION_DATASET_TABLE;
	}

	/**
	 * Initiates a new request for a dataset status
	 * 
	 * @return
	 */
	public String requestDatasetStatus() {

		DatasetStatus statusToRequest = DatasetStatus.getById(datasetStatusSelect);

		Set<Long> ids = new HashSet<Long>();

		for (String datasetIdString : selectedDatasetIds) {
			Long datasetId = Long.valueOf(datasetIdString);
			ids.add(datasetId);
		}

		List<Dataset> datasetsToUpdate = getDatasetByIds(ids);

		for (Dataset dataset : datasetsToUpdate) {
			DatasetStatus currentStatus = dataset.getDatasetStatus();

			if (validateStatusRequest(currentStatus, statusToRequest)) {
				dataset.setDatasetRequestStatus(statusToRequest);
			} else {
				this.addActionError("An illegal dataset status has been requested.");
			}

		}
		Set<Dataset> ds = new HashSet<Dataset>(datasetsToUpdate);
		updateDataset(ds);

		// only able to get to this method if you are already in edit mode
		setEditMode(true);
		// set list for table to populate
		currentDatasetList = getFilteredStudyDatasets();

		return PortalConstants.ACTION_DATASET_TABLE;
	}

	/**
	 * Returns true if the requested status is legal for a dataset with the currentStatus
	 * 
	 * @param currentStatus - current status of the dataset for which we requesting a request change
	 * @param requestedStatus - the requested status we are changing to
	 * @return
	 */
	protected static boolean validateStatusRequest(DatasetStatus currentStatus, DatasetStatus requestedStatus) {
		switch (currentStatus) {
			case ARCHIVED:
				return false;
			case ERROR:
				return false;
			case DELETED:
				throw new IllegalStateException("There should never be a dataset with a deleted status.");
			case LOADING:
				return false;
			case PRIVATE:
				if (DatasetStatus.SHARED == requestedStatus || DatasetStatus.ARCHIVED == requestedStatus
						|| DatasetStatus.DELETED == requestedStatus) {
					return true;
				} else {
					return false;
				}
			case SHARED:
				return DatasetStatus.ARCHIVED == requestedStatus ? true : false;
			case UPLOADING:
				return false;
			default: // this should never happen
				throw new UnsupportedOperationException();
		}
	}

	/**
	 * Cancels the dataset status request
	 * 
	 * @return
	 */
	public String cancelDatasetRequest() {

		Set<Long> ids = new HashSet<Long>();

		for (String datasetIdString : selectedDatasetIds) {
			Long datasetId = Long.valueOf(datasetIdString);
			ids.add(datasetId);
		}

		List<Dataset> datasetsToUpdate = getDatasetByIds(ids);

		for (Dataset dataset : datasetsToUpdate) {
			dataset.setDatasetRequestStatus(null);
		}
		Set<Dataset> ds = new HashSet<Dataset>(datasetsToUpdate);

		updateDataset(ds);

		// only able to get to this method if you are already in edit mode
		setEditMode(true);
		// set list for table to populate
		currentDatasetList = getFilteredStudyDatasets();

		return PortalConstants.ACTION_DATASET_TABLE;
	}

	/**
	 * Gets the current study's dataset submissions, filters out the errored dataset and order them by date submitted.
	 * 
	 * @return
	 * @throws UserPermissionException
	 */
	public List<Dataset> getStudyDatesets() {
		List<Dataset> setValues = new ArrayList<Dataset>();
		Study study = getSessionStudy().getStudy();
		if (study != null && study.getId() != null) {
			Long studyid = study.getId();
			Set<Long> studyIds = new HashSet<Long>();
			studyIds.add(studyid);

			List<Dataset> studyDatasets = getDatasetForStudy(studyIds);
			TreeMap<Timestamp, Dataset> map = new TreeMap<Timestamp, Dataset>(new Comparator<Timestamp>() {

				public int compare(Timestamp o1, Timestamp o2) {

					if (o1.after(o2)) {
						return -1;
					} else if (o1.before(o2) || o1.equals(o2)) {
						return 1;
					}
					return 0;
				}
			});
			setValues = new ArrayList<Dataset>();

			int i = 0;
			for (Dataset dtst : studyDatasets) {

				if (dtst.getSubmitDate() != null) {
					map.put((Timestamp) dtst.getSubmitDate(), dtst);
				} else {
					map.put(Timestamp.valueOf("1900-01-01 10:10:10." + i), dtst);
					i++;
				}
			}

			for (Dataset dt : map.values()) {
				if (!setValues.contains(dt))
					setValues.add(dt);
			}
			return setValues;
		}
		return setValues;
	}

	public List<Dataset> getFilteredStudyDatasets() {

		List<Dataset> datasets = getStudyDatesets();
		List<Dataset> setValues = new ArrayList<Dataset>();
		for (Dataset dtst : datasets) {
			if (dtst.getDatasetStatus().equals(DatasetStatus.PRIVATE)
					|| dtst.getDatasetStatus().equals(DatasetStatus.SHARED)) { // Uploading, loading, deleted, Archived
				setValues.add(dtst);
			}
		}
		return setValues;
	}

	private ArrayList<Dataset> searchDtDatasets(ArrayList<Dataset> datasets, IdtRequest request) {
		// we lowercase here to do case-insensitive matching.
		// for reasons to use the locale, @see http://mattryall.net/blog/2009/02/the-infamous-turkish-locale-bug
		String searchVal = request.getSearchVal().toLowerCase(Locale.ENGLISH);
		Set<Dataset> output = new HashSet<Dataset>();
		ArrayList<Dataset> outputDs = new ArrayList<Dataset>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (searchVal != null && searchVal != "") {
			// clones the list so we can remove them below to make the process faster
			List<IdtColumnDescriptor> columns = new ArrayList<IdtColumnDescriptor>(request.getColumnDescriptions());
			// pre-filter out columns we don't want to search
			Iterator<IdtColumnDescriptor> columnsIterator = columns.iterator();
			while (columnsIterator.hasNext()) {
				IdtColumnDescriptor column = columnsIterator.next();
				if (!column.isSearchable()) {
					columnsIterator.remove();
				}
			}

			Iterator<Dataset> dsIterator = datasets.iterator();
			// have to iterate on dataset first because that's the row and we don't want duplicate entries later
			while (dsIterator.hasNext()) {
				Dataset ds = dsIterator.next();
				// we lowercase here because we lowercased searchVal for case-insensitive searching
				String dsPrefix = ds.getPrefixedId().toLowerCase(Locale.ENGLISH);
				String dsName = ds.getName().toLowerCase(Locale.ENGLISH);
				String dsSubmitDate = (ds.getSubmissionDate() != null) ? sdf.format(ds.getSubmissionDate()) : "";
				String dsType = ds.getFileTypeString().toLowerCase(Locale.ENGLISH);
				String dsCount = (ds.getRecordCount() != null) ? ds.getRecordCount().toString() : "0";
				String dsStatus = "";
				if (ds.getDatasetRequestStatus() != null) {
					dsStatus = ds.getDatasetRequestStatus().getName().toLowerCase(Locale.ENGLISH);
				} else {
					dsStatus = ds.getDatasetStatus().getName().toLowerCase(Locale.ENGLISH);
				}

				Iterator<IdtColumnDescriptor> colIterator = columns.iterator();
				while (colIterator.hasNext()) {
					IdtColumnDescriptor column = colIterator.next();
					String data = column.getData();

					if ((data.equals("name") && dsName.contains(searchVal))
							|| (data.equals("prefixId") && dsPrefix.contains(searchVal))
							|| (data.equals("submitDate") && dsSubmitDate.contains(searchVal))
							|| (data.equals("type") && dsType.toLowerCase().contains(searchVal))
							|| (data.equals("status") && dsStatus.contains(searchVal))
							|| (data.equals("recordCount") && dsCount.equals(searchVal))) {
						output.add(ds);
					}
				}
			}
			outputDs.addAll(output);
		} else {
			outputDs = datasets;
		}
		return outputDs;
	}

	private ArrayList<Dataset> filterDtDatasets(ArrayList<Dataset> datasets, List<IdtFilterDescription> filters) {
		// @formatter:off
		/*
		 * Possible filters for this table: 
		 * Name             Possible Values         Meaning 
		 * -------------------------------------------
		 * failedDatasets   hide                    Show only Shared & Private 
		 *                  show
		 */
		// @formatter:on
		ArrayList<Dataset> output = new ArrayList<Dataset>();
		if (filters.size() != 0) {
			Iterator<Dataset> dsIterator = datasets.iterator();
			while (dsIterator.hasNext()) {
				for (IdtFilterDescription filter : filters) {
					if (filter.getName().equals("filterStatus") && filter.getValue().equals("hide")) {
						Dataset ds = dsIterator.next();
						DatasetStatus status = null;
						if (ds.getDatasetRequestStatus() != null) {
							status = ds.getDatasetRequestStatus();
						} else {
							status = ds.getDatasetStatus();
						}
						if (status.equals(DatasetStatus.PRIVATE) || status.equals(DatasetStatus.SHARED)) {
							output.add(ds);
						}

					} else {
						// break out early to give a full list of results
						return datasets;
					}
				}
			}
		} else {
			output = datasets;
		}
		return output;
	}

	private ArrayList<Dataset> orderDtDatasets(ArrayList<Dataset> datasets, IdtColumnDescriptor orderColumn)
			throws InvalidColumnException {

		if (orderColumn != null) {
			Comparator<Dataset> comparator = DatasetComparatorFactory.getComparator(orderColumn.getName());

			if (IdtRequest.ORDER_ASCENDING.equals(orderColumn.getOrderDirection())) {
				Collections.sort(datasets, comparator);
			} else {
				Collections.sort(datasets, Collections.reverseOrder(comparator));
			}
		}

		return datasets;
	}

	private ArrayList<Dataset> paginateDtDatasets(List<Dataset> datasets, IdtRequest request) {
		int startIndex = request.getStart();

		// use the startIndex + pageSize or just the dataset list size as the end index, which ever is smaller
		int endIndex = Math.min(startIndex + request.getLength(), datasets.size());

		ArrayList<Dataset> output = new ArrayList<Dataset>(datasets.subList(startIndex, endIndex));

		return output;
	}

	public Study getCurrentStudy() {

		if (getSessionStudy() != null) {
			return getSessionStudy().getStudy();
		} else {
			return null;
		}
	}

	public List<Dataset> getCurrentDatasetList() {
		return currentDatasetList;
	}

	public void setCurrentDatasetList(List<Dataset> currentDatasetList) {
		this.currentDatasetList = currentDatasetList;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public List<String> getSelectedDatasetIds() {
		return selectedDatasetIds;
	}

	public void setSelectedDatasetIds(List<String> selectedDatasetIds) {
		this.selectedDatasetIds = selectedDatasetIds;
	}

	public Long getSelectedDataset() {
		return selectedDataset;
	}

	public void setSelectedDataset(Long selectedDataset) {
		this.selectedDataset = selectedDataset;
	}

	public Long getDatasetStatusSelect() {
		return datasetStatusSelect;
	}

	public void setDatasetStatusSelect(Long datasetStatusSelect) {
		this.datasetStatusSelect = datasetStatusSelect;
	}

}
