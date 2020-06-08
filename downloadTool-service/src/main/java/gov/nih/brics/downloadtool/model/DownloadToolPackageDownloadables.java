package gov.nih.brics.downloadtool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DownloadToolPackageDownloadables
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-17T12:44:03.877-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownloadToolPackageDownloadables   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("userFile")
  private DownloadToolPackageUserFile userFile = null;

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
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * Get userFile
   * @return userFile
  **/
  @ApiModelProperty(value = "")

  @Valid
  public DownloadToolPackageUserFile getUserFile() {
    return userFile;
  }

  public void setUserFile(DownloadToolPackageUserFile userFile) {
    this.userFile = userFile;
  }

}
