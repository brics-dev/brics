<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.Version"%>
<%@ page import="gov.nih.nichd.ctdb.form.form.FormForm"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewforms"/>

<%
	FormForm formForm = (FormForm) request.getAttribute("formForm");
	String action = formForm.getAction();
	request.setAttribute("action", action);
%>

<html>

<s:set var="pageTitle" scope="request">
	<s:text name="form.addform.formdetail" />
</s:set>

<%-- Include popUp Header --%>
<jsp:include page="/common/popUpHeader_struts2.jsp" />

<script src="<s:property value='#webRoot'/>/common/js/template.js" type="text/javascript"></script>
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/conversionTable.js"></script>
<link rel="stylesheet" type="text/css" href="<s:property value="#systemPreferences.get('app.stylesheet')"/>">
<link rel="stylesheet" type="text/css" href="<s:property value='#webRoot'/>/common/css/dataCollection.css">

<script type="text/javascript">

	$(document).ready(function() {
		// Disable inputs
		$('.repeatButton').attr('disabled',true);
		$("input, textarea, select").prop("disabled", true);
		$("a, img").prop("tabindex", "-1");
		
		// Additional style sheet config
		var ssheets = document.styleSheets;
		
		if (ssheets[0].rules) {
		     rules = ssheets[0].rules;
		}
		else {
		    rules = ssheets[0].cssRules;
		}
		
	    for (var i = 0; i < rules.length; i++) {
		    if (rules[i].style.width == '450px' && rules[i].style.zIndex == '500') {
	            rules[i].style.width = '590px';
	        }
	    }
	});

</script>

<s:if test="%{param.action == 'view_form'}">
	<script type="text/javascript">
  		function checkButtons() {
    		void(0);
		}
	</script>
</s:if>
 
 <body>
 
 <s:if test="%{param.source == 'sectionhome'}">
    <style type="text/css">
        .textWelcome {
        	font-family: Arial; 
        	font-size: 12pt; 
        	font-style:normal; 
        	line-height: normal; 
        	font-weight:bold; 
        	font-variant:normal; 
        	color:#006600;
        }
        
        .pageTitle {
        	font-family:Arial; 
        	font-size:19pt; 
        	color:#006600; 
        	font-weight:bold;
        }
    </style>
</s:if>
 


<s:if test="%{param.showVersion == 'true'}">
    <span class="textWelcome"> Form Version : <%=new Version (Integer.parseInt ((String)request.getAttribute("formVersion")))%> </span>
</s:if>
	
<!-- <div id="overlay"> -->
	<div id="viewModeBanner">
		<div id="viewModeBannerText">
			V&nbsp;&nbsp;I&nbsp;&nbsp;E&nbsp;&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;M&nbsp;&nbsp;O&nbsp;&nbsp;D&nbsp;&nbsp;E
		</div>
	</div>
<!-- </div> -->
<br/><br/>
<!-- Add section for Action or IBIS Messaging messages -->
<jsp:include page="/common/messages_struts2.jsp" />

<div id="divdataentryform" style="display: block">
	<s:property value="#request.formdetail" escapeHtml="false" />
</div>
</body>
<script type="text/javascript">
// try to copy background colors from children (text blocks) to the parent cell
$(".questionTR td").each(function() {
	var $this = $(this);
	$this.find("span").each(function() {
		var childColor = $(this).css("background-color");
		if (childColor != "transparent") {
			$this.css("background-color", childColor);
		}
	});
});
</script>
</html>