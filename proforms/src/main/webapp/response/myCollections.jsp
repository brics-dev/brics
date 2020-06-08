<jsp:include page="/common/doctype.jsp" />
<%@ page
	import="gov.nih.nichd.ctdb.protocol.domain.Protocol,gov.nih.nichd.ctdb.common.CtdbConstants,java.util.List"%>

<%@ page import="gov.nih.nichd.ctdb.response.domain.AdministeredForm"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ page buffer="100kb"%>

<%
	Locale l = request.getLocale();
	Protocol protocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	int subjectDisplayType = protocol.getPatientDisplayType();
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
	String username = user.getUsername();
	Boolean protocolclosed = (Boolean)session.getAttribute(CtdbConstants.PROTOCOL_CLOSED_SESSION_KEY);
%>

<%-- CHECK PRIVILEGES --%>
<security:check
	privileges="dataentry,dataentryoversight,doublekeyresolution,unadministeraform,addeditauditorcomments" />
<html>
<s:set var="pageTitle" scope="request">
	<s:text name="response.collect.myCollections.title.display" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />
<style>
 	#patDataCollectionProgressTable_wrapper {
 		overflow: visible;
 	}	
</style>

<s:set var="disallowingPII"
	value="#systemPreferences.get('guid_with_non_pii')" />

<!-- subject action buttons  -->
<script type="text/javascript">
	var searchSubmitted = "<s:property value='searchSubmitted' />";
	
	$(document).ready(function() {

		var basePath = '<s:property value="#webRoot"/>';

		if ("ontouchstart" in document.documentElement) {
			$(function() {
				$("formType").selectbox();
			});
		}
					
	});
</script>

<!-- non subject action buttons  -->
<script type="text/javascript">
	$(document).ready(function() {
				
		$('#intervalSelectedId').append($('<option>', {
		    value: 'other',
		    text: 'Other'
		}));
		
		
		
				var basePath = "<s:property value="#webRoot"/>";

				$("#patDataCollectionProgressTable").idtTable({		
			            idtUrl: "<s:property value='#webRoot'/>/response/getMyCollection.action",
			            idtData: {
			              primaryKey: 'adminFormId'
			            },
			            
			          serverSide: true,
			          processing: true,
			          select: "multi",
			          pageLength: 15,
			          dom: "Bfrtip",
			          "autoWidth": false,
			          selectParams: {
							selectAllEnabled: true,
							selectNoneEnabled: true,
							selectFilteredEnabled: true,
					  },
			          filterData: {
						visitDatePreColl: $("#visitDateId").val(),
						intervalName: $("#intervalSelectedId").val(),
						formName: $("#formNameId").val(),
						assignedToUserOne: $("#assignedToUserOneId").val(),
						dataEntryStatus: $("#inProtocol").val()
			          },
			           columns: [
		            	<s:if test="#disallowingPII == 1">
							<%
								if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {
							%>
						          {
							          name: 'guid',
							          title: '<%=rs.getValue("response.resolveHome.tableHeader.subjectGUID", l)%>',
							          data: 'guid',
							          parameter: 'guid',
							          width: '12%'
						          },
							<%
								}
							%>						          
							    {
							      name: 'subjectid',
							      title: '<%=rs.getValue("subject.table.subjectID", l)%>',
							      parameter: 'subjectId',
							      data: 'subjectid',
							      width: '13%'
							    },    
								{
					              name: 'sVisitDate',
					              title: '<%=rs.getValue("scheduledvisitdate.display", l)%>',
					              parameter: 'scheduledVisitDate',
					              data: 'sVisitDate',
					              width: '9%'
				              	}, 
								{
							          name: 'pVisitDate',
							          title: '<%=rs.getValue("visitdate.display", l)%>',
							          parameter: 'date',
							          data: 'pVisitDate',
							          width: '9%'
						          },
						          {
							          name: 'intervalname',
							          title: '<%=rs.getValue("protocol.visitType.title.display", l)%>',
							          parameter: 'timePointDec',
							          data: 'intervalname',
							          width: '7%'
						          },
							      {
							          name: 'formName',
							          title: '<%=rs.getValue("form.name.display", l)%>',
							          parameter: 'formNameForAdminForm',
							          data: 'formName'
							      },
							      {
							          name: 'coll_status',
							          title: '<%=rs.getValue("response.collect.myCollections.dataEntry1Status", l)%>',
							          parameter: 'status1',
							          data: 'coll_status',
							          width: '7%'
							      },
							      {
							          name: 'firstname',
							          title: '<%=rs.getValue("response.collect.myCollections.user", l)%>',
							          parameter: 'dataEntry1',
							          data: 'firstname',
							          width: '12%'
							      },
							      {
							          name: 'finallockdate',
							          title: '<%=rs.getValue("response.collect.myCollections.finalLockDate", l)%>',
							          parameter: 'finalLockDate',
							          data: 'finallockdate',
							          width: "9%"
							      },							      
							      {
							          name: 'username',
							          title: '',
							          parameter: 'userName1',
							          data: 'username',
							          visible: false
							      },
							      {
							          name: 'status2',
							          title: '',
							          parameter: 'status2',
							          data: 'status2',
							          visible: false
							      },
						</s:if>
						<s:else>

							<%
								if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {
							%>	
							      {
							          name: 'guid',
							          title: '<%=rs.getValue("response.resolveHome.tableHeader.subjectGUID", l)%>',
							          parameter: 'guid',
							          data: 'guid'
							      },
							<%
								}
							%>
							<%
								if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
							%>
							      {
							          name: 'subjectid',
							          title: '<%=rs.getValue("subject.table.subjectID", l)%>',
							          parameter: 'subjectId',
							          data: 'subjectid'
							      },
							<%
								}
							%>
							<%
								if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {
							%>	
							      {
							          name: 'mrn',
							          title: '<%=rs.getValue("patient.mrn.display", l)%>',
							          parameter: 'mrn',
							          data: 'mrn'
							      },
							<%
								}
							%>	
							 	{
					              name: 'sVisitDate',
					              title: '<%=rs.getValue("scheduledvisitdate.display", l)%>',
					              parameter: 'scheduledVisitDate',
					              data: 'sVisitDate'
				              	  }, 
								{
							          name: 'pVisitDate',
							          title: '<%=rs.getValue("visitdate.display", l)%>',
							          parameter: 'date',
							          data: 'pVisitDate'
							      },
							      {
							          name: 'intervalname',
							          title: '<%=rs.getValue("protocol.visitType.title.display", l)%>',
							          parameter: 'timePointDec',
							          data: 'intervalname'
							      },
							      {
							          name: 'formName',
							          title: '<%=rs.getValue("form.name.display", l)%>',
							          parameter: 'formNameForAdminForm',
							          data: 'formName'
							      },
							      {
							          name: 'shortName',
							          title: 'Short Name',
							          parameter: 'shortName',
							          data: 'shortName'
							      },
							      {
							          name: 'coll_status',
							          title: '<%=rs.getValue("response.collect.myCollections.dataEntry1Status", l)%>',
							          parameter: 'status1',
							          data: 'coll_status'
							      },
							      {
							          name: 'firstname',
							          title: '<%=rs.getValue("response.collect.myCollections.user", l)%>',
							          parameter: 'dataEntry1',
							          data: 'firstname'
							      },
							      {
							          name: 'finallockdate',
							          title: '<%=rs.getValue("response.collect.myCollections.finalLockDate", l)%>',
							          parameter: 'finalLockDate',
							          data: 'finallockdate'
							      },
							      {
							          name: 'username',
							          title: '',
							          parameter: 'userName1',
							          data: 'username',
							          visible: false
							      },
							      {
							          name: 'status2',
							          title: '',
							          parameter: 'status2',
							          data: 'status2',
							          visible: false
							      },
						</s:else>		      
	     
			          	],
			          	buttons: [
		           			{
		           				text: "<s:text name="response.collect.myCollections.viewEntry1" />",
		           				className: "patFViewBtn1",
		                        enableControl: {
	                             count: 1,
	                             invert:true
	                         	},
	                         	enabled: false,
		           				action: function(e, dt, node, config) {
	           						var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
	           						var selected_Form_Ids = $("#patDataCollectionProgressTable").idtApi("getSelected");
	           						var url = basePath + '/response/viewForm.action?source=response_home&userEntryFlag=1&id='+selected_Form_Ids;
	           						openPopup(url, "", WindowArgs+ "width=500,height=400,menubar=yes,status=yes,location=yes,toolbar=yes,scrollbars=yes,resizable=yes");

		           				}
		           			},
		           			<security:hasProtocolPrivilege privilege="editanswer">
		           			<s:set var="hasEditAnswer" value="true" />
		           			{
		           				text: "<s:text name='button.Edit1'/>",
		           				className: "patFEditBtn1",
		                        enableControl: {
	                             count: 1,
	                             invert:true
	                         	},
	                         	enabled: false,
		           				action: function(e, dt, node, config) {
	           						var selected_Form_Ids = $("#patDataCollectionProgressTable").idtApi("getSelected");
	           						var url = basePath+ '/response/dataCollection.action?action=editForm&mode=formPatient&aformId='+selected_Form_Ids+'&editUser=1';
	           						redirectWithReferrer(url);
	           	

		           				}
		           			},
		           			</security:hasProtocolPrivilege>
		           			<s:if test="%{#hasEditAnswer != true}">
		           			<!--  only show the buttons below if user doesn't have 'editanswer' privilege  -->
		           			<security:hasProtocolPrivilege privilege="editdataentry">
		           			{
		           				text: "<s:text name='button.Edit'/>",
		           				className: "patFEditBtn",
		                        enableControl: {
	                             count: 1,
	                             invert:true
	                         	},
	                         	enabled: false,
		           				action: function(e, dt, node, config) {
	           						var selected_Form_Ids = $("#patDataCollectionProgressTable").idtApi("getSelected");
	           						var url = basePath+ '/response/dataCollection.action?action=editForm&mode=formPatient&aformId='+selected_Form_Ids;
	           						redirectWithReferrer(url);

		           				}
		           			},
		           			</security:hasProtocolPrivilege>
		           			</s:if>
		           			<security:hasProtocolPrivilege privilege="viewdataentryaudittrail">
		           			{
		           				text: "<s:text name='button.ViewAudit'/>",
		           				className: "patFViewAuditBtn",
		                        enableControl: {
	                             count: 1,
	                             invert:true
	                         	},
	                         	enabled: false,
		           				action: function(e, dt, node, config) {
		           					var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
	           						var selected_Form_Ids = $("#patDataCollectionProgressTable").idtApi("getSelected");
	           						var url = basePath+ '/response/viewEditedAnswer.action?id='+selected_Form_Ids;
	           						openPopup(url, "", WindowArgs+ "width=500,height=400,menubar=yes,status=yes,location=yes,toolbar=yes,scrollbars=yes,resizable=yes");
		           				}
		           			},
		           			</security:hasProtocolPrivilege>
		           			<security:hasProtocolPrivilege privilege="addeditauditorcomments">
		           			<!-- hasEditAnswer-->
		           			{
		           				text: "<s:text name='button.AuditorComments'/>",
		           				className: "patFAuditBtn1",
		                        enableControl: {
	                             count: 1,
	                             invert:true
	                         	},
	                         	enabled: false,
		           				action: function(e, dt, node, config) {
	           						var selected_Form_Ids = $("#patDataCollectionProgressTable").idtApi("getSelected");
	           						var url = basePath+ '/response/dataCollection.action?action=auditComments&mode=formPatient&aformId='+selected_Form_Ids+'&audit=true';
	           						redirectWithReferrer(url);
		           				}
		           			},
		           			</security:hasProtocolPrivilege>
		           			<security:hasProtocolPrivilege privilege="dataentryreassign">
		           			{
		           				text: "<s:text name='response.collect.myCollections.reassign'/>",
		           				className: "patFReassignBtn",
		           				enabled: false,
		           				action: function(e, dt, node, config) {
		    						var options = $("#patDataCollectionProgressTable").idtApi("getOptions");
		    						var selectedRowsData = options.rowsData;
		    						
		    						var reassignableAforms = new Array();
		    						var nonreassignableAforms = new Array();
		    						
		    						var rowsLen = selectedRowsData.length;
		    						for (var i = 0; i < rowsLen; i++) {
		    							var row = selectedRowsData[i];
		    							
		    							var aformId = row.DT_RowId;
		    							var flDate = row.finallockdate;
		    						<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
		    							var subj = row.guid;
		    						<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {%>	
		    							subj = row.subjectid;
		    						<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
		    							subj = row.mrn;
		    						<%}%>
		    							var vDate = row.pVisitDate;
		    							var vType = row.intervalname;
		    							var fName = row.formName;
		    							var ent1 = row.firstname;
		    							var stat1 = row.coll_status;
		    							var ent2 = row.username;
		    							var stat2 = row.status2;
		    							var aform = aformId + "::" + subj + "::" + vDate + "::" + vType + "::" + fName + "::" + ent1 + "::" + stat1 + "::" + ent2 + "::" + stat2;
		    							if (flDate == null || flDate == "") {
		    								reassignableAforms.push(aform);
		    							} else {
		    								nonreassignableAforms.push(aform);
		    							}
		    						}
		    						
<%--		    						
		    						selectedRowsData.map(function(row) {
		    							var aformId = row.DT_RowId;
		    							var flDate = row.finallockdate;
		    						<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
		    							var subj = row.guid;
		    						<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {%>	
		    							subj = row.subjectid;
		    						<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
		    							subj = row.mrn;
		    						<%}%>
		    							var vDate = row.pVisitDate;
		    							var vType = row.intervalname;
		    							var fName = row.formName;
		    							var ent1 = row.firstname;
		    							var stat1 = row.coll_status;
		    							var ent2 = row.username;
		    							var stat2 = row.status2;
		    							var aform = aformId + "::"
		    							+ subj + "::" + vDate
		    							+ "::" + vType + "::"
		    							+ fName + "::" + ent1
		    							+ "::" + stat1 + "::"
		    							+ ent2 + "::" + stat2;
		    							if (flDate == null
		    									|| flDate == "") {
		    								reassignableAforms
		    										.push(aform);
		    							} else {
		    								nonreassignableAforms
		    										.push(aform);
		    							}
		    							
		    							return aform;
		    						});
		    						
--%>		    						
		    						
		    						
		    						

		    						if (reassignableAforms.length == 0) {
		    							var cantReassignString = "";
		    							for (var k = 0; k < nonreassignableAforms.length; k++) {
		    								var nonreassignableAform = nonreassignableAforms[k];
		    								var splits = nonreassignableAform
		    										.split("::");
		    								var splitsString = "<li><b>Subject: </b>"
		    										+ splits[1]
		    										+ ", <b>Visit Date: </b>"
		    										+ splits[2]
		    										+ ", <b>Visit Type: </b>"
		    										+ splits[3]
		    										+ ", <b>Form Name: </b>"
		    										+ splits[4]
		    										+ "</li>";
		    								cantReassignString = cantReassignString
		    										+ splitsString;
		    							}

		    							$.ibisMessaging(
		    											"dialog",
		    											"info",
		    											'<span style="text-align: left; padding:5px"><p>The following collection(s) cannot be reassigned due to locked status: </p> <br\><span style="font-weight:normal"><ul>'
		    													+ cantReassignString
		    													+ '</ul></span> </span>',
		    											{
		    												width : "600px"
		    											});

		    						} else {
		    							var reassignableAformsJSON = JSON.stringify(reassignableAforms);
		    							var url = basePath + '/response/editAssignment.action?action=add_form';

		    							if (nonreassignableAforms.length > 0) {
		    								var reassignString = "";
		    								for (var k = 0; k < reassignableAforms.length; k++) {
		    									var reassignableAform = reassignableAforms[k];
		    									var splits = reassignableAform
		    											.split("::");
		    									var splitsString = "<li><b>Subject: </b>"
		    											+ splits[1]
		    											+ ", <b>Visit Date: </b>"
		    											+ splits[2]
		    											+ ", <b>Visit Type: </b>"
		    											+ splits[3]
		    											+ ", <b>Form Name: </b>"
		    											+ splits[4]
		    											+ "</li>";
		    									reassignString = reassignString + splitsString;
		    								}

		    								var cantReassignString = "";
		    								for (var k = 0; k < nonreassignableAforms.length; k++) {
		    									var nonreassignableAform = nonreassignableAforms[k];
		    									var splits = nonreassignableAform
		    											.split("::");
		    									var splitsString = "<li><b>Subject: </b>"
		    											+ splits[1]
		    											+ ", <b>Visit Date: </b>"
		    											+ splits[2]
		    											+ ", <b>Visit Type: </b>"
		    											+ splits[3]
		    											+ ", <b>Form Name: </b>"
		    											+ splits[4]
		    											+ "</li>";
		    									cantReassignString = cantReassignString + splitsString;
		    								}

		    								var dlgId = $.ibisMessaging("dialog","info",
		    									'<span style="text-align: left; padding:5px"><p>The following collection(s) can be reassigned:</p><br\><span style="font-weight:normal"><ul>'
		    									+ reassignString
		    									+ '</ul></span> <br\><p>The following collection(s) cannot be reassigned due to locked status: </p><br\><span style="font-weight:normal"><ul>'
		    									+ cantReassignString
		    									+ '</ul></span> <br\><p>Click Yes to proceed with reassignment for unlocked forms. Click No to cancel.</p></span>',
		    									{
		    										buttons : [
		    											{
		    												text : "Yes",
		    												click : function() {
		    													$('#reassignableAformsJSON').val(reassignableAformsJSON);
		    													var form = document.getElementById("bulkReassignForm");
		    													form.action = url;
		    													form.submit();
		    												}
		    											},
		    											{
		    												text : "No",
		    												click : function() {
		    													$.ibisMessaging("close", {
    																id : dlgId
    															});
		    												}
		    											} 
		    										],
		    										width : "900px"
		    								});
		    							} else {
		    								$('#reassignableAformsJSON').val(reassignableAformsJSON);
		    								var form = document.getElementById("bulkReassignForm");
		    								form.action = url;
		    								form.submit();
		    							}
		    						}

		           				}
		           			},
		           			</security:hasProtocolPrivilege> 
		           			<security:hasProtocolPrivilege privilege="editdataentry">
		           			{
		           				text: "<s:text name='response.collect.myCollections.deleteEntry1'/>",
		           				className: "patFUnadministerBtn1",
	                         	enabled: false,
		           				action: function(e, dt, node, config) {
		   							var selected_Form_Ids = $("#patDataCollectionProgressTable").idtApi("getSelected");
		  							var options = $("#patDataCollectionProgressTable").idtApi("getOptions");
		 							var selectedRowsData = options.rowsData;
		 						 	if(<%=user.isSysAdmin()%>){
			 							$.ibisMessaging(
			 								"dialog", 
			 								"info", 
			 								'<s:text name="response.collect.myCollections.alert.removeForm.dde1"/>', {
			 								buttons: {
			 									"OK": function() {
			 										var url = basePath + '/response/deleteDataEntry.action';
			 										
			 										$(this).dialog("close");
			 										return $.ajax({
			 											type: "POST",
			 											url: url,
			 											data: {dataentryflag: 1, id: selected_Form_Ids.join()},
			 											success: function(response){
			 												if (window.navigator.userAgent.indexOf('MSIE')>0){
			 													$("html").html(response);
			 												} else {
			 													document.open();
			 													document.write(response);
			 													document.close();
			 												}
			 												return "success";
			 											}									
			 										});
			 									},
			 									"Cancel": function() {
			 										$(this).dialog("close");
			 									}
			 								}
			 							});
		 						 }else{
		 							var username1 = selectedRowsData.username;
		 							var status1 = selectedRowsData.coll_status;
		 							var subjectIds = new Array();
		 							var disallowingPII = '<s:property value="#disallowingPII" />';
		 							var approvedFormIds = [];
		 							var approvedCollections = [];
		 							var rejectedCollections = [];
		     						
									var numSelectedRows = selectedRowsData.length;
		     						for (var i = 0; i < numSelectedRows; i++) {
		     							var row = selectedRowsData[i];
		     							var selectedFormId = row.DT_RowId;
		     							var username1 = row.username;
		     							var status1 = row.coll_status;
		     							var visitDate = row.pVisitDate;
		     							var formName = row.formName;
		     							var visitType = row.intervalname;
		     							var loggedInUsername = '<%= username %>';
		     							var subjId = "";
		     							if (disallowingPII == 1) {
		     								subjId = row.guid;
		     							} else {
		     								<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
		     									subjId = row.guid;
		     								<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {%>
		     									subjId = row.subjectid;
		     		    					<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
		     		    						subjId = row.mrn;
		     	    						<%}%>
		     							}   							
		 								if(loggedInUsername != username1 || status1 == "Locked") {
		 									var formNameList = formName.substring(formName.indexOf(">")+1,formName.length);
		 									formNameList = formNameList.substring(0,formNameList.indexOf("<"));
		 									var rejectedCollection = "<b>Subject:</b> " + subjId + "     <b>Visit Date:</b> " + visitDate + "     <b>Visit Type:</b> " + visitType + "     <b>Form Name:</b> " + formNameList;
		 									rejectedCollections.push(rejectedCollection);
		 								}else {
		 									var formNameList = formName.substring(formName.indexOf(">")+1,formName.length);
		 									formNameList = formNameList.substring(0,formNameList.indexOf("<"));
		 									var approvedCollection = "<b>Subject:</b> " + subjId + "     <b>Visit Date:</b> " + visitDate + "     <b>Visit Type:</b> " + visitType + "     <b>Form Name:</b> " + formNameList;
		 									approvedCollections.push(approvedCollection);
		 									approvedFormIds.push(selectedFormId);
		 								}
		     						}

		 							var infoText = "";
		 							if(approvedCollections.length > 0) {
		 								var dialogWidth;
		 								if(rejectedCollections.length == 0) {
		 									infoText = '<s:text name="response.collect.myCollections.alert.removeForm.dde1"/>';
		 									dialogWidth = "300px";
		 									
		 								}else if(rejectedCollections.length > 0) {
		 									var rejectedCollectionsString = "";
		 									var approvedCollectionsString = "";
		 									for(var k=0;k<rejectedCollections.length;k++) {
		 										rejectedCollectionsString += "<li>" + rejectedCollections[k] +"</li>";
		 									}
		 									for(var k=0;k<approvedCollections.length;k++) {
		 										approvedCollectionsString += "<li>" + approvedCollections[k] +"</li>";
		 									}
		 									infoText = '<span style="text-align: left; padding:5px"><p>The following Administered Form(s) will be deleted:</p><br\><span style="font-weight:normal"><ul>' + approvedCollectionsString + '</ul></span> <br\><p>The following Administered Form(s) can not be deleted because they are either in Locked status or you are not the assigned user for this Administered Form: </p><br\><span style="font-weight:normal"><ul>' + rejectedCollectionsString + '</ul></span> <br\><p>Do you want to proceed?</p></span>';
		 									dialogWidth = "1200px";
		 								}
		 								$.ibisMessaging(
		 										"dialog", 
		 										"info", 
		 										infoText, {
		 										buttons: {
		 											"OK": function() {
		 												var url = basePath+ '/response/deleteDataEntry.action?dataentryflag=1&id='+approvedFormIds ;
		 												redirectWithReferrer(url);
		 												$(this).dialog("close");
		 											},
		 											"Cancel": function() {
		 												$(this).dialog("close");
		 											}
		 										},
		 										width: dialogWidth
		 									});
		 							}else {
		 								infoText = '<s:text name="response.collect.myCollections.alert.removeForm.noPermission"/>';
		 								
		 								$.ibisMessaging(
		 										"dialog", 
		 										"info", 
		 										infoText, {
		 										buttons: {
		 											"OK": function() {
		 												$(this).dialog("close");
		 											}
		 										}
		 									});
		 							}
		 						 } //end if not sys admin
		           				}
		           			},
		           			</security:hasProtocolPrivilege>
		           			<security:hasProtocolPrivilege privilege="reporting">
		           			{
		           				text: "<s:text name='response.collect.myCollections.exportCollection'/>",
		           				className: "patFViewAuditBtn",
	                         	enabled: false,
		           				action: function(e, dt, node, config) {
		           					var selectedFormIds = $("#patDataCollectionProgressTable").idtApi("getSelected");
		           					var urlDownload = basePath+ '/response/dataCollectionExport.action?aformIds='+selectedFormIds;
		           					var urlValidation = basePath+ '/response/dataCollectionExportValidation.action?aformIds='+selectedFormIds;
		           					$.ajax({
		           						type: "GET",
		           						url: urlValidation,
		           						success: function(response) {
		           							if (response.status == "ok") {
		           								redirectWithReferrer(urlDownload);
		           							}
		           							else {
		           							 $.ibisMessaging("dialog","warning",response.message);
		           							}
		           						},
		           						error: function(response) {
		           							$.ibisMessaging("dialog","warning","There was a problem processing your request.  Please try again or contact your system administrator");
		           						}
		           					});
		           				}
		           			},
		           			</security:hasProtocolPrivilege>		           			
			           		<%-- <security:hasProtocolPrivilege privilege="addeditauditorcomments">
			           			{
			           				text: "<s:text name='response.collect.myCollections.audit'/>",
			           				className: "patFAuditBtn",
		                         	enabled: false,
			           			}
			           		</security:hasProtocolPrivilege> --%>
			          	],
			          	initComplete: function(){		          		

			          		var oTable = $("#patDataCollectionProgressTable").idtApi("getTableApi");
			          		var protocolclosed = <%=protocolclosed.booleanValue()%>;
			          		
			        		oTable.on('select', function(e, dt, type, indexes) {
			                	if(protocolclosed){
			                		oTable.buttons(['.patFEditBtn1']).disable();
			                		oTable.buttons(['.patFReassignBtn']).disable();
			                		oTable.buttons(['.patFUnadministerBtn1']).disable();
			                	}
			        		})
			        		
			        		oTable.on('deselect', function(e, dt, type, indexes) {
			        			if(protocolclosed){
			                		oTable.buttons(['.patFEditBtn1']).disable();
			                		oTable.buttons(['.patFReassignBtn']).disable();
			                		oTable.buttons(['.patFUnadministerBtn1']).disable();
			                	}
			        		})
			        		
			          		$('#patDataCollectionProgressContainer').find('.idt_searchContainer').mouseover(function(){

		          				$('#patDataCollectionProgressContainer').find('.idt_selectColumnCheckbox').unbind().on('click', function(e) {
					  				$("#patientPreviousSearchBtn").click();
						  			
			          		 	}); //end click

			          		});//end mouseover
			          		
			          		$('#patDataCollectionProgressContainer').find(".idt_searchInput").unbind().on("keyup", _.debounce( function(e) {					  			
				  				$("#patientPreviousSearchBtn").click();
				  				
					  		}, 100, true));
			          		
			          		
			          		 $('#patDataCollectionProgressTable thead th').each(function () {
			                     var $td = $(this);
			                     var headerText = $td.text(); 
			                     var headerTitle=""; 
			              		if (headerText == "Scheduled Visit Date" )
			                 			 headerTitle = "Date for which visit type is scheduled for";
			             		else if ( headerText == "Collection Visit Date" )
			                   			headerTitle =  "Date for which Measure/procedure was performed";
			         
			                    $td.attr('title', headerTitle);
			                  });

			                  /* Apply the tooltips */
			                  $('#example thead th[title]').tooltip(
			                  {
			                     "container": 'body'
			                  }); 

			          	} //end intiComplete
			          
					}); //end datatable

	});	//end document ready

</script>

<script language="javascript">
	var searchData = {};
	function resetSearchTable(resetButtonID) {
		var resetBtnId = resetButtonID.id;
		var oTable = $("#patDataCollectionProgressTable").idtApi("getTableApi");
		var options = $("#patDataCollectionProgressTable").idtApi('getOptions');
		if (resetBtnId == "patientFormViewResetBtn") {
			$(':text, :password, :file', '#patientViewDataCollectionSearchFrom').val('');
			$(':input,select option', '#patientViewDataCollectionSearchFrom').removeAttr('checked').removeAttr('selected');
			$('select option:first', '#patientViewDataCollectionSearchFrom').attr('selected', true);
			$("#intervalSelectedId").prop("selectedIndex", 0);

		} else if (resetBtnId == "npFormViewResetBtn") {
			$(':text, :password, :file', '#npViewDataCollectionSearchFrom').val('');
			$("#npType").prop("selectedIndex", 0);
			$("#npStatus").prop("selectedIndex", 0);
		}
		
		options.filterData = {};
		oTable.clearPipeline().draw();
	}
	
	function submitSearchTable(submitButtonID) {
		var submitButtonID = submitButtonID.id;
		var oTable = $("#patDataCollectionProgressTable").idtApi("getTableApi");
		var options = $("#patDataCollectionProgressTable").idtApi('getOptions');
		if (submitButtonID == "patientPreviousSearchBtn") {
			var visitDateId = $("#visitDateId").val(),
				formNameId = $("#formNameId").val(),
				intervalSelectedId = $("#intervalSelectedId").val(),
				inProtocol = $("#inProtocol").val(),
				assignedToUserOneId = $("#assignedToUserOneId").val();
			searchData = {
				visitDatePreColl: visitDateId,
				intervalName: intervalSelectedId,
				formName: formNameId,
				assignedToUserOne: assignedToUserOneId,
				dataEntryStatus: inProtocol
			}
			
			options.filterData = searchData;
			oTable.clearPipeline().draw();

		}
	}
	


	function startTest() {
		var url = "<s:property value="#webRoot"/>/response/dataCollection.action?action=fetchFormPSR&patientId=17&formId=1016&visitTypeId=2";
		redirectWithReferrer(url);
	}

	function editTest() {
		var url = "<s:property value="#webRoot"/>/response/dataCollection.action?action=editFormPSR&aformId=450";
		redirectWithReferrer(url);
	}
</script>

<%-- Presentation Logic Only Below--%>
<div>
	<td><s:text
			name="response.collect.myCollections.title.instruction" /></td>
</div>




<div id="patientPVDiv" class="optionalDisplayContainer">
	<s:form action="collectDataPreviousHome" method="post"
		id="patientViewDataCollectionSearchFrom">
		<h3 class="toggleable collapsed" tabindex="0">
			<s:text name="ibis.label.search" />
		</h3>
		<div id="searchFieldsPatPreviousCollection" class="searchContainer">
			<s:if test="#disallowingPII == 1">
				<div class="formrow_2">
					<label for="visitDatePreColl"> <s:text
							name="visitdate.display" /> <s:text
							name="ibis.label.data.formname" />
					</label>
					<s:textfield cssClass="dateField" name="visitDatePreColl" size="20"
						maxlength="50" id="visitDateId" />
				</div>
				<div class="formrow_2">
					<label for="intervalName"> <s:text
							name="protocol.visitType.title.display" /> <s:text
							name="ibis.label.data.formname" />
					</label>
					<s:select name="intervalName" id="intervalSelectedId"
						list="#session.intervalOptions" listKey="value" listValue="value"
						headerKey="all" headerValue="All" />
				</div>
				<div class="formrow_2">
					<label for="formName"> <s:text
							name="response.collect.label.formname" />
					</label>
					<s:textfield name="formName" size="20" maxlength="50"
						id="formNameId" />
				</div>
				<div class="formrow_2">
					<label for="assignedToUserOne"> <s:text
							name="response.collect.myCollections.assignedToUser1" /> <s:text
							name="ibis.label.data.formname" />
					</label>
					<s:textfield name="assignedToUserOne" size="20" maxlength="50"
						id="assignedToUserOneId" />
				</div>

			</s:if>
			<s:else>



				<%
					if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {
				%>
				<div class="formrow_2">
					<label for="mrn"> <s:text
							name="patient.scheduleVisit.mrn.display" />
					</label>
					<s:textfield name="mrn" maxlength="50" size="20" />
				</div>
				<%
					}
				%>
				<%
					if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {
				%>
				<div class="formrow_2">
					<label for="guid"> <s:text
							name="response.resolveHome.tableHeader.subjectGUID" />
					</label>
					<s:textfield name="guid" maxlength="50" size="20" />
				</div>

				<%
					}
				%>
				<%
					if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
				%>
				<div class="formrow_2">
					<label for=subjectId> <s:text
							name="protocol.add.patientname.no.display" />
					</label>
					<s:textfield name="subjectId" maxlength="50" size="20" />
				</div>
				<%
					}
				%>


			</s:else>

			<div class="formrow_2">
				<label for="inProtocol"> <s:text
						name="app.label.lcase.status" />
				</label>
				<s:select name="dataEntryStatus" id="inProtocol"
					list="#{'0':getText('app.status.all'), 
							'1':getText('app.status.locked'), '2':getText('app.status.inprogress'),'3':getText('app.status.completed')}" />
			</div>

			<div class="formrow_1">
				<input type="button" id="patientFormViewResetBtn"
					value="<s:text name='button.Reset'/>" title="Click to clear fields"
					alt="Reset" onclick="resetSearchTable(this)" /> <input type="button"
					id="patientPreviousSearchBtn"
					value="<s:text name='button.Search' />" title="Click to search"
					alt="Search" onclick="submitSearchTable(this)" />

			</div>
		</div>
	</s:form>

	<tr>
		<h3>
			<s:text name="response.collect.title.display" />
		</h3>
		<p>
			<s:text name="response.collect.myCollections.instruction" />
		</p>

		<td>
		<div id="patDataCollectionProgressContainer" class="idtTableContainer brics">
				<table id="patDataCollectionProgressTable" class="table table-striped table-bordered" width="100%">
			</table>
		</div>
		</td>
	</tr>
	<!-- </table> -->


</div>




<s:form id="bulkReassignForm" method="post">
	<s:hidden name="reassignableAformsJSON" id="reassignableAformsJSON" />
</s:form>

<jsp:include page="/common/footer_struts2.jsp" />
</html>