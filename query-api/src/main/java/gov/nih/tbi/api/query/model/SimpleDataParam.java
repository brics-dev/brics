package gov.nih.tbi.api.query.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * SimpleDataParam
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-02-25T14:40:04.026-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleDataParam   {
  @JsonProperty("formStudies")
  @Valid
  private List<BasicFormStudy> formStudies = new ArrayList<>();

  @JsonProperty("flattened")
  private Boolean flattened = false;

  /**
   * Gets or Sets outputFormat
   */
  public enum OutputFormatEnum {
    CSV("csv"),
    
    JSON("json");

    private String value;

    OutputFormatEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static OutputFormatEnum fromValue(String text) {
      for (OutputFormatEnum b : OutputFormatEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("outputFormat")
  private OutputFormatEnum outputFormat = OutputFormatEnum.CSV;

  /**
   * Get formStudies
   * @return formStudies
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull
  @Valid
@Size(min=1)   public List<BasicFormStudy> getFormStudies() {
    return formStudies;
  }

  public void setFormStudies(List<BasicFormStudy> formStudies) {
    this.formStudies = formStudies;
  }

  /**
   * Get flattened
   * @return flattened
  **/
  @ApiModelProperty(value = "")

  public Boolean isFlattened() {
    return flattened;
  }

  public void setFlattened(Boolean flattened) {
    this.flattened = flattened;
  }

  /**
   * Get outputFormat
   * @return outputFormat
  **/
  @ApiModelProperty(value = "")

  public OutputFormatEnum getOutputFormat() {
    return outputFormat;
  }

  public void setOutputFormat(OutputFormatEnum outputFormat) {
    this.outputFormat = outputFormat;
  }

}
