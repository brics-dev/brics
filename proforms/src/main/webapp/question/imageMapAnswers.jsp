<%@ page import="gov.nih.nichd.ctdb.question.common.QuestionConstants,
                 gov.nih.nichd.ctdb.question.domain.Question,
                 gov.nih.nichd.ctdb.question.domain.ImageMapQuestion,
                 gov.nih.nichd.ctdb.form.common.FormConstants" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditquestions"/>

<%  
	ImageMapQuestion q = (ImageMapQuestion)request.getSession().getAttribute(QuestionConstants.QUESTION_IN_PROGRESS);
    double cellWidth = Double.parseDouble((String)request.getAttribute("cellWidth"));
    double cellHeight = Double.parseDouble((String)request.getAttribute("cellHeight"));
    int resolution = Integer.parseInt((String)request.getAttribute("resolution"));
    int imgTop;
    int imgLeft = 200;
    
    if (request.getAttribute("hasErrors") != null) {
    	imgTop = 340;
    } else {
    	imgTop = 250;
    }
%>

<meta charset="UTF-8"> 
<html>

<%-- Include Header--%> 
<jsp:include page="/common/iframeHeader_struts2.jsp" />

<script type="text/javascript" src="<s:property value="#webRoot"/>/common/common.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/genericResize.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/ieemuResize.js"></script>
<script type="text/javascript" src="<s:property value="#webRoot"/>/common/imageMapOptionsHandler.js"></script>

<script type="text/javascript">

var activeCells = new ImageMapOptionsHandler();

function test () {
	activeCells.print();
}

function clickMap (row, col) {

    id = 'overlayImg'+row+"_"+col;
    if ( document.getElementById(id).style.zIndex < 400) {
        document.getElementById(id).style.zIndex = 400;
        document.getElementById(id).style.visibility="visible";
        activeCells.add (row, col);
    } else {
        document.getElementById(id).style.zIndex = 2;
        document.getElementById(id).style.visibility="hidden";
        activeCells.remove (row, col);
    }
}

</script>

<script language="javascript">
function enterOption () {
    if (document.getElementById('userOption').value == "") {
        alert ('Please enter an option before storing option / value pairs');
        return false;
    }
    optStr = document.getElementById('userOption').value;
    optStr +=" : ";

    webStr = activeCells.getWebString();
    if (webStr =="error") {
        alert ("Please select an area on the map prior to storing an option");
    } else {
        optStr += webStr;
        newitem = new Option(optStr,optStr, 0, 0);
        document.getElementById('options').options[document.getElementById('options').options.length] = newitem;
        document.getElementById('userOption').value = "";

        updateGrid();
        activeCells = new ImageMapOptionsHandler();
    }
}

function updateGrid() {
    for (i = 0; i < activeCells.rows.length; i++) {
        theCols = activeCells.cols[i];
        for (j=0; j<theCols.length; j++) {
            id = 'overlayImg' + activeCells.rows[i] +"_"+ theCols[j];
            document.getElementById(id).style.zIndex=-5;
            document.getElementById(id).style.visibility='hidden';
            id = 'selectedOverlayImg' + activeCells.rows[i] +"_"+ theCols[j];
            document.getElementById(id).style.zIndex=700;
            document.getElementById(id).style.visibility='visible';
        }
    }
}

function undoGrid() {
    sel = document.getElementById('options');
    if (sel.value == "") {
        alert ('please choose a mapping to remove');
        return false;
    }
    
    rem = new Array();
    for (i = 0; i < sel.options.length; i++) {
        if (sel.options[i].selected) {
            removeMapping (sel.options[i].value);
            rem[rem.length] = i;
        }
    }

    newOpts = new Array();
    for (i = 0; i < sel.options.length; i++){
    	removeIt = false;
        for (j=0; j < rem.length; j++){
            if (i == rem[j]) {
                removeIt = true;
            }
        }
        if (!removeIt) {
            newOpts[newOpts.length] = sel.options[i];
        }
    }
    
    while (true) {
        sel.options[0] = null;
        if (sel.options.length == 0) {
            break;
        }
    }
    for (i = 0; i < newOpts.length; i++) {
        sel.options[sel.options.length] = newOpts[i];
    }

}

function removeMapping (str) {

    while (true) {
        str = str.substring (str.indexOf (":"), str.length);
        str = str.substring (str.indexOf("(")+1, str.length);
        row = str.substring(0, str.indexOf(","));//charAt(0);
        str = str.substring (str.indexOf(",")+1, str.length);
        col = str.substring(0, str.indexOf(")"));//charAt(0);
        id = 'selectedOverlayImg' +row+"_"+col;
        id2 = 'selectedOverlayRedImg' +row+"_"+col;
        document.getElementById(id).style.visibility='hidden';
        document.getElementById(id).style.zIndex=2;

        document.getElementById(id2).style.visibility='hidden';
        document.getElementById(id2).style.zIndex=3;
        if (str.length < 5) {
            break;
        }
    }
}


function addMapping () {
    sel = document.getElementById('options');
    for (i = 0; i < sel.options.length; i++) {
        str = sel.options[i].value;
        while (true) {
            str = str.substring (str.indexOf("(")+1, str.length);
            row = str.substring(0, str.indexOf(","));//charAt(0);
            str = str.substring (str.indexOf(",")+1, str.length);
            col = str.substring(0, str.indexOf(")"));//charAt(0);
            id = 'selectedOverlayImg' +row+"_"+col;
            redId = 'selectedOverlayRedImg' +row+"_"+col;
            document.getElementById(id).style.visibility='visible';
            document.getElementById(id).style.zIndex=700;
            document.getElementById(redId).style.zIndex=2;
            if (str.length < 5) {
                break;
            }
        }
    }
}

function highlight() {
    addMapping();
    sel = document.getElementById('options');
    for (i = 0; i < sel.options.length; i++) {
        if (sel.options[i].selected) {
            str = sel.options[i].value;
            while (true) {
                str = str.substring (str.indexOf("(")+1, str.length);
                row = str.substring(0, str.indexOf(","));//charAt(0);
                str = str.substring (str.indexOf(",")+1, str.length);
                col = str.substring(0, str.indexOf(")"));//charAt(0);
                id = 'selectedOverlayRedImg' +row+"_"+col;
                document.getElementById(id).style.visibility='visible';
                document.getElementById(id).style.zIndex=800;
                if (str.length < 5) {
                    break;
                }
            }
        }
    }
}


function reset () {
    selectAllOptions (document.getElementById('options'));
    undoGrid();
}


function checkOptions () {
    if (document.getElementById('options').options.length == 0) {
        alert ('You must enter at lease one option to create an image map question');
        return false;
    } else {
        return true;
    }
}

</script>

<table border=0 width="100%">
    <tr>
        <td>
            <h3><s:text name="question.questionwizard.defineanswers.title.display"/>&nbsp;<s:text name="question.imagemapAnswer.Steps3"/></h3>
        </td>
    	<jsp:include page="/common/validationErrors_struts2.jsp" />
    </tr>
    <tr>
        <td class="standardText">
            <s:text name="question.imagemapAnswer.instruction"/>
        </td>
    </tr>
    
<s:form theme="simple" action="saveImageMapAnswers" method="post" onsubmit="return checkOptions();">
    <s:hidden name="action" />
	<input type="hidden" name="finish" id="finish" value="false">
	
	<table id=outer>
		<tr>
			<td>
				<table>
					<tr>
                    	<td  align="right">
                      		<label for="formName" class="requiredInput"><b><s:text name="question.imagemapAnswer.Option"/></b></label>
                    	</td>
                    	<td>
                       		<input type=text size=12 id="userOption" maxlength="200">
                    	</td>
                	</tr>
				</table>
			</td>
			<td>
				&nbsp;<img src="<s:property value="#imageRoot"/>/buttonArrowRight.png" alt="enter option" title="enter option" border="0"
                	height="23" width="23" onClick="enterOption()">&nbsp;
			</td>
			<td>
				<td valign="middle" align="right" class="formItemLabel">
					<s:select name="options" multiple="true" size="6" cssStyle="width:255" id="options" onchange="highlight();" list="#request.enteredOptions" />
                </td>
                <td valign="middle" align="left" class="formItemLabel">
					<img src="<s:property value="#imageRoot"/>/buttonRemove.png" alt="Delete" title="Delete"
                            border="0" width="23" height="23" onclick="undoGrid();"/>
                </td>
			</td>
		</tr>	
	</table>


	<table align="right">
    	<tr>
        	<td>
				<input type="submit" title="Next" value="<s:text name='button.next'/>" onClick="selectAllOptions(document.getElementById('options'));">            	
            	<input type="button" title="Reset" value="<s:text name='button.Reset'/>" onClick="location.reload()">
            	<s:if test="%{action != 'process_edit'}">
            		<input type="button" title="Cancel" value="<s:text name='button.Cancel'/>" onClick="location.href='addImageType.action'">
            	</s:if>
        	</td>
    	</tr>
    </table>

    <tr>
        <td>
            <table width="95%" align="center" border="0" cellspacing="0" cellpadding="2">
                <tr>
                    <td  valign="middle" align="right" class="formItemLabel" nowrap width="65">
                        <img src="<s:property value="#imageRoot"/>/questionimages/<%=q.getImageFileName()%>" border=1
                            height="<%=q.getHeightInt() %>"  width="<%=q.getWidthInt() %>"
                            style="position:absolute; top:<%=imgTop%>; left:20; z-index:50">
                        <img src="<%=q.getGridFileName()%>" border=0 id='mapGrid'
                            height="<%=q.getHeightInt()%>"  width="<%=q.getWidthInt()%>"
                            usemap="#theMap" style="position: absolute; top:<%=imgTop%>; left:20; z-index:500">

                            <%= request.getAttribute("theImageMap")%>

                            <% for (int i = 0; i < resolution; i ++) {
                                for (int j=0; j < resolution; j++) {
                                    double vSpace = (cellHeight* 0.175);
                                    double hSpace = (cellWidth * 0.175);
                            %>
                                <img src="<s:property value="#imageRoot"/>/transparentOverlay.gif"
                                    id="overlayImg<%=j+1%>_<%=i+1%>"
                                    style="position:absolute; visibility:hidden;
                                        height:<%=cellHeight%>; width:<%=cellWidth%>;
                                        top:<%=imgTop+(cellHeight*j)%>;
                                        left:<%=20+(cellWidth*i)%>; z-index:1"
                                    border=0 bordercolor=red>

                                <img src="<s:property value="#imageRoot"/>/selectedOverlay.gif"
                                    id="selectedOverlayImg<%=j+1%>_<%=i+1%>"
                                    style="position:absolute; visibility:hidden;
                                        height:<%=cellHeight%>; width:<%=cellWidth%>;
                                        top:<%=imgTop+(cellHeight*j)%>;
                                        left:<%=20+(cellWidth*i)%>; z-index:5"
                                    border=0 bordercolor=red>

                                <img src="<s:property value="#imageRoot"/>/selectedOverlayRed.gif"
                                    id="selectedOverlayRedImg<%=j+1%>_<%=i+1%>"
                                    style="position:absolute; visibility:hidden;
                                        height:<%=cellHeight%>; width:<%=cellWidth%>;
                                        top:<%=imgTop+(cellHeight*j)%>;
                                        left:<%=20+(cellWidth*i)%>; z-index:2"
                                    border=0 bordercolor=red>
                           <%
                                }
                            }
                           %>
                     </td>
                </tr>
            </table>
        </td>
    </tr>
   </s:form>
   </table>


<script langugae="Javascript">
    addMapping();
</script>

</html>

