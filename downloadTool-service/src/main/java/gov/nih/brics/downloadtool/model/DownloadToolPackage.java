package gov.nih.brics.downloadtool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DownloadToolPackage
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-09T10:29:16.716-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownloadToolPackage   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("dateAdded")
  private Long dateAdded = null;

  /**
   * Gets or Sets origin
   */
  public enum OriginEnum {
    QUERY_TOOL("QUERY_TOOL"),
    
    DATASET("DATASET"),
    
    ACCOUNT("ACCOUNT");

    private String value;

    OriginEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static OriginEnum fromValue(String text) {
      for (OriginEnum b : OriginEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("origin")
  private OriginEnum origin = null;

  @JsonProperty("downloadables")
  @Valid
  private List<DownloadToolPackageDownloadables> downloadables = null;

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
  @ApiModelProperty(required = true, value = "")
  @NotNull

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get dateAdded
   * @return dateAdded
  **/
  @ApiModelProperty(value = "")

  public Long getDateAdded() {
    return dateAdded;
  }

  public void setDateAdded(Long dateAdded) {
    this.dateAdded = dateAdded;
  }

  /**
   * Get origin
   * @return origin
  **/
  @ApiModelProperty(value = "")

  public OriginEnum getOrigin() {
    return origin;
  }

  public void setOrigin(OriginEnum origin) {
    this.origin = origin;
  }

  /**
   * Get downloadables
   * @return downloadables
  **/
  @ApiModelProperty(value = "")
  @Valid
  public List<DownloadToolPackageDownloadables> getDownloadables() {
    return downloadables;
  }

  public void setDownloadables(List<DownloadToolPackageDownloadables> downloadables) {
    this.downloadables = downloadables;
  }

}
