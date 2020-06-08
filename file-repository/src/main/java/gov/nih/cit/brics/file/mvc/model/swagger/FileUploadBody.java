package gov.nih.cit.brics.file.mvc.model.swagger;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * FileUploadBody
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-19T16:32:03.417-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadBody {
	@JsonProperty("fileId")
	private String fileId = null;

	@JsonProperty("fileCategoryId")
	private Long fileCategoryId = null;

	@JsonProperty("linkedObjectId")
	private Long linkedObjectId = null;

	@JsonProperty("file")
	private org.springframework.web.multipart.MultipartFile file = null;

	/**
	 * The alphanumeric identifer for the file.
	 * 
	 * @return fileId
	 **/
	@ApiModelProperty(required = true, value = "The alphanumeric identifer for the file.")
	@NotNull

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	/**
	 * The file's category ID to be used to calculate the file path on the server.
	 * 
	 * @return fileCategoryId
	 **/
	@ApiModelProperty(required = true, value = "The file's category ID to be used to calculate the file path on the server.")
	@NotNull

	public Long getFileCategoryId() {
		return fileCategoryId;
	}

	public void setFileCategoryId(Long fileCategoryId) {
		this.fileCategoryId = fileCategoryId;
	}

	/**
	 * The ID of the object associated with this file. Like a study or data set ID. This ID will depeand on what is
	 * chosen as the file category.
	 * 
	 * @return linkedObjectId
	 **/
	@ApiModelProperty(required = true, value = "The ID of the object associated with this file. Like a study or data set ID. This ID will depeand on what is chosen as the file category.")
	@NotNull

	public Long getLinkedObjectId() {
		return linkedObjectId;
	}

	public void setLinkedObjectId(Long linkedObjectId) {
		this.linkedObjectId = linkedObjectId;
	}

	/**
	 * The data contents of the file or the chunk of the file being updoaded.
	 * 
	 * @return file
	 **/
	@ApiModelProperty(required = true, value = "The data contents of the file or the chunk of the file being updoaded.")
	@NotNull

	@Valid
	public org.springframework.web.multipart.MultipartFile getFile() {
		return file;
	}

	public void setFile(org.springframework.web.multipart.MultipartFile file) {
		this.file = file;
	}

}
