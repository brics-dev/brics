package gov.nih.tbi.api.query.model;

import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * BasicFormStudy
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-02-10T13:16:21.736-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicFormStudy   {
  @JsonProperty("form")
  private String form = null;

  @JsonProperty("studies")
  @Valid
  private List<String> studies = null;

  /**
   * Get form
   * @return form
  **/
  @ApiModelProperty(value = "")

  public String getForm() {
    return form;
  }

  public void setForm(String form) {
    this.form = form;
  }

  /**
   * Get studies
   * @return studies
  **/
  @ApiModelProperty(value = "")

  public List<String> getStudies() {
    return studies;
  }

  public void setStudies(List<String> studies) {
    this.studies = studies;
  }

}
