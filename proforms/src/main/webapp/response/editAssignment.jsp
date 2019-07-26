<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.ResultControl"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol,gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="java.util.*"%>
<%@ page import="gov.nih.nichd.ctdb.util.jsp.SortTitle"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="dataentryreassign"/>
<%
	Locale l = request.getLocale();
	int subjectDisplayType = ((Protocol)session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY)).getPatientDisplayType();
	int singleDoubleEntry = Integer.parseInt((String)request.getAttribute("singleDoubleEntry"));
	boolean entry2Started =  Boolean.parseBoolean((String)request.getAttribute("entry2Started"));
	int dEntryNumberException = -1;
	if (request.getAttribute("dEntryNumberException") != null) {
		dEntryNumberException = Integer.parseInt((String)request.getAttribute("dEntryNumberException"));
	}

%>

<html>
<s:set var="pageTitle" scope="request">
	<s:text name="response.editassignment.title"/>
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<script language="javascript">

function cancel() {
	var url = "<s:property value="#webRoot"/>/response/collectDataPreviousHome.action";
	redirectWithReferrer(url);
}

function initialize(singleDoubleEntry, entry2Started, dEntryNumberException) {
	var dataEntryNumberSelect = document.getElementById("dataEntryNumberSelect");
	if (singleDoubleEntry==2 && !entry2Started) {
		//can only change dataEntry1 since dataEntry2 has not even begun so disable the select
		dataEntryNumberSelect.disabled=true;
	}

	if (dEntryNumberException == 1) {
		dataEntryNumberSelect.selectedIndex="0";
		toggleDataEntryNumber();
	} else if (dEntryNumberException == 2) {
		dataEntryNumberSelect.selectedIndex="1";
		toggleDataEntryNumber();
	}
}

function toggleDataEntryNumber() {
	var dataEntryNumberSelect = document.getElementById("dataEntryNumberSelect");
	var selected = dataEntryNumberSelect.options[dataEntryNumberSelect.selectedIndex].value;
	if (selected == "1") {
		$("#dataEntryNumber").val("1");
		$('#entry1Reassign').attr("style","display:block");
		$('#entry2Reassign').attr("style","display:none");
 	} else if (selected == "2"){
		$("#dataEntryNumber").val("2");
		$('#entry1Reassign').attr("style","display:none");
		$('#entry2Reassign').attr("style","display:block");
	}
}

</script>

<body onLoad="initialize(<%= singleDoubleEntry %>,<%= entry2Started %>,<%= dEntryNumberException %>)">

<p><s:text name="response.editassignment.msg"/></p> <br>
<%
	if (singleDoubleEntry == 1) {
%>
    <s:form>
    	<s:hidden name="dataEntryNumber" id="dataEntryNumber" value="1"/>
	
		<div class="formrow_2">
			<label for="visitDate1">Data Entry</label>
				<span>
				<select id="dataEntryNumberSelect" disabled="true">
					<option value="1"> 1&nbsp;</option>
				</select>
				</span>
		</div>	
    
    <div class="formrow_2">
		<label for="visitDate1">Reassign to</label>
		<% if (!((Map)session.getAttribute("eaUserMap")).isEmpty()) { %>
				<s:select name="userId" list="#session.eaUserMap" listKey="key" listValue="value" />
		<% } else { %>
				<span>
					<font color="red"><s:text name="response.editAssignment.noMoreUsers.message" /></font>
				</span>
		<% } %>
	</div>  
	<div class="clearfix"></div>
		<br>
		<div class="clearfix"></div>
		<div class="dataTableContainer" id="blah" >
	    <idt:jsontable name="eaAformList"  scope="request" decorator="gov.nih.nichd.ctdb.response.tag.EditAssignmentDecorator">
	       <% if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
				<idt:column property="guid" title='<%=rs.getValue("response.resolveHome.tableHeader.subjectGUID",l)%>' />
			<% } else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {%>
				<idt:column property="nihRecordNo" title='<%=rs.getValue("subject.table.subjectID",l)%>' />
			<% } else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_SUBJECT) {%>
				<idt:column property="subjectNo" title='<%=rs.getValue("patient.label.SubjectNumber",l)%>' />
			<% } else if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_NAME) {%>
				<idt:column property="subjectName" title='<%=rs.getValue("patient.label.SubjectName",l)%>' />
			<% } %>
			<idt:column property="visitDate" title='<%=rs.getValue("visitdate.display",l)%>'  />
			<idt:column property="visitType" title='<%=rs.getValue("protocol.visitType.title.display",l)%>'  />
			<idt:column property="formName" title='<%=rs.getValue("form.name.display",l)%>' />
	      
	        <idt:column property="status1" title='<%=rs.getValue("response.collect.myCollections.dataEntry1Status",l)%>' />
	        <idt:column property="user1" title='<%=rs.getValue("response.collect.myCollections.dataEntry1",l)%>'/>
	    </idt:jsontable>
	</div>
	<br>
	
	<div class="formrow_1">
	<% if ( ((Map)session.getAttribute("eaUserMap")).size() > 0 ) { %>
		<input type="button" value="Cancel" title="Cancel" alt="Cancel" onClick="cancel()"/>
		<s:submit action="saveAssignment" key="button.Save" title="Save" alt="Save"  />
	<% } else { %>
		<input type="button" value="Cancel" title="Cancel" alt="Cancel" onClick="cancel()"/>
	<% } %>
	</div>
	
	</s:form>
<%
	} else {
%>
		<div id="dataEntry" style="display:block">
		    <s:form>
    		<s:hidden name="dataEntryNumber" id="dataEntryNumber" value="1"/>
    		
			<br>
			<div class="formrow_2">
				<label for="visitDate1">Data Entry</label>
				<span>
					<select id="dataEntryNumberSelect" onChange="toggleDataEntryNumber()">
						<option value="1"> 1&nbsp;</option>
						<option value="2"> 2&nbsp;</option>
					</select>
				</span>
			</div>	
		    
		    <div class="formrow_2" id="entry1Reassign" style="display:block">
				<label for="visitDate1">Reassign to</label>
				<% if ( ((Map)session.getAttribute("eaUserMap")).size() > 0 ) { %>
						<s:select name="userId" list="#session.eaUserMap" listKey="key" listValue="value" />
				<% } else { %>
						<span>
							<font color="red"><s:text name="response.editAssignment.noMoreUsers.message" /></font>
						</span>
				<% } %>
			</div>
			
			<div class="formrow_2" id="entry2Reassign"  style="display: none" >
				<label for="visitDate1">Reassign to</label>
				<% if ( ((Map)session.getAttribute("eaUser2Map")).size() > 0 ) { %>
						<s:select name="user2Id" list="#session.eaUser2Map" listKey="key" listValue="value" />
				<% } else { %>
						<span>
							<font color="red"><s:text name="response.editAssignment.noMoreUsers.message" /></font>
						</span>
				<% } %>
		
			</div>
			
			<div class="clearfix"></div>
			<div class="dataTableContainer" id="blah" >
		    <display:table name="eaAformList"  scope="request" decorator="gov.nih.nichd.ctdb.response.tag.EditAssignmentDecorator">
	       <%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
				<display:column property="guid" title='<%=rs.getValue("response.resolveHome.tableHeader.subjectGUID",l)%>' />
			<%}%>
			<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
				<display:column property="nihRecordNo" title='<%=rs.getValue("subject.table.subjectID",l)%>' />
			<%}%>
			<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_SUBJECT) {%>
				<display:column property="subjectNo" title='<%=rs.getValue("patient.label.SubjectNumber",l)%>' />
			<%}%>
			<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_NAME) {%>
				<display:column property="subjectName" title='<%=rs.getValue("patient.label.SubjectName",l)%>' />
			<%}%>
			<display:column property="visitDate" title='<%=rs.getValue("visitdate.display",l)%>'  />
			<display:column property="visitType" title='<%=rs.getValue("protocol.visitType.title.display",l)%>'  />
			<display:column property="formName" title='<%=rs.getValue("form.name.display",l)%>' />
	      
	        <display:column property="status1" title='<%=rs.getValue("response.collect.myCollections.dataEntry1Status",l)%>' />
	        <display:column property="user1" title='<%=rs.getValue("response.collect.myCollections.dataEntry1",l)%>'/>
			
	    </display:table>
		</div>
			
			<br>
			<div class="formrow_1">
			
			<% if (((Map)session.getAttribute("eaUserMap")).size() > 0 ) { %>
				<input type="button" value="Cancel" title="Cancel" alt="Cancel" onClick="cancel()"/>
				<s:submit action="saveAssignment" key="button.Save" title="Save" alt="Save"  />
			<% } else { %>
				<input type="button" value="Cancel" title="Cancel" alt="Cancel" onClick="cancel()"/>
			<% } %>
			</div>

			</s:form>
		</div>
<% 	} %>
</body>

<jsp:include page="/common/footer_struts2.jsp" />
</html>