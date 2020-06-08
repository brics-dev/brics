package gov.nih.tbi.util;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.exceptions.FilterParseException;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.filter.Filter;
import gov.nih.tbi.filter.FilterFactory;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.service.model.DataCart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class DataCartUtil {

	private static final Logger log = Logger.getLogger(DataCartUtil.class);

	private DataCartUtil() {}


	public static DataElement getDataElement(DataCart dataCart, String formUri, String rgName, String deName) {

		FormResult form = dataCart.getFormFromCart(formUri);
		if (form == null) {
			throw new NoSuchElementException("No matching form found for uri in data cart " + formUri);
		}

		RepeatableGroup rg = form.getRepeatableGroupByName(rgName);
		if (rg == null) {
			throw new NoSuchElementException("No matching RepeatableGroup found for name in data cart " + rgName);
		}

		DataElement de = rg.getDataElement(deName);
		if (de == null) {
			throw new NoSuchElementException("No matching DataElement found for name in data cart " + deName);
		}

		return de;
	}

	public static DataElement getDataElement(DataCart dataCart, String formUri, String deName) {

		FormResult form = dataCart.getFormFromCart(formUri);
		if (form == null) {
			throw new NoSuchElementException("No matching form found for uri in data cart " + formUri);
		}

		for (RepeatableGroup rg : form.getRepeatableGroups()) {
			DataElement de = rg.getDataElement(deName);
			if (de != null) {
				return de;
			}
		}

		throw new NoSuchElementException("No matching DataElement found for name in data cart " + deName);
	}

	/**
	 * Creates a filter from the given json string and adds it to the form
	 * 
	 * @param dataCart - Data Cart object saved in session
	 * @param filterJson - Json representation of one filter
	 * @throws FilterParseException
	 */
	public static void addFilterFromJson(DataCart dataCart, JsonObject filterJson) throws FilterParseException {

		String formUri = filterJson.getAsJsonPrimitive("formUri").getAsString();
		String rgName = filterJson.getAsJsonPrimitive("groupName").getAsString();
		String deName = filterJson.getAsJsonPrimitive("elementName").getAsString();

		FormResult form = null;
		RepeatableGroup rg = null;
		DataElement de = null;

		if (formUri != null) {
			form = dataCart.getFormFromCart(formUri);

			if (form == null) {
				throw new NoSuchElementException("No matching form found for uri in data cart " + formUri);
			}
		}
		// we are only interested in repeatable group and data element if the filter is a DataElementFilter
		if (!deName.equals("Dataset") && formUri != null && rgName != null && deName != null) {
			form = dataCart.getFormFromCart(formUri);

			rg = form.getRepeatableGroupByName(rgName);

			if (rg == null) {
				throw new NoSuchElementException("No matching RepeatableGroup found for name in data cart " + rgName);
			}

			de = rg.getDataElement(deName);
		}

		Filter filter = FilterFactory.parseJson(form, rg, de, filterJson);

		if (filter != null) {
			form.getFilters().add(filter);
		}
	}

	public static String getColumnHeaderJson(InstancedDataTable instancedDataTable) {
		if (instancedDataTable == null) {
			return null;
		}

		Gson gson = new Gson();
		return gson.toJson(instancedDataTable.getHeaders());
	}


	public static String getInstancedDataTableJson(InstancedDataTable instancedDataTable, boolean showAgeRange) {

		if (instancedDataTable == null) {
			return null;
		}

		GsonBuilder gBuilder = new GsonBuilder();
		gBuilder.registerTypeAdapter(InstancedDataTable.class, new InstancedDataTableSerializer(showAgeRange));

		Gson gson = gBuilder.create();
		return gson.toJson(instancedDataTable);
	}


	public static JsonObject getDataCartToSavedQueryJson(DataCart dataCart, SavedQuery sq) {

		JsonObject output = new JsonObject();
		output.addProperty("name", sq.getName());
		output.addProperty("description", sq.getDescription());
		output.addProperty("lastUpdated", sq.getLastUpdated().toString());
		output.addProperty("outputCode", sq.getOutputCode());
		if (sq.getDateCreated() == null) {
			output.addProperty("dateCreated", "");
		} else {
			output.addProperty("dateCreated", sq.getDateCreated().toString());
		}

		if (dataCart.getInstancedDataTable() != null) {
			output.addProperty("filterExpression", dataCart.getInstancedDataTable().getFilterExpression());
		}

		JsonArray selectedForms = new JsonArray();
		List<String> selectedFormUris = dataCart.getSelectedFormUris();
		for (String selectedFormUri : selectedFormUris) {
			selectedForms.add(new JsonPrimitive(selectedFormUri));
		}

		// initialize the base properties
		JsonArray forms = new JsonArray();
		Map<Long, JsonObject> studiesJsonMap = new HashMap<Long, JsonObject>();

		Map<String, FormResult> formsInCart = dataCart.getFormsInCart();
		for (FormResult formResult : formsInCart.values()) {
			JsonObject form = new JsonObject();
			form.addProperty("name", formResult.getTitle());
			form.addProperty("shortName", formResult.getShortName());
			form.addProperty("uri", formResult.getUri());
			form.addProperty("id", formResult.getId());

			// -- STUDIES --
			JsonArray formStudyIds = new JsonArray();
			List<StudyResult> formStudies = formResult.getStudies();
			for (StudyResult studyResult : formStudies) {
				formStudyIds.add(new JsonPrimitive(studyResult.getId()));

				if (!studiesJsonMap.containsKey(studyResult.getId())) {
					JsonObject studyJson = new JsonObject();
					studyJson.addProperty("title", studyResult.getTitle());
					studyJson.addProperty("uri", studyResult.getUri());
					studyJson.addProperty("id", studyResult.getId());
					studiesJsonMap.put(studyResult.getId(), studyJson);
				}
			}
			form.add("studyIds", formStudyIds);

			// -- REPEATABLE GROUPS --
			JsonArray formGroups = new JsonArray();
			List<RepeatableGroup> rgs = formResult.getRepeatableGroups();
			if (rgs != null) {
				for (RepeatableGroup rg : rgs) {
					JsonObject rgJson = new JsonObject();
					rgJson.addProperty("name", rg.getName());
					rgJson.addProperty("uri", rg.getUri());

					// -- data elements --
					JsonArray rgDataElements = new JsonArray();
					List<DataElement> des = rg.getDataElements();
					if (des != null) {
						for (DataElement de : des) {
							JsonObject deJson = new JsonObject();
							deJson.addProperty("uri", de.getUri());
							deJson.addProperty("name", de.getTitle());
							deJson.addProperty("shortName", de.getName());
							deJson.addProperty("id", de.getId());
							deJson.addProperty("selected", de.isSelected());
							rgDataElements.add(deJson);
						}
						rgJson.add("elements", rgDataElements);
					}

					// add the RG to the json object
					formGroups.add(rgJson);
				}
				form.add("groups", formGroups);
			}

			// -- FILTERS --
			JsonArray formFilters = new JsonArray();
			List<Filter> filters = formResult.getFilters();
			if (filters != null) {
				for (Filter filter : filters) {
					formFilters.add(filter.toJson());
				}
			}
			form.add("filters", formFilters);

			forms.add(form);
		} // end each form

		output.add("selectedFormURIList", selectedForms);
		output.add("forms", forms);

		JsonArray studies = new JsonArray();
		if (studiesJsonMap != null && !studiesJsonMap.isEmpty()) {
			for (JsonObject studyJson : studiesJsonMap.values()) {
				studies.add(studyJson);
			}
		}
		output.add("studies", studies);

		return output;
	}


	public static String getBiosampleId(String formName, DataCart dataCart, String rowUri) {

		if (dataCart.getInstancedDataCache() == null) {
			throw new InstancedDataException("The row cache is empty!");
		}

		InstancedRow row = dataCart.getInstancedDataCache().getByFormName(formName).getByRowUri(rowUri);

		if (row != null) {
			for (Entry<DataTableColumn, CellValue> cellValueEntry : row.getCell().entrySet()) {
				CellValue cellValue = cellValueEntry.getValue();
				// type check the cell value since we only want to add non-repeating cells
				if (cellValue instanceof NonRepeatingCellValue) {
					NonRepeatingCellValue nrc = (NonRepeatingCellValue) cellValue;
					if (DataType.BIOSAMPLE == nrc.getDataElementType()) {
						return nrc.getValue();
					}
				}
			}
		}

		return null;
	}


	public static Set<String> getBiosampleIds(DataCart dataCart, Map<String, String> rowUriFormMap) {

		if (dataCart.getInstancedDataCache() == null) {
			throw new InstancedDataException("The row cache is empty!");
		}

		Set<String> biosampleIds = new HashSet<String>();

		for (Entry<String, String> rowUriFormEntry : rowUriFormMap.entrySet()) {
			String rowUri = rowUriFormEntry.getKey();
			String formName = rowUriFormEntry.getValue();

			InstancedRow row = dataCart.getInstancedDataCache().getByFormName(formName).getByRowUri(rowUri);

			if (row != null) {
				for (Entry<DataTableColumn, CellValue> cellValueEntry : row.getCell().entrySet()) {
					CellValue cellValue = cellValueEntry.getValue();
					// type check the cell value since we only want to add non-repeating cells
					if (cellValue instanceof NonRepeatingCellValue) {
						NonRepeatingCellValue nrc = (NonRepeatingCellValue) cellValue;
						if (DataType.BIOSAMPLE == nrc.getDataElementType()) {
							biosampleIds.add(nrc.getValue());
						}
					}
				}
			}
		}

		return biosampleIds;
	}

	public static Set<String> getAllBiosampleIds(DataCart dataCart, Map<String, String> unselectedRowUriFormMap,
			String bioFormName) {

		if (dataCart.getInstancedDataCache() == null) {
			throw new InstancedDataException("The row cache is empty!");
		}
		Set<String> biosampleIds = new HashSet<String>();

		if (dataCart.getSelectedForms().size() == 1) {
			// TODO: Francis do this for single form
			Map<String, InstancedRow> rows =
					dataCart.getInstancedDataCache().getByFormName(bioFormName).getAllRows(unselectedRowUriFormMap);
		} else if (dataCart.getSelectedForms().size() > 1) {
			List<InstancedRecord> joinedList = dataCart.getInstancedDataCache().getResultCache();
			for (InstancedRecord ir : joinedList) {

				// FILTER rows from unselected
				List<InstancedRow> rowList =
						ir.getSelectedRows().stream().filter(x -> unselectedRowUriFormMap.get(x.getRowUri()) == null)
								.collect(Collectors.<InstancedRow>toList());
				for (InstancedRow irow : rowList) {
					if (irow != null) {
						if (irow.getFormShortName().equals(bioFormName)) {
							for (Entry<DataTableColumn, CellValue> cellValueEntry : irow.getCell().entrySet()) {
								CellValue cellValue = cellValueEntry.getValue();
								// type check the cell value since we only want to add non-repeating cells
								if (cellValue instanceof NonRepeatingCellValue) {
									NonRepeatingCellValue nrc = (NonRepeatingCellValue) cellValue;
									if (DataType.BIOSAMPLE == nrc.getDataElementType()) {
										biosampleIds.add(nrc.getValue());
										break;
									}
								}
							}

						}
					}
				}

			}
		} else {
			throw new InstancedDataException("There is no form selected!");
		}


		return biosampleIds;
	}

}
