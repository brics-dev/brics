package gov.nih.tbi.api.query.model;

import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * DataElement
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-03-23T17:09:30.581-04:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataElement   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("position")
  private Integer position = null;

  @JsonProperty("title")
  private String title = null;

  @JsonProperty("description")
  private String description = null;

  /**
   * Gets or Sets dataType
   */
  public enum DataTypeEnum {
    ALPHANUMERIC("Alphanumeric"),
    
    NUMERIC_VALUES("Numeric Values"),
    
    DATE_OR_DATE_TIME("Date or Date & Time"),
    
    GUID("GUID"),
    
    FILE("File"),
    
    THUMBNAIL("Thumbnail"),
    
    BIOSAMPLE("Biosample"),
    
    TRI_PLANAR("Tri-Planar");

    private String value;

    DataTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static DataTypeEnum fromValue(String text) {
      for (DataTypeEnum b : DataTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("dataType")
  private DataTypeEnum dataType = null;

  @JsonProperty("permissibleValue")
  @Valid
  private List<String> permissibleValue = null;

  @JsonProperty("minimumValue")
  private Double minimumValue = null;

  @JsonProperty("maximumValue")
  private Double maximumValue = null;

  /**
   * Gets or Sets inputRestriction
   */
  public enum InputRestrictionEnum {
    FREE_FORM_ENTRY("Free-Form Entry"),
    
    SINGLE_PRE_DEFINED_VALUE_SELECTED("Single Pre-Defined Value Selected"),
    
    MULTIPLE_PRE_DEFINED_VALUES_SELECTED("Multiple Pre-Defined Values Selected");

    private String value;

    InputRestrictionEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static InputRestrictionEnum fromValue(String text) {
      for (InputRestrictionEnum b : InputRestrictionEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("inputRestriction")
  private InputRestrictionEnum inputRestriction = null;

  /**
   * Gets or Sets requiredType
   */
  public enum RequiredTypeEnum {
    REQUIRED("Required"),
    
    RECOMMENDED("Recommended"),
    
    OPTIONAL("Optional");

    private String value;

    RequiredTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static RequiredTypeEnum fromValue(String text) {
      for (RequiredTypeEnum b : RequiredTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("requiredType")
  private RequiredTypeEnum requiredType = null;

  /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get position
   * @return position
  **/
  @ApiModelProperty(value = "")

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  /**
   * Get title
   * @return title
  **/
  @ApiModelProperty(value = "")

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get dataType
   * @return dataType
  **/
  @ApiModelProperty(value = "")

  public DataTypeEnum getDataType() {
    return dataType;
  }

  public void setDataType(DataTypeEnum dataType) {
    this.dataType = dataType;
  }

  /**
   * Get permissibleValue
   * @return permissibleValue
  **/
  @ApiModelProperty(value = "")

  public List<String> getPermissibleValue() {
    return permissibleValue;
  }

  public void setPermissibleValue(List<String> permissibleValue) {
    this.permissibleValue = permissibleValue;
  }

  /**
   * Get minimumValue
   * @return minimumValue
  **/
  @ApiModelProperty(value = "")

  public Double getMinimumValue() {
    return minimumValue;
  }

  public void setMinimumValue(Double minimumValue) {
    this.minimumValue = minimumValue;
  }

  /**
   * Get maximumValue
   * @return maximumValue
  **/
  @ApiModelProperty(value = "")

  public Double getMaximumValue() {
    return maximumValue;
  }

  public void setMaximumValue(Double maximumValue) {
    this.maximumValue = maximumValue;
  }

  /**
   * Get inputRestriction
   * @return inputRestriction
  **/
  @ApiModelProperty(value = "")

  public InputRestrictionEnum getInputRestriction() {
    return inputRestriction;
  }

  public void setInputRestriction(InputRestrictionEnum inputRestriction) {
    this.inputRestriction = inputRestriction;
  }

  /**
   * Get requiredType
   * @return requiredType
  **/
  @ApiModelProperty(value = "")

  public RequiredTypeEnum getRequiredType() {
    return requiredType;
  }

  public void setRequiredType(RequiredTypeEnum requiredType) {
    this.requiredType = requiredType;
  }

}
