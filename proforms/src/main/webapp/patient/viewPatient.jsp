<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol,
                 gov.nih.nichd.ctdb.patient.domain.PhoneType,
                 gov.nih.nichd.ctdb.patient.domain.Phone"%>
<%@ page import="gov.nih.nichd.ctdb.patient.domain.Patient"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ page import="java.text.DateFormat"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewpatients"/>

<%
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	Locale l = request.getLocale();
%>

<html>
<s:set var="pageTitle" scope="request" >
	<s:text name="patient.view.title.display" />: 
	<s:property value="patientForm.firstName"/> <s:property value="patientForm.lastName" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<s:set var="disallowingPII" value="#systemPreferences.get('guid_with_non_pii')" />

<style type="text/css">
	#visitsTableContainer table th {
		text-align: center;
	}
	
	#visitsTableContainer table td {
		text-align: center;
	}
</style>

<script type="text/javascript">
var basePath = "<s:property value='#webRoot'/>";

$(document).ready(function() {
	$("#btnEditPatient").click(function(event) {
		var url = basePath + '/patient/showEditPatient.action?patientId=' + $("#patientId").val() + "&sectionDisplay=default";
		redirectWithReferrer(url);
	});
	
	$("#btnEditPatientStudy").click(function(event) {
		var url = basePath + '/patient/showEditPatient.action?patientId=' + $("#patientId").val() + "&sectionDisplay=study";
		redirectWithReferrer(url);
	});
	
	$("#btnEditAttachments").click(function(event) {
		var url = basePath + '/patient/showEditPatient.action?patientId=' + $("#patientId").val() + "&sectionDisplay=attachments";
		redirectWithReferrer(url);
	});

	$("#btnEditPatientVisits").click(function(event) {
		var url = basePath + '/patient/patientVisitHome.action?patientId=' + $("#patientId").val();
		redirectWithReferrer(url);
	});
	
	$("#btnViewAttachmentAudit").click(function(event) {
		var selectedAttachments = IDT.getSelectedOptions($("#divPatientAttachments table"));
		
		if (selectedAttachments.length != 1) {
			$.ibisMessaging("dialog", "warning", "Only one attachment can be selected for auditing.");
			
			return false;
		}
		
		var url = basePath + "/attachments/attachmentAudit.action?id=" + selectedAttachments[0];
		openPopup(url, "Attachment_Audit", "width=1200,height=300,toolbar=0,location=0,directories=0,,menubar=0,status=1,scrollbars=1,resizable=1");
	});

});
</script>

<s:form theme="simple" method="post">
	<s:hidden name="patientForm.id" id="patientId"/>
	
	<p><s:text name='patient.viewdetails.instruction'/></p>
	<div id="accordion">
 		<h3 class="toggleable <s:if test="%{patientForm.sectionDisplay != 'default'}">collapsed</s:if>">
			<s:text name='patient.SubjectInformation'/> 
		</h3>
		<div class="DivPatientInfo">
			<div class="formrow_2">
				<label for="validated"> <s:text name='patient.MarkValidated'/></label>
				<s:checkbox name="patientForm.validated" id="validated" disabled="true" />
			</div>
			<div class="formrow_2"></div>
		
        <s:if test="#disallowingPII == 1">
			<div class="formrow_2">
 				<label for="guid"><s:text name="patient.guid.display" /> </label>
				<span><s:property value="patientForm.guid" /></span>
			</div>
			<div class="formrow_2"></div>
			<div class="formrow_2">
				<label for="recordNumber"><s:text name="patient.recordnumber.display" /> </label>
				<span id="subjIdSpan"><s:property value="patientForm.subjectId" /></span>
			</div>
			<!-- <div class="formrow_2"></div>
			<div class="formrow_2">
				<label for="biorepositoryId"><s:text name="patient.biorepositoryid.display" /> </label>
				<span><s:property value="patientForm.biorepositoryId" /> </span>
			</div> -->
			<div class="formrow_2"></div>
			<div class="formrow_2">
				<label for="recruited">Recruit</label>
				<s:checkbox name="patientForm.recruited" fieldValue="true" disabled="true" />
			</div>
		</s:if>
		<s:else>
			<div class="formrow_2">
				<label for="mrn"><s:text name="patient.mrn.display" /> </label>
				<span><s:property value="patientForm.mrn" /> </span>
			</div>
			<div class="formrow_2">
				<label for="recruited">Recruit</label>
				<s:checkbox name="patientForm.recruited" fieldValue="true" disabled="true" />
			</div>
			<div class="formrow_2">
				<label for="lastName"> <s:text name="patient.lastname.display" /></label>
				<span><s:property value="patientForm.lastName" /></span>
			</div>
			<div class="formrow_2">
				<label for="dateOfBirth"> <s:text name="patient.dateofbirth.display" /></label>
				<span><s:property value="patientForm.dateOfBirth" /></span>
			</div>
			
			<div class="formrow_2">
				<label for="firstname"> <s:text name="patient.firstname.display" /></label>
				<span><s:property value="patientForm.firstName" /></span>
			</div>
			<div class="formrow_2">
				<label for="birthCity">Birth City</label>
				<span><s:property value="patientForm.birthCity" /></span>
			</div>
 			<div class="formrow_2">
				<label for="middleName"> <s:text name="patient.middlename.display" /></label>
				<span><s:property value="patientForm.middleName" /></span>
			</div>
			<div class="formrow_2">
				<label for="birthCountryId"> Birth Country</label>
				<span><s:property value="patientForm.displayBirthCountry" /></span>
			</div>
			<div class="formrow_2">
				<label for="sex"> <s:text name="patient.sex.display" /></label>
				<span><s:property value="patientForm.sex" /></span>
			</div>
			<div class="formrow_2">
				<label for="address1"> <s:text name="patient.address1.display" /></label>
				<span><s:property value="patientForm.address1" /></span>
			</div>
 			<div class="formrow_2">
				<label for="email"> <s:text name="patient.email.display" /></label>
				<span><s:property value="patientForm.email" /></span>
			</div>
 			<div class="formrow_2">
				<label for="address2"> <s:text name="patient.address2.display" /></label>
				<span><s:property value="patientForm.address2" /></span>
			</div>
			<div class="formrow_2">
				<label for="homePhone"> <s:text name="patient.homephone.display" /></label>
				<span><s:property value="patientForm.homePhone" /></span>
			</div>
 			<div class="formrow_2">
				<label for="city"> <s:text name="patient.city.display" /></label>
				<span><s:property value="patientForm.city" /></span>
			</div>
			<div class="formrow_2">
				<label for="workPhone"> <s:text name="patient.workphone.display" /></label>
				<span><s:property value="patientForm.workPhone" /></span>
			</div>

 			<div class="formrow_2">
				<label for="state"> <s:text name="patient.state.display" /></label>
				<span><s:property value="patientForm.displayHomeState" /></span>
			</div>
 			<div class="formrow_2">
				<label for="mobilePhone"> <s:text name="patient.mobilephone.display" /></label>
				<span><s:property value="patientForm.mobilePhone" /></span>
			</div>
 			<div class="formrow_2">
				<label for="zip"> <s:text name="patient.zip.display" /></label>
				<span><s:property value="patientForm.zip" /></span>
			</div>
			<div class="formrow_2">
				<label for="guid"> <s:text name="patient.table.GUID" /></label>
				<span><s:property value="patientForm.guid" /></span>
			</div>
			<div class="formrow_2">
				<label for="recordNumber"><s:text name="patient.recordnumber.display" /> </label>
				<span id="subjIdSpan"><s:property value="patientForm.subjectId" /></span>
			</div>
			
 			<div class="formrow_2">
				<label for="country"> <s:text name="patient.country.display" /></label>
				<span><s:property value="patientForm.displayHomeCountry" /></span>
			</div>
			
		</s:else>

			<security:hasProtocolPrivilege privilege="addeditpatients">
				<div class="formrow_1">
					<input type="button" id="btnEditPatient" value="<s:text name='button.Edit'/>" title="<s:text name='tooltip.edit'/>" />
				</div>		
			</security:hasProtocolPrivilege>
		</div>
		
		<h3 class="toggleable collapsed"> <s:text name='patient.viewdetails.study'/> </h3>
		<div class="divPatientInfo">
 			<div class="formrow_2">
 				<label><s:text name="patient.protocolname.display" /></label> 
				<s:checkbox name="patientForm.currentProtocolId" fieldValue="%{patientForm.currentProtocolId}"  
						value="%{associatedWithStudy}" id="currProtocolCheckbox" disabled="true" />
			</div>
			<div class="formrow_2">
				<label> <s:text name="patient.futurestudy.display" /> </label> 
				<s:checkbox name="patientForm.futureStudy" id="futureStudy" disabled="true" />
			</div>
			
 			<div class="formrow_2">
				<!--  <label for="subjectNumber"> <s:text name="patient.subjectnumber.display" /></label>
				<span><s:property value="patientForm.subjectNumber" /></span>-->
			</div>
			<div class="formrow_2">
				<label for="recordNumber"> <s:text name="patient.recordnumber.display" /></label>
				<span><s:property value="patientForm.recordNumber" /></span>
			</div>

 			<div class="formrow_2">
				<label for="enrollmentDate"> <s:text name="patient.enrollmentdate.display" /></label>
				<span><s:property value="patientForm.enrollmentDate" /></span>
			</div>
			<div class="formrow_2">
				<label for="siteId"><s:text name='patient.viewdetails.subjectSite'/></label>
				<span><s:property value="patientForm.displaySiteName" /></span>
			</div>
 			<div class="formrow_2">
				<label for="completionDate"> <s:text name="patient.completionDate.display" /></label>
				<span><s:property value="patientForm.completionDate" /></span>
			</div>
			<div class="formrow_2">
				<label for="active"> <s:text name='patient.viewdetails.subjectStatus'/></label>
				<s:radio name="patientForm.active" list="#{'true':''}" disabled="true" /> <s:text name="patient.active.display"/>
				<s:radio name="patientForm.active" list="#{'false':''}" disabled="true" /> <s:text name="patient.inactive.display"/>
			</div>
			<security:hasProtocolPrivilege privilege="addeditpatients">
			<div class="formrow_1">
				<input type="button" id="btnEditPatientStudy" value="<s:text name='button.Edit'/>" title="<s:text name='tooltip.edit'/>" />
			</div>		
			</security:hasProtocolPrivilege>
		</div>
 		
		<h3 class="toggleable collapsed"><s:text name='patient.scheduleVisit.subtitle.display'/></h3>
		<div class="divPatientInfo">
			<div id="visitsTableContainer" class="dataTableContainer">
				<idt:jsontable name="_patientVisits" scope="request" decorator="gov.nih.nichd.ctdb.patient.tag.PatientVisitDecorator">
	                <idt:setProperty name="basic.msg.empty_list" value="There are no visits scheduled for the patient at this time."/>
					<idt:column property="visitDate" title='<%=rs.getValue("patient.visitdate.display",l)%>' decorator="gov.nih.nichd.ctdb.common.tag.YyyyMMddHHmmColumnDecorator"/>
					<idt:column property="intervalName" title='<%=rs.getValue("protocol.visitType.title.display",l)%>'/>
				</idt:jsontable>
			</div>  
		        			
			<security:hasProtocolPrivilege privilege="addeditschedulevisits">
			<div class="formrow_1">
				<input type="button" id="btnEditPatientVisits" value="<s:text name='button.Edit'/>" title="<s:text name='tooltip.edit'/>" />
			</div>
			</security:hasProtocolPrivilege>
		</div>

 		<h3 class="toggleable collapsed">
 		 	<s:text name='patient.viewdetails.attachments'/>
		</h3>
		<div class="divPatientInfo">
 			<div class="dataTableContainer" id="divPatientAttachments"> 
				<security:hasProtocolPrivilege privilege="viewaudittrails">
 				<ul>
					<li>
						<input type="button" id="btnViewAttachmentAudit"
							value="<s:text name='button.ViewAudit'/>" title="<s:text name='tooltip.viewAudit.attachment'/>" />
					</li>
				</ul> 
		 		</security:hasProtocolPrivilege>
		 		
         		<idt:jsontable name="_attachments" scope="request" decorator="gov.nih.nichd.ctdb.attachments.tag.AttachmentHomeDecorator">
         			<idt:setProperty name="basic.msg.empty_list" value="There are no attachments to display at this time."/>
					<idt:column nowrap="true" property="checkbox" title="" />
                	<idt:column property="patientAttachmentName" title="Name" />
                    <idt:column property="description" title="Description"/>
                    <idt:column property="category.name" title="Category"/>
                </idt:jsontable>
			</div>
			
			<security:hasProtocolPrivilege privilege="manageAttachments">
			<div class="formrow_1">
				<input type="button" id="btnEditAttachments" value="<s:text name='button.Edit'/>" title="<s:text name='tooltip.edit'/>" />
			</div>
			</security:hasProtocolPrivilege>
		</div>
		
		<h3 class="toggleable <s:if test="%{patientForm.sectionDisplay != 'allCompletedForms'}">collapsed</s:if>">
			<s:text name='patient.viewdetails.completedForms'/>
		</h3>
		<div class="dataTableContainer">
         	<idt:jsontable name="_completedForms" scope="request" decorator="gov.nih.nichd.ctdb.response.tag.DataCollectionSearchDecorator">
         		<idt:setProperty name="basic.msg.empty_list" value="There are no compelted forms avaible for patients with current protocol."/>
                <idt:column property="formName" title="eForm Name" />
                <idt:column property="formLastUpdatedDate" title="Completed Date and Time"/>
          	</idt:jsontable>
		</div>
	</div> 

</s:form>

<jsp:include page="/common/footer_struts2.jsp" />
</html>