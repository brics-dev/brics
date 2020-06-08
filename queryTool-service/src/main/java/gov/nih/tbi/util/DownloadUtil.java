package gov.nih.tbi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.pojo.FormResult;

public class DownloadUtil {

	
	/**
	 * Runs the user supplied query name through a series of validation tests. The query name will also have any leading
	 * or trailing white spaces removed during the validation process.
	 * 
	 * @return True if and only if the query name passes all of the validation tests.
	 */
	public static String validatePackageName(String packageName) {

		String pkgName = (packageName != null) ? packageName.trim() : "";
		String errorMsg = null;

		if (ValUtil.isBlank(pkgName)) {
			errorMsg = "The package name is required.";

		} else if (pkgName.length() > 100) {
			errorMsg = "The package name must not exceed 100 characters in length.";

		} else {
			// Verify no illegal characters (\ / : * ? | < >).
			Pattern pattern = Pattern.compile("[\\\\/:\\*\\?\\|\\<\\>]");
			Matcher matcher = pattern.matcher(pkgName);

			if (matcher.find()) {
				errorMsg = "The package name cannot contain the following special characters: \\ / : * ? | < >";
			}
		}

		return errorMsg;
	}


	public static List<FormResult> cloneFormResults(Collection<FormResult> formResults) {

		List<FormResult> formList = new ArrayList<FormResult>();

		for (FormResult form : formResults) {
			formList.add(new FormResult(form));
		}

		return formList;
	}


}
