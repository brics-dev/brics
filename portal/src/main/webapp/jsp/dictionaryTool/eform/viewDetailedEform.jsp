<%@include file="/common/taglibs.jsp"%>

<html>

<title><s:property escapeHtml="true" value="sessionEform.eform.title" /></title>


<script src="https://code.jquery.com/jquery-1.7.2.min.js" 
			integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" 
			crossorigin="anonymous"></script>
<%-- Include popUp Header  --%>
<jsp:include page="popUpHeader_struts2.jsp" />

<script src="/portal/formbuilder/js/common/template.js"></script> 

<link rel="stylesheet" type="text/css" href="<s:url value='/formbuilder/css/formBuilderView.css'/>"/>
<link rel="stylesheet" type="text/css" href="<s:url value='/formbuilder/css/dataCollection.css'/>">

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
		
		 $(".imgThumb").each(function(){
			 var image = $(this);
			var yImageName = $(this).attr('imageName');
			var yQuestionId = $(this).attr('questionId');
			var baseUrl = '<s:property value="modulesDDTURL"/>/dictionary/';
			$.ajax({
				type: "POST",
				url: baseUrl+"renderImageAction!renderSingleImage.action",
				async: false,
				data:{questionId:yQuestionId,imageFileName:yImageName},
			success: function (data) {
				  var imgArray = JSON.parse(data);
				  for(y=0;y<imgArray.length;y++){	
				  	image.attr("src", imgArray[y]);
				  }
				} ,
			error : function(e) { 
					 alert("error"+e);
				}
			});
		}); 
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

	
<!-- <div id="overlay"> -->
	<div id="viewModeBanner">
		<div id="viewModeBannerText">
			V&nbsp;&nbsp;I&nbsp;&nbsp;E&nbsp;&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;M&nbsp;&nbsp;O&nbsp;&nbsp;D&nbsp;&nbsp;E
		</div>
	</div>
<!-- </div> -->
<br/><br/>

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