
package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlID;

import org.apache.commons.lang.StringEscapeUtils;

@Entity
@Table(name = "DATA_ELEMENT")
@XmlRootElement(name = "StructuralDataElement")
@XmlAccessorType(XmlAccessType.FIELD)
public class StructuralDataElement implements Serializable
{

    private static final long serialVersionUID = 3333413702783642452L;

    public static final String DOCUMENTATION_URL = "documentationUrl";
    public static final String DOCUMENTATION_FILE_ID = "documentationFileId";

    /**
     * These values are stored in the database and should be pulled from there, however, they are being defined here to
     * match the existing architecture. They are also being "swapped" - since the original code was backward.
     */
    public static final Long COMMON_DATA_ELEMENT = 1L;
    public static final Long UNIQUE_DATA_ELEMENT = 0L;

    /**** PROPERTIES *********************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATA_ELEMENT_SEQ")
    @SequenceGenerator(name = "DATA_ELEMENT_SEQ", sequenceName = "DATA_ELEMENT_SEQ", allocationSize = 1)
    private Long id;

    @XmlID
    @Column(name = "ELEMENT_NAME")
    private String name;

    @Column(name = "VERSION")
    private String version;

    @Column(name = "ELEMENT_SIZE")
    private Integer size;

    @Column(name = "MAXIMUM_VALUE")
    private String maximumValue;

    @Column(name = "MINIMUM_VALUE")
    private String minimumValue;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ELEMENT_TYPE_ID")
    private DataType type;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "INPUT_RESTRICTION_ID")
    private InputRestrictions restrictions;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "DATA_ELEMENT_STATUS_ID")
    private DataElementStatus status;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataElement", targetEntity = ValueRange.class, orphanRemoval = true)
    @OrderBy("valueRange")
    private Set<ValueRange> valueRangeList;

    @Column(name = "QUESTION")
    private String suggestedQuestion;

    @Column(name = "DATE_CREATED")
    private Date dateCreated;

    @ManyToOne(cascade = { CascadeType.DETACH })
    @JoinColumn(name = "MEASURING_UNIT_ID")
    private MeasuringUnit measuringUnit;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataElement", targetEntity = Alias.class, orphanRemoval = true)
    private Set<Alias> aliasList;

    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;
    
    @XmlTransient
	@JoinTable(name = "data_element_supporting_documentation", joinColumns = {@JoinColumn(name = "data_element_id")}, inverseJoinColumns = {@JoinColumn(name = "supporting_documentation_id")})
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = DictionarySupportingDocumentation.class)
	private Set<DictionarySupportingDocumentation> supportingDocumentationSet = new HashSet<DictionarySupportingDocumentation>();
    
    //added by Ching-Heng
    @Column(name = "CAT_OID")
    private String catOid;
    
    @Column(name = "FORM_ITEM_OID")
    private String formItemId;
    
    /**** CONSTRUCTORS *******************************************************/

    public StructuralDataElement()
    {

    }

    public StructuralDataElement(StructuralDataElement structuralObject)
    {

        this.id = structuralObject.getId();
        this.name = structuralObject.getName();
        this.version = structuralObject.getVersion();
        this.size = structuralObject.getSize();
        this.maximumValue = structuralObject.getMaximumValueString();
        this.minimumValue = structuralObject.getMinimumValueString();
        this.type = structuralObject.getType();
        this.status = structuralObject.getStatus();
        this.suggestedQuestion = structuralObject.getSuggestedQuestion();
        this.dateCreated = structuralObject.getDateCreated();
        this.measuringUnit = structuralObject.getMeasuringUnit();
        this.restrictions = structuralObject.getRestrictions();
        // added by Ching-Heng
        this.catOid = structuralObject.getCatOid();
        this.formItemId = structuralObject.getFormItemId();
        
        if (structuralObject.getCategory() != null)
        {
            Category category = new Category(structuralObject.getCategory());
            this.category = category;
        }

        if (structuralObject.getAliasList() != null)
        {
            Set<Alias> aliasList = new HashSet<Alias>();
            for (Alias alias : structuralObject.getAliasList())
            {
                aliasList.add(new Alias(alias));
            }
            this.aliasList = aliasList;
        }
        
        if (structuralObject.getSupportingDocumentationSet() != null)
        {
            Set<DictionarySupportingDocumentation> supportingDocumentationSet = new HashSet<DictionarySupportingDocumentation>();
            for (DictionarySupportingDocumentation suppDoc : structuralObject.getSupportingDocumentationSet())
            {
            	DictionarySupportingDocumentation supportingDocumentation = new DictionarySupportingDocumentation();
            	
            	supportingDocumentation.setDescription(suppDoc.getDescription());
            	supportingDocumentation.setFileType(suppDoc.getFileType());
            	supportingDocumentation.setUrl(suppDoc.getUrl());
            	supportingDocumentation.setUserFile(suppDoc.getUserFile());
            	supportingDocumentation.setDateCreated(suppDoc.getDateCreated());
            	
            	supportingDocumentationSet.add(supportingDocumentation);
            }
          
            this.supportingDocumentationSet = supportingDocumentationSet;
        }
        
        

        // Copy value range list and its schemaPvs
        if (structuralObject.getValueRangeList() != null)
        {
            Set<ValueRange> vrList = new HashSet<ValueRange>();
            for (ValueRange vr : structuralObject.getValueRangeList())
            {
                ValueRange newVr = new ValueRange();
                newVr.setDescription(vr.getDescription());
                newVr.setOutputCode(vr.getOutputCode());
                newVr.setValueRange(vr.getValueRange());
                newVr.setDataElement(this);
                
                Set<SchemaPv> schemaPvs = new HashSet<SchemaPv>();
                for (SchemaPv spv : vr.getSchemaPvs()) {
                	SchemaPv pv = new SchemaPv();
                	pv.setPermissibleValue(spv.getPermissibleValue());
                	pv.setSchema(spv.getSchema());
                	pv.setSchemaDeId(spv.getSchemaDeId());
                	pv.setSchemaPvId(spv.getSchemaPvId());
                	pv.setSchemaDataElementName(spv.getSchemaDataElementName());
                	pv.setDataElement(this);
                	pv.setValueRange(newVr);
                	
                	schemaPvs.add(pv);
                }
                newVr.setSchemaPvs(schemaPvs);
                
                vrList.add(newVr);
            }
            this.valueRangeList = vrList;
        }
    }

    /**** GETTERS AND SETTERS ************************************************/

    public Category getCategory()
    {

        return category;
    }

    public void setCategory(Category category)
    {

        this.category = category;
    }

    public MeasuringUnit getMeasuringUnit()
    {

        return measuringUnit;
    }

    public void setMeasuringUnit(MeasuringUnit measuringUnit)
    {

        this.measuringUnit = measuringUnit;
    }

    public InputRestrictions getRestrictions()
    {

        return restrictions;
    }

    public void setRestrictions(InputRestrictions restrictions)
    {

        this.restrictions = restrictions;
    }

    public void setValueRangeList(Set<ValueRange> valueRangeList)
    {

        this.valueRangeList = valueRangeList;
    }

    public Set<ValueRange> getValueRangeList()
    {

        if (valueRangeList == null)
        {
			valueRangeList = new LinkedHashSet<ValueRange>();
            return valueRangeList;
        }

        return valueRangeList;
    }

    public boolean isCommonDataElement()
    {

        Category c = this.getCategory();
        if (COMMON_DATA_ELEMENT.equals(c.getId()))
        {
            return true;
        }

        return false;
    }

    public String getSuggestedQuestion()
    {

        return StringEscapeUtils.unescapeHtml(suggestedQuestion);
    }

    public void setSuggestedQuestion(String suggestedQuestion)
    {

        this.suggestedQuestion = suggestedQuestion;
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public void setSize(Integer size)
    {

        this.size = size;
    }

    public Integer getSize()
    {

        return size;
    }

    public String getMaximumValueString()
    {

        return maximumValue;
    }

    public String getMinimumValueString()
    {

        return minimumValue;
    }

    public BigDecimal getMaximumValue()
    {

        if (maximumValue == null || maximumValue.equals(ModelConstants.EMPTY_STRING))
        {
            return null;
        }
        return new BigDecimal(maximumValue);
    }

    public void setMaximumValue(BigDecimal maximumValue)
    {

        if (maximumValue == null)
        {
            this.maximumValue = null;
        }
        else
        {
            this.maximumValue = maximumValue.toString();
        }
    }

    public BigDecimal getMinimumValue()
    {

        if (minimumValue == null || minimumValue.equals(ModelConstants.EMPTY_STRING))
        {
            return null;
        }
        return new BigDecimal(minimumValue);
    }

    public void setMinimumValue(BigDecimal minimumValue)
    {

        if (minimumValue == null)
        {
            this.minimumValue = null;
        }
        else
        {
            this.minimumValue = minimumValue.toString();
        }
    }

    public DataType getType()
    {

        return type;
    }

    public void setType(DataType type)
    {

        this.type = type;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String getVersion()
    {

        return version;
    }

    public void setVersion(String version)
    {

        this.version = version;
    }

    public Set<Alias> getAliasList()
    {

        if (aliasList == null)
        {
            aliasList = new LinkedHashSet<Alias>();
        }

        return this.aliasList;
    }

    // TODO: this should be renamed to follow standard bean conventions
    public void setAliasList(Set<Alias> aliasList)
    {

        this.aliasList = aliasList;
    }

    public DataElementStatus getStatus()
    {

        return status;
    }

    public void setStatus(DataElementStatus status)
    {

        this.status = status;
    }

    public Date getDateCreated()
    {

        return dateCreated;
    }

    public String getDateCreatedString()
    {

        if (dateCreated != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            StringBuffer date = new StringBuffer();
            sdf.format(dateCreated, date, new FieldPosition(DateFormat.MONTH_FIELD));
            return date.toString();
        }
        else
        {
            return ModelConstants.EMPTY_STRING;
        }
    }

    public void setDateCreated(Date dateCreated)
    {

        this.dateCreated = dateCreated;
    }

    public String getCatOid() {
		return catOid;
	}

	public void setCatOid(String catOid) {
		this.catOid = catOid;
	}

	public String getFormItemId() {
		return formItemId;
	}

	public void setFormItemId(String formItemId) {
		this.formItemId = formItemId;
	}

	/**** FUNCTIONS **********************************************************/

    // /**
    // * This method will test the CSV specifically for blank comma rows The method will only return true if all the
    // data
    // * in the DE is null
    // *
    // * @param testDE
    // * @return
    // */
    // public boolean dataElementHasData(DataElement testDE) throws IllegalArgumentException, IllegalAccessException
    // {
    //
    // for (Field field : testDE.getClass().getDeclaredFields())
    // {
    // // allow access to the class
    // field.setAccessible(true);
    // Class<?> type = field.getType();
    // // Ignore fields that are automatically set by the system
    // if (!field.getName().equalsIgnoreCase("serialVersionUID")
    // && !field.getName().equalsIgnoreCase("DOCUMENTATION_URL")
    // && !field.getName().equalsIgnoreCase("DOCUMENTATION_FILE_ID")
    // && !field.getName().equalsIgnoreCase("status")
    // && !field.getName().equalsIgnoreCase("externalIdSet"))
    // {
    // // test to see if fields get by
    // if (field.get(this) != null)
    // {
    // // test to see if the field is a string with blank data
    // if (field.get(this) instanceof String && !field.get(this).toString().trim().isEmpty())
    // {
    // return true;
    // }
    // // check to see if the fiel is a set, if it is and the set is empty, ignore
    // if (field.get(this) instanceof Set<?>)
    // {
    // Set<Object> set = (Set<Object>) field.get(this);
    // if (!set.isEmpty())
    // {
    // return true;
    // }
    //
    // }
    // }
    // }
    // }
    // return false;
    // }

    @Override
    public String toString()
    {

        return "DataElement [id=" + getId() + ", name=" + getName() + ", size=" + getSize() + ", valueRange="
                + valueRangeList + "]";
    }

    public Double getNumericVersion()
    {

        return Double.valueOf(version);
    }

    public String displayValueRange()
    {

        String sb = "";
        sb += "{";

        if (InputRestrictions.FREE_FORM.equals(getRestrictions()))
        {
            if (getMinimumValue() != null)
            {
                sb += "MIN: " + getMinimumValue() + ", ";
            }

            if (getMaximumValue() != null)
            {
                sb += "MAX: " + getMaximumValue() + ", ";
            }

        }
        else
        {

            for (ValueRange vr : getValueRangeList())
            {
                sb += vr.getValueRange() + ", ";
            }
        }

        if (sb.contains(","))
        {
            sb = sb.substring(0, sb.lastIndexOf(","));
        }

        sb += "}";
        return sb.toString();
    }

    public void addValueRange(ValueRange valueRange)
    {

        if (valueRange == null)
        {
            this.valueRangeList = new TreeSet<ValueRange>();
        }

        this.valueRangeList.add(valueRange);
    }
    
    public void removeValueRange(ValueRange valueRange){  	
    	this.valueRangeList.remove(valueRange);
    }

    public String getNameAndVersion()
    {

        return getName() + "V" + getVersion();
    }

	public Set<DictionarySupportingDocumentation> getSupportingDocumentationSet() {
		return supportingDocumentationSet;
	}

	public void setSupportingDocumentationSet(Set<DictionarySupportingDocumentation> supportingDocumentationSet) {
		this.supportingDocumentationSet = supportingDocumentationSet;
	}

	public void addSupportingDocumentation(DictionarySupportingDocumentation supportingDocumentation) {
		if (this.supportingDocumentationSet == null) {
			this.supportingDocumentationSet = new HashSet<DictionarySupportingDocumentation>();
		}

		this.supportingDocumentationSet.add(supportingDocumentation);
	}
}
