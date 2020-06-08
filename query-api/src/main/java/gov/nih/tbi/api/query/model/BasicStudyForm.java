package gov.nih.tbi.api.query.model;

import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * BasicStudyForm
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-02-19T11:05:24.685-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicStudyForm   {
  @JsonProperty("study")
  private String study = null;

  @JsonProperty("forms")
  @Valid
  private List<String> forms = null;

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
   * Get forms
   * @return forms
  **/
  @ApiModelProperty(value = "")

  public List<String> getForms() {
    return forms;
  }

  public void setForms(List<String> forms) {
    this.forms = forms;
  }

}
