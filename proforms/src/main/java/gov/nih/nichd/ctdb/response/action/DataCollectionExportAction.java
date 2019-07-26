package gov.nih.nichd.ctdb.response.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.form.domain.DataCollectionExport;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;

public class DataCollectionExportAction extends BaseAction {
	private static final long serialVersionUID = -3114723249529471699L;

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public String exportDataCollection() throws Exception {
		// initialize managers
		ResponseManager rm = new ResponseManager();
		FormManager fm = new FormManager();
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();

		// get list of selected aformIds from request
		String aformIdsString = request.getParameter("aformIds");

		// convert it to int array
		String[] aformIdsStringArray = aformIdsString.split(",");
		int[] aformIds = new int[aformIdsStringArray.length];
		for (int i = 0; i < aformIds.length; i++) {
			aformIds[i] = Integer.valueOf(aformIdsStringArray[i]);
		}

		// sort the array in ascending order...this should, as a result, sort them by date
		Arrays.sort(aformIds);

		// just use the first aformid to get the form
		int eformId = rm.getFormId(aformIds[0]);
		String shortName = fm.getEFormShortNameByAFormId(aformIds[0]);
		Form form = fsUtil.getEformFromBrics(request, shortName);
		form.setId(eformId);


		// get list of all repeatable section parents in the form...this represents how many different repeatable groups
		// there are
		List<Integer> repeatableSectionParentIds = fsUtil.getRepeableSectionParentSectionIds(form);

		// for each repeatable section parent, find out max repeated section showing from visibleadminsteredsection
		// based on all the aformids for each repeatable section parent
		HashMap<Integer, Integer> maxRepeatedSectionMap = new HashMap<Integer, Integer>();
		for (Integer repeatedSectionParentId : repeatableSectionParentIds) {
			int max = rm.getMaxRepeatedSectionsForAdminForms(form, aformIds, repeatedSectionParentId);
			maxRepeatedSectionMap.put(repeatedSectionParentId, Integer.valueOf(max));
		}

		// set up response out
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		String formName = form.getName();
		formName = formName.replaceAll("\\s", "");
		String fileName = "exportCollections_" + formName + "_" + df.format(new Date()) + ".csv";
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
		ServletOutputStream out = response.getOutputStream();

		// gets all the data for each aformId
		HashMap<Integer, HashMap<String, DataCollectionExport>> aformIdsDCEMap =
				rm.getDataCollectionExport(aformIds, form);

		// iterate over each aform in this hashmap
		int aformCounter = 0;
		for (Integer aformIdInteger : aformIdsDCEMap.keySet()) {
			// get the locked data which is in hashmap with section_question as key for the aformId
			HashMap<String, DataCollectionExport> dceMap = aformIdsDCEMap.get(aformIdInteger);

			// if there are no answers, dont need to try and write anything
			if (dceMap.keySet().size() == 0) {
				continue;
			}

			// get the values out of the map and sort by the order of each question in the form
			List<DataCollectionExport> dceList = new ArrayList<DataCollectionExport>(dceMap.values());
			Collections.sort(dceList, new Comparator<DataCollectionExport>() {
				public int compare(DataCollectionExport o1, DataCollectionExport o2) {
					return Integer.valueOf(o1.getOrder()).compareTo(Integer.valueOf(o2.getOrder()));
				}
			});

			// write out column headers
			if (aformCounter == 0) {
				StringBuffer columnHeadersSB = new StringBuffer();
				int repeatedSectionCounter = 1;
				Integer repeatedSectionParentKey = null;
				for (int k = 0; k < dceList.size(); k++) {
					DataCollectionExport dce = (DataCollectionExport) dceList.get(k);
					boolean isRepeatable = dce.isRepeatable();
					int sectionId = dce.getSectionId();
					int repeatedSectionParentId = dce.getRepeatedSectionParent();
					int questionOrder = dce.getQuestionOrder();

					if (isRepeatable && repeatedSectionParentId == -1) {
						repeatedSectionCounter = 1;
						repeatedSectionParentKey = sectionId;
					} else if (isRepeatable && repeatedSectionParentId != -1 && questionOrder == 1) {
						repeatedSectionCounter++;
						repeatedSectionParentKey = repeatedSectionParentId;
					}

					String columnLabel = dce.getColumnLabel();

					if (!isRepeatable) {
						columnLabel = StringEscapeUtils.escapeCsv(columnLabel);
						columnHeadersSB.append(columnLabel);
						columnHeadersSB.append(",");
					} else {
						int max = maxRepeatedSectionMap.get(repeatedSectionParentKey);
						if (repeatedSectionCounter <= max) {
							columnLabel = StringEscapeUtils.escapeCsv(columnLabel);
							columnHeadersSB.append(columnLabel);
							columnHeadersSB.append(",");
						}
					}


				}

				// remove last ,
				columnHeadersSB.deleteCharAt(columnHeadersSB.length() - 1);

				out.println(columnHeadersSB.toString());


			}

			// write out the data
			StringBuffer dataSB = new StringBuffer();
			int repeatedSectionCounter = 1;
			Integer repeatedSectionParentKey = null;
			for (int k = 0; k < dceList.size(); k++) {
				DataCollectionExport dce = (DataCollectionExport) dceList.get(k);
				String submitAnswer = dce.getSubmitAnswer();
				boolean isRepeatable = dce.isRepeatable();
				int sectionId = dce.getSectionId();
				int repeatedSectionParentId = dce.getRepeatedSectionParent();
				int questionOrder = dce.getQuestionOrder();

				if (isRepeatable && repeatedSectionParentId == -1) {
					repeatedSectionCounter = 1;
					repeatedSectionParentKey = sectionId;
				} else if (isRepeatable && repeatedSectionParentId != -1 && questionOrder == 1) {
					repeatedSectionCounter++;
					repeatedSectionParentKey = repeatedSectionParentId;
				}

				if (!isRepeatable) {
					if (submitAnswer != null) {
						submitAnswer = StringEscapeUtils.escapeCsv(submitAnswer);
						dataSB.append(submitAnswer);
						dataSB.append(",");
					} else {
						dataSB.append("");
						dataSB.append(",");
					}

				} else {
					int max = maxRepeatedSectionMap.get(repeatedSectionParentKey);
					if (repeatedSectionCounter <= max) {
						if (submitAnswer != null) {
							submitAnswer = StringEscapeUtils.escapeCsv(submitAnswer);
							dataSB.append(submitAnswer);
							dataSB.append(",");
						} else {
							dataSB.append("");
							dataSB.append(",");
						}
					}
				}


			}

			// remove last ,
			dataSB.deleteCharAt(dataSB.length() - 1);

			out.println(dataSB.toString());

			aformCounter++;
		}


		out.flush();
		out.close();

		return null;

	}



}
