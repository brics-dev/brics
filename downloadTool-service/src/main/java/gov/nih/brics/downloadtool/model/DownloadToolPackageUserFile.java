package gov.nih.brics.downloadtool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DownloadToolPackageUserFile
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-17T12:55:20.804-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownloadToolPackageUserFile   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("study")
  private String study = null;

  @JsonProperty("size")
  private Long size = null;

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
   * Get path
   * @return path
  **/
  @ApiModelProperty(value = "")

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Get study
   * @return study
  **/
  @ApiModelProperty(value = "")

  public String getStudy() {
    return study;
  }

  public void setStudy(String study) {
    this.study = study;
  }

  /**
   * Get size
   * @return size
  **/
  @ApiModelProperty(value = "")

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

}
