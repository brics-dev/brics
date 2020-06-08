package gov.nih.cit.brics.file.mvc.model.swagger;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * FileUploadDetails
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-19T16:34:07.992-05:00[America/New_York]")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadDetails {
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

	@JsonProperty("fileType")
	private String fileType = null;

	@JsonProperty("crc")
	private Long crc = null;

	@JsonProperty("uploadFileSize")
	private Long uploadFileSize = null;

	@JsonProperty("minRange")
	private Long minRange = null;

	@JsonProperty("maxRange")
	private Long maxRange = null;

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
	 * The ID of the object that is assoicated with the file. Like a study or data set ID. The ID will depend on what
	 * file category is chosen.
	 * 
	 * @return linkedObjectId
	 **/
	@ApiModelProperty(value = "The ID of the object that is assoicated with the file. Like a study or data set ID. The ID will depend on what file category is chosen.")

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

	/**
	 * The uploaded file type from the form data content disposition object.
	 * 
	 * @return fileType
	 **/
	@ApiModelProperty(value = "The uploaded file type from the form data content disposition object.")

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * The CRC value that was calculated while reading in the data from the uploaded file or chunck.
	 * 
	 * @return crc
	 **/
	@ApiModelProperty(value = "The CRC value that was calculated while reading in the data from the uploaded file or chunck.")

	public Long getCrc() {
		return crc;
	}

	public void setCrc(Long crc) {
		this.crc = crc;
	}

	/**
	 * The total size of the file in bytes as reported from the \"Content-Range\" header. This property will only be
	 * included if a file chunck is sent.
	 * 
	 * @return uploadFileSize
	 **/
	@ApiModelProperty(value = "The total size of the file in bytes as reported from the \"Content-Range\" header. This property will only be included if a file chunck is sent.")

	public Long getUploadFileSize() {
		return uploadFileSize;
	}

	public void setUploadFileSize(Long uploadFileSize) {
		this.uploadFileSize = uploadFileSize;
	}

	/**
	 * The minimum byte range from the \"Content-Range\" header. This property will only be included if a file chunck is
	 * sent.
	 * 
	 * @return minRange
	 **/
	@ApiModelProperty(value = "The minimum byte range from the \"Content-Range\" header. This property will only be included if a file chunck is sent.")

	public Long getMinRange() {
		return minRange;
	}

	public void setMinRange(Long minRange) {
		this.minRange = minRange;
	}

	/**
	 * The maximum byte range from the \"Content-Range\" header. This poperty will only be included if a file chunck is
	 * sent.
	 * 
	 * @return maxRange
	 **/
	@ApiModelProperty(value = "The maximum byte range from the \"Content-Range\" header. This poperty will only be included if a file chunck is sent.")

	public Long getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(Long maxRange) {
		this.maxRange = maxRange;
	}

}
