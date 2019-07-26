package gov.nih.tbi.dictionary.model.formbuilder;

public class FormBuilderRepeatableGroup {
	
	public String name;
	
	public int threshold;
	
	public  String type;
	
	public FormBuilderRepeatableGroup(String name, int threshold, String type){
		setName(name);
		setThreshold(threshold);
		setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
}
