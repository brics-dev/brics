package gov.nih.tbi.dictionary.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.xml.XmlGenerationUtil;

public class SearchParamParserUtil {

	private static Logger logger = Logger.getLogger(XmlGenerationUtil.class);

	private static final Set<String> PAGINATION_VALUES =
			new HashSet<String>(Arrays.asList(new String[] {"sort", "pageSize", "page"}));

	public static Map<FormStructureFacet, Set<String>> generateFormStructureFacetMap(Map<String, String[]> map) {

		Map<FormStructureFacet, Set<String>> facetMap = new HashMap<FormStructureFacet, Set<String>>();
		for (String key : map.keySet()) {
			// Check if it is a valid pagination parameter
			if (PAGINATION_VALUES.contains(key)) {
				continue; // ignore this key and move on to the next one.
			}

			// Attempt to identify facet
			FormStructureFacet f = convertToFacet(key);
			if (f == null) {
				// TODO: ignore, but in the future this should throw an BRICS web exception
				continue;
			}

			Set<String> s = new HashSet<String>();
			// This loop will have more than one value in the case where ?disease=ABC&disease=XYZ
			for (String v : map.get(key)) {
				// parse by comma for case where ?disease=ABC,XYZ
				s.addAll(Arrays.asList(v.split(",")));
			}
			
			translateToRdfValues(f, s);
			
			facetMap.put(f, s);
		}

		return facetMap;
	}

	/**
	 * Crappy manual conversion for now, should be looked up from a configurable map (no not
	 * generating facets at all with new DAO strats)
	 * 
	 * @param key
	 * @return
	 */
	private static FormStructureFacet convertToFacet(String key) {
		FormStructureFacet f = null;
		switch (key) {
			case "status":
				f = FormStructureFacet.STATUS;
				break;
			case "formType":
				f = FormStructureFacet.SUBMISSION_TYPE;
				break;
			case "diseases":
				f = FormStructureFacet.DISEASE;
				break;
			case "modifiedDate":
				f = FormStructureFacet.MODIFIED_DATE;
				break;
			case "name":
				f = FormStructureFacet.SHORT_NAME;
				break;
			case "title":
				f = FormStructureFacet.TITLE;
				break;
			case "description":
				// f = FormStructureFacet.DESCRIPTION; // Description is not currently searched by
				// our system.
				break;
			default:
				break;
		}

		return f;
	}

	/**
	 * For enumerated values, the all caps input that is supplied by the user will not match the case sensitive search that is used by the core package's RDF DAOs.  This code has hardcoded translations from the former to the latter and should quickly be replaced by configurations like all the other hardcoded implementations in the first pass of the dictionary service.
	 */
	private static void translateToRdfValues(FormStructureFacet f, Set<String> s) {
		if (s != null) {
			for (String v : s) {
				if (FormStructureFacet.STATUS.equals(f)) {
					switch (v) {
						case "DRAFT":
							v = StatusType.DRAFT.getType();
							break;
						case "AWAITING_PUBLICATION":
							v = StatusType.AWAITING_PUBLICATION.getType();
							break;
						case "PUBLISHED":
							v = StatusType.PUBLISHED.getType();
							break;
						case "ARCHIVED":
							v = StatusType.ARCHIVED.getType();
							break;
						case "SHARED_DRAFT":
							v = StatusType.SHARED_DRAFT.getType();
							break;
						default:
							break;
					}
				}
			}
		}
	}
	
	
}
