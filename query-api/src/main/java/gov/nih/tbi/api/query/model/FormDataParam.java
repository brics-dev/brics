package gov.nih.tbi.api.query.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * FormDataParam
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-02-18T16:58:59.561-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormDataParam   {
  @JsonProperty("formStudy")
  @Valid
  private List<BasicFormStudy> formStudy = new ArrayList<>();

  @JsonProperty("filter")
  @Valid
  private List<Filter> filter = null;

  @JsonProperty("flattened")
  private Boolean flattened = false;

  /**
   * Get formStudy
   * @return formStudy
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull
  @Valid
@Size(min=1,max=5)   public List<BasicFormStudy> getFormStudy() {
    return formStudy;
  }

  public void setFormStudy(List<BasicFormStudy> formStudy) {
    this.formStudy = formStudy;
  }

  /**
   * Get filter
   * @return filter
  **/
  @ApiModelProperty(value = "")
  @Valid
  public List<Filter> getFilter() {
    return filter;
  }

  public void setFilter(List<Filter> filter) {
    this.filter = filter;
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

}
