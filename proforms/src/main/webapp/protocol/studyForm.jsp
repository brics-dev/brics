<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.common.ProtocolConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User" %>
<%@ page import="gov.nih.nichd.ctdb.security.dao.SecurityManagerDao" %>
<%@ page import="gov.nih.nichd.ctdb.site.domain.Site" %>
<%@ page import="gov.nih.nichd.ctdb.util.domain.Address" %>
<%@ page import="gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice" %>
<%@ page import="gov.nih.nichd.ctdb.audit.domain.Audit" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<style>
	label.protocolDlgInputlabel {
		font-size: 11px;
		width: 175px !important;
		margin-right: 13px !important;
	}
	label.protocolAddress2label {
		font-size: 11px;
		width: 157px !important;
		margin-right: 23px !important;
		padding-right: 8px;
	}		
	div.protocolDlgInput{
		width: auto !important;
	}	
	#allProcedureFields > div > div {
		width: auto !important;
    }
    #allProcedureFields > div > label {
		width: 175px !important;
    }
	
</style>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewprotocols" />
<html>

<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="study.info.title.display" />
</s:set>
<jsp:include page="/common/header_struts2.jsp" />
<s:set var="disallowingPII" value="#systemPreferences.get('guid_with_non_pii')" />
<s:text name="study.info.instruction.display" />

<%
	User user = (User) request.getSession().getAttribute("user");
	Locale l = request.getLocale();
%>

<% 
	if(request.getSession().getAttribute("subjectlabelconfiguration") != null && ((Boolean)request.getSession().getAttribute("subjectlabelconfiguration")).booleanValue()){
%>
		<script type="text/javascript">
			var hasSubjectlabelconfiguration = true;		
		</script>
<% 
	}else{
%>

		<script type="text/javascript">
			var hasSubjectlabelconfiguration = false;	
		</script>

<%

	}
%>

<script type="text/javascript">
	var webRoot = "<s:property value='#webRoot'/>";
	var canEdit = false;
	var bricsStudySites = new HashTable();
	var currentPatientDisplayType;
	
	function StudySite(bricsStudySiteId, siteName, address1, address2, city, stateId, stateName, countryId, countryName, zipCode, isPrimary) {
		this.bricsStudySiteId = bricsStudySiteId;
		this.siteName = siteName;
		this.address1 = address1;
		this.address2 = address2;
		this.city = city;
		this.stateId = stateId;
		this.stateName = stateName;
		this.countryId = countryId;
		this.countryName = countryName;
		this.zipCode = zipCode;
		this.isPrimary = isPrimary;
	}
	
	function setNavigationFlag(whereToGo) {
		$("#protocolNavigationFlag").val(whereToGo);
	}
	
	function resetSiteDrugDeviceFlags() {
		// resetting site & drug flag; because need to validate only study fields 
		$("#protocolSiteActionFlag").val("");
		$("#protocolDrugDeviceActionFlag").val("");
	}
	

 
	function iframeRef(frameRef) {
		return frameRef.contentWindow ? frameRef.contentWindow.document
				: frameRef.contentDocument
	}
	
	function popupIFrame(studyId) {
		var wsDomain = '<s:property  value="#systemPreferences.get('brics.modules.home.url')"/>';
		var url = wsDomain + "portal/study/viewStudyAction!lightbox.ajax?studyId="+studyId;
		$("#studyDetailsDiv").dialog({ 
			width : "1000px", 
			autoResize: true,
			position: {
				my: "center",
				at: "center",
				of: window
			}
		});
		
		$('#studyDetailsDiv').html('<i id="loading" class="fa fa-spinner fa-pulse fa-4x fa-fw" style="position: absolute;display: block;top: 50%;left: 50%;"></i><iframe id="studyDetailsPopup" src="' + url + '" width="960" height="400"/ seamless >');
		
		$('#studyDetailsPopup').load(function() {
		    $('#loading').hide();
		    $('iframe').show();
		});
	}
	
	
	
	function checkForBlankSubjectIds() {
		var url =  "<s:property value="#webRoot"/>/protocol/protocolBlankSubjectIds.action";
		var studyId = $("#studyId").val();
		$.ajax({
	  		type: "get",
	  		url: url,
	  		data : {
	  			"studyId" : studyId
	  		},
	  		success: function(response) {
	  			if(response == "true") {
	  				//show popup warning
	  				var infoText = "This protocol has subject(s) with blank Subject IDs. If 'Subject ID' is selected, tables can have blank fields for these subject(s). <br> Do you want to proceed with your selection?";
	  				$.ibisMessaging(
								"dialog", 
								"info", 
								infoText, {
								buttons: {
									"OK": function() {
										$(this).dialog("close");
									},
									"Cancel": function() {
										$("input:radio[name='protoForm.patientDisplayType'][value='" + currentPatientDisplayType + "']").prop("checked", true);  
										$(this).dialog("close");
									}
								},
								modal : true,
								width: "390px"
							});
	  			}
	  		},
	  		
	  		error : function(jqXHR, textStatus, errorThrown) {
	  			console.error("Error when getting study sites from the repository: " + errorThrown);
	  		}
		});
		
		
		
	}
			
	
	$(document).ready(function() {

        var checkEnableEsignature = $("input:radio[name='protoForm.enableEsignature']:checked").val();
        
        currentPatientDisplayType =  $("input:radio[name='protoForm.patientDisplayType']:checked").val();
 		
        if (checkEnableEsignature == "true") {
			$("#reasonForEsignatureDiv").hide();
		} 
		else if ($("#message_1").hasClass("ibisMessaging-error") ) {
			$("#reasonForEsignatureDiv").show();
		}  
 		else {
			$("input:radio[name='protoForm.enableEsignature']:not(:checked)").prop("disabled", true);
			$("input:radio[name='protoForm.enableEsignature']").addClass("disabled");

			$("#reasonForEsignatureDiv").show();
			$("#reasonForEsignature").prop("readonly", true); //"readonly"
			$("#reasonForEsignature").addClass("disabled");
			


	 		
		}

		// Show the site edit button if an error is displayed.
		if ( $("#protocolSiteActionFlag").val() == "edit_site_form" ) {
			$("#addSiteBtn").hide();
			$("#updateSiteBtn").show();
		}
		
		// Show the drug device edit button if an error is displayed.
		if ( $("#protocolDrugDeviceActionFlag").val() == "edit_DrugDevice_form" ) {
			$("#updateDrugDeviceBtn").show();
			$("#addDrugDeviceBtn").hide();
		}

		// Check if the user can edit the page. If not disable all fields 
		if ( !canEdit ) {
			if(hasSubjectlabelconfiguration){
				// Removed the submit buttons at the bottom of the page.
				//comment
				//$("#mainBtnSection").remove();
				//$("input, textarea, select").prop("disabled", true);
				$("#studyName").prop("readonly", true);
				$("#studyName").prop("class", "disabled");

				$("#studyNumber").prop("readonly", true);
				$("#studyNumber").prop("class", "disabled");

				$("#protocolSitesTable").addClass("disabled");

				$('input[name="protoForm.patientDisplayType"]').attr('disabled', false);
				$('input[name="protoForm.enableEsignature"]').attr('disabled', true);
				$('input[name="protoForm.useEbinder"]').attr('disabled', true);
				$('input[type="button"]').attr('disabled',false);
				//comment
				//$("input:button, input:reset, input:submit").remove();
				$("div.dataTables_filter input:text:disabled").prop("disabled", false);
			}else{
				// Removed the submit buttons at the bottom of the page.
				$("#mainBtnSection").remove();
			
				$("input, textarea, select").prop("disabled", true);
				$("input:button, input:reset, input:submit").remove();
				$("div.dataTables_filter input:text:disabled").prop("disabled", false);
			}
		}
		
		// Initialize the help link dialog box 
		$("#helpMessage").dialog({
			autoOpen: false,
			draggable: false,
			hide: "fade",
			modal: true,
			show: "fade",
			width: 815
		});


 		$("#enableEsignatureNofalse").click(function(event) {
			$("#reasonForEsignatureDiv").show();
		});
 		
		$("#enableEsignatureYestrue").click(function(event) {
			$("#reasonForEsignatureDiv").hide();
		});
		
		$("#mainSaveBtn").click(_.debounce(function(event) {
			$(this).prop("disabled", true);
			//To keep protocolInfoExpanded.
			$("#protoDetail").val("protolDetailInfoExpanded");

			/*var subjectTypeRadioClicked = $("#subjectTypeRadio input:radio:checked").val();
			if (subjectTypeRadioClicked === undefined || !subjectTypeRadioClicked.length) {
				$.ibisMessaging("close", {type:"primary"}); 
				$.ibisMessaging("primary", "error", "Subject Label is required field.");
				$(this).prop("disabled", false);
				return false;
			}*/
			
			//study sites
			var selectedStudySites = $("#protocolSitesTable").idtApi('getSelected');
			if (selectedStudySites.length > 0) {
				var selectedSites = [];
				for (var i = 0; i < selectedStudySites.length; i++) {
					var siteId = selectedStudySites[i];
					var site = bricsStudySites.getItem(siteId);
					selectedSites.push(site);
				}
				
				var selectedSitesStringified = JSON.stringify(selectedSites);
				$("#selectedSites").val(selectedSitesStringified);
			}
			else {
				$("#selectedSites").val("[]");
			}

			resetSiteDrugDeviceFlags();
			setNavigationFlag("save_navigate_home");
			$("#studyDetailsForm").attr("action", "saveProtocol.action").submit();
			
		}, 1000, true));
		
		$("#mainResetBtn").click(function(event) {
			$("#studyDetailsForm")[0].reset();

			var checkEnableEsignature = $("#eSigBtnDiv input:radio:checked").val();
			
			if (checkEnableEsignature == "true") {
				$("#reasonForEsignatureDiv").hide();
			}
	 		else {
				$("#reasonForEsignatureDiv").show();
			}
							
			resetSiteDrugDeviceFlags();
			$("#siteResetBtn").click();
			$("#resetDrugDeviceBtn").click();
		});

		$("#cancelBtn").click(function(event) {
			var studyId = $("#studyId").val();
			if(studyId < 0) {
				redirectWithReferrer(webRoot + '/home.action');
			}else {
				redirectWithReferrer(webRoot + '/protocol/showStudy.action?studyId=' + $("#studyId").val());
			}
			
		});
		
		$("#studyDetailsForm").submit(function(event) {
			// Check if the page is in read only mode.
			if ( !canEdit ) {
				if(!hasSubjectlabelconfiguration){
					return false;
				}
			}
			
			// Set the selected BRICS study
			var selectedStudies = $("#bricsStudyTable").idtApi('getSelected');
			
			if ( selectedStudies.length == 1 ) {
				$("#bricsStudyId").val(selectedStudies[0]);
			}
		});
		
/************************************************  Site Event Handlers  ********************************************************************************/
		$("#primarySite").click(function(event) {
			var $checkbox = $(this);
			
			if ( $checkbox.is(":checked") ) {
				$checkbox.val(true);
			}
			else {
				$checkbox.val(false);
			}
		});
		
		$("#editSiteBtn").click(_.debounce(function(event) {
			var selectedSites = IDT.getSelectedOptions($("#siteTableDiv table"));
			
			if ( selectedSites.length != 0 ) {
				var param = {
					id : selectedSites[0]
				};
				
				$.getJSON(webRoot + "/studyjson/getSite.action", param, function(data, textStatus, jqXHR) {
					var site = $.parseJSON(data);
					
					// Set the site form data.
					$("#selectedSiteId").val(site.id);
					$("#primarySite").prop("checked", site.isPrimarySite).val(site.isPrimarySite);
					$("#siteName").val(site.name);
					$("#studySiteId").val(site.studySiteId);
					$("#sitePiId").val(site.sitePiId);
					$("#siteURL").val(site.siteURL);
					
					//// Set address data.
					$("#selectedAddressId").val(site.addressId);
					$("#addressOne").val(site.addressOne);
					$("#addressTwo").val(site.addressTwo);
					$("#siteDescription").val(site.description);
					$("#city").val(site.city);
					$("#stateId").val(site.stateId);
					$("#zipCode").val(site.zipCode);
					$("#countryId").val(site.countryId);
					$("#phoneNumber").val(site.phoneNumber);
					
					// Show the update site button.
					$("#updateSiteBtn").show();
					$("#addSiteBtn").hide();
				})
				.fail(function(jqXHR, textStatus, errorThrown) {
					$.ibisMessaging("dialog", "error", "Could not get the site data from the server.");
				});
			}
		}, 1000, true));
		
		$("#siteResetBtn").click(function(event) {
			$("#selectedSiteId").val("-1");
			$("#siteInfoDiv").find("input:text").val("");
			$("#sitePiId").val("<%= user.getId() %>");
			$("#primarySite").prop("checked", true).val(true);
			$("#stateId").val("0");
			$("#countryId").val("0");
			
			$("#addSiteBtn").show();
			$("#updateSiteBtn").hide();
			
			// Collapse Site Div 
			$("#sectionDisplay").val("");
			
			if ($("#siteInfo").next("div").is(":visible")) {
				$("#siteInfo").click();
			}
			
			// Clear the checkboxes in the site table.
			IDT.clearSelected($("#siteTableDiv table"));
		});
		
		$("#addSiteBtn").click(_.debounce(function(event) {
			// Set the action type, and set flag to keep the site info section visable
			$("#sectionDisplay").val("siteInfoExpanded");
			$("#protocolSiteActionFlag").val("add_site_form");
			$("#studyDetailsForm").attr("action", "processSite.action");
		}, 1000, true));
		
		$("#updateSiteBtn").click(_.debounce(function(event) {
			// Set the action type, and set flag to keep the site info section visable
			$("#sectionDisplay").val("siteInfoExpanded");
			$("#protocolSiteActionFlag").val("edit_site_form");
			$("#studyDetailsForm").attr("action", "processSite.action");
		}, 1000, true));
		
		$("#deleteSiteBtn").click(function(event) {
			$.ibisMessaging("dialog", "info", "Are you sure you want to delete the selected study site(s)?", {
				modal : true,
				buttons : [
					{
						text : "Yes",
						"class" : "ui-priority-primary",
						click : _.debounce(function() {
							var selectedSites = IDT.getSelectedOptions($("#siteTableDiv table"));
							
							$("#sectionDisplay").val("siteInfoExpanded");  // To keep the site info div expanded.
							$("#protocolSiteActionFlag").val("delete_Site_form");
							$("#selectedSiteIds").val(JSON.stringify(selectedSites));
							$("#studyDetailsForm").attr("action", "processSite.action").submit();
						}, 1000, true)
					},
					
					{
						text : "No",
						"class" : "ui-priority-secondary",
						click : function() {
							$(this).dialog("close");
						}
					}
				]
			});
		});
/************************************************  END -- Site Event Handlers -- END  *********************************************************************/	
/******************************************************  Drug Device Event Handlers  **********************************************************************/
		$("#editDrugDeviceBtn").click(_.debounce(function(event) {
			var selectedDevices = IDT.getSelectedOptions($("#drugDevicesTableDiv table"));
			
			if ( selectedDevices.length != 0 ) {
				var param = {
					id : selectedDevices[0]
				};
				
				$.getJSON(webRoot + "/studyjson/getDrugDevice.action", param, function(data, textStatus, jqXHR) {
					var device = $.parseJSON(data);
					
					// Set the drug device data
					$("#selectedDeviceId").val(device.id);
					$("#fdaInd").val(device.fdaInd);
					$("#sponsor").val(device.sponsor);
					
					// Show the update drug device button.
					$("#updateDrugDeviceBtn").show();
					$("#addDrugDeviceBtn").hide();
				})
				.fail(function(jqXHR, textStatus, errorThrown) {
					$.ibisMessaging("dialog", "error", "Could not get the drug device data from the server.");
				});
			}
		}, 1000, true));
		
		$("#resetDrugDeviceBtn").click(function(event) {
			// Reset form fields.
			$("div#DrugDeviceInfoDiv").find("input:text").val("");
			$("#selectedDeviceId").val("");
			
			// Show the add button.
			$("#addDrugDeviceBtn").show();
			$("#updateDrugDeviceBtn").hide();
			
			// Collapse the drug device section.
			$("#sectionDisplay").val("");
			
			if ($("#drugDeviceInfo").next("div").is(":visible")) {
				$("#drugDeviceInfo").click();
			}
		});
		
		$("#addDrugDeviceBtn").click(_.debounce(function(event) {
			// Set the action type, and set flag to keep the drug device info section visable
			$("#sectionDisplay").val("DrugDeviceInfoExpanded");
			$("#protocolDrugDeviceActionFlag").val("add_DrugDevice_form");
			$("#studyDetailsForm").attr("action", "processDrugDevice.action");
		}, 1000, true));
		
		$("#updateDrugDeviceBtn").click(_.debounce(function(event) {
			// Set the action type, and set flag to keep the drug device info section visable
			$("#sectionDisplay").val("DrugDeviceInfoExpanded");
			$("#protocolDrugDeviceActionFlag").val("edit_DrugDevice_form");
			$("#studyDetailsForm").attr("action", "processDrugDevice.action");
		}, 1000, true));
		
		$("#deleteDrugDevicesBtn").click(function(event) {
			$.ibisMessaging("dialog", "info", "Are you sure you want to delete the selected FDA Ind(s)?", {
				modal : true,
				buttons : [
					{
						text : "Yes",
						"class" : "ui-priority-primary",
						click : _.debounce(function() {
							var selectedDrugs = IDT.getSelectedOptions($("#drugDevicesTableDiv table"));
							
							$("#sectionDisplay").val("DrugDeviceInfoExpanded");
							$("#protocolDrugDeviceActionFlag").val("delete_DrugDevice_form");
							$("#selectedDrugDeviceIds").val(selectedDrugs);
							$("#studyDetailsForm").attr("action", "processDrugDevice.action").submit();
						}, 1000, true)
					},
					
					{
						text : "No",
						"class" : "ui-priority-secondary",
						click : function() {
							$(this).dialog("close");
						}
					}
				]
			});
		});
		
		
		/// If study is not selected make patient label empty
		var bricsStudyId = $("#bricsStudyId").val();
		if(!bricsStudyId.length){
			$("#subjectTypeRadio input:radio").prop('checked', false);
		}
		
/****************************************************** END --  Drug Device Event Handlers -- END  ***********************************************************/
	});
</script>

<s:if test="%{!hasActionErrors() && !hasFieldErrors()}">
	<script type="text/javascript">
		$(document).ready(function() {
			// reset all the user input after submitting only if there are no errors 
			$("#siteInfoDiv").find("input:text, textarea, select").val("");
			$("#primarySite").prop("checked", true).val(true);
			$("#DrugDeviceInfoDiv").find("input:text, textarea").val("");
		});
	</script>
</s:if>

<!-- Check if the user can edit the page -->
<security:hasProtocolPrivilege privilege="addeditprotocols">
	<script type="text/javascript">
		canEdit = true;
	</script>
</security:hasProtocolPrivilege>

<s:form id="studyDetailsForm" theme="simple" method="post" name="protocolForm" enctype="multipart/form-data" >
	<s:hidden name="protoForm.id" id="studyId"/>
	<s:hidden name="protoForm.bricsStudyId" id="bricsStudyId"/>
	<s:hidden name="protoForm.selectedSites" id="selectedSites" />
	<s:hidden name="protoForm.selectedSiteIds" id="selectedSiteIds" />

	
	<h3 class="toggleable">
		<s:text name="study.dataRepo.title.display"/>
	</h3>
	<div>
		<p><s:text name="study.dataRepo.instruction.display"/></p>
		<br/>
		<div id="bricsStudyContainer" class="idtTableContainer brics">
			<table id="bricsStudyTable" class="table table-striped table-bordered" width="100%">
			</table>
		</div>
		<script type="text/javascript">	

		$.fn.dataTable.ext.order['dom-selected'] = function  ( settings, col )
		{
		  return this.api().column( col, {order:'index'} ).nodes().map( function ( td, i ) {
		    return $(td).closest('tr').hasClass('selected') ? '0' : '1';
		  } );
		}

		var manageProtocolSites =  _.debounce(function(studyId) {
			bricsStudySites.clear();
			
			var $protocolSitesTable = $("#protocolSitesTable").idtApi('getTableApi');
			var selected = $("#bricsStudyTable").idtApi('getSelected');
			var wsDomain = '<s:property  value="#systemPreferences.get('brics.modules.home.url')"/>';
			var studySitesWS = '<s:property  value="#systemPreferences.get('webservice.restful.studySites')"/>';
			var url = wsDomain + studySitesWS;
			
			$protocolSitesTable.clear().draw();
			
			$.ajax({
		  		type: "get",
		  		url: url,
		  		data : {
		  			"studyId" : studyId
		  		},
		  		
		  		success: function(response) {
	  				var obj = $(response);
					var selectedBricsStudySiteIdsArr = JSON.parse($("#selectedSiteIds").val());
									
					obj.find('StudySite').each(function() {
						var elem = $(this);
						var bricsStudySiteId = elem.children('id').text();
						var siteName = elem.children('siteName').text();
						var address1 = elem.children('address').children('address1').text();
						var address2 = elem.children('address').children('address2').text();
						var city = elem.children('address').children('city').text();
						var stateId = elem.children('address').children('state').children('id').text();
						var stateName = elem.children('address').children('state').children('name').text();
						var countryId = elem.children('address').children('country').children('id').text();
						var countryName = elem.children('address').children('country').children('name').text();
						var zipCode = elem.children('address').children('zipCode').text();
						var isPrimary = elem.children('isPrimary').text();

						var site = new StudySite(bricsStudySiteId, siteName, address1, address2, city, stateId, stateName, countryId, countryName, zipCode, isPrimary);
						bricsStudySites.setItem(bricsStudySiteId,site);
						
						
						if(isPrimary=="true") {
							siteName = siteName + " (Primary)";
						}
	
						var newRow = {
								DT_RowId:bricsStudySiteId, 
								siteName: siteName,
								city:city,
								state:stateName
						};
						
						var data = [];
						data.push(newRow);
						$("#protocolSitesTable").idtApi('addRow', data);
						
						if($('#studyId').val() > 0) {
						    if (selected.length > 0) {
						    	$("#bricsStudyTable").idtApi('disableSelection');
						    }
						}


						for(var k=0;k<selectedBricsStudySiteIdsArr.length;k++) {
							var sSite = selectedBricsStudySiteIdsArr[k];
							if(Number(sSite) == Number(bricsStudySiteId)) {
								var createId = "#" + bricsStudySiteId;
								$("#protocolSitesTable").idtApi('selectRow', createId);
								$protocolSitesTable.order([1,'asc']).draw();
								break;
							}
						}
						

					})
		  		},
		  		
		  		error : function(jqXHR, textStatus, errorThrown) {
		  			console.error("Error when getting study sites from the repository: " + errorThrown);
		  		}
		  	});
			
			var selectedRowIds = $("#bricsStudyTable").idtApi("getSelected");
			var refId = "#" + selectedRowIds[0];
			var getRowData = $("#bricsStudyTable").idtApi("getApiRow", refId);
			var studyTypeArr = getRowData.data();
			var studyType = studyTypeArr.studyType;
			$("#studyType").val(studyType); 
			
			//Set PI from WS call to repo
			var piType = studyTypeArr.principalInvestigator;
			$("#principleInvestigator").val(piType); 
			
		}, 1000, true);
					
$(document).ready(function() {
	var studyId = $("#studyId").val();
	var url = "";
	
	if (studyId < 0) {
		url = "<s:property value='#webRoot'/>/protocol/createBricsStudy.action";
	} else {
		url = "<s:property value='#webRoot'/>/protocol/getBricsStudy.action";
	}
	
		$("#bricsStudyTable").idtTable({
			idtUrl: url,
		filterData: {
			studyId: studyId
		}, 			
		idtData: {
			primaryKey: 'prefixedId'
		},
		select: "single",
		columns: [
			{
		          name: 'title',
		          title: '<%= rs.getValue("study.dataRepo.studyTitle.display", l) %>',
		          data: 'title',
		          parameter:'title',
		          orderDataType: 'dom-selected',
		          type: 'string',
		          render: function ( data, type, full) {//console.log("type: "+type+" | full: "+JSON.stringify(full));
		        	  if ( type === 'filter'  ) {
		              	var $target = $('<div>').html(data);
		            	var value = $target.text();
		            	//console.log("value: "+value);
		                return value;
		        	  } else {
		        		  return data;
		        	  }
		          }
			},
			{
		          name: 'prefixedId',
		          title: '<%= rs.getValue("study.dataRepo.studyId.display", l) %>',
		          data: 'prefixedId',
		          parameter:'prefixedId'
			},
			{
		          name: 'principalInvestigator',
		          title: '<%= rs.getValue("study.dataRepo.pi.display", l) %>',
		          data: 'principalInvestigator',
		          parameter:'principalInvestigator'					
			},
			{
		          name: 'studyPermission',
		          title: '<%= rs.getValue("study.dataRepo.studyPermission.display", l) %>',
		          data: 'studyPermission',
		          parameter:'studyPermission'					
			},
			{
		          name: 'studyType',
		          title: '',
		          data: 'studyType',
		          parameter:'studyType',
		          visible: false
			},
			{
		          name: 'id',
		          title: '',
		          data: 'id',
		          parameter:'id',
		          visible: false
			},
		],
		
		initComplete: function(setting) {
			var $bricsTable = $("#bricsStudyTableDiv table");
			var oTable = $("#bricsStudyTable").idtApi('getTableApi');
			var options = $("#bricsStudyTable").idtApi('getOptions');
			
			if($("#bricsStudyId").val()) {
				var bricsStudyId = '#'+$("#bricsStudyId").val();
				var checkForExistingRow = $("#bricsStudyTable").idtApi('getApiRow', bricsStudyId);
				// Check if the user can edit
				if ( canEdit ) {
					// Check if the Data Repository Study table is being initialized.
					if(checkForExistingRow.length != 0) {
						$("#bricsStudyTable").idtApi('selectRow', bricsStudyId);
						$("#bricsStudyTable").idtApi('disableSelection');
						 var selectedRowId = oTable.row('.selected').data();
						 manageProtocolSites(selectedRowId.id);

					}
				}
				// The current user cannot edit the study, disable selection for this table
				else {
					if(checkForExistingRow.length != 0) {
						$("#bricsStudyTable").idtApi('selectRow', bricsStudyId);
						$("#bricsStudyTable").idtApi('disableSelection');
						 var selectedRowId = oTable.row('.selected').data();
						 manageProtocolSites(selectedRowId.id);

					}
				}

			}

			oTable.on('select', function(e, dt, type, indexes) {
				var selected = $("#bricsStudyTable").idtApi('getSelected');
				var selectedRow = dt.rows(indexes).data()[0];
				manageProtocolSites(selectedRow.id);
			})
		}
	});
	$.fn.dataTable.ext.order['dom-text'] = function  ( settings, col ) {
        return this.api().column( col, {order:'index'} ).nodes().map( function ( td, i ) {
        	var $target = $('a', td);
        	var value = $target.text(); //console.log("select val(): "+value);
            return value;
        } );
    }
})
</script>					
	</div>
	
	<%-- ---------------------------------------------------Protocol Details starts---------------------------------------------------- --%>
	<h3 id="protolDetailInfo" class="toggleable <s:if test="%{protoForm.protoDetail != 'protolDetailInfoExpanded'}">collapsed</s:if>">
		<s:text name="protocol.create.title.display"/>
	</h3>
	<div>
		<security:hasProtocolPrivilege privilege="addeditprotocols">
			<s:set var="actionName" value="%{#context['struts.actionMapping'].name}"></s:set>
			<p align="left">
				<s:text name="protocol.create.instruction.display" /><br/>
				<label class="requiredInput"></label>
				<i> <s:text name="protocol.create.requiredSymbol.display" /> </i>
			</p>
		</security:hasProtocolPrivilege>
		
		<div id="studyConfigDiv">
			<div class="formrow_1">
				<label for="studyName" class="requiredInput"><s:text name="study.add.name.display" /></label>
				<s:textfield name="protoForm.name" id="studyName" maxlength="400" />
			</div>
			
			<div class="formrow_1">
				<label for="studyNumber" class="requiredInput"><s:text name="study.add.number.display" /></label>
				<s:textfield name="protoForm.protocolNumber" id="studyNumber" maxlength="50" />
			</div>
			
			<div class="formrow_1">
				<label for="principleInvestigator" class="requiredInput"><s:text name="study.add.principalInvestigator" /></label>
				<s:textfield name="protoForm.principleInvestigator"  class="disabled"  readonly="true"  id="principleInvestigator" maxlength="127" />
			</div>
			
			<div class="formrow_1">
				<label for="studyType" class="requiredInput"><s:text name="study.add.type.display" /></label>
				<s:textfield name="protoForm.studyType"  class="disabled"  readonly="true" id="studyType" maxlength="50" />
				
			</div>
			
			<div class="formrow_1" id="subjectTypeRadio">
				<label for="PatientLabel" class="requiredInput"><s:text name="patient.label.display" /></label>
				<s:radio name="protoForm.patientDisplayType" 
					list="#{@gov.nih.nichd.ctdb.common.CtdbConstants@PATIENT_DISPLAY_GUID:''}" />
				<s:text name="patient.label.SubjectGUID" />
				<s:radio name="protoForm.patientDisplayType" 
					list="#{@gov.nih.nichd.ctdb.common.CtdbConstants@PATIENT_DISPLAY_ID:''}" onClick="checkForBlankSubjectIds()"/>
				<s:text name="patient.label.SubjectID" />
				
				 	<s:if test="#disallowingPII == 0">
				
					<s:radio name="protoForm.patientDisplayType"
						list="#{@gov.nih.nichd.ctdb.common.CtdbConstants@PATIENT_DISPLAY_MRN:''}" />
					<s:text name="patient.mrn.display" />
					</s:if>		
			</div>
	
			<div class="formrow_1">
				<label for="UseERegulatoryBinder"><s:text name="protocol.add.eUseBinder" /></label>
				<s:radio name="protoForm.useEbinder" list="#{'false':''}" /><s:text name="protocol.add.eUseBinder.no" />
				<s:radio name="protoForm.useEbinder" list="#{'true':''}" /><s:text name="protocol.add.eUseBinder.yes" />
			</div>
	

	
			<div id="eSigBtnDiv" class="formrow_1">
				<label for="EnableEsignature"><s:text name="protocol.add.enableEsignature" /></label>
				<s:radio id="enableEsignatureNo" name="protoForm.enableEsignature" list="#{'false':''}" /><s:text name="protocol.add.enableEsignature.no" />
				<s:radio id="enableEsignatureYes" name="protoForm.enableEsignature" list="#{'true':''}" /><s:text name="protocol.add.enableEsignature.yes" />
			</div>
			<div class="formrow_1 hidden" id="reasonForEsignatureDiv">
				<label for="ReasonForEsignature" class="requiredInput"><s:text name="protocol.add.reasonForEsignature.display" /></label>
				<s:textarea name="protoForm.reasonForEsignature" id="reasonForEsignature" rows="4" cols="30" />
			</div>
			<%-- <div id="mileStoneDiv" class="formrow_1">
				<label for="MilesStone"><s:text name="protocol.add.milesStones" /></label>
				<s:select id="milesStone" list="milesStones" multiple="true" size="5"
					listKey="id" listValue="name"
					name="protoForm.milesStones"
					value="protoForm.milesStones.id" headerKey="" />
			</div> --%>
			
		</div>
	</div>
	<%-- ---------------------------------------------------Protocol Details ends---------------------------------------------------- --%>

	<s:set var="displayClinicalPoint" value="#systemPreferences.get('display.protocol.clinicalPoint')" />
	<s:if test="#displayClinicalPoint">
	<%-- ---------------------------------------------------Protocol Miles Stones starts---------------------------------------------------- --%>
	<h3 id="protomilesStones" class="toggleable <s:if test="%{protoForm.locProcPOCDisplay != 'default'}">collapsed</s:if>">
		<s:text name="protocol.create.milesStones.title.display"/>
	</h3>
	<div id="milesStonesDiv">
		<div class="formrow_1">
			<label for="protoMilesStones">
				<s:text name="protocol.add.milesStones.display"/>
			</label>
			<div id="protoMilesStones" style="float:left; ">
				<jsp:include page="addProtocolMilesStones.jsp" />
			</div>
		</div>
	</div>
	<%-- ---------------------------------------------------Protocol Miles Stones ends---------------------------------------------------- --%>
	<%-- ---------------------------------------------------Protocol Clinical Location starts---------------------------------------------------- --%>
	<h3 id="protoClinicLocation" class="toggleable <s:if test="%{protoForm.locProcPOCDisplay != 'default'}">collapsed</s:if>">
		<s:text name="protocol.create.clinicalLocation.title.display"/>
	</h3>
	<div id="clinicLocInfoDiv">
		<div class="formrow_1">
			<label for="protoClinicLocLab" class="requiredInput">
				<s:text name="protocol.add.clinicalLocation.display"/>
			</label>
			<div id="protoClinicalLocation" style="float:left; ">
				<jsp:include page="addProtocolClinicalLocations.jsp" />
			</div>
		</div>
	</div>
	<%-- ---------------------------------------------------Protocol Location ends---------------------------------------------------- --%>
	
	<%-- ---------------------------------------------------Protocol Procedure starts---------------------------------------------------- --%>
	<h3 id="protoProcedure" class="toggleable <s:if test="%{protoForm.locProcPOCDisplay != 'default'}">collapsed</s:if>">
		<s:text name="protocol.create.procedure.title.display"/>
	</h3>
	<div id="procedureInfoDiv">
		<div class="formrow_1">
			<label for="protoProceduresLab" class="requiredInput">
				<s:text name="protocol.add.procedure.display"/>
			</label>
			<div id="protoProcedure" style="float:left; ">
				<jsp:include page="addProtocolProcedures.jsp" />
			</div>
		</div>
	</div>
	<%-- ---------------------------------------------------Protocol Procedure ends---------------------------------------------------- --%>

	<%-- ---------------------------------------------------Protocol Point of Contact starts---------------------------------------------------- --%>
	<h3 id="protoPointOfContact" class="toggleable <s:if test="%{protoForm.locProcPOCDisplay != 'default'}">collapsed</s:if>">
		<s:text name="protocol.create.pointOfContatc.title.display"/>
	</h3>
	<div id="pointOfContactInfoDiv">
		<div class="formrow_1">
			<label for="protoPOCsLab" class="requiredInput">
				<s:text name="protocol.add.pointOfContact.display"/>
			</label>
			<div id="protoPointOfContact" style="float:left; ">
				<jsp:include page="addProtocolPointOfContacts.jsp" />
			</div>
		</div>
	</div>
	<%-- ---------------------------------------------------Protocol Point of Contact ends---------------------------------------------------- --%>
	</s:if>
	
	<%-- ---------------------------------------------------Protocol Sites starts---------------------------------------------------- --%>
	<h3 id="siteInfo" class="toggleable <s:if test="%{protoForm.sectionDisplay != 'siteInfoExpanded'}">collapsed</s:if>">
				<s:text name="app.label.lcase.multi.site.study" />
			</h3>
			
		<div id="siteInfoDiv">
			<p><s:text name="protocol.protocolSites.instruction"/></p>
			<br/>

			<div id="protocolSitesContainer" class="idtTableContainer brics">
				<table id="protocolSitesTable" class="table table-striped table-bordered" width="100%">
				</table>
			</div>
			<script type="text/javascript">
		
			$("#protocolSitesTable").idtTable({
				select: "multi",
				columns: [
					{
				          name: 'siteName',
				          title: "Site Name",
				          data: 'siteName',
				          orderDataType: 'dom-selected'
					},
					{
				          name: 'city',
				          title: "City",
				          data: 'city'							
					},
					{
				          name: 'state',
				          title: "State",
				          data: 'state'							
					}						
				],
				data: []
			})
			</script>								

		</div>

<% 
	if(request.getSession().getAttribute("subjectlabelconfiguration") != null && ((Boolean)request.getSession().getAttribute("subjectlabelconfiguration")).booleanValue()){
%>
		<div id="mainBtnSection" class="formrow_1">
			<input type="button" id="cancelBtn" value="<s:text name='button.Cancel'/>" title="<s:text name='tooltip.cancel'/>" />
		
			<input type="button" id="mainSaveBtn" value="<s:text name='button.Save'/>" title="<s:text name='tooltip.save'/>" />
		</div>

<%
	}else{
	
%>	
	<security:hasProtocolPrivilege privilege="addeditprotocols">
	
		<div id="mainBtnSection" class="formrow_1">
			<input type="button" id="cancelBtn" value="<s:text name='button.Cancel'/>" title="<s:text name='tooltip.cancel'/>" />
		
			<input type="button" id="mainSaveBtn" value="<s:text name='button.Save'/>" title="<s:text name='tooltip.save'/>" />
		</div>
	</security:hasProtocolPrivilege>
<%
	}
%>
	<%-- ---------------------------------------------------Protocol Sites ends---------------------------------------------------- --%>
</s:form>

<div id="studyDetailsDiv"></div>



<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />

</html>
