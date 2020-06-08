package gov.nih.tbi.api.query.model;

import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * StudyForm
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-17T13:05:26.988-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudyForm   {
  @JsonProperty("studyId")
  private String studyId = null;

  @JsonProperty("forms")
  @Valid
  private List<FormStructure> forms = null;

  /**
   * Get studyId
   * @return studyId
  **/
  @ApiModelProperty(value = "")

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  /**
   * Get forms
   * @return forms
  **/
  @ApiModelProperty(value = "")
  @Valid
  public List<FormStructure> getForms() {
    return forms;
  }

  public void setForms(List<FormStructure> forms) {
    this.forms = forms;
  }

}
