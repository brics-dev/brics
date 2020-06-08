package gov.nih.cit.brics.file.mvc.model.swagger;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * ValidationResult
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-19T16:32:03.417-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResult {
	@JsonProperty("isHashValid")
	private Boolean isHashValid = null;

	@JsonProperty("generatedHash")
	private String generatedHash = null;

	@JsonProperty("providedHash")
	private String providedHash = null;

	/**
	 * boolean \"is the hash valid\"
	 * 
	 * @return isHashValid
	 **/
	@ApiModelProperty(value = "boolean \"is the hash valid\"")

	public Boolean isIsHashValid() {
		return isHashValid;
	}

	public void setIsHashValid(Boolean isHashValid) {
		this.isHashValid = isHashValid;
	}

	/**
	 * the MD5 or CRC hash generated from the file target
	 * 
	 * @return generatedHash
	 **/
	@ApiModelProperty(value = "the MD5 or CRC hash generated from the file target")

	public String getGeneratedHash() {
		return generatedHash;
	}

	public void setGeneratedHash(String generatedHash) {
		this.generatedHash = generatedHash;
	}

	/**
	 * the provided MD5 or CRC hash
	 * 
	 * @return providedHash
	 **/
	@ApiModelProperty(value = "the provided MD5 or CRC hash")

	public String getProvidedHash() {
		return providedHash;
	}

	public void setProvidedHash(String providedHash) {
		this.providedHash = providedHash;
	}

}
