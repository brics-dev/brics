package gov.nih.tbi.dictionary.model.formbuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public class FormBuilderDataElement{
	//flag to let us know if this data element came from a group that is truly repeatbale (not repeats only 1)
		public Boolean groupRepeatable;
		public int order;
		//added for FB in DD
		public String name;
		public String  title;
		public DataType type; 
		public String shortDescription;
		public String suggestedQuestion;
		public InputRestrictions restrictions;
		public LinkedHashSet<ValueRange> valueRangeList;
		public Integer size;
		public BigDecimal maximumValue;
		public BigDecimal minimumValue;
		// added by Ching-Heng
		public String catOid;
		public String formItemOid;
	
	public FormBuilderDataElement(){
		
	}
	
	public FormBuilderDataElement(DataElement de,String rgName, boolean isGroupRepeatable, int position){		
		this.setName(rgName+"."+de.getName());
		if (de.getTitle() != null) {
			this.setTitle(de.getTitle());
		}
		this.setGroupRepeatable(isGroupRepeatable);
		this.setType(de.getType());
		this.setShortDescription(de.getShortDescription());
		this.setSuggestedQuestion(de.getSuggestedQuestion());
		this.setRestrictions(de.getRestrictions());
		
		// ValueRange
		LinkedHashSet<ValueRange> vrList = new LinkedHashSet<ValueRange>(de.getSortedValueRangeList());
		LinkedHashSet<ValueRange> newVr = new LinkedHashSet<ValueRange>();
		 
		for(ValueRange vr:vrList){
			//ValueRange vRange= new ValueRange(vr.getValueRange(), vr.getDescription());
			ValueRange vRange= new ValueRange(vr.getValueRange(), vr.getDescription(), vr.getElementOid(), vr.getItemResponseOid());
			newVr.add(vRange);
		}
		this.setValueRangeList(newVr);
		 
//		this.setValueRangeList(vrList);
		this.setSize(de.getSize());
		if(de.getMaximumValue() == null) {
			this.setMaximumValue(new BigDecimal(-99999));
		}else {
			this.setMaximumValue(de.getMaximumValue());
		}
		
		if(de.getMinimumValue() == null) {
			this.setMinimumValue(new BigDecimal(-99999));
		}else {
			this.setMinimumValue(de.getMinimumValue());
		}
		
		this.setOrder(position);
		
		this.setCatOid(de.getCatOid());
		this.setFormItemOid(de.getFormItemId());
	}
	
	
	public BigDecimal getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(BigDecimal maximumValue) {
		this.maximumValue = maximumValue;
	}

	public BigDecimal getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(BigDecimal minimumValue) {
		this.minimumValue = minimumValue;
	}

	public Integer getSize() {
		
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public LinkedHashSet<ValueRange> getValueRangeList() {
		return valueRangeList;
	}

	public void setValueRangeList(LinkedHashSet<ValueRange> valueRangeList) {
		this.valueRangeList = valueRangeList;
	}

	public InputRestrictions getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(InputRestrictions restrictions) {
		this.restrictions = restrictions;
	}

	public String getSuggestedQuestion() {
		return suggestedQuestion;
	}

	public void setSuggestedQuestion(String suggestedQuestion) {
		this.suggestedQuestion = suggestedQuestion;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public DataType getType()
    {

        return type;
    }

    public void setType(DataType type)
    {
    		this.type=type;
    }

	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getGroupRepeatable() {
		return groupRepeatable;
	}

	public void setGroupRepeatable(Boolean groupRepeatable) {
		this.groupRepeatable = groupRepeatable;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getCatOid() {
		return catOid;
	}

	public void setCatOid(String catOid) {
		this.catOid = catOid;
	}

	public String getFormItemOid() {
		return formItemOid;
	}

	public void setFormItemOid(String formItemOid) {
		this.formItemOid = formItemOid;
	}	
}
