package gov.nih.tbi.api.query.model;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * FormStructure
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-04-01T12:12:14.591-04:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormStructure   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("shortName")
  private String shortName = null;

  @JsonProperty("title")
  private String title = null;

  @JsonProperty("version")
  private String version = null;

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
   * Get shortName
   * @return shortName
  **/
  @ApiModelProperty(value = "")

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
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
   * Get version
   * @return version
  **/
  @ApiModelProperty(value = "")

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

}
