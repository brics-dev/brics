package gov.nih.tbi.commons.portal;

import gov.nih.tbi.commons.model.hibernate.FileType;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Since the documentation upload pages should be reused for object types that allow multiple documentations to be
 * uploaded, this interface will ensure that action classes include all of the methods necessary for the share .jsp's to
 * work correctly.
 * 
 * @author Fancy Chen
 *
 * @param <T> - class of the object we are trying to manage the documentation of
 */
public interface DocumentationUploadAction<T> {
	/**
	 * Returns the session object of type T Essentially returns the applicable session object that the user is currently
	 * trying to upload documentations to.
	 * 
	 * @return
	 */
	public T getSessionObject();

	/**
	 * Returns the maximum amount of documentations allowed to be added. If no limit should exist, just return any
	 * negative integer.
	 * 
	 * @return
	 */
	public int getDocumentationLimit();

	/**
	 * Returns the name of the non-validation action
	 * 
	 * @return
	 */
	public String getActionName();

	/**
	 * Returns the name of the validation action
	 * 
	 * @return
	 */
	public String getValidationActionName();

	/**
	 * Returns the select documentation in the from the edit workflow
	 * 
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public String getSelectedDocumentFromParam() throws UnsupportedEncodingException;
	
	/**
	 * Returns list of file types
	 * @return
	 */
	public List<FileType> getSupportingDocTypes();
}
