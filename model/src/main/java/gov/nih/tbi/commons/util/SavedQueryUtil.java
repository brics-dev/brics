package gov.nih.tbi.commons.util;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.query.model.hibernate.SavedQuery;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SavedQueryUtil {
	public static synchronized JsonObject savedQueryToJsonWithQuery(SavedQuery sq) {
		JsonObject output = savedQueryToJsonNoPermissions(sq);

		// Add in the query property.
		try {
			output.add("query", savedQueryToJsonView(sq));
		} catch (Exception e) {
			// if we have an exception, just send an empty object
			output.add("query", new JsonObject());
		}

		return output;
	}
	
	public static synchronized JsonObject toJsonViewWithPerms(SavedQuery sq, List<EntityMap> permissions) throws Exception {
		JsonObject output = savedQueryToJsonView(sq);
		output.add("linkedUsers", permissionsToJsonArray(permissions));
		return output;
	}

	public static synchronized JsonObject savedQueryToJsonView(SavedQuery sq) throws Exception {

		Gson gson = new Gson();
		return gson.fromJson(sq.getQueryData(), JsonObject.class);
	}


	/**
	 * This method parses xml data of SavedQuery and converts it
	 * 
	 * @param sq - Saved Query to be converted
	 * @return - JsonObject representation of xml data
	 * @throws Exception
	 */
	public static JsonObject savedQueryXmlToJson(SavedQuery sq) throws Exception {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder dbuilder = dbf.newDocumentBuilder();
		Document doc = dbuilder.parse(new InputSource(new StringReader(sq.getXml())));

		JsonObject output = new JsonObject();
		output.addProperty("name", sq.getName());
		output.addProperty("description", sq.getDescription());
		output.addProperty("lastUpdated", BRICSTimeDateUtil.dateToDateTimeString(sq.getLastUpdated()));

		// initialize the base properties
		JsonArray forms = new JsonArray();
		JsonArray studies = new JsonArray();
		
		Pattern uriWithVersionPatten = Pattern.compile("(.*)_v(\\d+[.]\\d+)");
		Pattern rgUriWithVersionPatten = Pattern.compile("(.*)_v(\\d+[.]\\d+)(%20.*)");

		NodeList studiesInCartTags = doc.getElementsByTagName("studiesInCart");
		// there should only be one studiesInCartTags entry, so just get it
		if (studiesInCartTags.getLength() > 0) {
			Node studiesInCartTag = studiesInCartTags.item(0);
			// <entry> containing <key>,<value>
			NodeList entryTags = studiesInCartTag.getChildNodes();
			for (int i = 0; i < entryTags.getLength(); i++) {
				Element entry = (Element) entryTags.item(i);
				NodeList studyValues = entry.getElementsByTagName("value");
				for (int j = 0; j < studyValues.getLength(); j++) {
					JsonObject study = new JsonObject();
					Element studyElement = (Element) studyValues.item(j);
					study.addProperty("title", getTextFromNamedTag(studyElement, "title"));
					study.addProperty("uri", getTextFromNamedTag(studyElement, "uri"));
					study.addProperty("id", studyElement.getAttribute("id"));
					studies.add(study);
				}
			}
		}

		// Some newer SavedQuery may not have studiesInCart tag, we will then get studies from forms associated
		Map<String, JsonObject> studiesJson = null;
		if (studies.size() == 0) {
			studiesJson = new HashMap<String, JsonObject>();
		}

		// now do forms
		NodeList formsInCartTags = doc.getElementsByTagName("formsInCart");
		// there should only be one formsInCartTags entry, so just get it
		if (formsInCartTags.getLength() > 0) {
			Node formsInCartTag = formsInCartTags.item(0);
			// <entry> containing <key>,<value>
			NodeList entryTags = formsInCartTag.getChildNodes();
			for (int i = 0; i < entryTags.getLength(); i++) {
				Element entry = (Element) entryTags.item(i);
				NodeList formValues = entry.getElementsByTagName("value");
				for (int j = 0; j < formValues.getLength(); j++) {
					JsonObject form = new JsonObject();
					Element formElement = (Element) formValues.item(j);
					form.addProperty("name", getTextFromNamedTag(formElement, "title"));
					
					String formUri = getTextFromNamedTag(formElement, "uri");
					Matcher matcher = uriWithVersionPatten.matcher(formUri);
					if (matcher.find()) {
						formUri = matcher.group(1);
					}
					form.addProperty("uri", formUri);
					
					form.addProperty("id", formElement.getAttribute("id"));

					// -- STUDIES --
					JsonArray formStudyIds = new JsonArray();
					NodeList formStudies = formElement.getElementsByTagName("studies");
					// there should only be one, so grab it
					Element formStudiesElement = (Element) formStudies.item(0);
					NodeList studyResults = formStudiesElement.getElementsByTagName("studyResult");
					for (int k = 0; k < studyResults.getLength(); k++) {
						Element studyResult = (Element) studyResults.item(k);
						String studyId = studyResult.getAttribute("id");
						formStudyIds.add(new JsonPrimitive(studyId));

						if (studiesJson != null && !studiesJson.containsKey(studyId)) {
							JsonObject study = new JsonObject();
							study.addProperty("title", getTextFromNamedTag(studyResult, "title"));
							study.addProperty("uri", getTextFromNamedTag(studyResult, "uri"));
							study.addProperty("id", studyResult.getAttribute("id"));
							studiesJson.put(studyId, study);
						}
					}
					form.add("studyIds", formStudyIds);

					// -- REPEATABLE GROUPS --
					JsonArray formGroups = new JsonArray();
					NodeList formRGs = formElement.getElementsByTagName("repeatableGroups");
					Element formRGElement = null;

					for (int z = 0; z < formRGs.getLength(); z++) {
						Element rg = (Element) formRGs.item(z);
						if (rg.getElementsByTagName("repeatableGroup").getLength() > 0) {
							formRGElement = rg;
						}
					}

					// there should only be one, so grab it
					// Element formRGElement = (Element) formRGs.item(0);
					if (formRGElement != null) {
						NodeList repeatableGroups = formRGElement.getElementsByTagName("repeatableGroup");

						for (int k = 0; k < repeatableGroups.getLength(); k++) {
							JsonObject group = new JsonObject();
							Element rg = (Element) repeatableGroups.item(k);
							group.addProperty("name", getTextFromNamedTag(rg, "name"));
							
							String rgUri = getTextFromNamedTag(rg, "uri");
							Matcher rgMatcher = rgUriWithVersionPatten.matcher(rgUri);
							if (rgMatcher.find()) {
								rgUri = rgMatcher.group(1) + rgMatcher.group(3);
							}
							group.addProperty("uri", rgUri);

							// -- data elements --
							JsonArray rgDataElements = new JsonArray();
							NodeList dataElementContainerTags = rg.getElementsByTagName("dataElements");
							// there is only one container, get it
							Element dataElementContainer = (Element) dataElementContainerTags.item(0);
							NodeList dataElements = dataElementContainer.getElementsByTagName("dataElement");
							for (int m = 0; m < dataElements.getLength(); m++) {
								JsonObject de = new JsonObject();
								Element deElement = (Element) dataElements.item(m);
								de.addProperty("uri", getTextFromNamedTag(deElement, "uri"));
								de.addProperty("name", getTextFromNamedTag(deElement, "title"));
								de.addProperty("id", deElement.getAttribute("id"));
								rgDataElements.add(de);
							}
							group.add("elements", rgDataElements);

							// add the RG to the json object
							formGroups.add(group);
						}
						form.add("groups", formGroups);
					}

					// -- FILTERS --
					JsonArray filters = new JsonArray();
					NodeList filterElements = formElement.getElementsByTagName("filters");

					Element filterElement = null;

					for (int f = 0; f < filterElements.getLength(); f++) {
						Element filter = (Element) filterElements.item(f);
						if (filter.getElementsByTagName("filter").getLength() > 0) {
							filterElement = filter;
							break;
						}
					}

					if (filterElement != null) {
						NodeList filterTags = filterElement.getElementsByTagName("filter");
						for (int k = 0; k < filterTags.getLength(); k++) {
							JsonObject filter = new JsonObject();
							
							String filterFormUri = getTextFromNamedTag(formElement, "uri");
							Matcher filterFormMatcher = uriWithVersionPatten.matcher(filterFormUri);
							if (filterFormMatcher.find()) {
								filterFormUri = filterFormMatcher.group(1);
							}
							filter.addProperty("formUri", filterFormUri);
							
							Element filterTag = (Element) filterTags.item(k);
							// de subelement, there's only one
							NodeList filterDes = filterTag.getElementsByTagName("element");
							Element filterDe = (Element) filterDes.item(0);
							filter.addProperty("elementUri", getTextFromNamedTag(filterDe, "uri"));

							NodeList filterRgs = filterTag.getElementsByTagName("group");
							Element filterRg = (Element) filterRgs.item(0);
							
							String rgUri = getTextFromNamedTag(filterRg, "uri");
							Matcher rgMatcher = rgUriWithVersionPatten.matcher(rgUri);
							if (rgMatcher.find()) {
								rgUri = rgMatcher.group(1) + rgMatcher.group(3);
							}
							filter.addProperty("groupUri", rgUri);

							// filter values and such
							filter.addProperty("freeFormValue", getFilterProperty(filterTag, "freeFormValue"));
							filter.addProperty("blank", getFilterProperty(filterTag, "blank"));
							filter.addProperty("minimum", getFilterProperty(filterTag, "minimum"));
							filter.addProperty("maximum", getFilterProperty(filterTag, "maximum"));
							filter.addProperty("dateMin", getFilterProperty(filterTag, "dateMin"));
							filter.addProperty("dateMax", getFilterProperty(filterTag, "dateMax"));

							// -- permissibleValues
							JsonArray permissibleValueArray = new JsonArray();
							NodeList permissibleValues = filterTag.getElementsByTagName("permissibleValues");
							if (permissibleValues.getLength() > 0) {
								
								// Get the last permissibleValues since element tag also contains permissibleValues
								Element permissibleValuesContainerTag =
										(Element) permissibleValues.item(permissibleValues.getLength() - 1);
								NodeList permissibleValueList =
										permissibleValuesContainerTag.getElementsByTagName("permissibleValue");
								for (int m = 0; m < permissibleValueList.getLength(); m++) {
									Element permissibleValueTag = (Element) permissibleValueList.item(m);
									permissibleValueArray.add(new JsonPrimitive(permissibleValueTag.getTextContent()));
								}

							}
							filter.add("permissibleValues", permissibleValueArray);

							filters.add(filter);
						}
					}
					form.add("filters", filters);

					// form is all done
					forms.add(form);
				} // end each form
			} // end all form entry

		}
		// the selected list, it's an array of form URIs
		JsonArray selectedForms = new JsonArray();
		NodeList selectedFormsXml = doc.getElementsByTagName("selectedFormURIList");
		if (selectedFormsXml.getLength() > 0) {
			Element selectedFormXmlTag = (Element) selectedFormsXml.item(0);
			NodeList formUriList = selectedFormXmlTag.getElementsByTagName("formURI");

			for (int i = 0; i < formUriList.getLength(); i++) {
				Element formUriTag = (Element) formUriList.item(i);
				
				String formUri = formUriTag.getTextContent();
				Matcher matcher = uriWithVersionPatten.matcher(formUri);
				if (matcher.find()) {
					formUri = matcher.group(1);
				}
				selectedForms.add(new JsonPrimitive(formUri));
			}
		}

		output.add("selectedFormURIList", selectedForms);
		output.add("forms", forms);

		if (studies.size() == 0 && studiesJson != null && studiesJson.size() > 0) {
			for (JsonObject studyJson : studiesJson.values()) {
				studies.add(studyJson);
			}
		}
		output.add("studies", studies);

		return output;
	}

	private static String getFilterProperty(Element filter, String tagName) {
		NodeList filterTypeList = filter.getElementsByTagName(tagName);
		if (filterTypeList.getLength() > 0) {
			Element tempFilterElement = (Element) filterTypeList.item(0);
			return tempFilterElement.getTextContent();
		}
		return "";
	}

	private static String getTextFromNamedTag(Element parent, String tagName) {
		NodeList children = parent.getElementsByTagName(tagName);
		if (children.getLength() > 0) {
			// assuming only 1 as per function signature
			Element child = (Element) children.item(0);
			return child.getTextContent();
		}
		return null;
	}

	/**
	 * Converts the given SavedQuery POJO to a simplified version of the Backbone SavedQuery model. The generated JSON
	 * object will only contain the "id" and "name" attributes of the SavedQuery model.
	 * 
	 * @param sq - The SavedQuery POJO to convert.
	 * @return JSON for a simplified SavedQuery Backbone model.
	 */
	public static synchronized JsonObject savedQueryToSimpleJson(SavedQuery sq) {
		JsonObject output = new JsonObject();

		output.add("id", new JsonPrimitive(sq.getId()));
		output.add("name", new JsonPrimitive(sq.getName()));

		return output;
	}

	/**
	 * Converts the given SavedQuery POJO and EntityMap list into a JSON object that can be passed into the SavedQuery
	 * Backbone model constructor. The XML of the query object saved with in the SavedQuery POJO will be ignored for
	 * this conversion.
	 * 
	 * @param sq - The SavedQuery POJO to convert.
	 * @param permissions - A list of EntityMap POJOs to convert.
	 * @return JSON for a JavaScript object that should conform to the Backbone model described in the
	 *         src/main/webapp/js/models/SavedQuery.js file. Please note that the "lastUpdated" object member will be
	 *         converted to a date string in the ISO8601 format.
	 */
	public static synchronized JsonObject savedQueryToBackBoneModel(SavedQuery sq, List<EntityMap> permissions) {
		JsonObject out = savedQueryToJsonNoPermissions(sq);

		// Add in the users with their assigned permissions.
		out.add("linkedUsers", permissionsToJsonArray(permissions));

		return out;
	}

	/**
	 * Converts the given SavedQuery object to an a JSON object that can be used to create a SavedQuery Backbone model.
	 * The returned JSON object will not contain the user permissions for the Backbone model. Note the "lastUpdated"
	 * attribute will be represented in ISO8601 format.
	 * 
	 * @param sq - The SavedQuery object whose data will be used in the conversion.
	 * @return A JSON object with only the non-permission attributes of the SavedQuery Backbone model.
	 */
	private static JsonObject savedQueryToJsonNoPermissions(SavedQuery sq) {
		JsonObject output = new JsonObject();

		output.addProperty("id", sq.getId());
		output.addProperty("name", sq.getName());
		output.addProperty("description", sq.getDescription());
		output.addProperty("copyFlag", sq.getCopyFlag());
		output.addProperty("lastUpdated", (BRICSTimeDateUtil.dateToDateTimeString(sq.getLastUpdated())));

		return output;
	}

	/**
	 * Converts the given list of EntityMap POJOs to a JSON array for the "linkedUsers" attribute of the SavedQuery
	 * Backbone model.
	 * 
	 * @param permissions - A list of EntityMap object to convert.
	 * @return A JSON array that conforms to the "linkedUsers" Backbone collection for the SavedQuery Backbone model.
	 */
	private static JsonArray permissionsToJsonArray(List<EntityMap> permissions) {
		JsonArray linkedUsers = new JsonArray();

		for (EntityMap em : permissions) {
			Account account = em.getAccount();
			User usr = account.getUser();
			JsonObject user = new JsonObject();
			JsonObject perm = new JsonObject();

			// Populate the permission object.
			perm.addProperty("entityMapId", em.getId());
			perm.addProperty("entityId", em.getEntityId());
			perm.addProperty("permission", em.getPermission().getName());

			// Populate user fields.
			user.addProperty("id", account.getId());
			user.addProperty("userName", account.getUserName());
			user.addProperty("firstName", usr.getFirstName());
			user.addProperty("lastName", usr.getLastName());
			user.addProperty("email", usr.getEmail());
			user.add("assignedPermission", perm);

			// Add new user object to the linked users array.
			linkedUsers.add(user);
		}

		return linkedUsers;
	}

	/**
	 * Converts a JSON object to a SavedQuery POJO. Please note that the generated POJO will not have the have the saved
	 * QT query set. The given JSON object must conform to the SavedQuery Backbone model used in the Query Tool
	 * interface.
	 * 
	 * @param jo - A JSON object based on the SavedQuery Backbone model.
	 * @return A SavedQuery POJO with the data from the given JSON object. Only the saved query meta data will be set in
	 *         the returned POJO. The QT query data will not be set by this method.
	 * @throws IllegalStateException When there is an error accessing data from the JSON array.
	 */
	public static synchronized SavedQuery jsonToSavedQueryWithNoQuery(JsonObject jo) throws IllegalStateException {
		SavedQuery sq = new SavedQuery();

		sq.setName(jo.getAsJsonPrimitive("name").getAsString());
		sq.setDescription(jo.getAsJsonPrimitive("description").getAsString());
		sq.setCopyFlag(Boolean.valueOf(jo.getAsJsonPrimitive("copyFlag").getAsBoolean()));

		// Set the ID of the saved query, if able.
		long sqId = jo.getAsJsonPrimitive("id").getAsLong();

		if (sqId > 0) {
			sq.setId(Long.valueOf(sqId));
		}

		// Get last updated date
		String dateStr = jo.getAsJsonPrimitive("lastUpdated").getAsString();

		if (!dateStr.isEmpty()) {
			sq.setLastUpdated(BRICSTimeDateUtil.stringToDate(dateStr));
		}

		return sq;
	}

	/**
	 * Converts the given JSON array of user permissions to entity maps. The JSON array must conform to the
	 * "linkedUsers" Backbone collection found in the SavedQuery Backbone model.
	 * 
	 * @param linkedUsers - The JSON array of user permissions to be converted.
	 * @param accountMap - A map of Account objects which matches the amount of users referenced in the "linkedUsers"
	 *        JSON array. The key for this map will be the usernames of each associated Account POJO.
	 * @return A list of EntityMap POJOs that mirrors the user permissions in the given JSON array.
	 * @throws IllegalStateException When there is an error accessing data from the JSON array.
	 */
	public static synchronized List<EntityMap> jsonToEntityMapList(JsonArray linkedUsers,
			Map<String, Account> accountMap) throws IllegalStateException {
		List<EntityMap> entityList = new ArrayList<EntityMap>(linkedUsers.size());

		// Convert the permissions for each linked user to entity maps.
		for (JsonElement usrElem : linkedUsers) {
			JsonObject userJson = usrElem.getAsJsonObject();
			JsonObject entityMapJson = userJson.getAsJsonObject("assignedPermission");
			EntityMap em = new EntityMap();

			em.setAccount(accountMap.get(userJson.getAsJsonPrimitive("userName").getAsString()));
			em.setType(EntityType.SAVED_QUERY);

			// Set the entity map ID.
			long entityMapId = entityMapJson.getAsJsonPrimitive("entityMapId").getAsLong();

			if (entityMapId > 0) {
				em.setId(Long.valueOf(entityMapId));
			}

			// Set the permission type
			String permType = entityMapJson.getAsJsonPrimitive("permission").getAsString();
			em.setPermission(PermissionType.getByName(permType));

			entityList.add(em);
		}

		return entityList;
	}
}
