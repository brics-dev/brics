<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.attachments.manager.AttachmentManager,
				 gov.nih.nichd.ctdb.response.domain.AdministeredForm,
                 gov.nih.nichd.ctdb.response.common.ResponseConstants,
                 gov.nih.nichd.ctdb.common.StrutsConstants,
                 gov.nih.nichd.ctdb.security.domain.User,
                 java.util.Set,
                 java.util.TreeSet" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="doublekeyresolution,dataentry,dataentryoversight,externaldataimport"/>

<% AdministeredForm aForm = (AdministeredForm)session.getAttribute("ResponseHomeFormForPrint"); 

	String hiddenSectionsQuestionsPVsElementIdsJSON = (String) session.getAttribute(StrutsConstants.HIDDENIDS); 
	
	boolean isEformConfigured = false;
	if (request.getAttribute(CtdbConstants.IS_EFORM_CONFIGURED) != null) {
		isEformConfigured = (Boolean)request.getAttribute(CtdbConstants.IS_EFORM_CONFIGURED);
	}

%>
 
 <script type="text/javascript">
 <%-- on load of form, if eform is configurd that elements are hidden--%>	
 var areElementsHidden = true; 
 </script>
<html>
<s:set var="pageTitle" scope="request">
	View Entry Page
</s:set>
	
<%-- Include popUp Header --%>
<jsp:include page="/common/popUpHeader_struts2.jsp" />

<script src="<s:property value='#webRoot'/>/common/js/template.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/js/ibisCommon.js" type="text/javascript"></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/conversionTable.js"></script>

<link rel="stylesheet" type="text/css" href="<s:property value="#systemPreferences.get('app.stylesheet')"/>">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/dataCollection.css">


<body>
	
	<div id="viewModeBanner">
		<div id="viewModeBannerText">
			V&nbsp;&nbsp;I&nbsp;&nbsp;E&nbsp;&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;M&nbsp;&nbsp;O&nbsp;&nbsp;D&nbsp;&nbsp;E
		</div>
	</div>
	
	<br><br><jsp:include page="/common/messages_struts2.jsp" />

<br/><br/>

<table style="width: 100%">

	<s:if test="%{#session.dataentryheader != null}">
		<jsp:include page="/response/dataEntryHeader.jsp" />
	</s:if>
	
	<%
	if(isEformConfigured) {
	%>
			<security:hasProtocolPrivilege privileges="viewConfigureEform,editConfigureEform">
				<div class="floatRight">	
					<input type="button" id="showHideConfig"  value="Show hidden eForm elements" onclick="showHideEformConfig()" />
				</div>
			</security:hasProtocolPrivilege>
	<%
			}
	%>
    
    <tr>
        <td>
        	<h3></h3>
			<div id="divdataentryform" style="display: block">
				<s:property value="#request.formdetail" escapeHtml="false" />
			</div>
        </td>
    </tr>
</table>
</body> 
</html>


<script type="text/javascript">

	$(document).ready(function(){

		// added by Ching Heng for hide replace file filed
		$('.repeatButton').prop('disabled',true);
		$("input,textarea,select").prop("disabled", true);
		$("a,img").prop("tabindex", "-1");
		$("#showHideConfig").prop('disabled',false);
		
		//loadQuestionGraphics();
		
		/*CISTAR-641:set table cell width to be 0 if no text in it*/
		$(".questionTextContainerTd").each(function(element) { 
			 var $this = $(this); 
			 var tblCellTxt = $this.find(".questionTextImmediateContainer").text();
			 if (tblCellTxt.length == 0) { 
				 $this.width(0); 
			 } else if ($.trim(tblCellTxt).length == 0) { 
				 //contains white spaces only, then reset width in percentage
				 var widthPercent = tblCellTxt.length / $this.parent().width() * 100 + "%";
				 $this.width(widthPercent);  
			 }   
		});
		
	});
	
	function downloadQuestionFile(fileID,formID){
		url = "<s:property value="#webRoot"/>/attachments/download.action?id="+fileID+"&associatedId="+formID+"&typeId=<%=AttachmentManager.FILE_COLLECTION%>";
		redirectWithReferrer(url);
	}
	
	
	
	

	//PSR forms can be configured to hide certain sections (along with its questions) and/or certain questions within sections
	//so lets hide them here
	var idlist = JSON.parse('<%= hiddenSectionsQuestionsPVsElementIdsJSON %>');
	for(var i=0;i<idlist.length;i++) {
		var elementId = idlist[i];
		var $hideElement = $("[hideid='" + elementId + "']");
		$hideElement.hide();
		$hideElement.addClass("eformConfigureHidden");
		
		
		//handle repeatables
		if (elementId.indexOf("questionContainer") === -1) {
			//this is a section
			//no need to hide the child sections since we are hiding the repeat button:
			//hide repeat button
			
			var $repeatButton = $hideElement.parents("tr").first().next("tr").find(".repeatButton");
			$repeatButton.removeClass("repeatButton");
			$repeatButton.addClass("eformConfigureHidden");
			$repeatButton.hide();
			
		}else {
			//this is a question or pv
			var isQuestion = true;
			var sectionId;
			var questionId;
			var pvId;
			
			if(elementId.split("_").length - 1 == 3) { //handles pvs
				isQuestion = false;
				var s_q_p = elementId.substring(elementId.indexOf("questionContainer")+18, elementId.length);
				sectionId = s_q_p.substring(0,s_q_p.indexOf("_"));
				var q_p = s_q_p.substring(s_q_p.indexOf("_") + 1 ,s_q_p.length);
				questionId = q_p.substring(0,q_p.indexOf("_"));
				pvId = q_p.substring(q_p.indexOf("_") + 1, q_p.length);
				
			
			}else  { //handles questions
				sectionId = elementId.substring(elementId.indexOf("questionContainer")+18, elementId.lastIndexOf("_"));
				questionId = elementId.substring(elementId.lastIndexOf("_")+1, elementId.length);
			}
			
			//hide children corresponding questions and add eformConfigureHidden class 
			var $childRepeatables = $('[parent="' + sectionId + '"]');
			$childRepeatables.each(function(index) {
				$this = $(this);
				var childId = $this.attr("id");
				var childElementId;
				
				if(isQuestion) {
					childElementId = "questionContainer_" + childId + "_" + questionId;
				}else {
					childElementId = "questionContainer_" + childId + "_" + questionId + "_" + pvId;
				}
				
				var $childElement = $this.find(("[hideid='" + childElementId + "']"));
				$childElement.hide();
				$childElement.addClass("eformConfigureHidden");
			});

		}
	}
	
/* 	function loadQuestionGraphics() {

		$(".imgThumb").each(function(){
		   var str = $(this).attr("src");
		   var splits = str.split('/');
		   var qId = splits[0];
		   var filename = splits[1];
		   var image = $(this);

		   
		   var url = "<s:property value="#webRoot"/>/response/dataCollection.action?action=getQuestionGraphic&qId=" + qId + "&filename=" + filename;


		   
		   $.ajax({
		  		type: "get",
		  		url: url,
		  		success: function(response) {
					image.attr("src",response);
		  		},
		  		error: function(e) {
		  			alert("error&&&" + e );
		  		}
		  	});
		   

		});
	} */
		
</script>
