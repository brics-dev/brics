<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol,
                 gov.nih.nichd.ctdb.form.domain.Form,
                 gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType,
                 gov.nih.nichd.ctdb.common.*"%>
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<% Locale l = request.getLocale(); %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditforms"/>

<script type="text/javascript">

var formMode;
var formFooterFCK;
var formHeaderFCK;

var originalDataStructureName = "";

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

function initialize(fMode, _form_nonPatientForm, webserviceException) {
	
	if (webserviceException == "webserviceException") {
		$("#errorContainer").html("");
		$.ibisMessaging(
				"primary", 
				"error", 
				"There was a problem with retrieving Form Structures from the web service",
				{
					container: "#errorContainer"
				});
		$("#errorContainer").show();
		
		if (fMode == "edit") {
			$('#createFormSubmitEdit').attr("disabled", true);
			$('#createFormSubmitBack').attr("disabled", true);
			$('#eReset').attr("disabled", true);
		} else {
			$('#createFormSubmitSave').attr("disabled", true);
			$('#resetBtn').attr("disabled", true);
		}
		$('#createFormSubmitSave2').attr("disabled", true);
		$('#eReset2').attr("disabled", true);
	}

	var formName = document.getElementById("formName");
	formMode = fMode;

	if (_form_nonPatientForm == "true") {
		formName.selectedIndex = "1";	//meaans patient form
	} else {
		formName.selectedIndex = "0";
	}
	
	var nonpatientformtypeidObj = document.getElementsByName("formForm.nonpatientformtypeid")[0];
	for (var i=0; i<nonpatientformtypeidObj.length; i++) {
		var nonpatientformtypeidObjVal = nonpatientformtypeidObj.options[i].value;
		if (nonpatientformtypeidObjVal == $('#formtypeid').val()) {
			nonpatientformtypeidObj.selectedIndex = i;
			break;
		}
	}
	
	toggleFormType();
	
	//if we are in edit mode, then we need to disale changing the form type
	if (formMode == "edit") {
		originalDataStructureName = $('#dataStructureName').val() + "(" + $('#dataStructureVersion').val()+ ")";
		
		var formDataStructuresDiv = document.getElementById("formDataStructures");
		var table = formDataStructuresDiv.getElementsByTagName("table")[0];
		var $table = $(table);
		
		var dsName = $('#dataStructureName').val();
		if (dsName != "") {   
			//if coming from imported form, the form has no form structure assoc to it...so there is nothing to select
			dsName = $('#dataStructureName').val() + "(" + $('#dataStructureVersion').val()+ ")";

			var $inputs = IDT.getAllInputs($table);
//			if ($inputs.filter($('[value="'+dsName+'"]')).length > 0) {
//				IDT.addSelectedOptionValue($table, dsName);
//			}
			// the method above did not find some dsNames so we have to do the below
			var foundMatch = false;
			$inputs.each(function() {
			    if ($(this).val() == dsName) {
			        foundMatch = true;
			        return;
			    }
			});
			if (foundMatch) {
				IDT.addSelectedOptionValue($table, dsName);
			}
			IDT.dataTables[$table.attr("id")].fnDraw();
		}
		
		var formName = document.getElementById("formName");
		var copyrightY = document.getElementsByName("formForm.copyRight")[0];
		var copyrightN = document.getElementsByName("formForm.copyRight")[1];
		
		if (navigator.userAgent.indexOf('MSIE') > 0){
			formName.disabled = "disabled";
			//copyrightY.disabled = "disabled";
			//copyrightN.disabled = "disabled";
		} else {
			formName.disabled = true;
			//copyrightY.disabled = true;
			//copyrightN.disabled = true;
		}
		
		var dStructureName = document.getElementById("dStructureName");
		if ($('#dataStructureName').val() != "") {
			dStructureName.innerHTML = originalDataStructureName
		} else {
			dStructureName.innerHTML = "None";
		}
		
		var dataStructureNameTR = document.getElementById("dataStructureNameTR");
		dataStructureNameTR.style.display="block";
		
		//get all the info for edit mode so we can reset back to this
		eFormNameIndex = formName.selectedIndex;
		eFormTypeId = $('#formtypeid').val();
		eName = $('#fname').val();
		eDescription = $('#fdescription').val();
		
		var status = document.getElementsByName("formForm.status")[0];
		var statusHidden = document.getElementsByName("formForm.statusHidden")[0];
		statusHidden.value = status.value;
		eStatusIndex = status.selectedIndex;
		
		if (navigator.userAgent.indexOf('MSIE') > 0) {
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
		
		//if double entry is checked disable allow multiple collections instances radio and default to no
		/*if ($('input[name=formForm.dataEntryFlag]:radio:checked').val()==2){
			$('#multipleNay').attr('checked',true);
			$('#multipleYey').attr('disabled',true);
			$('#multipleNay').attr('disabled',true);
		}*/
	}
}


function resetFormInfo() {
	
	if (formMode == "edit") {

		$('#formtypeid').val(eFormTypeId);

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
		
		var copyright0 = document.getElementsByName("formForm.copyRight")[0];
		var copyright1 = document.getElementsByName("formForm.copyRight")[1];
		copyright0.checked = eCopyright0;
		copyright1.checked = eCopyright1;
		
		var formHeader = document.getElementsByName("formForm.formHeader")[0];
		formHeader.value=eFormHeader;
		
		var formFooter = document.getElementsByName("formForm.formFooter")[0];
		formFooter.value=eFormFooter;

		var formborder0 = document.getElementsByName("formForm.formborder")[0];
		var formborder1 = document.getElementsByName("formForm.formborder")[1];
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
		
	} else {
		
		var formName = document.getElementById("formName");
		formName.selectedIndex="0";
		
		$('#formtypeid').val("10");  //this means patient form
		$('#fname').val("");  
		$('#fdescription').val("");  
		
		var status = document.getElementsByName("formForm.status")[0];
		status.selectedIndex="0";

		var nonpatientformtypeTR = document.getElementById("nonpatientformtypeTR");
		nonpatientformtypeTR.style.display = "none";
		var nonpatientformtypeid = document.getElementsByName("formForm.nonpatientformtypeid")[0];
		nonpatientformtypeid.selectedIndex="0";
		
		var tabdisplay0 = document.getElementsByName("formForm.tabdisplay")[0];
		var tabdisplay1 = document.getElementsByName("formForm.tabdisplay")[1];
		tabdisplay0.checked=false;
		tabdisplay1.checked=true;

		var accessFlag0 = document.getElementsByName("formForm.accessFlag")[0];
		var accessFlag1 = document.getElementsByName("formForm.accessFlag")[1];
		accessFlag0.disabled=false;
		accessFlag1.disabled=false;
		accessFlag0.checked=true;
		accessFlag1.checked=false;

		var dataEntryFlag0 = document.getElementsByName("formForm.dataEntryFlag")[0];
		dataEntryFlag0.disabled=false;
		dataEntryFlag0.checked=true;
		
		var copyright0 = document.getElementsByName("formForm.copyRight")[0];
		var copyright1 = document.getElementsByName("formForm.copyRight")[1];
		copyright0.disabled = false;
		copyright1.disabled = false;
		copyright0.checked = false;
		copyright1.checked = true;

		var formHeader = document.getElementsByName("formForm.formHeader")[0];
		formHeader.value="";
		
		var formFooter = document.getElementsByName("formForm.formFooter")[0];
		formFooter.value="";

		var formborder0 = document.getElementsByName("formForm.formborder")[0];
		var formborder1 = document.getElementsByName("formForm.formborder")[1];
		formborder0.checked=true;
		formborder1.checked=false;
		
		var sectionborder0 = document.getElementsByName("formForm.sectionborder")[0];
		var sectionborder1 = document.getElementsByName("formForm.sectionborder")[1];
		sectionborder0.checked=true;
		sectionborder1.checked=false;
		
		var formfont = document.getElementsByName("formForm.formfont")[0];
		formfont.selectedIndex="0";
		
		var fontSize = document.getElementById("fSize");
		fontSize.selectedIndex = "2";
		
		var formcolor = document.getElementsByName("formForm.formcolor")[0];
		formcolor.selectedIndex="0";
		
		var sectionfont = document.getElementsByName("formForm.sectionfont")[0];
		sectionfont.selectedIndex="0";
		
		var sectioncolor = document.getElementsByName("formForm.sectioncolor")[0];
		sectioncolor.selectedIndex="0";
		
		var cellpadding = document.getElementsByName("formForm.cellpadding")[0];
		cellpadding.selectedIndex="1";
	}
}


function toggleFormType() {
	
	var formName = document.getElementById("formName");
	var selected = formName.options[formName.selectedIndex].value;
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


function nonPatientFormTypeChange() {
	var nonpatientformtypeid = document.getElementsByName("formForm.nonpatientformtypeid")[0];
	var formTypeId = document.getElementsByName("formForm.formtypeid")[0];
	var npSelected = nonpatientformtypeid.options[nonpatientformtypeid.selectedIndex].value;

	formTypeId.value=npSelected;
}

</script>

<body>
<%
	if (request.getAttribute("formMode").equals("create")) {
%>
		<div id="errorContainer" style="display:none"></div>
		<p class="leftAlign">
			<s:text name="form.forms.formInfo.enterFormInfo.instruction"/> <br/>
			<label class="requiredInput" align="left"></label> 
            <i class="leftAlign"><s:text name="form.forms.formInfo.enterFormInfo.requiredSymbol"/></i>
		</p>  
<%
	} else {
%>
	<div id="errorContainer" style="display:none"></div>
	<p class="leftAlign">
		<s:text name="form.forms.formInfo.editFormInfo.instruction"/><br/>
		<label class="requiredInput" align="left"></label> 
		<i class="leftAlign"><s:text name="form.forms.formInfo.enterFormInfo.requiredSymbol"/></i>
	</p>
<%
	}
%>

<div id="createEditForm">
	<s:form onsubmit="return false" theme="simple">
		<s:hidden name="formForm.formtypeid" id="formtypeid" />
		<s:hidden name="formForm.formid" id="formid" />
		<s:hidden name="formForm.id"/>
		<s:hidden name="formForm.dataStructureName" id="dataStructureName"/>
		<s:hidden name="formForm.dataStructureVersion" id="dataStructureVersion"/>
			
		<div id="fdsDiv">
		
		<% if (request.getAttribute("formMode").equals("create")) { %>
				<h3 align="left" class="toggleable expanded" id="fdsH3" >
					<label class="requiredInput"><s:text name="form.forms.formInformation.formDataStructure"/></label>
				</h3>
		<% } else { %>
				<h3 align="left" class="toggleable collapsed" id="fdsH3" >
					<label class="requiredInput"><s:text name="form.forms.formInformation.formDataStructure"/></label>
				</h3>
		<% } %>
			
			<div class="dataTableContainer" id="formDataStructures" style="display:block">
				<display:table name="dsList"  scope="request" decorator="gov.nih.nichd.ctdb.form.tag.CreateEditFormDecorator">
					<display:column property="dataStructureRadio" title="" />
					<display:column property="shortName" title='<%=rs.getValue("app.label.lcase.name",l)%>' />
					<display:column property="version" title='<%=rs.getValue("app.version",l)%>'  />
					<display:column property="description" title='<%=rs.getValue("form.formgroup.description.display",l)%>'  />
					<display:column property="isCopyrighted" title='<%=rs.getValue("form.isCopyrighted",l)%>' />
				</display:table>
			</div>
		</div>

	<% if (request.getAttribute("formMode").equals("create")) { %>
			<h3 align="left" class="toggleable collapsed" onclick="collapseFormStructureDiv()" id="fidH3"><s:text name="form.forms.formInfo.formInfoDisplay"/></h3>
	<% } else { %>
			<h3 align="left" class="toggleable expanded" id="fidH3"><s:text name="form.forms.formInfo.formInfoDisplay"/></h3>
	<% } %>

	<div id="formInformationDiv">

		<div class="formrow_2">
			<label class="requiredInput" for="formName"><s:text name="form.forms.formInfo.FormDisplay"/></label>
			<select id="formName" onChange="toggleFormType()">
				<option name="Patient">Subject</option>
				<option name="Non-Patient">Non-Subject</option>
	       	</select>
		</div>
	
		<div class="formrow_2 ">
			<label for="useTabDisplayDisplay"><s:text name="form.forms.formInfo.useTabDisplayDisplay"/></label>
			<s:radio name="formForm.tabdisplay" onclick="enabledisableRepeatable('true')" list="#{'true':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.tabdisplay" onclick="enabledisableRepeatable('false')" list="#{'false':''}" /><s:text name="form.formatting.no"/>
		</div>
	
		<div class="formrow_2">
			<label for="fname" class="requiredInput"><s:text name="app.label.lcase.name"/></label>
			<s:textfield name="formForm.name" maxlength="50" size="35" id="fname"/>
		</div>
	
		<div class="formrow_2">
			<label for="access"><s:text name="form.access.display"/></label>
			<s:radio name="formForm.accessFlag" list="#{'1':''}" /><s:text name="form.private.display"/>&nbsp;
			<s:radio name="formForm.accessFlag" list="#{'2':''}" /><s:text name="form.public.display"/>
		</div>
	
		<div class="formrow_2">
			<label for="description"><s:text name="app.label.lcase.description"/></label>
			<s:textarea rows="6" name="formForm.description" id="fdescription"/>
		</div>
	
		<div class="formrow_2 allowWrap">
			<label for="dataentry"><s:text name="app.label.lcase.dataentry"/></label>
			<s:radio name="formForm.dataEntryFlag" id="singleKeyRadio" list="#{'1':''}" /><s:text name="form.singlekey.display"/>&nbsp;
			<%-- <s:radio name="formForm.dataEntryFlag" id="doubleKeyRadio" list="#{'2':''}" /><s:text name="form.doublekey.display"/> --%>
		</div>
	
		<div class="formrow_2 allowWrap">
			<label for="CopyrightedForm"><s:text name="form.copyrightedForm" /></label>
			<s:if test="%{formForm.copyRight}">
				<s:radio name="formForm.copyRight" id="copyRightY" list="#{'true':''}" disabled="true" /><s:text name="form.formatting.yes"/>&nbsp;
				<s:radio name="formForm.copyRight" id="copyRightN" list="#{'false':''}" disabled="true" /><s:text name="form.formatting.no"/>
			</s:if>
			<s:else>
				<s:radio name="formForm.copyRight" id="copyRightY" list="#{'true':''}" /><s:text name="form.formatting.yes"/>&nbsp;
				<s:radio name="formForm.copyRight" id="copyRightN" list="#{'false':''}" /><s:text name="form.formatting.no"/>
			</s:else>
		</div>
	
		<div class="formrow_2 allowWrap" >
			<label for="allowmultiplecollectioninstances" > <s:text name="form.multiplecollectioninstances.display"/></label>
			<s:radio name="formForm.allowMultipleCollectionInstances" id="multipleYey" list="#{'true':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.allowMultipleCollectionInstances" id="multipleNay" list="#{'false':''}" /><s:text name="form.formatting.no"/>
			<a title="<s:text name="form.multiplecollectioninstances.display.tooltip" />"><img src="../images/icons/info-icon.png"></a> 
		</div> 
	
		<div class="formrow_2"></div>
		<div class="formrow_2 ">
			<label for="status"><s:text name="app.label.lcase.status"/></label>
			<s:select name="formForm.status" list="#session.xformstatus" listKey="id"  listValue="shortName" />
		</div>
		<s:hidden name="formForm.statusHidden"/>

		<div class="formrow_2"></div>

		<div class="formrow_2" id="nonpatientformtypeTR" style="display:none">
			<label for="formtype"><s:text name="form.forms.formInfo.FormTypeDisplay"/></label>
			<s:select name="formForm.nonpatientformtypeid" onchange="nonPatientFormTypeChange()" list="#session.formtypes" listKey="id" listValue="shortName" />
		</div>

		<div class="formrow_2 allowWrap" id="dataStructureNameTR" style="display:none">
			<label for="dsName"><s:text name="form.forms.formInformation.formDataStructure"/></label>
			<div id="dStructureName" class="formrowinput"></div>
		</div>
	</div>

	<h3 align="left" id="formFormattingToggle" class="toggleable collapsed"><s:text name="form.forms.formInfo.formFormattingDisplay"/></h3>
	<div id="formFormattingDiv">

		<div class="formrow_2">
			<label for="formheader"><s:text name="form.formatting.formheader"/></label>
			<s:textarea rows="6" id="formHeader" name="formForm.formHeader" />
		</div>

		<div class="formrow_2">
			<label for="formfooter"><s:text name="form.formatting.formfooter"/></label>
			<s:textarea rows="6" id="formFooter" name="formForm.formFooter"/>
		</div>

		<div class="formrow_2">
			<label for="formborder"><s:text name="form.formatting.formborder"/></label>
			<s:radio name="formForm.formborder" list="#{'yes':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.formborder" list="#{'no':''}" /><s:text name="form.formatting.no"/>
		</div>

		<div class="formrow_2">
			<label for="formnamefont"><s:text name="form.formatting.formnamefont"/></label>
			<s:select name="formForm.formfont" list="#{'arial':'Arial', 'helvetica':'Helvetica', 'courier':'Courier', 
					'times':'Times', 'avant guard':'Avant Guard', 'lucida sans':'Lucida Sans'}" />
		</div>

		<div class="formrow_2">
			<label for="formfontsize"><s:text name="form.forms.formInfo.formFontSizeDisplay"/></label>
			<s:select id="fSize" name="formForm.fontSize" list="{'8','9','10','11','12','13','14','15','16','17','18'}" />
		</div>

		<div class="formrow_2">
			<label for="formnamecolor"><s:text name="form.formatting.formnamecolor"/></label>
			<s:select name="formForm.formcolor" list="#{'black':'Black', 'red':'Red', 'green':'Green', 
					'yellow':'Yellow', 'blue':'Blue', 'purple':'Purple'}" />
		</div>

		<div class="formrow_2">
			<label for="formelementpadding"><s:text name="form.forms.formInfo.formElementPaddingisplay"/></label>
			<s:select name="formForm.cellpadding" list="{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}" />
		</div>
	</div>

	<h3 align="left" class="toggleable collapsed"><s:text name="form.forms.formInfo.sectionFormattingDisplay"/></h3>
	<div id="sectionFormattingDiv">
		<div class="formrow_2">
			<label for="sectionborder"><s:text name="form.formatting.sectionborder"/></label>
			<s:radio name="formForm.sectionborder" list="#{'yes':''}" /><s:text name="form.formatting.yes"/>&nbsp;
			<s:radio name="formForm.sectionborder" list="#{'no':''}" /><s:text name="form.formatting.no"/>
		</div>

		<div class="formrow_2">
			<label for="sectionnamefont"><s:text name="form.formatting.sectionnamefont"/></label>
			<s:select name="formForm.sectionfont" list="#{'arial':'Arial', 'helvetica':'Helvetica', 'courier':'Courier', 
					'times':'Times', 'avant guard':'Avant Guard', 'lucida sans':'Lucida Sans'}" />
		</div>

		<div class="formrow_2">
			<label for="sectionnamecolor"><s:text name="form.formatting.sectionnamecolor"/></label>
			<s:select name="formForm.sectioncolor" list="#{'black':'Black', 'red':'Red', 'green':'Green', 
					'yellow':'Yellow', 'blue':'Blue', 'purple':'Purple'}" />
		</div>

		<div class="formrow_2">
			<label></label>
		</div>
		
	</div>
	<br>

	<% if (request.getAttribute("formMode").equals("create")) { %>
		<div id="eDiv0" class="formrow_1" >
			<input type="button" value="<s:text name='button.Cancel' />" onclick="cancelAddEditFormInfo()" title="Click to cancel (changes will not be saved)."/>
			<input id="resetBtn" type="button" value="<s:text name='button.Reset' />" onclick="resetFormInfo()" title="Click to clear fields" />
			<input id="createFormSubmitSave" type="submit" value="<s:text name='button.Save' />" title="Click to save" onclick="doAddEditFormInfoAjaxPost('<%= request.getAttribute("formMode") %>','continue')" title="Click to save changes"/>
		</div>
		
	<% } else { %>
		<div id="eDiv1" class="formrow_1" style="display:none">
			<input type="button" value="<s:text name='button.Cancel' />" onclick="cancelAddEditFormInfo()" title="Click to cancel (changes will not be saved)."/>
			<input id="eReset2" type="button" value="<s:text name='button.Reset' />" onclick="resetFormInfo()" title="Click to clear fields" />
			<input id="createFormSubmitSave2" type="submit" value="<s:text name='button.Save' />" title="Click to save" onclick="doAddEditFormInfoAjaxPost('<%= request.getAttribute("formMode") %>','continue')" title="Click to save changes"/>
		</div>

		<div id="eDiv2" class="formrow_1" style="display:block">
			<input type="button" value="<s:text name='button.Cancel' />" onclick="cancelAddEditFormInfo()" title ="Click to cancel (changes will not be saved)."/>
			<input id="eReset" type="button" value="<s:text name='button.Reset' />" onclick="resetFormInfo()" title="Click to clear fields" />
			<input id="createFormSubmitEdit" type="submit"  value="<s:text name='form.UpdateAndEditLayout' />"  onclick="doAddEditFormInfoAjaxPost('<%= request.getAttribute("formMode") %>','continue')" title="Click to save and continue"/>
			<input id="createFormSubmitBack" type="submit"  value="<s:text name='form.UpdateAndGoBack' />"  onclick="doAddEditFormInfoAjaxPost('<%= request.getAttribute("formMode") %>','finish')" title="Click to save and return to previous page" />
		</div>
	<% } %>

	</s:form>
</div>

<script type="text/javascript">
	
	$(document).ready(function() {
		initialize('<%= request.getAttribute("formMode") %>','<%= request.getAttribute("_form_nonPatientForm") %>','<%= request.getAttribute("webserviceException") %>');
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
		});*/
	});
</script>
</body>