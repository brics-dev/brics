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

<%-- CHECK PRIVILEGES --%>
<security:check privileges="doublekeyresolution,dataentry,dataentryoversight,externaldataimport"/>

<% AdministeredForm aForm = (AdministeredForm)session.getAttribute("ResponseHomeFormForPrint"); %>
  
<html>
<s:set var="pageTitle" scope="request">
	View Entry Page
</s:set>
	
<%-- Include popUp Header --%>
<jsp:include page="/common/popUpHeader_struts2.jsp" />

<script src="<s:property value='#webRoot'/>/common/js/template.js" type="text/javascript"></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/conversionTable.js"></script>

<link rel="stylesheet" type="text/css" href="<s:property value="#systemPreferences.get('app.stylesheet')"/>">
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/dataCollection.css">

<script type="text/javascript">

	$(document).ready(function(){

		// added by Ching Heng for hide replace file filed
		$('.repeatButton').attr('disabled',true);
		$("input,textarea,select").prop("disabled", true);
		$("a,img").prop("tabindex", "-1");
		
		//loadQuestionGraphics();

		
	});
	
	function downloadQuestionFile(fileID,formID){
		url = "<s:property value="#webRoot"/>/attachments/download.action?id="+fileID+"&associatedId="+formID+"&typeId=<%=AttachmentManager.FILE_COLLECTION%>";
		redirectWithReferrer(url);
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
