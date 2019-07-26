package gov.nih.tbi.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

import gov.nih.tbi.dictionary.model.formbuilder.FormBuilderDataElement;
import gov.nih.tbi.dictionary.model.formbuilder.FormBuilderRepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;

public class FormStructureJSONUtil {

	private FormStructure formStructure;
	private boolean isRepeatable = false;

	private JSONObject dataStructObj = new JSONObject();
	private List<JSONObject> dataElementList = new ArrayList<JSONObject>();
	private List<JSONObject> repeatableGroupList = new ArrayList<JSONObject>();
	private JSONArray repeatableGroupNamesArr = new JSONArray();
	private List<JSONObject> allGroupsList = new ArrayList<JSONObject>();

	static Logger logger = Logger.getLogger(FormStructureJSONUtil.class);

	public FormStructureJSONUtil(FormStructure formStructure) {
		setFormStructure(formStructure);
	}

	/*
	 * return a json object representation of class form structure
	 */
	public JSONObject getDataStructJson() {

		// sort the list of RGs
		ArrayList<RepeatableGroup> rgList = new ArrayList<RepeatableGroup>(getFormStructure().getRepeatableGroups());
		Collections.sort(rgList);

		for (RepeatableGroup rg : rgList) {

			buildRepeatableGroupJSON(rg);

			// gets all the map elements from a RG and sorts them on position
			List<MapElement> mapElements = new ArrayList<MapElement>(rg.getDataElements());
			Collections.sort(mapElements);

			for (MapElement me : mapElements) {
				buildDataElementJSON(me, rg);
			}
		}

		dataStructObj.put("dataStructName", getFormStructure().getShortName());
		dataStructObj.put("dataElements", dataElementList);
		dataStructObj.put("dataStructVersion", getFormStructure().getVersion());
		dataStructObj.put("repeatableGroupNames", repeatableGroupNamesArr);
		dataStructObj.put("repeatableGroupList", repeatableGroupList);
		dataStructObj.put("allGroupsList", allGroupsList);
		dataStructObj.put("copyrightedForm", getFormStructure().getIsCopyrighted());
		//added by Chng-Heng
		dataStructObj.put("isCAT", getFormStructure().isCAT());
		dataStructObj.put("measurementType", getFormStructure().getMeasurementType());
		return dataStructObj;
	}

	/*
	 * this methods sets the JSON for the RG passed in and sets whether the RG is repeatable
	 */
	private void buildRepeatableGroupJSON(RepeatableGroup rg) {
		// reset the repeatable boolean to false
		isRepeatable = false;
		FormBuilderRepeatableGroup PRG = new FormBuilderRepeatableGroup(rg.getName(), rg.getThreshold(), rg.getType().getValue());

		// build RG JSON and set repeat boolean
		switch (rg.getType()) {
			case EXACTLY:
				// if exactly at least twice it repeats
				if (rg.getThreshold() > 1) {
					isRepeatable = true;
				}
				break;
			case LESSTHAN:
				// if greater than 2 it repeats at least twice
				if (rg.getThreshold() > 2) {
					isRepeatable = true;
				}
				break;
			case MORETHAN:
				// always repeats
				isRepeatable = true;
				break;
		}
		Gson buildGson = new Gson();
		String repeatableGroupJson = buildGson.toJson(PRG);
		
		
		JSONObject repeatableGroupjson = new JSONObject(repeatableGroupJson);
		repeatableGroupList.add(repeatableGroupjson);
		allGroupsList.add(repeatableGroupjson);
		repeatableGroupNamesArr.put(rg.getName());
	}

	private void buildDataElementJSON(MapElement me, RepeatableGroup rg) {
		// get the full data element from the DE map
		DataElement de = getFormStructure().getDataElements().get(me.getStructuralDataElement().getNameAndVersion());

		FormBuilderDataElement DE = new FormBuilderDataElement(de, rg.getName(), isRepeatable, me.getPosition());
		
		JSONObject dataElementJson = new JSONObject(DE);
		dataElementJson.put("requiredType", me.getRequiredType().name());

		dataElementList.add(dataElementJson);
	}

	private FormStructure getFormStructure() {
		return this.formStructure;
	}

	private void setFormStructure(FormStructure formStructure) {
		this.formStructure = formStructure;
	}
}
