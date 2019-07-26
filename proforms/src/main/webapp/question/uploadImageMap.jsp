<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.question.common.QuestionConstants,
                 gov.nih.nichd.ctdb.question.domain.Question"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<!-- when user create a new imageMap, we clear the "QuestionConstants.ORIGINAL_QUESTION_OBJ", make it go to ACTION_ADD_Form in DefineMapAction.java
if user want to edit the imageMap, they won't go to this page, that means the request doesn't be cleared, so it will go to ACTION_EDIT_FORM -->

<% request.getSession().removeAttribute(QuestionConstants.ORIGINAL_QUESTION_OBJ); %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditquestions"/>
<html>
<jsp:include page="/common/iframeHeader_struts2.jsp" />

<h3 align="left"> <s:text name="question.questionwizard.uploadimage.title.display"/>&nbsp;</h3>
<table border=0 width="100%">
    <tr>
    <jsp:include page="/common/validationErrors_struts2.jsp" />
    <tr>
        <td class="standardText" align="left">
           <s:text name="question.imagemap.step1.instruction1"/><span style="color:red">.jpg</span>, <span style="color:red">.gif</span>, <s:text name="question.imagemap.step1.instruction.and"/> <span style="color:red">.png</span>.  
           <s:text name="question.imagemap.step1.instruction2"/>
        </td>
    </tr>
	<tr>
		<td>
			<img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="15" alt="" border="0" />
		</td>
	</tr>
    <tr>
        <td>
            <s:form theme="simple" action="uploadImageMap" method="post" enctype="multipart/form-data" id="uploadForm">
            <s:hidden name="id"/>
            <table width="95%" align="center" border="0" cellspacing="0" cellpadding="2">
                <tr>
                    <td  valign="middle" align="left" class="formItemLabel" nowrap width="65">
                        <label for="formName" class="requiredInput"><b><s:text name="question.questionwizard.uploadfile.display"/></b></label>
                        <s:file name="imageFile" size="45" id="file" />
                    </td>
                </tr>           
                <tr>
                	<td align="right" valign="middle">
                        <input type="submit" title="Next" value="<s:text name='button.next'/>">
 						<s:reset key="button.Reset" title="Reset"/>   
                    </td>
                </tr>
            </table>
            </s:form>
        </td>
    </tr>
</table>
</html>

