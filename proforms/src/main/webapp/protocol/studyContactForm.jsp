<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.common.ProtocolConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbLookup"%>
<%@ page import="gov.nih.nichd.ctdb.common.LookupSessionKeys"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditpublications" />
<html>
	<s:set var="pageTitle" scope="request">
		<s:text name="study.contacts.title"/>
	</s:set>
    <%-- Include Header --%>
	<jsp:include page="/common/header_struts2.jsp"/>
	
	<script type="text/javascript">
		var webRoot = "<s:property value='#webRoot'/>";
		
		$(document).ready(function()
		{
			// Check if the page should be in edit mode
			if ($("#contactId").val() > 0) {
				$("#addBtn").hide();
				$("#updateBtn").show();
			}
			
			// ++++++ Event Listners ++++++
			
			// A listener for all text fields when a change occurs
			$("input:text").change(function(event) {
				// Remove all leading and trailing white spaces
				$(this).val(jQuery.trim($(this).val()));
			});
			
			// The form cancel button listener
			$("#cancelBtn").click(function(event) {
				redirectWithReferrer(webRoot + "/protocol/studyContact.action");
			});
			
			// The edit contact button listener
			$("#editContactBtn").click(function(event) {
				var url = webRoot + "/protocol/studyContact.action?id=";
				var docIds = IbisDataTables.getSelectedOptions(IbisDataTables.getTable($("#contactsDisplayTable")));
				
				redirectWithReferrer(url + docIds[0]);
			});
			
			// The delete contact button listener
			$("#deleteContactBtn").click(function(event) {
				var agree = confirm("<s:text name="report.alert.delete" />");
				
				if (agree) {
					var url = webRoot + "/protocol/deleteContact.action?idsToDelete=";
					var docIds = IbisDataTables.getSelectedOptions(IbisDataTables.getTable($("#contactsDisplayTable")));
					var params = "";
					
					// Convert the array of IDs to a comma delimited string list
					for (var idx = 0; idx < docIds.length; idx++) {
						params += docIds[idx];
						
						if ((idx + 1) < docIds.length) {
							params += ",";
						}
					}
					
					redirectWithReferrer(url + params);
				}
			});
		});
	</script>
	
	<p><s:text name="study.contacts.instruction"/></p>
	<h3 class="toggleable">
		<s:text name="study.contacts.addEdit.title"/>
	</h3>
	<div id="addEditContactsSection">
		<s:form enctype="multipart/form-data" id="contactsForm" theme="simple" method="post">
			<s:hidden name="id" id="contactId"/>
			<s:hidden name="studyId" id="studyId"/>
			<s:hidden name="addressId"/>
			
			<label class="requiredInput"></label> 
			<i><s:text name="protocol.create.requiredSymbol.display"/></i>
			<br/><br/>
			<div class="formrow_2">
				<label class="requiredInput" for="contactName"><s:text name="study.contacts.name.display"/></label>
				<s:textfield id="contactName" name="name" maxlength="255"/>			
			</div>
			<div class="formrow_2">
				<label for="contactStudySite"><s:text name="study.contacts.studySite.display"/></label>
				<s:select id="contactStudySite" name="studySite" headerKey="%{@java.lang.Integer@MIN_VALUE}" headerValue="- -"
						list="#request._study_sites" listKey="id" listValue="shortName" />
			</div>
			<div class="formrow_2">
				<label for="contactOrg"><s:text name="study.contacts.organization.display"/></label>
				<s:textfield id="contactOrg" name="organization"  maxlength="255" />
			</div>
			<div class="formrow_2">
				<label  for="contactAddress1"><s:text name="study.contacts.address1.display"/></label>
				<s:textfield id="contactAddress1" name="address1" maxlength="210"/>
			</div>
			<div class="formrow_2">
				<label for="contactInstitute"><s:text name="study.contacts.institute.display"/></label>
				<s:select id="contactInstitute" name="instituteId" list="#request._lookup__xinstitute" listKey="id" listValue="shortName" />
			</div>
			<div class="formrow_2">
				<label for="contactAddress2"><s:text name="study.contacts.address2.display"/></label>
				<s:textfield id="contactAddress2" name="address2" maxlength="255" />
			</div>
			<div class="formrow_2">
				<label for="contactPhone1"><s:text name="study.contacts.phone1.display"/></label>
				<s:textfield id="contactPhone1" name="phone1" maxlength="25" />
			</div>
			<div class="formrow_2">
				<label for="contactCity"><s:text name="study.contacts.city.display"/></label>
				<s:textfield id="contactCity" name="city" maxlength="210" />
			</div>
			<div class="formrow_2">
				<label for="contactPhone2"><s:text name="study.contacts.phone2.display"/></label>
				<s:textfield id="contactPhone2" name="phone2"  maxlength="25" />
			</div>
			<div class="formrow_2">
				<label for="contactState"><s:text name="study.contacts.state.display"/></label>
				<s:select id="contactState" name="state" list="#request._lookup__xstate" listKey="id" listValue="longName" />
			</div>
			<div class="formrow_2">
				<label for="contactType"><s:text name="study.contacts.contactType.display"/></label>
				<s:select id="contactType" name="contactType" list="#request._lookup__xexternalcontacttype" listKey="id" listValue="shortName" />
			</div>
			<div class="formrow_2">
				<label for="contactZipCode"><s:text name="study.contacts.zipCode.display"/></label>
				<s:textfield id="contactZipCode" name="zipCode" maxlength="25" />
			</div>
			<div class="formrow_2">
				<label for="contactEmail"><s:text name="study.contacts.email.display"/></label>
				<s:textfield id="contactEmail" name="emailAddress" maxlength="255" />
			</div>
			<div class="formrow_2">
				<label for="contactCountry"><s:text name="study.contacts.country.display"/></label>
				<s:select id="contactCountry" name="country" list="#request._lookup__xcountry" listKey="id" listValue="longName" />
			</div>
			<div class="formrow_1">
				<input type="button" id="cancelBtn" value="<s:text name='button.Cancel'/>" title="Click to discard all changes and start over again"/>
				<input type="reset" value="<s:text name='button.Reset'/>" title="Click to clear fields"/>
				<s:submit id="updateBtn" cssClass="hidden" action="saveContact" key="button.updateContact" title="Click to update contact information"/>
				<s:submit id="addBtn" action="saveContact" key="button.addContact" title="Click to add new contact"/>
			</div>
		</s:form>
		
		<h3><s:text name="study.contacts.myContacts.title"/></h3>
		<p><s:text name="study.contacts.myContacts.instruction"/></p>
		<br/>
		<div class="dataTableContainer" id="contactsDisplayTable">
			<ul>
				<li>
					<input type="button" id="editContactBtn" value="<s:text name='button.Edit'/>" title="Click to make changes"/>
				</li>
				<li>
					<input type="button" id="deleteContactBtn" value="<s:text name='button.Delete'/>" class="enabledOnMany" title="Click to delete"/>
				</li>
			</ul>
			<display:table name="<%= ProtocolConstants.CONTACTS_LIST %>" scope="request" decorator="gov.nih.nichd.ctdb.protocol.tag.StudyContactDecorator">
				<display:setProperty name="basic.msg.empty_list" value="There are no contacts to display at this time."/>
				<display:column property="contactsCheckBox" title="" />
				<display:column property="name" title="Name" />
				<display:column property="organization"  title="organization"  />
				<display:column property="institute.shortName" title="Institute"/>
				<display:column property="phone1" title="Phone"/>
				<display:column property="emailAddress" title="Email Address"/>
				<display:column property="displayableAddress" title="Address"/>
			</display:table>
		</div>
	</div>
	
	<%-- Include Footer --%>
	<jsp:include page="/common/footer_struts2.jsp" />
</html>