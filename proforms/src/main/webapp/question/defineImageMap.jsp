<%@ page import="gov.nih.nichd.ctdb.question.common.QuestionConstants,
                 gov.nih.nichd.ctdb.question.domain.ImageMapQuestion,
                 gov.nih.nichd.ctdb.form.common.FormConstants"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<% 
	ImageMapQuestion q = (ImageMapQuestion)request.getSession().getAttribute(QuestionConstants.QUESTION_IN_PROGRESS);
%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditquestions" />
<meta charset="UTF-8"> 

<html>

<script language="Javascript">

// find out the browser
var ua= navigator.userAgent;
var OPERA = (ua.indexOf("Opera") >= 0);
var OMNI = (ua.indexOf("Omni") > 0);
var MAC = (navigator.platform.indexOf("PPC") > 0);
var WIN = (navigator.platform=="Win32");
var IE5_0, IE5,IE6,IE5_5,N4,N5,NS6,MAC_IE5,MOZ;

if (!OPERA && !OMNI) {
    IE6 = (ua.indexOf("MSIE 6") > 0);
    IE5_0 = (ua.indexOf("MSIE 5.0") > 0);
    IE5_5 = (ua.indexOf("MSIE 5.5") > 0);
    IE5 = (ua.indexOf("MSIE 5") > 0  || IE6 || IE5_5);
    N4 = (document.layers) ? 1 : 0;
    NS6= N5 = (ua.indexOf("Gecko") > 0);
    MAC_IE5 = (MAC && IE5) ? 1 : 0;
    MAC_IE5_1b = (MAC && ua.indexOf("MSIE 5.1b") >= 0);
    WIN_IE5= IE5 && !MAC;
    MOZ= N5 && !(ua.indexOf("Netscape") > 0);
}


/* Global variables to hold the grid images
*/
var img_5x5;
var img_8x8;
var img_10x10;
var img_15x15;
var img_20x20;

/* global vars to keep track of image orginal size and the height/width ration
*/
var yProportion;
var origHeight;
var origWidth;

var loadedGrids = 'large';

var origImage = new Image;
origImage.src = "<s:property value="#imageRoot"/>/questionimages/<%=q.getImageFileName()%>";

/*  Loads the grid images onto the page for use
*/
function loadLargeGrids () {

	img_5x5 = new Image;
	img_5x5.src = "<s:property value="#imageRoot"/>/5x5_large.gif";
	img_8x8=new Image;
	img_8x8.src = "<s:property value="#imageRoot"/>/8x8_large.gif";

	img_10x10=new Image;
	img_10x10.src = "<s:property value="#imageRoot"/>/10x10_large.gif";

	img_15x15=new Image;
	img_15x15.src = "<s:property value="#imageRoot"/>/15x15_large.gif";

	img_20x20=new Image;
	img_20x20.src = "<s:property value="#imageRoot"/>/20x20_large.gif";
    loadedGrids = 'large';
}

function loadSmallGrids() {

	img_5x5 = new Image;
	img_5x5.src = "<s:property value="#imageRoot"/>/5x5_small.gif";
	img_8x8=new Image;
	img_8x8.src = "<s:property value="#imageRoot"/>/8x8_small.gif";

	img_10x10=new Image;
	img_10x10.src = "<s:property value="#imageRoot"/>/10x10_small.gif";

	img_15x15=new Image;
	img_15x15.src = "<s:property value="#imageRoot"/>/15x15_small.gif";

	img_20x20=new Image;
	img_20x20.src = "<s:property value="#imageRoot"/>/20x20_small.gif";
    loadedGrids = 'small';
}

function loadSmallRectangleGrids() {

	img_5x5 = new Image;
	img_5x5.src = "<s:property value="#imageRoot"/>/5x5Rec_small.gif";
	img_8x8=new Image;
	img_8x8.src = "<s:property value="#imageRoot"/>/8x8Rec_small.gif";

	img_10x10=new Image;
	img_10x10.src = "<s:property value="#imageRoot"/>/10x10Rec_small.gif";

	img_15x15=new Image;
	img_15x15.src = "<s:property value="#imageRoot"/>/15x15Rec_small.gif";

	img_20x20=new Image;
	img_20x20.src = "<s:property value="#imageRoot"/>/20x20Rec_small.gif";
    loadedGrids = 'smallRectangle';
}

function loadLargeRectangleGrids() {

	img_5x5 = new Image;
	img_5x5.src = "<s:property value="#imageRoot"/>/5x5Rec_large.gif";
	img_8x8=new Image;
	img_8x8.src = "<s:property value="#imageRoot"/>/8x8Rec_large.gif";

	img_10x10=new Image;
	img_10x10.src = "<s:property value="#imageRoot"/>/10x10Rec_large.gif";

	img_15x15=new Image;
	img_15x15.src = "<s:property value="#imageRoot"/>/15x15Rec_large.gif";

	img_20x20=new Image;
	img_20x20.src = "<s:property value="#imageRoot"/>/20x20Rec_large.gif";
    loadedGrids = 'largeRectangle';
}

function correctGrids() {
    var rec = false;
    if (yProportion < 0.57) {
        rec = true;
    }
    var curHeight;
    if (NS6) {
        curHeight = document.getElementById('theImage').style.height;
    } else {
        curHeight = document.getElementById('theImage').style.pixelHeight;
    }
    
    if (rec && parseInt (curHeight) < 115 && loadedGrids != 'smallRectangle') {
        loadSmallRectangleGrids();
    } else if (! rec && parseInt (curHeight) < 115 && loadedGrids != 'small') {
        loadSmallGrids();
    } else if (rec && parseInt (curHeight) > 115 && loadedGrids != 'largeRectangle') {
        loadLargeRectangleGrids();
    } else if (!rec && parseInt (curHeight) > 115 && loadedGrids != 'large') {
        loadLargeGrids();
    }
}


function initImage() {
	if (MAC) {
	    if (origImage.height>600) {
	    	document.getElementById('theImage').style.height = Math.floor(document.getElementById('theImage').offsetHeight*(600/document.getElementById('theImage').offsetHeight));	
	    } else {
	    	document.getElementById('theImage').style.height = document.getElementById('theImage').offsetHeight;
	    }
	    if (origImage.width>600) {
	    	document.getElementById('theImage').style.width = Math.floor(document.getElementById('theImage').offsetWidth*(600/document.getElementById('theImage').offsetWidth));
	    } else {
	    	document.getElementById('theImage').style.width = document.getElementById('theImage').offsetWidth;
	    }
	} else {
	    if (origImage.height>600) {
	    	document.getElementById('theImage').style.height = Math.floor(origImage.height*(600/origImage.height));
	    } else {
	    	document.getElementById('theImage').style.height = origImage.height;
	    }
	    if (origImage.width>600) {
	    	document.getElementById('theImage').style.width = Math.floor(origImage.width*(600/origImage.width));
	    } else {
	    	document.getElementById('theImage').style.width = origImage.width;
	    }
	}
}


/* 
 * Changes the grid size to match the image size
 */
function doGridOverlay() {
    if (NS6) {
        document.getElementById('theGrid').style.height = document.getElementById('theImage').style.height;
	    document.getElementById('theGrid').style.width = document.getElementById('theImage').style.width;
    } else {
	    document.getElementById('theGrid').style.pixelHeight = document.getElementById('theImage').style.pixelHeight;
	    document.getElementById('theGrid').style.pixelWidth = document.getElementById('theImage').style.pixelWidth;
    }
	$('#nextBut').removeAttr('disabled'); // when every thing is done, enable the next button
}

/* Switches the grid as selected by the user
*/
function swapGrid() {

	if (document.getElementById('grids').value==1) {
		document.getElementById('theGrid').src = img_5x5.src;
	} else if (document.getElementById('grids').value==2) {
		document.getElementById('theGrid').src = img_8x8.src;
	}  else if (document.getElementById('grids').value==3) {
		document.getElementById('theGrid').src = img_10x10.src;
	} else if (document.getElementById('grids').value==4) {
		document.getElementById('theGrid').src = img_15x15.src;
	} else if (document.getElementById('grids').value==5) {
		document.getElementById('theGrid').src = img_20x20.src;
	}

	doGridOverlay();
}


/* finds the ratio of height / width for use in reszing
 * MUST CALL WHEN PAGE LOADS
 */
function findImgProportions () {
    if (NS6) {
        height = document.getElementById('theImage').style.height;
        origHeight = parseInt ( height.substring (0, height.length -2));
        width = document.getElementById('theImage').style.width;
        origWidth = parseInt (width.substring(0, width.length - 2));
        yProportion =  origHeight / origWidth;
    } else {
    	origHeight = document.getElementById('theImage').style.pixelHeight;
	    origWidth = document.getElementById('theImage').style.pixelWidth;
	    yProportion =  origHeight / origWidth;
    }
}

/* Resets image to original size
*/
function resetIt() {
	document.getElementById ('theImage').style.width=origWidth;
	document.getElementById ('theImage').style.height=origHeight;
    correctGrids();
	doGridOverlay();
}

/* Decreases the size of the image
*/
function shrinkImg(factor) {
    if (NS6) {
        nsWidth=document.getElementById('theImage').style.width;
        nsWidth = parseInt (nsWidth.substring (0, nsWidth.length -2));
        document.getElementById('theImage').style.width = (nsWidth - (1*factor))+'px';

        nsHeight=document.getElementById('theImage').style.height;
        nsHeight = parseInt (nsHeight.substring (0, nsHeight.length -2));
        document.getElementById('theImage').style.height = (nsHeight - (yProportion * factor)) + 'px';
    } else {
	    document.getElementById('theImage').style.pixelWidth -= (1 * factor);
	    document.getElementById('theImage').style.pixelHeight -= (yProportion * factor);
    }
    correctGrids();
	doGridOverlay();
}

/* increases the size of the image
*/
function growImg(factor) {
    if (NS6) {
        nsWidth=document.getElementById('theImage').style.width;
        nsWidth = parseInt (nsWidth.substring (0, nsWidth.length -2));
        document.getElementById('theImage').style.width = (nsWidth + (1*factor))+'px';

        nsHeight=document.getElementById('theImage').style.height;
        nsHeight = parseInt (nsHeight.substring (0, nsHeight.length -2));
        document.getElementById('theImage').style.height = (nsHeight + (yProportion * factor)) + 'px';
    } else {
	    document.getElementById('theImage').style.pixelWidth += (1 * factor);
	    document.getElementById('theImage').style.pixelHeight += (yProportion * factor);
    }
    correctGrids();
	doGridOverlay();
}

/* size the image to users pre-saved specifications
*/
function adjustSize() {
    if (NS6) {
        document.getElementById('theImage').style.width = document.getElementById('width').value;
        document.getElementById('theImage').style.height = document.getElementById('height').value;
    } else {
        document.getElementById('theImage').style.pixelWidth = document.getElementById('width').value;
        document.getElementById('theImage').style.pixelHeight = document.getElementById('height').value;
    }
    doGridOverlay();
}

function saveSize() {
    if (NS6) {
       document.getElementById('width').value  =  document.getElementById('theImage').style.width;
       document.getElementById('height').value = document.getElementById('theImage').style.height;
    } else {
       document.getElementById('width').value  =  document.getElementById('theImage').style.pixelWidth;
       document.getElementById('height').value = document.getElementById('theImage').style.pixelHeight;
   }
   document.getElementById('gridFileName').value = document.getElementById('theGrid').src;
}

function setup() {
    initImage();
    swapGrid();
	findImgProportions();

}

</script>

<jsp:include page="/common/iframeHeader_struts2.jsp" />

<s:form theme="simple" action="saveGridResolution" id="imageMapForm" onsubmit="saveSize();return true;">
	<s:hidden name="action"/>
	<s:hidden name="id"/>
	<s:hidden name="height" id="height"/>
	<s:hidden name="width" id="width"/>
	<s:hidden name="gridFileName" id="gridFileName"/>
	
	<table border=0 width="100%">
    	<tr>
        	<td>
            	<h3><s:text name="question.imagemap.sizeandgrid.title.step.display"/></h3>
        	</td>
    	</tr>
    	<jsp:include page="/common/validationErrors_struts2.jsp" />
          
     	<tr>
        	<td class="standardText">
            	<s:text name="question.imagemap.sizeandgrid.instruxtion.display"/>
            	<s:if test="%{action == 'process_edit'}">
                	<br><font color='red'>Warning: if you change the grid resolution, the mapping values for this question will be reset.</font>
            	</s:if>
        	</td>
    	</tr>
    	<tr>
        	<td><img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="10" alt="" border="0"/></td>
    	</tr>
    	<tr>
        	<td>
            	<table cellpadding="2" cellspacing="0" border="0">
                	<tr>
                    	<td align="right" class="formItemLabel">
                        	<b><s:text name="question.imagemap.sizeandgrid.subtitleDisplay.display"/></b>&nbsp;
                    	</td>
                    	<td>
                        	<s:checkbox name="showGrid" />
                    	</td>
                	</tr>
                	<tr>
                    	<td align="right" class="formItemLabel">
                        	<b><s:text name="question.imagemap.sizeandgrid.resolution.display"/></b>&nbsp;
                    	</td>
                    	<td align="left" valign="top" class="formItemLabel">
                        	<s:select name="gridResolution" id="grids" onchange="swapGrid();" list="#request.resolutions" listKey="id" listValue="shortName" />
                   	</td>
                	</tr>
            	</table>
        	</td>
    	</tr>
    	
     	<tr>
        	<td>
            	<span align="left">
                	<img src="<s:property value="#imageRoot"/>/decrease3.png" border=0 onClick="shrinkImg(40);">
                	<img src="<s:property value="#imageRoot"/>/decrease2.png" border=0 onClick="shrinkImg(10);">
                	<img src="<s:property value="#imageRoot"/>/decrease1.png" border=0 onClick="shrinkImg(2);">
            	</span>
            	<span align="center">
                	<img src="<s:property value="#imageRoot"/>/buttonReset.png" border=0 onClick="resetIt();" value="<s:text name='button.Reset'/>">
            	</span>
            	<span align="right">
                	<img src="<s:property value="#imageRoot"/>/increase1.png" border=0 onClick="growImg(2);">
                	<img src="<s:property value="#imageRoot"/>/increase2.png" border=0 onClick="growImg(10);">
                	<img src="<s:property value="#imageRoot"/>/increase3.png" border=0 onClick="growImg(40);">
            	</span>
            	<span align="right">
            		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                	<input type="submit" title="Click to the next step" value="<s:text name='button.next'/>" id='nextBut' disabled='true' onClick="saveSize();">
               		<input type="button" title="Click to clear fields" value="<s:text name='button.Reset'/>" onClick="javascript:document.getElementById('imageMapForm').reset(); resetIt();swapGrid();">

            		<s:if test="%{action != 'process_edit'}">
                		<input type="button" title="Click to cancel (changes will not be saved)." value="<s:text name='button.Cancel'/>" onClick="location.href='uploadImageMap.action'">
					</s:if>
				</span>
        	</td>
    	</tr>

     	<tr>
            <s:if test="%{action == 'process_edit'}">
	        	<td align="left">
	            	<img src="<s:property value="#imageRoot"/>/questionimages/<%=q.getImageFileName()%>" border=1
	                	id="theImage" style="position:absolute; top:215; left:20; z-index:5;">
	            	<img src="<s:property value="#imageRoot"/>/5x5_large.gif" border=1
	                	id="theGrid" style="position:absolute; top:215; left:20; z-index:500" >
	        	</td>
	        </s:if>
        	<s:else>
	        	<td align="left">
	            	<img src="<s:property value="#imageRoot"/>/questionimages/<%=q.getImageFileName()%>" border=1
	                	id="theImage" style="position:absolute; top:200; left:20; z-index:5;">
	            	<img src="<s:property value="#imageRoot"/>/5x5_large.gif" border=1
	                	id="theGrid" style="position:absolute; top:200; left:20; z-index:500" >
	        	</td>
	        </s:else>
    	</tr>
    </table>
    
</s:form>

<script language="Javascript">
    loadSmallGrids();
    setTimeout ("setup();", 1700);//700
    setTimeout ("correctGrids();", 2200); //1200
    setTimeout ("swapGrid();", 2300); //1300
</script>

</html>