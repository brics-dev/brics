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
 * RepeatableGroup
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-22T13:48:13.189-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepeatableGroup   {
  @JsonProperty("uri")
  private String uri = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("position")
  private Integer position = null;

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    EXACTLY("Exactly"),
    
    UP_TO("Up To"),
    
    AT_LEAST("At Least");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("threshold")
  private Integer threshold = null;

  @JsonProperty("dataElements")
  @Valid
  private List<DataElement> dataElements = null;

  /**
   * Get uri
   * @return uri
  **/
  @ApiModelProperty(value = "")

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
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
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")

  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   * Get threshold
   * @return threshold
  **/
  @ApiModelProperty(value = "")

  public Integer getThreshold() {
    return threshold;
  }

  public void setThreshold(Integer threshold) {
    this.threshold = threshold;
  }

  /**
   * Get dataElements
   * @return dataElements
  **/
  @ApiModelProperty(value = "")
  @Valid
  public List<DataElement> getDataElements() {
    return dataElements;
  }

  public void setDataElements(List<DataElement> dataElements) {
    this.dataElements = dataElements;
  }

}
