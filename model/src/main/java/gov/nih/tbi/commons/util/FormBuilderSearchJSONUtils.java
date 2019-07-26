package gov.nih.tbi.commons.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;

public class FormBuilderSearchJSONUtils {
	
	private List<SemanticFormStructure> formStructureList;
	
	private String shortNameColumnTitle;
	
	private String versionColumnTitle;
	
	private String descriptionColumnTitle;
	
	private String titleColumnTitle;

	public FormBuilderSearchJSONUtils(List<SemanticFormStructure> formStructureList, String shortNameColumnTitle, String versionColumnTitle, String descriptionColumnTitle, String titleColumnTitle){
		setFormStructureList(formStructureList);
		setShortNameColumnTitle(shortNameColumnTitle);
		setVersionColumnTitle(versionColumnTitle);
		setDescriptionColumnTitle(descriptionColumnTitle);
		setTitleColumnTitle(titleColumnTitle);
	}
	
	public String convertFormStructureToJSON(){
		
		JSONArray columns = new JSONArray();
		JSONObject columnDef = new JSONObject();
		
		// // "Short Name" column
		columnDef.put("title", "Short Name");
		columnDef.put("data", "shortName");
		columnDef.put("name", "shortName");
		columns.put(columnDef);

		// // "Version" column
		columnDef = new JSONObject();
		columnDef.put("title", "Version");
		columnDef.put("data", "version");
		columnDef.put("name", "version");
		columns.put(columnDef);

		// // "Description" column
		columnDef = new JSONObject();
		columnDef.put("title", "Description");
		columnDef.put("data", "description");
		columnDef.put("name", "description");
		columns.put(columnDef);
		
		columnDef = new JSONObject();
		columnDef.put("title", "Title");
		columnDef.put("data", "title");
		columnDef.put("name", "title");
		columnDef.put("visible", false);
		columns.put(columnDef);
		
		
		JSONArray rows = new JSONArray();
		
		for (SemanticFormStructure fs : getFormStructureList()) {
			JSONObject row = new JSONObject();
			
			row.put("DT_RowId", fs.getShortName());
			row.put("shortName", fs.getShortName());
			row.put("version",fs.getVersion());
			row.put("description",fs.getDescription());
			row.put("title",fs.getTitle());
			rows.put(row);
		}
		
		// Bring table and column definitions together in the final JSON object
		JSONObject fsDataTablesObj = new JSONObject();

		fsDataTablesObj.put("data", rows);
		fsDataTablesObj.put("columns", columns);
		
		return fsDataTablesObj.toString();
	}
	
	
	private void setFormStructureList(List<SemanticFormStructure> formStructureList){
		this.formStructureList = formStructureList;
	}
	
	public  List<SemanticFormStructure> getFormStructureList(){
		return this.formStructureList;
	}

	public String getShortNameColumnTitle() {
		return shortNameColumnTitle;
	}

	public void setShortNameColumnTitle(String shortNameColumnTitle) {
		this.shortNameColumnTitle = shortNameColumnTitle;
	}

	public String getVersionColumnTitle() {
		return versionColumnTitle;
	}

	public void setVersionColumnTitle(String versionColumnTitle) {
		this.versionColumnTitle = versionColumnTitle;
	}

	public String getDescriptionColumnTitle() {
		return descriptionColumnTitle;
	}

	public void setDescriptionColumnTitle(String descriptionColumnTitle) {
		this.descriptionColumnTitle = descriptionColumnTitle;
	}

	public String getTitleColumnTitle() {
		return titleColumnTitle;
	}

	public void setTitleColumnTitle(String titleColumnTitle) {
		this.titleColumnTitle = titleColumnTitle;
	}
	
	
}
