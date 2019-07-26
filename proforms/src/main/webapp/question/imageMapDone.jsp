<%@ page import="gov.nih.nichd.ctdb.question.common.QuestionConstants,
                 gov.nih.nichd.ctdb.question.domain.Question,
                 gov.nih.nichd.ctdb.question.domain.ImageMapQuestion,
                 gov.nih.nichd.ctdb.form.common.FormConstants,
                 java.util.Iterator,
                 java.util.List,
                 gov.nih.nichd.ctdb.question.domain.ImageMapOption,
                 org.json.JSONArray"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditquestions"/>

<%  ImageMapQuestion q = (ImageMapQuestion)request.getSession().getAttribute(QuestionConstants.QUESTION_IN_PROGRESS);
    double cellWidth = Double.parseDouble((String)request.getAttribute("cellWidth"));
    double cellHeight = Double.parseDouble((String)request.getAttribute("cellHeight"));
    int resolution = Integer.parseInt((String)request.getAttribute("resolution"));

    int imgTop;
    int imgLeft = 200;
    if (request.getAttribute("hasErrors") != null) {
    	imgTop = 50;
    } else {
    	imgTop = 50;
    }
%>

<html>
<%-- Include Header--%> 
<jsp:include page="/common/iframeHeader_struts2.jsp" />

<script type="text/javascript" src="<s:property value="#webRoot"/>/common/imageMapOptionsHandler.js"></script>

<script type="text/javascript">
	$(document).ready(function(){
		if(typeof(parent.FormBuilder) !== 'undefined'){
			var result = [];
			result.push('<%=q.getImageFileName()%>');
			<%
			  JSONArray optionArray = new JSONArray();
			  List<ImageMapOption> options = q.getOptions();
			  for (int i = 0; i < options.size(); i++) {
				  optionArray.put(options.get(i).getOption());
			  }
			%>
			result.push('<%=optionArray.toString()%>');
			parent.FormBuilder.page.previous("imageMapProcessView").showButton(result);
		}
	});
</script>

<table border=0 width="100%">
    <tr>
        <td>
            <s:if test="%{action == 'process_edit'}">
            	<h3>Image Map Definition has been edited (Steps:4/4)</h3>
            </s:if>
            <s:else>
            	<h3>
            		<s:text name="question.imagemapDone"/>
	            	<input type="button" title="Cancel" value="<s:text name='button.Cancel'/>" onClick="location.href='addImageType.action'">
            	</h3>
            </s:else>
        </td>
    </tr>
    <tr>
        <td>
            <table width="95%" align="center" border="0" cellspacing="0" cellpadding="2">
                <tr>
                    <td  valign="middle" align="right" class="formItemLabel" nowrap width="65">
                    	<br>
                        <img src="<s:property value="#imageRoot"/>/questionimages/<%=q.getImageFileName()%>" border=1
                            height="<%=q.getHeightInt() %>"  width="<%=q.getWidthInt() %>"
                            style="position:absolute; top:<%=imgTop%>; left:10; z-index:50">
                     </td>
                </tr>
            </table>
        </td>
    </tr>
   </table>

</html>

