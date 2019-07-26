<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol,
                 gov.nih.nichd.ctdb.form.domain.Form,
                 gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType,
                 gov.nih.nichd.ctdb.common.*"%>
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/WEB-INF/FCKeditor.tld" prefix="FCK" %>

<%
	String webserviceException = "" ;
	if (request.getAttribute("webserviceException") != null) {
		webserviceException = (String)request.getAttribute("webserviceException");
	}
	String form_nonPatientForm = (String)request.getAttribute("_form_nonPatientForm");
	String isCopyright = (String)request.getAttribute("isCopyright");
%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditforms"/>

<html>

<s:set var="pageTitle" scope="request">
	<s:text name="form.addform.saveasform"/>
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript">

var formMode;
var originalDataStructureName;

//below is the initial info for when in edit mode....so when they hit reset, it will go this state
var eFormNameIndex;
var eFormTypeId;
var eName;
var eDescription;
var eStatusIndex;
var eNPFormTypeIndex;
var eTabdisplay0;
var eTabdisplay1;
var eAccessFlag0;
var eAccessFlag1;
var eDataEntryFlag0;
var eDataEntryFlag1;
var eFormHeader;
var eFormFooter;
var eFormborder0;
var eFormborder1;
var eSectionborder0;
var eSectionborder1;
var eFormfontIndex;
var eFontSizeIndex;
var eFormcolorIndex;
var eSectionfontIndex;
var eSectioncolorIndex;
var eCellpaddingIndex;
var eCopyright0;
var eCopyright1;
var eAllowMult0;
var eAllowMult1;


function initialize(fMode, _form_nonPatientForm, isCopyright) {
	//if double entry is checked disable allow multiple collections instances radio and default to no
	/*if($('input[name=dataEntryFlag]:radio:checked').val()==2){
		$('#multipleNay').attr('checked',true);
		$('#multipleYey').attr('disabled',true);
		$('#multipleNay').attr('disabled',true);
	}*/
	
	
	
	originalDataStructureName = $('#dataStructureName').val();
	
	var dStructureName = document.getElementById("dStructureName");
	if ($('#dataStructureName').val() != "") {
		dStructureName.innerHTML = originalDataStructureName
	} else {
		dStructureName.innerHTML = "None";
	}
	
	var id = $('#id').val();
	document.getElementById("formid").value= id;
	
	var formDataStructuresDiv = document.getElementById("formDataStructures");
	
	EventBus.on("init:table", function() {
		var table = formDataStructuresDiv.getElementsByTagName("table")[0];
		var $table = $(table);
		var dsName = $('#dataStructureName').val();
		if (dsName != "") {   //if coming from imported form, the form has no form structure assoc to it...so there is nothing to select
			dsName = $('#dataStructureName').val();
			if (IDT.hasRowValue($table, dsName)) {
				IDT.addSelectedOptionValue($table, dsName);
				IDT.dataTables[$table.attr("id")].fnDraw();
			}
		}
	});


	var formType = document.getElementById("formType");
	formMode = fMode;

	if (_form_nonPatientForm == "true") {  //means patient form
		formType.selectedIndex = "1";	
	} else {
		formType.selectedIndex = "0";
	}
	
	formType.disabled = true;
	
	var ftypeid = document.getElementsByName("formForm.formtypeid")[0];
	var nonpatientformtypeidObj = document.getElementsByName("formForm.nonpatientformtypeid")[0];
	
	for (var i=0;i<nonpatientformtypeidObj.length;i++) {
		var nonpatientformtypeidObjVal = nonpatientformtypeidObj.options[i].value;
		if (nonpatientformtypeidObjVal == ftypeid.value) {
			nonpatientformtypeidObj.selectedIndex = i;
			break;
		}
	}
	
	toggleFormType();
	if (isCopyright == "true") { 
		disableEverything(_form_nonPatientForm);

	} else {
		//just disable the copyright button becasue we currently are not supporting going from non-copyright to copyright
		//but this will be fixed soon
		$("#copyRight2").attr("checked", "true");		
		$("#copyRight1").attr("disabled", "true");
		$("#copyRight2").attr("disabled", "true");
	}
	
	//get all the info for edit mode so we can reset back to this
	var formType = document.getElementById("formType");
	eFormNameIndex = formType.selectedIndex;
	
	var formTypeId = document.getElementsByName("formForm.formtypeid")[0];
	eFormTypeId = formTypeId.value;

	var name = document.getElementById("fname");
	eName = name.value;

	var desc = document.getElementById("fdescription");
	eDescription = desc.value;
	
	var status = document.getElementsByName("formForm.status")[0];
	var statusHidden = document.getElementsByName("formForm.statusHidden")[0];
	statusHidden.value = status.value;
	eStatusIndex = status.selectedIndex;
	
	if (navigator.userAgent.indexOf('MSIE') > 0){
		status.disabled = "disabled";
	} else {
		status.disabled = true;
	}

	var nonpatientformtypeid = document.getElementsByName("formForm.nonpatientformtypeid")[0];
	eNPFormTypeIndex = nonpatientformtypeid.selectedIndex;

	var tabdisplay0 = document.getElementsByName("formForm.tabdisplay")[0];
	var tabdisplay1 = document.getElementsByName("formForm.tabdisplay")[1];
	eTabdisplay0 = tabdisplay0.checked;
	eTabdisplay1 = tabdisplay1.checked;

	var accessFlag0 = document.getElementsByName("formForm.accessFlag")[0];
	var accessFlag1 = document.getElementsByName("formForm.accessFlag")[1];
	eAccessFlag0 = accessFlag0.checked;
	eAccessFlag1 = accessFlag1.checked;

	var dataEntryFlag0 = document.getElementsByName("formForm.dataEntryFlag")[0];
	eDataEntryFlag0 = dataEntryFlag0.checked;

	var copyright0 = document.getElementsByName("formForm.copyRight")[0];
	var copyright1 = document.getElementsByName("formForm.copyRight")[1];
	eCopyright0 = copyright0.checked;
	eCopyright1 = copyright1.checked;

	
	
	var allowMultt0 = document.getElementsByName("formForm.allowMultipleCollectionInstances")[0];
	var allowMultt1 = document.getElementsByName("formForm.allowMultipleCollectionInstances")[1];
	eAllowMultt0 = allowMultt0.checked;
	eAllowMultt1 = allowMultt1.checked;
	
	
	var formHeader = document.getElementsByName("formForm.formHeader")[0];
	eFormHeader = formHeader.value;
	
	var formFooter = document.getElementsByName("formForm.formFooter")[0];
	eFormFooter = formFooter.value;

	var formborder0 = document.getElementsByName("formForm.formborder")[0];
	var formborder1 = document.getElementsByName("formForm.formborder")[1];
	eFormborder0 = formborder0.checked;
	eFormborder1 = formborder1.checked;

	var sectionborder0 = document.getElementsByName("formForm.sectionborder")[0];
	var sectionborder1 = document.getElementsByName("formForm.sectionborder")[1];
	eSectionborder0 = sectionborder0.checked;
	eSectionborder1 = sectionborder1.checked;

	var formfont = document.getElementsByName("formForm.formfont")[0];
	eFormfontIndex = formfont.selectedIndex;

	var fontSize = document.getElementById("fSize");
	eFontSizeIndex = fontSize.selectedIndex;

	var formcolor = document.getElementsByName("formForm.formcolor")[0];
	eFormcolorIndex = formcolor.selectedIndex;

	var sectionfont = document.getElementsByName("formForm.sectionfont")[0];
	eSectionfontIndex = sectionfont.selectedIndex;
	
	var sectioncolor = document.getElementsByName("formForm.sectioncolor")[0];
	eSectioncolorIndex = sectioncolor.selectedIndex;
	
	var cellpadding = document.getElementsByName("formForm.cellpadding")[0];
	eCellpaddingIndex = cellpadding.selectedIndex;

}

function setCopyright(isCopyright) {
	if (isCopyright) {
		$('[id*="copyRight1"]').prop('checked', true);
		$('[id*="copyRight2"]').prop('checked', false);
	} else {
		$('[id*="copyRight1"]').prop('checked', false);
		$('[id*="copyRight2"]').prop('checked', true);
	}
}

function disableEverything(_form_nonPatientForm) {
	$("#fdescription").attr("disabled", "true");
	$("#fStatus").attr("disabled", "true");
	
	if (_form_nonPatientForm == "true") {
		$("#fnonpatientformtypeid").attr("disabled", "true");
	}

	$("#tabdisplay1").attr("disabled", "true");
	$("#tabdisplay2").attr("disabled", "true");
	
	$("#accessFlag1").attr("disabled", "true");
	$("#accessFlag2").attr("disabled", "true");
	
	$("#dataEntryFlag1").attr("disabled", "true");
	//$("#dataEntryFlag2").attr("disabled", "true");
	
	$("#dataEntryWorkflowType1").attr("disabled", "true");
	$("#dataEntryWorkflowType2").attr("disabled", "true");
	
	$("#ds0").attr("disabled", "true");
	$("#ds1").attr("disabled", "true");
	
	//set copyright to true since you can only save a copyrighted form as a new copyrighted form
	$("#copyRight1").attr("checked", "true");
	
	$("#copyRight1").attr("disabled", "true");
	$("#copyRight2").attr("disabled", "true");
	
	$("#fHeader").attr("disabled", "true");
	$("#fFooter").attr("disabled", "true");
	
	$("#fBorderYes").attr("disabled", "true");
	$("#fBorderNo").attr("disabled", "true");
	
	$("#fFont").attr("disabled", "true");
	$("#fSize").attr("disabled", "true");
	$("#fColor").attr("disabled", "true");
	$("#cPadding").attr("disabled", "true");
	$("#sBorderYes").attr("disabled", "true");
	$("#sBorderNo").attr("disabled", "true");
	$("#sFont").attr("disabled", "true");
	$("#sColor").attr("disabled", "true");
}


function enableEverything(_form_nonPatientForm) {
	$("#fdescription").attr("disabled", false);
	$("#fStatus").attr("disabled", false);
	
	if(_form_nonPatientForm == "true") {
		$("#fnonpatientformtypeid").attr("disabled", false);
	}
	
	$("#tabdisplay1").attr("disabled", false);
	$("#tabdisplay2").attr("disabled", false);
	
	$("#accessFlag1").attr("disabled", false);
	$("#accessFlag2").attr("disabled", false);
	
	$("#dataEntryFlag1").attr("disabled", false);
	//$("#dataEntryFlag2").attr("disabled", false);
	
	$("#dataEntryWorkflowType1").attr("disabled", false);
	$("#dataEntryWorkflowType2").attr("disabled", false);
	
	$("#ds0").attr("disabled", false);
	$("#ds1").attr("disabled", false);
	
	//set copyright to true since you can only save a copyrighted form as a new copyrighted form
	$("#copyRight1").attr("disabled", false);
	$("#copyRight2").attr("disabled", false);
	
	$("#fHeader").attr("disabled", false);
	$("#fFooter").attr("disabled", false);
	
	$("#fBorderYes").attr("disabled", false);
	$("#fBorderNo").attr("disabled", false);
	
	$("#fFont").attr("disabled", false);
	$("#fSize").attr("disabled", false);
	$("#fColor").attr("disabled", false);
	$("#cPadding").attr("disabled", false);
	$("#sBorderYes").attr("disabled", false);
	$("#sBorderNo").attr("disabled", false);
	$("#sFont").attr("disabled", false);
	$("#sColor").attr("disabled", false);
}

function toggleFormType() {

	var formType = document.getElementById("formType");
	var selected = formType.options[formType.selectedIndex].value;
	var formTypeId = document.getElementsByName("formForm.formtypeid")[0];

	var nonpatientformtypeTR = document.getElementById("nonpatientformtypeTR");
	if (selected == "Subject") {
		
		nonpatientformtypeTR.style.display = "none";
		formTypeId.value = "10";  //this means patient form
		
		var accessFlag0 = document.getElementsByName("formForm.accessFlag")[0];
		var accessFlag1 = document.getElementsByName("formForm.accessFlag")[1];
		accessFlag0.disabled=false;
		accessFlag1.disabled=false;
		
		var dataEntryFlag0 = document.getElementsByName("formForm.dataEntryFlag")[0];
		dataEntryFlag0.disabled=false;
		
	} else {
		var nonpatientformtypeid = document.getElementsByName("formForm.nonpatientformtypeid")[0];
		var npSelected = nonpatientformtypeid.options[nonpatientformtypeid.selectedIndex].value;
		
		formTypeId.value=npSelected;
		nonpatientformtypeTR.style.display = "block";

		var dataEntryFlag0 = document.getElementsByName("formForm.dataEntryFlag")[0];
		dataEntryFlag0.checked=true;
		dataEntryFlag0.disabled=true;
	}
}

function resetFormInfo() {
	
	var formTypeId = document.getElementsByName("formForm.formtypeid")[0];
	formTypeId.value = eFormTypeId;

	var name = document.getElementById("fname");
	name.value=eName;

	var desc = document.getElementById("fdescription");
	desc.value=eDescription;
	
	var status = document.getElementsByName("formForm.status")[0];
	status.selectedIndex=eStatusIndex;

	var nonpatientformtypeid = document.getElementsByName("formForm.nonpatientformtypeid")[0];
	nonpatientformtypeid.selectedIndex=eNPFormTypeIndex;
	
	var tabdisplay0 = document.getElementsByName("formForm.tabdisplay")[0];
	var tabdisplay1 = document.getElementsByName("formForm.tabdisplay")[1];
	tabdisplay0.checked=eTabdisplay0;
	tabdisplay1.checked=eTabdisplay1;

	var accessFlag0 = document.getElementsByName("formForm.accessFlag")[0];
	var accessFlag1 = document.getElementsByName("formForm.accessFlag")[1];
	accessFlag0.checked=eAccessFlag0;
	accessFlag1.checked=eAccessFlag1;

	var dataEntryFlag0 = document.getElementsByName("formForm.dataEntryFlag")[0];
	dataEntryFlag0.checked=eDataEntryFlag0;
	
	var copyright0 = document.getElementById("copyRight1");
	var copyright1 = document.getElementById("copyRight2");
	copyright0.checked = eCopyright0;
	copyright1.checked = eCopyright1;
	
	var allowMultt0 = document.getElementsByName("formForm.allowMultipleCollectionInstances")[0];
	var allowMultt1 = document.getElementsByName("formForm.allowMultipleCollectionInstances")[1];
    allowMultt0.checked = eAllowMultt0;
	allowMultt1.checked = eAllowMultt1;
	
	var formHeader = document.getElementsByName("formForm.formHeader")[0];
	formHeader.value=eFormHeader;
	var formFooter = document.getElementsByName("formForm.formFooter")[0];
	formFooter.value=eFormFooter;

	var formborder0 = document.getElementsByName("formborder")[0];
	var formborder1 = document.getElementsByName("formborder")[1];
	formborder0.checked=eFormborder0;
	formborder1.checked=eFormborder1;
	
	var sectionborder0 = document.getElementsByName("formForm.sectionborder")[0];
	var sectionborder1 = document.getElementsByName("formForm.sectionborder")[1];
	sectionborder0.checked=eSectionborder0;
	sectionborder1.checked=eSectionborder1;
	
	var formfont = document.getElementsByName("formForm.formfont")[0];
	formfont.selectedIndex=eFormfontIndex;
	
	var fontSize = document.getElementById("fSize");
	fontSize.selectedIndex = eFontSizeIndex;
	
	var formcolor = document.getElementsByName("formForm.formcolor")[0];
	formcolor.selectedIndex=eFormcolorIndex;
	
	var sectionfont = document.getElementsByName("formForm.sectionfont")[0];
	sectionfont.selectedIndex=eSectionfontIndex;
	
	var sectioncolor = document.getElementsByName("formForm.sectioncolor")[0];
	sectioncolor.selectedIndex=eSectioncolorIndex;
	
	var cellpadding = document.getElementsByName("formForm.cellpadding")[0];
	cellpadding.selectedIndex=eCellpaddingIndex;
}


//trim helper function
function trim(str) {
    return str.replace(/^\s+|\s+$/g,"");
}

function validate() {

	var errorContainer = document.getElementById("errorContainer");
	errorContainer.innerHTML="";
	var formName = document.getElementById("fname");
	var formNameTrim = trim(formName.value);
	if (formNameTrim=="") {
		$.ibisMessaging(
				"primary", 
				"error", 
				"<s:text name="errors.text"/><b>Form Name is required.</b>",
				{
					container: "#errorContainer"
				});
		$("#errorContainer").show();
		return false;
	} else {
		errorContainer.style.display="none";
		errorContainer.innerHTML="";
		return true;
	}
}

function nonPatientFormTypeChange() {
	var nonpatientformtypeid = document.getElementsByName("formForm.nonpatientformtypeid")[0];
	var formTypeId = document.getElementsByName("formForm.formtypeid")[0];
	var npSelected = nonpatientformtypeid.options[nonpatientformtypeid.selectedIndex].value;
	formTypeId.value = npSelected;
}

function cancel() {
	var url = "<s:property value="#webRoot"/>/form/formHome.action";
	redirectWithReferrer(url);
}

function setDataStructure(name,version) {

	$('#dataStructureName').val(name);
	$('#dataStructureVersion').val(version);
	//var fullName = name + "(" + version + ")";
	
	var formDataStructuresDiv = document.getElementById("formDataStructures");
	var table = formDataStructuresDiv.getElementsByTagName("table")[0];
	var $table = $(table);
	IDT.addSelectedOptionValue($table, name);

	if (IDT.hasOption($table, name)) {
		IDT.addSelectedOptionValue($table, name);
	}
}

//ajax call and response for adding form info
function doAddEditFormInfoAjaxPost() {
	var np = <%= form_nonPatientForm %>;
	enableEverything(np);
	
	var params = $('#createEditForm form').serialize();
	var formInfoURL = "<s:property value="#webRoot"/>/form/saveNewForm.action?action=process_saveas";

	
	$.ajax({
		type: "POST",
		url: formInfoURL,
		data: params,
        beforeSend: function(){
        	var errorContainer = document.getElementById("errorContainer");
        	errorContainer.innerHTML="";
        	
    	
        	
        	var formName = document.getElementById("fname");
        	var formNameTrim = trim(formName.value);
        	if (formNameTrim == "") {
        		$.ibisMessaging(
        				"primary", 
        				"error", 
        				"<s:text name="errors.text"/><b>Form Name is required.</b>",
        				{
        					container: "#errorContainer"
        				});
        		$("#errorContainer").show();
        		if ("<%= isCopyright %>" == "true") {
        			disableEverything(np);
        		}
         		return false;
         		
        	} else {
        		errorContainer.style.display="none";
            	errorContainer.innerHTML="";
            	return true;

        	}
        },
		success: function(response) {
			if (response != "errors.duplicate.form" && response != "errors.required.data.elements" && response != "errors.web.service") {
        		var url = "<s:property value="#webRoot"/>/form/formHome.action?message=formSaved";
				redirectWithReferrer(url);

			} else {
				if (response == "errors.duplicate.form") {
	        		$.ibisMessaging(
	        				"primary", 
	        				"error", 
	        				"<s:text name="errors.text"/><b>Please enter a different form name. The form name must be unique in the study.</b>",
	        				{
	        					container: "#errorContainer"
	        				});
	        		$("#errorContainer").show();
					if ("<%= isCopyright %>" == "true") {
	        			disableEverything(np);
	        		}
				} else if (response == "errors.required.data.elements") {
					$.ibisMessaging(
	        				"primary", 
	        				"error", 
	        				"<s:text name="errors.text"/><b>The form can not be activated because not all required data elements are associated to questions on the form.</b>",
	        				{
	        					container: "#errorContainer"
	        				});
	        		$("#errorContainer").show();
					if ("<%= isCopyright %>" == "true") {
	        			disableEverything(np);
	        		}
				} else if (response == "errors.web.service") {
					$.ibisMessaging(
	        				"primary", 
	        				"error", 
	        				"<s:text name='errors.text'/><b><s:text name='form.forms.formActivation.webserviceException'><s:param>" + fName + "</s:param></s:text></b>",
	        				{
	        					container: "#errorContainer"
	        				});
	        		$("#errorContainer").show();
					if("<%= isCopyright %>" == "true") {
	        			disableEverything(np);
	        		}
				}
			}
		},
		error: function(e) {
			alert("error" + e );
		}
	});
} 
</script>

<div id="createEditForm">
<s:form theme="simple" onsubmit="return false">
	<s:hidden name="id" id="id"/> 
	<s:hidden name="formForm.formtypeid"/>
	<s:hidden name="formForm.formid" id="formid"/>
	<s:hidden name="formForm.dataStructureVersion" id="dataStructureVersion"/>
	<s:hidden name="formForm.dataStructureName" id="dataStructureName"/>

	<div id="formtypeDiv"></div>

	<div>
		<div id="errorContainer" style="display:none"></div>
		<p class="leftAlign"><s:text name="form.forms.saveFormAsText.display"/><br>
			<label class="requiredInput"></label> 
			<i><s:text name="form.forms.saveFormAsText.requiredSymbol.display"/></i><br>
		</p>
	</div>




	<h3 align="left" class="toggleable expanded" ><s:text name="form.forms.formInfo.formInfoDisplay"/></h3>
	<div id="formInformationDiv">
		<div class="formrow_2">
			<label for="formType" class="requiredInput"><s:text name="form.forms.formInfo.FormDisplay"/></label>
			<select id="formType" onChange="toggleFormType()">
				<option name="Patient">Subject</option>
				<option name="Non-Patient">Non-Subject</option>
			</select>
		</div>
	
		<div class="formrow_2">
			<label for="useTabDisplayDisplay"><s:text name="form.forms.formInfo.useTabDisplayDisplay"/></label>
			<s:radio name="formForm.tabdisplay" id="tabdisplay1" list="#{'true':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.tabdisplay" id="tabdisplay2" list="#{'false':''}" /><s:text name="form.formatting.no"/>
		</div>
	
		<div class="formrow_2">
			<label for="fname" class="requiredInput"><s:text name="app.label.lcase.name"/></label>
			<s:textfield name="formForm.name" maxlength="50" size="35" id="fname"/>
		</div>
	
		<div class="formrow_2">
			<label for="access"><s:text name="form.access.display"/></label>
			<s:radio name="formForm.accessFlag" id="accessFlag1" list="#{'1':''}" /><s:text name="form.private.display"/>&nbsp;
			<s:radio name="formForm.accessFlag" id="accessFlag2" list="#{'2':''}" /><s:text name="form.public.display"/>
		</div>
	
		<div class="formrow_2 allowWrap">
			<label for="description"><s:text name="app.label.lcase.description"/></label>
			<s:textarea name="formForm.description" id="fdescription" rows="6"/>
		</div>
	
		<div class="formrow_2">
			<label for="dataentry"><s:text name="app.label.lcase.dataentry"/></label>
		 	<s:radio name="formForm.dataEntryFlag" id="singleKeyRadio" list="#{'1':''}" /><s:text name="form.singlekey.display"/>&nbsp;
			<%-- <s:radio name="formForm.dataEntryFlag" id="doubleKeyRadio" list="#{'2':''}" /><s:text name="form.doublekey.display"/> --%>
		</div>
	
		<div class="formrow_2">
			<label for="CopyrightedForm"><s:text name="form.copyrightedForm" /></label>
			<s:radio name="formForm.copyRight" id="copyRight1" list="#{'true':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.copyRight" id="copyRight2" list="#{'false':''}" /><s:text name="form.formatting.no"/>
		</div>
		
		<div class="formrow_2 allowWrap" >
			<label for="allowmultiplecollectioninstances" > <s:text name="form.multiplecollectioninstances.display"/></label>
			<s:radio name="formForm.allowMultipleCollectionInstances" id="multipleYey" list="#{'true':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.allowMultipleCollectionInstances" id="multipleNay" list="#{'false':''}" /><s:text name="form.formatting.no"/>
			<a title="<s:text name="form.multiplecollectioninstances.display.tooltip" />"><img src="../images/icons/info-icon.png"></a> 
		</div> 
	
		<div class="formrow_2"></div>

	
		<div class="formrow_2">
			<label for="status"><s:text name="app.label.lcase.status"/></label>
			<s:select name="formForm.status" id="fStatus" list="#session.xformstatus" listKey="id" listValue="shortName" />
			<s:hidden name="formForm.statusHidden"/>
		</div>
		
		<div class="formrow_2"></div>
	
		<div class="formrow_2" id="nonpatientformtypeTR" style="display:none">
			<label for="formtype"><s:text name="form.forms.formInfo.FormTypeDisplay"/></label>
			<s:select name="formForm.nonpatientformtypeid" onchange="nonPatientFormTypeChange()" id="fnonpatientformtypeid" 
				list="#session.formtypes" listKey="id" listValue="shortName" />
		</div>

		<div class="formrow_2" id="dataStructureNameTR" >
			<label for="dsName"><s:text name="form.forms.formInformation.formDataStructure"/></label>
			<div id="dStructureName" class="formrowinput"></div>
		</div>
	</div>

	<h3 align="left" class="toggleable collapsed" ><s:text name="form.forms.formInfo.formFormattingDisplay"/></h3>
	<div id="formFormattingDiv">

		<div class="formrow_2 allowWrap">
			<label for="formheader"><s:text name="form.formatting.formheader"/></label>
			<s:textarea id="fHeader" rows="6" name="formForm.formHeader"/>
		</div>
		<div class="formrow_2">
			<label for="formborder"><s:text name="form.formatting.formborder"/></label>
			<s:radio name="formForm.formborder" id="fBorderYes" list="#{'yes':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.formborder" id="fBorderNo" list="#{'no':''}" /><s:text name="form.formatting.no"/>
		</div>
	
		<div class="formrow_2">
			<label for="formnamefont"><s:text name="form.formatting.formnamefont"/></label>
			<s:select id="fFont" name="formForm.formfont" list="#{'arial':'Arial', 'helvetica':'Helvetica', 
				'courier':'Courier', 'times':'Times', 'avant guard':'Avant Guard', 'lucida sans':'Lucida Sans'}" />
		</div>
	
		<div class="formrow_2">
			<label for="formfontsize"><s:text name="form.forms.formInfo.formFontSizeDisplay"/></label>
			<s:select id="fSize" name="formForm.fontSize" list="{'8','9','10','11','12','13','14','15','16','17','18'}" />
		</div>
		<div class="formrow_2">
			<label for="formnamecolor"><s:text name="form.formatting.formnamecolor"/></label>
			<s:select id="fColor" name="formForm.formcolor" list="#{'black':'Black', 'red':'Red', 
									'green':'Green', 'yellow':'Yellow', 'blue':'Blue', 'purple':'Purple'}" />
		</div>
		<div class="formrow_2">
			<label for="formfooter"><s:text name="form.formatting.formfooter"/></label>
            <s:textarea id="fFooter" rows="6" name="formForm.formFooter"/>
		</div>

		<div class="formrow_2">
			<label for="formelementpadding"><s:text name="form.forms.formInfo.formElementPaddingisplay"/></label>
			<s:select id="cPadding" name="formForm.cellpadding" list="{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}" />
		</div>
	</div>

	<h3 align="left" class="toggleable collapsed" ><s:text name="form.forms.formInfo.sectionFormattingDisplay"/></h3>
	<div id="sectionFormattingDiv">
		<div class="formrow_1" >
			<label for="sectionborder"><s:text name="form.formatting.sectionborder"/></label>
			<s:radio name="formForm.sectionborder" id="sBorderYes" list="#{'yes':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.sectionborder" id="sBorderNo" list="#{'no':''}" /><s:text name="form.formatting.no"/>
		</div>

		<div class="formrow_1" >
			<label for="sectionnamefont"><s:text name="form.formatting.sectionnamefont"/></label>
			<s:select id="sFont" name="formForm.sectionfont" list="#{'arial':'Arial', 'helvetica':'Helvetica',
					'courier':'Courier', 'times':'Times', 'avant guard':'Avant Guard', 'lucida sans':'Lucida Sans'}" />
		</div>

		<div class="formrow_1" >
			<label for="sectionnamecolor"><s:text name="form.formatting.sectionnamecolor"/></label>
			<s:select id="sColor" name="formForm.sectioncolor" list="#{'black':'Black', 'red':'Red', 
					'green':'Green', 'yellow':'Yellow', 'blue':'Blue', 'purple':'Purple'}" />
		</div>

		<div class="formrow_2">
			<label></label>
		</div>
	</div>
	<br>

	<div id="submitDiv">
	<table align="right">
		<tr>
			<td>
				<input id="createFormSubmit" type="submit" value="<s:text name='button.Save'/>" onclick="doAddEditFormInfoAjaxPost()" title="Click to save changes"/>
		 	</td>
			<td>
				<input type="button" value="<s:text name='button.Reset'/>" onclick="resetFormInfo()" title="Click to clear fields"/>
		 	</td>
	    	<td>
				<input type="button" value="<s:text name='button.Cancel'/>" onclick="cancel()" title="Click to cancel(changes will not be saved)."/>
	    	</td>
		</tr>
	</table>
	</div>
</s:form>
</div>

<script type="text/javascript">
	$(document).ready(function() {
		initialize('<%= request.getAttribute("formMode") %>',
				'<%= request.getAttribute("_form_nonPatientForm") %>',
				'<%= request.getAttribute("isCopyright") %>');
		
		//Allow multiple collection instances on single key entry
		 $('#singleKeyRadio').click(function(){
			 $('#multipleYey').attr('disabled',false);
			 $('#multipleNay').attr('disabled',false);
		});
		//Do not allow multiple collection instances on single key entry
		/*$('#doubleKeyRadio').click(function(){
			$('#multipleNay').attr('checked',true);
			$('#multipleYey').attr('disabled',true);
			$('#multipleNay').attr('disabled',true);
		}); */
	});
</script>

<jsp:include page="/common/footer_struts2.jsp" />
</html>