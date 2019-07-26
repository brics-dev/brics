<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.lang.Integer"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditquestions"/>

<script>
   var gCountPrevious = 1;
   var gLastDivNum = 1;

function countchange() {
    // Get the user's input from the form.
    var msg;
    var count = document.questionImageForm.imageCount.value;
    var v = parseInt(count);
    if (isNaN(v) || v < 1 || v > 5) {
        msg  = "Invalid number: " + count + ".\n";
        msg += "Number of images to upload must be a valid integer between 1 and 5.\n";
        msg += "Please input a valid number.\n";
        alert(msg);
        return;
    }

    if (v > gCountPrevious) {
        var div = "";
        var numFound = 0;
        for (var i = gCountPrevious + 1; i <= v; i++) {
            var divId = "divInput" + i;
            if (document.getElementById(divId) != null) {
                numFound++;
                document.getElementById(divId).innerHTML = getDivInner(i);
                convertFileInputs();
            } else {
                div +=getDiv(i);
            }
        }
        
        if (numFound != (v - gCountPrevious)) {
            var lastDiv = "div" + gLastDivNum;
            gLastDivNum++;
            div += "<div id=\"div" + gLastDivNum + "\"></div>"

            document.getElementById(lastDiv).innerHTML = div;
            convertFileInputs();
        }
        gCountPrevious = v;
    }

    if (v < gCountPrevious) {
        for (var i = v + 1; i <= gCountPrevious; i++) {
            var divId = "divInput" + i;
            document.getElementById(divId).innerHTML = "";
        }
        gCountPrevious = v;
    }
}

function getDiv(i) {
    var div = "";
    div += "<div id=\"divInput" + i + "\">";
    div += getDivInner(i);
    div += "</div>";
    return div;
}

function getDivInner(i) {
    var div = "";
    div += "<small>" + i + ".  </small>";
    div += "<input type=\"file\" size=\"45\" class=\"fileInput\" name=\"imageFile\">";
    div += "<br>";
    return div;
}

function goImgWin(myImage,myWidth,myHeight,origLeft,origTop) {
    myHeight += 24;
    myWidth += 24;
    TheImgWin = openPopup(myImage,'image', 'height=' + myHeight + ',width=' + myWidth +
            ',toolbar=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=yes');
    TheImgWin.moveTo(origLeft,origTop);
    TheImgWin.focus();
}

function hideGraphics() {
	var deletGraphics = document.getElementsByName('namesToDelete');
	
	for (var i=0;i<deletGraphics.length;i++) {
		if (deletGraphics[i].checked) {
			document.getElementById(deletGraphics[i].value).parentNode.parentNode.style.display = "none";
		}
	}
}

</script>

<html>
<%-- Include Header--%> 
<jsp:include page="/common/nojsHeader_struts2.jsp" />


<table border="0" width="100%">
	<s:if test="hasActionMessages()">
        <tr><td>&nbsp;</td></tr>
        <tr>
            <td class="confirmationText">
				<s:iterator value="actionMessages">
					<li class="success">
						<s:property escapeHtml="false" />
					</li>
				</s:iterator>
            </td>
        </tr>
    </s:if>

    <jsp:include page="/common/validationErrors_struts2.jsp" />
    <tr>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td align="left">
            <s:form name="questionImageForm" theme="simple" action="saveQuestionImage" method="post" enctype="multipart/form-data">
            	<s:hidden name="questionId" id="forGarphicQuestionId" />
            	
                <table width="50%" align="left" border="0" cellspacing="0" cellpadding="2">
                	<s:if test="%{(questionId != null) && (questionId > 0)}">
                        <tr>
                            <td class="protocolReference" align="left" colspan="3">
                                <b><s:text name="question.image.graphic.CurrentQuestionGraphics"/></b>
                            </td>
                        </tr>
                        <tr>
                        	<td align="left">
                        		&nbsp;
                        	</td>
                        </tr>
                        
                    <s:if test="%{#session.noGraphics == 'false'}">
                        <tr>
							<td>
								<input type="button" value="<s:text name='button.DeleteGraphic'/>" onclick="hideGraphics()">
							</td>
						</tr>

                        
                        <tr>
                            <td>
                                <display:table border="0" width="50%" cellpadding="4" cellspacing="1" name="imageNames" 
                                		scope="request" decorator="gov.nih.nichd.ctdb.question.tag.QuestionImageDecorator">
                                    <display:setProperty name="basic.msg.empty_list" value="<s:text name='question.image.noImage'/>"/>
                                    <display:column property="checkboxDec" title="Select to Delete" width="20%" align="center" valign="center" nowrap="true"/>
                                    <display:column property="numberDec" width="20%" align="center" valign="center" title="Number" />
                                    <display:column property="thumbnailDec" width="60%" align="center" valign="center" title="Thumbnail"/>
                                </display:table>
                            </td>
                        </tr>
                    </s:if>
                    </s:if>
                </table>

                <table width="95%" align="left" border="0" cellspacing="0" cellpadding="2">
                    <tr>
                        <td align="left">
                        	<b><s:text name="question.image.count.display"/></b>
			      		    <s:select name="imageCount" onchange="javascript:countchange()" id="graphicSelect" list="{1,2,3,4,5}" />&nbsp;
						    <input type="button" value="<s:text name='button.Reset'/>" onclick="javascript:document.questionImageForm.reset();countchange()"/>
                        </td>
                    </tr>
                    <tr><td align="left">&nbsp;</td></tr>
                    <tr>
                    	<td>
                    		<div id="divUpload">
			                     <small>1. </small><input type="file" size="45" name="imageFile" id="firstUploadFile" class="fileInput">
			                </div>
			                <div id="div1">
			                </div>
                   		</td>
                    </tr>
                </table>
            </s:form> 
        </td>
    </tr>
</table>

</html>





