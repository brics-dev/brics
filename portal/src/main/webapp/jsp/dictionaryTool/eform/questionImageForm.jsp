<%@include file="/common/taglibs.jsp"%>
<%@taglib uri="/struts-tags" prefix="s"%>
<%@page import="gov.nih.tbi.PortalConstants"%>
<jsp:include page="/common/script-includes.jsp"></jsp:include>

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
                //convertFileInputs();
            } else {
                div +=getDiv(i);
            }
        }
        
        if (numFound != (v - gCountPrevious)) {
            var lastDiv = "div" + gLastDivNum;
            gLastDivNum++;
            div += "<div id=\"div" + gLastDivNum + "\"></div>"

            document.getElementById(lastDiv).innerHTML = div;
           // convertFileInputs();
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
    div += "<input type=\"file\" size=\"45\" class=\"fileInput\" name=\"imageFile\"  onchange=\"fileValidation(this.value,this)\"  >";
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
	var questionId = document.getElementById('forGarphicQuestionId').value;
	var namesToDeleteArr = new Array();
	for (var i=0;i<deletGraphics.length;i++) {
		if (deletGraphics[i].checked) {
			$.ajax({
				type: 	"POST",
				url:    "deleteQuestionImage!deleteQuestionImage.action",
				data: 	{	"qId": questionId,
							"namesToDelete": deletGraphics[i].value,
						},
				success: function(data) {
			
				}
			});			
			namesToDeleteArr.push(deletGraphics[i]);

			document.getElementById(deletGraphics[i].value).parentNode.parentNode.style.display = "none";			
		}
	}
	var namesToDelete = "";
	for(var i=0; i<namesToDeleteArr.length; i++){		
		if(i == namesToDeleteArr.length - 1){
			namesToDelete += namesToDeleteArr[i].value; 
		} else {
			namesToDelete += namesToDeleteArr[i].value + ", ";
		}
	}
	$.ibisMessaging("close", {type:"primary"});
	$.ibisMessaging("primary", "warning", namesToDelete +" got deleted.",{container: "#graphicsMsg"});
	
}

var questionDocumentTypesStr = "<%=PortalConstants.PERMISSIBLE_UPLOAD_FILETYPES%>";
var questionDocumentTypes = questionDocumentTypesStr.split(","); 

var permissibleFileSize = "<%=PortalConstants.PERMISSIBLE_UPLOAD_FILESIZE%>";//in MB

function fileValidation(val,fileDoc){
	$.ibisMessaging("close", {type:"primary"}); 
	var name = fileDoc.files[0].name;
	var sizeInMB = (fileDoc.files[0].size)/(1024*1024);
	var filename = val.split('\\').pop().split('/').pop();
	var fileExtension = filename.split('.').pop().toUpperCase();
	
	 if((jQuery.inArray(fileExtension, questionDocumentTypes) > 0) && (sizeInMB > permissibleFileSize)){
		 $.ibisMessaging("close", {type:"primary"}); 
		 $.ibisMessaging("primary", "error", "Please upload file with size less than "+permissibleFileSize+" MB.",{container: "#fileExtError"});
	 }else if((jQuery.inArray(fileExtension, questionDocumentTypes) < 0) && (sizeInMB <= permissibleFileSize)){
		 $.ibisMessaging("close", {type:"primary"}); 
		 $.ibisMessaging("primary", "error", "Please upload file of type "+questionDocumentTypesStr+".",{container: "#fileExtError"});
	 }else if((jQuery.inArray(fileExtension, questionDocumentTypes) < 0) && (sizeInMB > permissibleFileSize)){
		 $.ibisMessaging("close", {type:"primary"}); 
		 $.ibisMessaging("primary", "error", "Please upload file of type "+questionDocumentTypesStr+" and size less than "+permissibleFileSize+" MB.",{container: "#fileExtError"});
	 }
}

function clearErrorMsg() {

	$.ibisMessaging("close", {type:"primary"}); 
	
}



$(document).ready(function(){ //hide fancybox navigation
	$(".fancy-ico").text("");
});
</script>

<html>
	<head>
		<style type="text/css">
		 form > table {
		 	font-family: Arial,Helvetica,sans-serif;
		 	font-size: 14px;
		 }
		 div.dataTables_wrapper .ui-widget-header, div.dataTables_wrapper table {
		 	font-size: 12px;
		 }
		 div.dataTables_wrapper > table > tbody > tr {
		    text-align: center;
		 }
		 div.dataTableContainer .tableCellHeader nav ul {
		 	position: absolute;
		 	padding-left: 23px;
		 }
		 .dataTableContainer .tableCellHeader nav ul ul a {
		 	text-align: left;
		 }
  		</style>						
	</head>				
							       
  	<s:form name="questionImageForm"  theme="simple" action="saveQuestionImage" method="post" enctype="multipart/form-data">
  			
           	<s:hidden name="questionId" id="forGarphicQuestionId" />
			  <table width="50%" align="left" border="0" cellspacing="0" cellpadding="2">
			  <div id="graphicsMsg"></div>
                	<s:if test="%{(questionId != null) && (questionId > 0)}">
                        <tr>
                            <td class="protocolReference" align="left" colspan="3">
                                <b>Current question files / graphics </br>
                                (click on the thumbnail to view full size graphics or click on the file name to download the file):</b>
                            </td>
                        </tr>
                        <tr>
                        	<td align="left">
                        		&nbsp;
                        	</td>
                        </tr>
                        
                    <s:if test="%{imageNames != null}">
                        <tr>
							<td>
								<input type="button" value="Delete File(s) / Graphic(s)" onclick="hideGraphics()">
							</td>
						</tr>

                        
                        <tr>
                            <td>
                                <div id="questionImageListContainer" class="dataTableContainer dataTableJSON">
                                <idt:jsontable name="questionDocumentList" id="questionImages" decorator="gov.nih.tbi.taglib.datatableDecorators.QuestionImageDecorator">
									<idt:setProperty name="basic.msg.empty_list" value="Currently there are no images added for this question." />
									<idt:column title="Select to Delete" property="checkboxDec" valign="center" nowrap="true"/>
									<idt:column title="Number" property="numberDec" valign="center"/>
									<idt:column title="File Name / Thumbnail" property="thumbnailDec" />
								</idt:jsontable>
								</div>
                                
                            </td>
                        </tr>
                    </s:if>
                    </s:if>
                </table>

                <table width="45%" align="left" border="0" cellspacing="0" cellpadding="2" style="margin-left: 20px;">
                    <tr>
                        <td align="left">
                        	<b><s:text name="question.image.count.display"/></b>
			      		    <s:select name="imageCount" onchange="javascript:countchange()" id="graphicSelect" list="{1,2,3,4,5}" />&nbsp;
						    <input type="button" value="<s:text name='button.Reset'/>" onclick="javascript:document.questionImageForm.reset();countchange();clearErrorMsg()"/>
                        </td>
                    </tr>
                    <tr><td align="left">&nbsp;</td></tr>
                    <tr>
                    	<td>
                    		<div id="divUpload">
                    				<div id="fileExtError"></div>
			                     <small>1. </small><input type="file" size="45" name="imageFile" id="firstUploadFile" class="fileInput" onchange="fileValidation(this.value,this)"/>
			                </div>
			                <div id="div1">
			                </div>
                   		</td>
                    </tr>
                </table>
	</s:form>
</html>





