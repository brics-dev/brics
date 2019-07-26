package gov.nih.tbi.util;

import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DownloadPVMappingRow;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadUtil {

	/**
	 * This method returns a list of DownloadPVMappingRows objects that are used to generate the mapping file when
	 * downloading the data table to queue.
	 * 
	 * @return list of DownloadPVMappingRow objects, each row maps to a data element with permissible value defined.
	 */
	public static List<DownloadPVMappingRow> getDownloadPVMappings(List<FormResult> selectedForms, String displayOption,
			CodeMapping codeMapping) {

		Map<String, Map<String, ValueRange>> dePVMap = codeMapping.getDeValueRangeMap();

		// no data mapping, return empty list
		if (dePVMap == null) {
			return new ArrayList<DownloadPVMappingRow>();
		}

		Map<String, DownloadPVMappingRow> deNameMap = new HashMap<String, DownloadPVMappingRow>();

		boolean displaySchema = InstancedDataUtil.isDisplaySchema(displayOption);

		for (FormResult formResult : selectedForms) {
			for (RepeatableGroup rg : formResult.getRepeatableGroups()) {
				for (DataElement de : rg.getDataElements()) {

					// Only data elements with permissible value defined are included
					if (de.hasPermissibleValues()) {

						String deName = de.getName();
						if (!deNameMap.containsKey(deName)) {
							DownloadPVMappingRow mapping = new DownloadPVMappingRow();
							mapping.setDeName(deName);
							mapping.setDeDescription(de.getDescription());
							mapping.setDeTitle(de.getTitle());

							if (dePVMap.containsKey(deName)) {
								String pvValueDisplay = "", pvCodeDisplay = "", pvDescDisplay = "",
										schemaPvDisplay = "";

								Map<String, ValueRange> pvMap = dePVMap.get(deName);

								// Use TreeSet to sort the permissible values in alphabetic order
								Set<String> pvKeys = new TreeSet<String>(pvMap.keySet());
								for (String pvValue : pvKeys) {
									ValueRange vr = pvMap.get(pvValue);
									Integer outputCode = vr.getOutputCode();
									String desc = vr.getDescription();

									pvValueDisplay += pvValue + ";";
									pvCodeDisplay += (outputCode == null ? "" : outputCode) + ";";
									pvDescDisplay += desc + ";";

									if (displaySchema) {
										SchemaPv schemaPv = vr.getSchemaPvBySchema(displayOption);
										if (schemaPv != null) {
											mapping.setSchemaDeId(schemaPv.getSchemaDeId());
											schemaPvDisplay += (schemaPv.getPermissibleValue() == null ? "" : schemaPv
													.getPermissibleValue());
										}
										schemaPvDisplay += ";";
									}
								}

								if (pvValueDisplay.endsWith(";")) {
									pvValueDisplay = pvValueDisplay.substring(0, pvValueDisplay.length() - 1);
								}
								if (pvCodeDisplay.endsWith(";")) {
									pvCodeDisplay = pvCodeDisplay.substring(0, pvCodeDisplay.length() - 1);
								}
								if (pvDescDisplay.endsWith(";")) {
									pvDescDisplay = pvDescDisplay.substring(0, pvDescDisplay.length() - 1);
								}

								mapping.setPvValue(pvValueDisplay);
								mapping.setPvCode(pvCodeDisplay);
								mapping.setPvDesciption(pvDescDisplay);

								if (displaySchema) {
									if (mapping.getSchemaDeId() == null) {
										schemaPvDisplay = "";
									} else if (schemaPvDisplay.endsWith(";")) {
										schemaPvDisplay = schemaPvDisplay.substring(0, schemaPvDisplay.length() - 1);
									}
									mapping.setSchemaValue(schemaPvDisplay);
								}
							}

							deNameMap.put(deName, mapping);
						}
					}
				}
			}
		}

		List<DownloadPVMappingRow> mappingList = new ArrayList<DownloadPVMappingRow>();
		mappingList.addAll(deNameMap.values());

		// Finally sorting the entire list by data element name asc
		Collections.sort(mappingList, new Comparator<DownloadPVMappingRow>() {

			@Override
			public int compare(DownloadPVMappingRow row1, DownloadPVMappingRow row2) {
				return row1.getDeName().compareToIgnoreCase(row2.getDeName());
			}
		});

		return mappingList;
	}


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
