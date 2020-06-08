<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol,gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%
	Protocol protocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	Locale l = request.getLocale();
	int subjectDisplayType = protocol.getPatientDisplayType();
	String SUBJECT_DISPLAY_LABEL = protocol.getPatientDisplayLabel();
	Boolean protocolclosed = (Boolean)session.getAttribute(CtdbConstants.PROTOCOL_CLOSED_SESSION_KEY);
%>

<body onLoad="setSelected('<%=request.getAttribute("searchFormType")%>')">

<%-- CHECK PRIVILEGES --%>
<security:check privileges="doublekeyresolution,dataentry,dataentryoversight,externaldataimport" />

<html>
	<s:set var="pageTitle" scope="request">
		<s:text name="repoonse.collectDataLandingHome.title" />
	</s:set>
	<jsp:include page="/common/header_struts2.jsp" />	
	<s:set var="disallowingPII" value="#systemPreferences.get('guid_with_non_pii')" />
	
	<script type="text/javascript">

	var searchSubmitted = "<s:property value='searchSubmitted' />";
	<%-- This function toggles between showing the pateint forms div and the non-Subject forms div --%>
	 function formsToggleVisibility() {
		var selected = $("#formType").val();

		$(".optionalDisplayContainer").hide();
		$("#" + selected).show();
		
		if (selected == "patientFormsData") {
			$("input[name='dataForm.searchFormType']").val("patientFormsData");
		}  else if (selected == "patientData") {
			$("input[name='dataForm.searchFormType']").val("patientData");	
		}
		$("#" + selected).show();
	}
	 
 	function resetSearch(resetButtonID) {
		var resetBtnId = resetButtonID.id;
		
		if (resetBtnId == "patientViewResetBtn") {
			$(':text, :password, :file', '#patientViewDataCollectionSearchFrom').val('');
			$(':input, select option', '#patientViewDataCollectionSearchFrom').removeAttr('checked').removeAttr('selected');
			$('select option:first', '#patientViewDataCollectionSearchFrom').attr('selected', true);
			$("form[name='pvDataForm']").submit();
			
		} else if (resetBtnId == "patientFormViewResetBtn") {
			$(':text, :password, :file', '#patientFormViewDataCollectionSearchFrom').val('');
			$(':input, select option','#patientFormViewDataCollectionSearchFrom').removeAttr('checked').removeAttr('selected');
			$('select option:first', '#patientFormViewDataCollectionSearchFrom').attr('selected', true);
			$("form[name='eformDataForm']").submit();
		}
	}
	
	function submitSearchTable(submitButtonID){
		var submitButtonID = submitButtonID.id;
		
		if(submitButtonID == "patientViewSearchBtn"){
			$("form[name='pvDataForm']").submit();
		} else if(submitButtonID == "patientSearchBtn"){
			$("form[name='eformDataForm']").submit();
		}
	}
	
	function setSelected(selected) {
		var formType = document.getElementById("formType");
		if (selected == "patientData") {
			formType.selectedIndex = "0";
		} else if (selected == "patientFormsData") {
			formType.selectedIndex = "1";
		} 
		
		formsToggleVisibility();
	}
		
</script>

<script type="text/javascript">

$(document).ready(function() {
	
	var basePath = "<s:property value="#webRoot"/>";
	var pvFormData = {
			pvVisitDate:$("#calId3").val(),
			pvGuid:$("#patIdpvGUID").val()
	}
	$("#pvDataList").idtTable({
		
		idtUrl: "<s:property value='#webRoot'/>/response/getDataCollectionPVList.action",
		dom: 'Bfrtip',
		select: "multi",
        idtData: {
            primaryKey: 'patId'
        },
		filterData: {
			dataForm: pvFormData
    	},   
        language: {
             "processing": "DataTables is currently busy",
             "emptyTable" : "There are no data collection records to display at this time."
         },

        columns: [
		        	
        	<s:if test="#disallowingPII == 1">
	         	<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
	        		{
	        			name: 'pvGuid',
		                title: '<%=rs.getValue("response.resolveHome.tableHeader.subjectGUID",l)%>',
		                parameter: 'pvGuid',
		                data: 'pvGuid'
		             },
        		<%}%>
        	
	        	
		        	{
		        		name: 'nihRecordNo',
		                title: '<%=rs.getValue("subject.table.subjectID",l)%>',
		                parameter: 'patientRecordNumber',
		                data: 'nihRecordNo'
		        	},

	        	
		        	{
		        		name: 'pvIntervalName',
		                title: '<%=rs.getValue("protocol.visitType.title.display",l)%>',
		                parameter: 'pvIntervalName',
		                data: 'pvIntervalName'
		        	},
	
		        	{
		        		name: 'pvVisitDate',
		                title: '<%=rs.getValue("scheduledvisitdate.display",l)%>',
		                parameter: 'patientViewVisitDate',
		                data: 'pvVisitDate',
		                "render": IdtActions.formatDate()
		        	}, 
	        </s:if>
	        <s:else>
				<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
					{
		        		name: 'mrn',
		                title: '<%=rs.getValue("patient.mrn.display",l)%>',
		                parameter: 'mrn',
		                data: 'mrn'
		        	},
				<%}%>
				<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
					{
		        		name: 'pvGuid',
		                title: '<%=rs.getValue("response.resolveHome.tableHeader.subjectGUID",l)%>',
		                parameter: 'pvGuid',
		                data: 'pvGuid'
		        	},
				<%}%>
				<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
					{
		        		name: 'nihRecordNo',
		                title: '<%=rs.getValue("subject.table.subjectID",l)%>',
		                parameter: 'patientRecordNumber',
		                data: 'nihRecordNo'
		        	},
				<%}%>
					{
		        		name: 'pvPatientName',
		                title: '<%=rs.getValue("patient.name.display",l)%>',
		                parameter: 'pvPatientName',
		                data: 'pvPatientName'
		        	},
		        	{
		        		name: 'pvIntervalName',
		                title: '<%=rs.getValue("protocol.visitType.title.display",l)%>',
		                parameter: 'pvIntervalName',
		                data: 'pvIntervalName'
		        	},
		        	{
		        		name: 'pvVisitDate',
		                title: '<%=rs.getValue("scheduledvisitdate.display",l)%>',
		                parameter: 'patientViewVisitDate',
		                data: 'pvVisitDate',
		                "render": IdtActions.formatDate()
		        	},
			 </s:else>
				
        ],
        
    	buttons: [
    		<security:hasProtocolPrivilege privilege="dataentry">
    		{
    			text: "<s:text name='repoonse.collectDataLandingHome.startDataCollection' />",
    			titleAttr: "<s:text name='tooltip.repoonse.collectDataLandingHome.startDataCollection' />",
    			className: "patStartDataCollectionBySubjectBtn",
    			enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
                action: function(e, dt, node, config) {
					var selectedSubject = $("#pvDataList").idtApi("getSelected");
					var url = basePath + '/response/dataCollection.action?action=formParams&mode=patient&patientVisitId='+ selectedSubject[0];
					redirectWithReferrer(url);
				}
    		},
    		{
    			text: "<s:text name='repoonse.collectDataLandingHome.dataEntrySummary' />",
    			titleAttr: "<s:text name='tooltip.repoonse.collectDataLandingHome.dataEntrySummary' />",
    			className: "dataEntrySummaryBySubjectBtn",
    			enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
                action: function(e, dt, node, config) {	
					var rowdata = $("#pvDataList").idtApi('getApiRow', '.selected').data();
					var patient = "";
					if("<%= SUBJECT_DISPLAY_LABEL %>" == "GUID") {
						patient = rowdata.pvGuid;
					}else {
						patient = rowdata.nihRecordNo;
					}
					var url = basePath + '/response/collectDataPreviousHome.action?searchFormType=patientPVDiv&bsFormId='+ patient;
					redirectWithReferrer(url);
				}
    		}
    		</security:hasProtocolPrivilege>
    		
    	],
    	initComplete:function(settings) {
    		var oTable = $("#pvDataList").idtApi("getTableApi");
    		var protocolclosed = <%=protocolclosed.booleanValue()%>;
    		
    		oTable.on('select', function(e, dt, type, indexes) {
            	if(protocolclosed){
            		oTable.buttons(['.patStartDataCollectionBySubjectBtn']).disable();
            	}
    		})
    		
    		oTable.on('deselect', function(e, dt, type, indexes) {
            	if(protocolclosed){
            		oTable.buttons(['.patStartDataCollectionBySubjectBtn']).disable();
            	}
    		})
    	}
        
	});
	
	var eformFormData = {
			formLastUpdatedDate: $("#calId3").val(),
	    	patientFormViewStatus: $("#inProtocolPat").val(),
	    	formName: $("#formName").val()
	}

	$("#dataFormList").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getDataCollectionDataFormList.action",
		dom: 'Bfrtip',
		select: "multi",
        idtData: {
            primaryKey: 'formId'
        },
        filterData: {
        	dataForm: eformFormData
        },
        columns: [
        	{
        		name: 'formName',
                title: '<%=rs.getValue("form.name.display",l)%>',
                parameter: 'formName',
                data: 'formName'
        	},
        	{
        		name: 'formStatusName',
                title: '<%=rs.getValue("form.forms.formInformation.status",l)%>',
                parameter: 'formStatusName',
                data: 'formStatusName'
        	},
        	{
        		name: 'formLastUpdatedDate',
                title: '<%=rs.getValue("form.public.search.date.display",l)%>',
                parameter: 'formLastUpdatedDate',
                data: 'formLastUpdatedDate',
                "render": IdtActions.formatDate()
        	}
		],
		
    	buttons: [
    		<security:hasProtocolPrivilege privilege="dataentry">
    		{
    			text: "<s:text name='repoonse.collectDataLandingHome.startDataCollection' />",
    			titleAttr: "<s:text name='tooltip.repoonse.collectDataLandingHome.startDataCollection' />",
   				className: "startDataCollectionByFormSubjectBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
						var selectedFormId = $("#dataFormList").idtApi("getSelected");
						var url = basePath+ '/response/dataCollection.action?action=formParams&mode=formPatient&formId='+ selectedFormId[0];
						redirectWithReferrer(url);
   				}
    		},
    		</security:hasProtocolPrivilege>
    		
    		{
    			text: "<s:text name='repoonse.collectDataLandingHome.dataEntrySummary' />",
    			titleAttr: "<s:text name='tooltip.repoonse.collectDataLandingHome.dataEntrySummary' />",
   				className: "dataEntrySummaryByFormSubjectBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
   						var selectedFormId = $("#dataFormList").idtApi("getSelected");
   						var url = basePath + '/response/collectDataPreviousHome.action?formId='+selectedFormId[0];
   						redirectWithReferrer(url);
   				}
    		},
    		
    		{
    			text: "<s:text name='button.View' />",
    			titleAttr: "<s:text name='tooltip.viewFormByFormSubjectBtn' />",
   				className: "viewFormByFormSubjectBtn",
                enableControl: {
                    count: 1,
                    invert:true
                },
                enabled: false,
   				action: function(e, dt, node, config) {
   					var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
   					var selectedFormId = $("#dataFormList").idtApi("getSelected");
   					var url = basePath+ '/form/viewFormDetail.action?source=popup&id='+ selectedFormId[0];
   					openPopup(url, "", WindowArgs+ "width=500,height=400,menubar=yes,status=yes,location=yes,toolbar=yes,scrollbars=yes,resizable=yes,");
   				}
    		}
    	],
    	initComplete:function(settings) {
    		var oTable = $("#dataFormList").idtApi("getTableApi");
    		oTable.on('select', function(e, dt, type, indexes) {
    			var protocolclosed = <%=protocolclosed.booleanValue()%>;
            	if(protocolclosed){
            		oTable.buttons(['.startDataCollectionByFormSubjectBtn']).disable();
            	}
    		})
    	}
	});
	});
	
	</script> 
	
	<div>
		<s:text name="repoonse.collectDataLandingHome.instruction" /><br><br>

		<label><s:text name="ibis.label.viewtext" /></label>
		<select id="formType" onChange="formsToggleVisibility()" 
				title="View drop-down for Subject, Form for subject or non-subject">
			<option value="patientData">
				<s:text name="repoonse.collectDataLandingHome.bySubject" /> (<%=SUBJECT_DISPLAY_LABEL%>)
			</option>
			<option value="patientFormsData">
				<s:text name="repoonse.collectDataLandingHome.byFormSubject" /> (<%=SUBJECT_DISPLAY_LABEL%>)
			</option>
		</select>
	</div>
	
	<!-- Patient Form -->
	<div id="patientFormsData" class="optionalDisplayContainer" style="display:none">
	
		<s:form method="post" id="patientFormViewDataCollectionSearchFrom" action="dataCollectingLandingSearch"  name="eformDataForm">
			<s:hidden name="id" />
			<s:hidden name="dataForm.searchFormType" value="patientFormsData" />
			
			<!--  this will switch to publicForms when user changes the dropdown -->
			<h3 class="toggleable collapsed" tabindex="0">
				<s:text name="ibis.label.search" />
			</h3>
			<div id="search_collectData" class="searchContainer" style="display: none;">
				<div class="formrow_2">
					<label for="formName">
						<s:text name="response.collect.label.formname" />
						<s:text name="ibis.label.data.formname" />
					</label>
					<s:textfield name="dataForm.formName" maxlength="50" id="formName" />
				</div>
				<div class="formrow_2">
					<label for="formLastUpdatedDate">
						<s:text name="form.public.search.date.display" />
						<s:text name="ibis.label.data.formlastupdated" />
					</label>
					<s:textfield name="dataForm.formLastUpdatedDate" cssClass="dateField" id="calId10" maxlength="50" />
				</div>
				<div class="formrow_2">
					<label for="inProtocolPat"><s:text name="form.status.display" /></label>
					<s:select name="dataForm.patientFormViewStatus" id="inProtocolPat" list="#{'0':'All', '1':'Active', '2':'In progress'}"/>
				</div>
				<div class="formrow_1">
					<div style="float: left;margin-right: 10px;margin-left: 600px">
						<%-- <s:submit id="patientSearchBtn" action="dataCollectingLandingSearch" key="button.Search" title="Click to search" /> --%>
						<input type="button" value="<s:text name='button.Search'/>" id="patientSearchBtn" title = "Click to search" 
							onclick="submitSearchTable(this)"/> 
					</div>
					<div style="float: left;">
						<input type="submit" id="patientFormViewResetBtn" value="Reset" title="Click to clear fields" 
							alt="Reset" onclick="resetSearch(this)" />
					</div>
				</div>
				<!-- form search table close -->
			</div>
			<h3><s:text name="repoonse.collectDataLandingHome.subtitle" /></h3>
			<p><s:text name="ibis.label.text.action" /></p>
			<div id="dataFormContainer" class="idtTableContainer brics">
				<table id="dataFormList" class="table table-striped table-bordered" width="100%"></table>
			</div>			
		</s:form>
		

	</div>
	

	
	<!-- Patient -->
	<div id="patientData" class="optionalDisplayContainer">
		<s:form  id="patientViewDataCollectionSearchFrom" action="dataCollectingLandingSearch" name="pvDataForm">
			<s:hidden name="dataForm.searchFormType" value="patientFormsData" />
			
			<!--  this will switch to nonSubject when user changes the dropdown -->
			<h3 class="toggleable collapsed" tabindex="0"><s:text name="ibis.label.search" /></h3>

			<div id="search_collectData_pv" class="searchContainer" style="display: none;">
				<s:if test="#disallowingPII == 1">
					<div class="formrow_2">
						<label for="pvVisitDate">
							<s:text name="scheduledvisitdate.display" />
							<s:text name="ibis.label.data.formname" />
						</label>
						<s:textfield name="dataForm.pvVisitDate" cssClass="dateField" maxlength="50" id="calId3" />
					</div>

						<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
							<div class="formrow_2">
								<label for="pvGuid">
									<s:text name="response.resolveHome.tableHeader.subjectGUID" />
								</label>
								<s:textfield name="dataForm.pvGuid" maxlength="50" id="patIdpvGUID" />
							</div>
							
						<%}%>
						<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
								<div class="formrow_2">
									<label for="nihRecordNo">
										<s:text name="subject.table.subjectID"/>									
									</label>
									<s:textfield name="dataForm.nihRecordNo" maxlength="50" id="patIdpvID" />
								</div>
						<%}%>
				</s:if>
				<s:else>
						<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
							<div class="formrow_2">
								<label for="mrn">
									<s:text name="patient.scheduleVisit.mrn.display"/>
								</label>
								<s:textfield name="dataForm.mrn" maxlength="50" id="patIdpvMRN" />
							</div>							
						<%}%>
						<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
							<div class="formrow_2">
								<label for="pvGuid">
									<s:text name="response.resolveHome.tableHeader.subjectGUID" />
								</label>
								<s:textfield name="dataForm.pvGuid" maxlength="50" id="patIdpvGUID" />
							</div>
							
						<%}%>
						<%if (subjectDisplayType  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
								<div class="formrow_2">
									<label for="nihRecordNo">
										<s:text name="subject.table.subjectID"/>
									</label>
									<s:textfield name="dataForm.nihRecordNo" maxlength="50" id="patIdpvID" />
								</div>
						<%}%>
					<div class="formrow_2">
						<label for="patientFirstName">
							<s:text name="patient.firstname.display" />
							<s:text name="ibis.label.data.formname" />
						</label>
						<s:textfield name="dataForm.patientFirstName" maxlength="50" id="patFirstName" tabindex="0" />
					</div>
					<div class="formrow_2">
						<label for="patientLastName">
							<s:text name="patient.lastname.display" />
							<s:text name="ibis.label.data.formname" />
						</label>
						<s:textfield name="dataForm.patientLastName" maxlength="50" id="patLastName" tabindex="0" />
					</div>
				</s:else>
				
				<div class="formrow_1">
					<div style="float: left;margin-left: 600px;margin-right: 10px">
						<%-- <s:submit id="patientViewSearchBtn" action="dataCollectingLandingSearch" key="button.Search" title="Click to search" /> --%>
						<input type="button" id="patientViewSearchBtn" value="<s:text name='button.Search' />" title="Click to search" 
							alt="Search" onclick="submitSearchTable(this)" />
					</div>
					<div style="float: left;">
						 <input type="button" id="patientViewResetBtn" value="<s:text name='button.Reset' />" title="Click to clear fields" 
						 	alt="Reset" onclick="resetSearch(this)"/> 
					</div>
				</div>
			</div>

			<h3><s:text name="repoonse.collectDataLandingHome.subtitle.upComingCollections" /></h3>

			<p><s:text name="ibis.label.text.action" /></p>
			
			<div id="pvDataContainer" class="idtTableContainer brics">
				<table id="pvDataList" class="table table-striped table-bordered" width="100%"></table>
			</div>
		</s:form>
</div>
			
	<jsp:include page="/common/footer_struts2.jsp" />
</html>