<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.Privilege" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Set"%>
<%@ page import="gov.nih.nichd.ctdb.form.domain.Section"%>
<%@ page import="gov.nih.nichd.ctdb.question.domain.Question"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<jsp:include page="/common/header_struts2.jsp" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/configurePSR.css">
<script type="text/javascript" src="<s:property value='#webRoot'/>/common/js/jquery.form.min.js"></script>


<html>
<%
	String eformName = (String)request.getAttribute(CtdbConstants.EFORM_NAME);
%>

<h3><s:text name="protocol.psr.eform.configure.title.display"/></h3>
<p><b><%= eformName %></b></p>
<br>

<%
	//write out the sections and questions along for the eform (but dont show required)
	ArrayList<Section> sectionList = (ArrayList)request.getAttribute(CtdbConstants.EFORM_SECTIONLIST);
	Iterator<Section> sectionIter = sectionList.iterator();
	while(sectionIter.hasNext()) {
		Section section = sectionIter.next();
		int sectionId = section.getId();
		String sectionName = section.getName();
		sectionName = sectionName.replaceAll("\\<[^>]*>","");
		String sectionIdText = section.getIdText();
		boolean isRepeatable = section.isRepeatable();
		int parent = section.getRepeatedSectionParent();
		boolean hasAnyRequiredQuestions = section.isHasAnyRequiredQuestions();	
%>
			<div id="<%=sectionIdText%>_div" class="sectionDiv" sid="<%= sectionId %>" isRepeatable="<%= isRepeatable %>" parent="<%= parent %>">
				<table class='configurePsr-table'>
<%
				if(hasAnyRequiredQuestions) {
%>
				<thead>
					<th ><div class='more thWrapper'><%= sectionName %></div></th>
					<th><input type="radio" checked="checked" disabled="true">Show</th>
					<th><input type="radio" value="hide" disabled="true">Hide</th>
				</thead>
				<tbody>

<%
					
				}else {
%>

				<thead>
					<th ><div class='more thWrapper'><%= sectionName %></div></th>
					<th><input type="radio" class="showHideSelector sectionRadio" name="<%= sectionIdText%>" id="<%= sectionIdText%>_show" value="show" onclick="enableSectionQuestions('<%=sectionIdText%>_div','<%= sectionId %>','<%= isRepeatable %>','<%= parent %>','false')"  checked="checked">Show</th>
					<th><input type="radio" class="showHideSelector sectionRadio"  name="<%= sectionIdText%>" id="<%= sectionIdText%>_hide" onclick="disableSectionQuestions('<%=sectionIdText%>_div','<%= sectionId %>','<%= isRepeatable %>','<%= parent %>','false')"  value="hide">Hide</th>
				</thead>
				<tbody>

<%
					
				}
%>
				
<%	
				List<Question> questionList = section.getQuestionList();
				Iterator<Question> questionIter = questionList.iterator();
				while(questionIter.hasNext()) {
					Question question = questionIter.next();
					String questionText = question.getText();
					String questionIdText = question.getIdText();
					int questionId = question.getId();
					questionText = questionText.replaceAll("\\<[^>]*>","");
					boolean isQuestionRequired = question.getFormQuestionAttributes().isRequired();
					if(!isQuestionRequired) {
%>
						<tr class="formrow_1" id="<%=questionIdText%>_div" qid="<%= questionId%>">
							<td><div class='more tdWrapper'><%= questionText %></div></td>
							<td><input type="radio" class="showHideSelector questionRadio " qid="<%= questionId%>" name="<%= questionIdText%>" id="<%= questionIdText%>_show" value="show" checked="checked">Show</td>
							<td><input type="radio" class="showHideSelector questionRadio" qid="<%= questionId%>" name="<%= questionIdText%>" id="<%= questionIdText%>_hide" value="hide">Hide</td>
						</tr>
<%			
					}else {
%>
						<tr class="formrow_1" id="<%=questionIdText%>_div" qid="<%= questionId%>">
							<td><div class='more tdWrapper'><span style="color:red">*</span><%= questionText %></div></td>
							<td><input type="radio"  value="show" checked="checked" disabled="true">Show</td>
							<td><input type="radio"  value="hide" disabled="true">Hide</td>
						</tr>					
<%
					}
		}	
%>
			</tbody>
			</table>
			</div> 
<%
	}
%>
	
	<security:hasProtocolPrivilege privilege="editPSR">
		<div class="formrow_1">	
			<input type="button" id="save" value="Save" onclick="save()" />
			<input type="button" id="cancel" value="Cancel" onclick="cancel()" />
		</div>
	</security:hasProtocolPrivilege>
	
	
	<s:form id="myForm" method="post" enctype="multipart/form-data">
		<s:hidden name="eformId" id="eformId" />
		<s:hidden name="hiddenSectionsQuestionsIdsJSON" id="hiddenSectionsQuestionsIdsJSON" />
		<s:hidden name="hasEditPriv" id="hasEditPriv" />
	</s:form>
	
	
		
		
		
	<script type="text/javascript">

    

    
    function enableSectionQuestions(sectionIdDiv,sectionId,isRepeatable,parent,isEdit) {
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("checked", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("disabled", false);
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("disabled", false);
    }
    
    function disableSectionQuestions(sectionIdDiv,sectionId,isRepeatable,parent,isEdit) {
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("checked", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="hide"]')).attr("disabled", true);
    	$("#" + sectionIdDiv).find($('.showHideSelector.questionRadio[value="show"]')).attr("disabled", true);
    }
	
	function save() {
		var webRoot = "<s:property value='#webRoot'/>";
		var configurePSRHomeUrl = webRoot + "/protocol/configurePSReFormsHome.action";
		var sectionQuestionIdsToHideArray = new Array();
		var selectedHideInputs = $('.showHideSelector[value="hide"]:checked:enabled');
		selectedHideInputs.each(function(index) {
			var sectionQuestionId =  $(this).attr("name");
			sectionQuestionIdsToHideArray.push(sectionQuestionId);
		});
		var sectionQuestionIdsToHideArrayString = JSON.stringify(sectionQuestionIdsToHideArray);
		$("#hiddenSectionsQuestionsIdsJSON").val(sectionQuestionIdsToHideArrayString);
		$('#myForm').ajaxSubmit({
		    url: "<s:property value="#webRoot"/>/protocol/configureEformAjax.action?action=saveHiddenElements",
		    data: "",
		    success: function(data) {
		    	if(data == ""){
		    		redirectWithReferrer(configurePSRHomeUrl);
		    	}else{
		    		//errors
		    	}
		    },	
		    error: function(jqXHR, textStatus, errorThrown) {
		        document.write(jqXHR.responseText + ':' + textStatus + ':' + errorThrown);
		    }
		});	
	}
	
	function cancel() {
		var webRoot = "<s:property value='#webRoot'/>";
		var configurePSRHomeUrl = webRoot + "/protocol/configurePSReFormsHome.action";
		redirectWithReferrer(configurePSRHomeUrl);
	}
	
	
	
	$(document).ready(function() {	
		//check appropriate hide radio options in edit mode
		var idlist = JSON.parse($("#hiddenSectionsQuestionsIdsJSON").val());
		for(var i=0;i<idlist.length;i++) {
			var elementId = idlist[i];
			$("#" + elementId + "_hide").attr("checked",true);
		}
		
		//if in edit mode, the section is set to hide, we need to call disableSectionQuestion function to disable the questions in it
		var selectedHideSectionInputs = $('.showHideSelector.sectionRadio[value="hide"]:checked');
		selectedHideSectionInputs.each(function(index) {
			var sectionIdName =  $(this).attr("name");
			//get parent div Info
			var parentDiv = $("#" + sectionIdName + "_div");
			var sectionId =  parentDiv.attr("sid");
			var isRepeatable =  parentDiv.attr("isRepeatable");
			var parent =  parentDiv.attr("parent");
			disableSectionQuestions(sectionIdName + "_div",sectionId,isRepeatable,parent,'true');
		});
		


		//disable all inputs if user does not have edit priv (if user only has viewpsr priv)
		if($("#hasEditPriv").val() == "false") {
			$('.showHideSelector').attr("disabled",true);
		}

		//code to handle the shore more/show less for text
	    var showChar = 130;  // How many characters are shown by default
	    var ellipsestext = "...";
	    var moretext = "Show more >";
	    var lesstext = "Show less";
	    $('.more').each(function() {
	        var content = $(this).html();
	        if(content.length > showChar) {
	            var c = content.substr(0, showChar);
	            var h = content.substr(showChar, content.length - showChar);
	            var html = c + '<span class="moreellipses">' + ellipsestext+ '&nbsp;</span><span class="morecontent"><span>' + h + '</span>&nbsp;&nbsp;<a href="" class="morelink">' + moretext + '</a></span>';
	            $(this).html(html);
	        }
	    });
	 
	    $(".morelink").click(function(){
	        if($(this).hasClass("less")) {
	            $(this).removeClass("less");
	            $(this).html(moretext);
	        } else {
	            $(this).addClass("less");
	            $(this).html(lesstext);
	        }
	        $(this).parent().prev().toggle();
	        $(this).prev().toggle();
	        return false;
	    });													
	});
	</script>


<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />


</html>
