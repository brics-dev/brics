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


<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditforms"/>

<script type="text/javascript">

$(document).ready(function() {  
	if ($('input[name=formForm.tabdisplay]:checked').val()=='true') {
		$('#sectionCollapsible').attr('disabled','true');
	}
});

function changeRepeatableGroupName() {

	if ($('#activeSectionId').val() == "") {
		//means we are in Add Section..so they can do whatever they want
		populateRepeatableGroupInfoDialog();
	} else {
		if (confirm("Changing the repeatable group will clear all data element associations to questions already attached to this section. Do you wish to continue?")) {
			populateRepeatableGroupInfoDialog();
			
		} else {
			//need to reset to old settings
			var sectionIndex = SectionHandler.findSectionIndex(activeSId);
			var section = sectionsArray[sectionIndex];
			var rGroupName = section.repeatableGroupName;
			$('#repeatableGroupSelect').val(rGroupName);
			populateRepeatableGroupInfoDialog();
		}
	}
}


function populateRepeatableGroupInfoDialog() {

	var selectedRGroup = $('#repeatableGroupSelect').val();
	
	if (selectedRGroup == "None") {
		//hide info icon
		$('#opener').attr("style","display:none");
		$("#repeatableGroupSelect").width(180);
	} else {
		//show info icon
		$('#opener').attr("style","display:block");
		$("#repeatableGroupSelect").width(170);
		
		var rgName;
		var thresh;
		var type;
		
		for (var i=0;i<DSObj.repeatableGroupsArr.length;i++){
			var rg = DSObj.repeatableGroupsArr[i];
			rgName = rg.repeatableGroupName;

			if (selectedRGroup == rgName) {
				thresh = rg.repeatableGroupThreshold;
				type = rg.repeatableGroupType;
				break;
			}
		}
		
		var topTable = "<table>" + 
							"<tr><th align='left' colspan='2'><font color='red'>General Details</font></th></tr>" +
							"<tr><td align='left'><b>Repeatable Group Name:</b></td><td align='left'>&nbsp;&nbsp;" + rgName + "</td></tr>" +
							"<tr><td align='left'><b>Number of Times Repeated:</b></td><td align='left'>&nbsp;&nbsp;" + type + "</td></tr>" +
							"<tr><td align='left'><b>Threshold:</b></td><td align='left'>&nbsp;&nbsp;" + thresh + "</td></tr>" +
						"</table>";
						 
		var tdRows = "";					 
		for (var i=0; i<DSObj.dataElementsArr.length; i++) {
			var DEObj=DSObj.dataElementsArr[i];
			var deFullName = DEObj.dataElementName;
			var index=deFullName.indexOf("."); 
			var rGroup = deFullName.substring(0,index);
			var deName = deFullName.substring(index+1);
			
			if (rGroup == selectedRGroup) {
			  	var order = DEObj.order;
			  	var title = DEObj.title;
			  	var desc = DEObj.description;
			  	var req = DEObj.requiredType;
			  	tdRows = tdRows + "<tr><td align=\"left\">" + order + "</td><td align=\"left\">" + title + "</td><td align=\"left\">" + desc + "</td><td align=\"left\">" + deName + "</td><td align=\"left\">" + req + "</td></tr>";
			}
		}
						 
		var bottomTable = "<h3 align='left' id='rGroupPopupH'>Data Elements Included</h3>" + 
		   	"<div id='rGroupPopupDiv' class='dataTableContainer'>" + 
		   		"<table id='rGroupDataElementTable'>" +
					"<thead>" +
	   					"<tr class='tableRowHeader'><th id='orderH' class='tableCellHeader'>Order</th><th class='tableCellHeader'>Title</th><th class='tableCellHeader'>Short Description</th><th class='tableCellHeader'>Variable Name</th><th class='tableCellHeader'>Required?</th></tr>" + 
					"</thead>" +
					"<tbody>" + tdRows + "</tbody>" +
	   			"</table>" +
	  		"</div>";
	   					
		var popupHtml = "<div>" +  topTable +  bottomTable + "<div style=\"clear:both\"><!-- ie fix --></div></div>";
		$('#repGroupInfoDialog').html(popupHtml);
	}
}


function populateRepeatableGroupNameSelect() {

	var activeSId = $('#activeSectionId').val();
	var repeatableGroupNamesInnerHTML = "<option value='None'>None</option>";
	
	for (var i=0; i<DSObj.repeatableGroupNamesArr.length; i++){
		var repeatableGroupName=DSObj.repeatableGroupNamesArr[i];

		//need to filter out ones that have already been associated
		var alreadyAssociated = false;
		for (var j=0;j<sectionsArray.length;j++) {
			if (sectionsArray[j].isRepeatable) {
				
				var sectionId = sectionsArray[j].id;
				var rGroupName = sectionsArray[j].repeatableGroupName;
				if (repeatableGroupName == rGroupName) {
					if (sectionId != activeSId) {
						alreadyAssociated = true;
					}
					break;
				}
			}
		}
			
		if (!alreadyAssociated) {
			var repeatableGroupNamesOption = '<option value="' + repeatableGroupName + '">' + repeatableGroupName + '</option>';
			repeatableGroupNamesInnerHTML = repeatableGroupNamesInnerHTML + repeatableGroupNamesOption;
		}
	}
	$('#repeatableGroupSelect').html(repeatableGroupNamesInnerHTML);
}


function showHideRepeatableOptions() {
	var activeSId = $('#activeSectionId').val();
	
	if (activeSId != "") {
		if (confirm("Changing the repeatable group will clear all data element associations to questions already attached to this section. Do you wish to continue?")) {

		} else {
			//need to reset to old settings
			var sectionIndex = SectionHandler.findSectionIndex(activeSId);
			var section = sectionsArray[sectionIndex];
			var isRepeatable = section.isRepeatable;
			var rGroupName = section.repeatableGroupName;
			var initRepeatedSecs = section.initRepeatedSecs;
			var maxRepeatedSecs = section.maxRepeatedSecs;
			
			if (isRepeatable) {
				$('#sectionRepeatbale').attr('checked','checked');
				$('#repeatableGroupSelect').val(rGroupName);
				$('#initialRepeated').val(initRepeatedSecs);
				$('#maximumRepeated').val(maxRepeatedSecs);
				
			} else {
				$('#sectionRepeatbale').removeAttr('checked');
			}
		}	
	}

	if ($('#sectionRepeatbale').is(':checked')) {
		$('#initRepeatedTR').attr("style","display:block");
		$('#maxRepeatedTR').attr("style","display:block");
		$('#repeatableGroupTR').attr("style","display:block");
		populateRepeatableGroupNameSelect();
		populateRepeatableGroupInfoDialog(); 

	} else {
		$('#initRepeatedTR').attr("style","display:none");
		$('#maxRepeatedTR').attr("style","display:none");
		$('#repeatableGroupSelect').html("");
		$('#repeatableGroupTR').attr("style","display:none");
	}
}

</script>

<script>

$("#repGroupInfoDialog").dialog({ 
		width : "945px", 
		title :"Data Elements", 
		autoOpen : false, 
		open : function() {
			IbisDataTables.fullBuild($("#repGroupInfoDialog" ).find("#rGroupDataElementTable"));
		} 
});

$("#opener").click( function() {
		$("#repGroupInfoDialog").dialog("open");
});

</script>

<p class="leftAlign">
	<s:text name="form.addsection.instruction"/><br/>
	<label class="requiredInput"></label> 
	<i><s:text name="form.addsection.requiredSumbol"/></i><br>
</p>

<div id="errorContainer2" style="display:none"></div><br>

<div style="width: 760px">
	<div class="formrow_2">
		<label for="sectionName" class="requiredInput"><s:text name="form.addsection.sectionname"/></label>
		<input type="text" id="sectionName" maxlength="128" size="35"  />
	</div>
	
	<div class="formrow_2">
		<label for="sectionCollapsible"><s:text name="form.addsection.collapsible"/></label>
		<input type="checkbox" id="sectionCollapsible"/>
	</div>

	<div class="formrow_2 allowWrap">
		<label for="sectionDescription"><s:text name="form.addsection.sectiontext"/></label>
		<textarea id="sectionDescription" maxlength="4000" rows="5" cols="35"></textarea>
	</div>

	<div class="formrow_2" style="display:none">
		<label for="sectionImage"><s:text name="form.addsection.responseimage"/></label>
		<input type="checkbox" id="sectionImage"/>
	</div>

	<div class="formrow_2 allowWrap">
		<label for="sectionRepeatbale"><s:text name="form.addsection.repeatable"/></label>
		<input type="checkbox"  id="sectionRepeatbale" onchange="showHideRepeatableOptions()" />
	</div>
	
	<div class="formrow_2"></div>
	<div class="formrow_2"></div>

	<div class="formrow_2" id="initRepeatedTR"  style="display:none">
		<label for="initialRepeated"><s:text name="form.addsection.initialRepeated"/></label>
		<input type="text" id="initialRepeated" size="5" />
	</div>
	
	<div class="formrow_2"></div>

	<div class="formrow_2" id="maxRepeatedTR" style="display:none">
		<label for="maximumRepeated"><s:text name="form.addsection.maximumRepeated"/></label>
		<input type="text" id="maximumRepeated" size="5" />
	</div>
	
	<div class="formrow_2"></div>

	<div class="formrow_2" id="repeatableGroupTR" style="display:none">
		<label for="repeatableGroupSelect"><s:text name="form.addsection.repeatablegroupname"/></label>
		<select id="repeatableGroupSelect" onchange="changeRepeatableGroupName()"></select>
		<a href="javascript:;" id="opener" style="display:none;float:left"><img src="../images/icons/info-icon.png" alt="open the dialog"></a>
	</div>

	<div class="formrow_1">
		<label for=""></label>
		<input type="button" value="<s:text name='button.Cancel'/>" onclick="cancelAddEditSection()" id="cancelAddEditSection" title = "Click to cancel (changes will not be saved)."/>
		<input type="button" class="singleClick" value="<s:text name='button.Add'/>" onclick="addSection()" id="addSectionAddButton" title = "Click to add section"/>
		<input type="button" class="singleClick" value="<s:text name='button.Save'/>" onclick="SectionHandler.editSectionCallback()" id="addSectionEditButton" style="display: none;" title = "Click to make changes"/>
	</div>
</div>

<div id="repGroupInfoDialog" title="Dialog Title"></div>


