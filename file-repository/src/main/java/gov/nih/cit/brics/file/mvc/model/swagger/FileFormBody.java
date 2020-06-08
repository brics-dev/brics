package gov.nih.cit.brics.file.mvc.model.swagger;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * FileFormBody
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-03-18T16:38:08.206-04:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileFormBody {
	@JsonProperty("fileId")
	private String fileId = null;

	@JsonProperty("fileCategoryId")
	private Long fileCategoryId = null;

	@JsonProperty("linkedObjectId")
	private Long linkedObjectId = null;

	@JsonProperty("fileName")
	private String fileName = null;

	@JsonProperty("legacyUserFileId")
	private Long legacyUserFileId = null;

	/**
	 * The existing alphanumeric file ID for updating an existing record or not included to create a new file record.
	 * 
	 * @return fileId
	 **/
	@ApiModelProperty(value = "The existing alphanumeric file ID for updating an existing record or not included to create a new file record.")

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	/**
	 * The file category identifier used to store the file.
	 * 
	 * @return fileCategoryId
	 **/
	@ApiModelProperty(required = true, value = "The file category identifier used to store the file.")
	@NotNull

	public Long getFileCategoryId() {
		return fileCategoryId;
	}

	public void setFileCategoryId(Long fileCategoryId) {
		this.fileCategoryId = fileCategoryId;
	}

	/**
	 * The ID of the object that is assoicated with the file. Like a study or data set ID. The ID will depend on what
	 * file category is chosen.
	 * 
	 * @return linkedObjectId
	 **/
	@ApiModelProperty(required = true, value = "The ID of the object that is assoicated with the file. Like a study or data set ID. The ID will depend on what file category is chosen.")
	@NotNull

	public Long getLinkedObjectId() {
		return linkedObjectId;
	}

	public void setLinkedObjectId(Long linkedObjectId) {
		this.linkedObjectId = linkedObjectId;
	}

	/**
	 * The file's name.
	 * 
	 * @return fileName
	 **/
	@ApiModelProperty(required = true, value = "The file's name.")
	@NotNull

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * The ID of the UserFile object of an existing file stored in the SFTP share.
	 * 
	 * @return legacyUserFileId
	 **/
	@ApiModelProperty(value = "The ID of the UserFile object of an existing file stored in the SFTP share.")

	public Long getLegacyUserFileId() {
		return legacyUserFileId;
	}

	public void setLegacyUserFileId(Long legacyUserFileId) {
		this.legacyUserFileId = legacyUserFileId;
	}

}
