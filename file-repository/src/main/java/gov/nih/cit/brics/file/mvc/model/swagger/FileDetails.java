package gov.nih.cit.brics.file.mvc.model.swagger;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * FileDetails
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-19T16:34:07.992-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDetails {
	@JsonProperty("fileId")
	private String fileId = null;

	@JsonProperty("fileCategoryId")
	private Long fileCategoryId = null;

	@JsonProperty("linkedObjectId")
	private Long linkedObjectId = null;

	@JsonProperty("fileName")
	private String fileName = null;

	@JsonProperty("fileUrl")
	private String fileUrl = null;

	@JsonProperty("filePath")
	private String filePath = null;

	@JsonProperty("fileSize")
	private Long fileSize = null;

	/**
	 * The file's ID string
	 * 
	 * @return fileId
	 **/
	@ApiModelProperty(value = "The file's ID string")

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	/**
	 * The ID of the category that the file is assigned to. Like study, data set, etc.
	 * 
	 * @return fileCategoryId
	 **/
	@ApiModelProperty(value = "The ID of the category that the file is assigned to. Like study, data set, etc.")

	public Long getFileCategoryId() {
		return fileCategoryId;
	}

	public void setFileCategoryId(Long fileCategoryId) {
		this.fileCategoryId = fileCategoryId;
	}

	/**
	 * The ID of the associated object in the system. Like the study or data set ID. This will closly corrispond to the
	 * file's chosen category.
	 * 
	 * @return linkedObjectId
	 **/
	@ApiModelProperty(value = "The ID of the assoicated object in the system. Like the study or data set ID. This will closly corrispond to the file's chosen category.")

	public Long getLinkedObjectId() {
		return linkedObjectId;
	}

	public void setLinkedObjectId(Long linkedObjectId) {
		this.linkedObjectId = linkedObjectId;
	}

	/**
	 * The file's name. This will be used when downloading the file, since the file names on the server are anonymize.
	 * 
	 * @return fileName
	 **/
	@ApiModelProperty(value = "The file's name. This will be used when downloading the file, since the file names on the server are anonymize.")

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * The URL used to download the file.
	 * 
	 * @return fileUrl
	 **/
	@ApiModelProperty(value = "The URL used to download the file.")

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	/**
	 * The relative path to the file.
	 * 
	 * @return filePath
	 **/
	@ApiModelProperty(value = "The relative path to the file.")

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * The size of the file in bytes.
	 * 
	 * @return fileSize
	 **/
	@ApiModelProperty(value = "The size of the file in bytes.")

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

}
