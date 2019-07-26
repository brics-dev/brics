package gov.nih.nichd.ctdb.form.domain;

public class ProformsRepeatableGroup {
	
	
	public static final String REPEATABLE_TYPE_EXACTLY = "Exactly";
    public static final String REPEATABLE_TYPE_MORE_THAN = "At Least";
    public static final String REPEATABLE_TYPE_LESS_THAN = "Up To";
	
	
	public String name;
	
	
	public int threshold;
	
	public  String type;


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
