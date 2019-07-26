<%@include file="/common/taglibs.jsp"%>
<title>Associate Keywords</title>

<s:set var="currentDataElement" value="currentDataElement" />
<s:set var="formType" value="formType" />
<s:set var="dataType" value="dataType" />


<div class="border-wrapper">
	<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" />
	<h1 class="float-left"><s:if test="inAdmin">(Admin)&nbsp;</s:if>Data Dictionary</h1>
	<div style="clear:float"></div>
	<div id="main-content"  style="margin-top:15px;">
		<s:if test="%{formType == 'edit'}">
			<div id="breadcrumb">
				<s:if test="inAdmin"><s:a action="searchDataElementAction" method="list" namespace="/dictionaryAdmin">Manage Data Elements</s:a></s:if>
				<s:else><s:a action="searchDataElementAction" method="list" namespace="/dictionary">Search Data Elements</s:a></s:else>
				&gt;
				<s:url action="dataElementAction" method="view" var="viewTag">
					<s:param name="dataElementId">
						<s:property value="currentDataElement.id" />
					</s:param>
				</s:url>
				<a href="<s:property value="#viewTag" />"><s:property value="currentDataElement.title" /></a> &gt; Edit DataElement
			</div>
		</s:if>

		<s:form id="theForm" name="keywordForm" action="dataElementAction" method="post">
		<s:token />

			<s:if test="dataType == 'dataElement'">
				<ndar:dataElementChevron action="dataElementAction" chevron="Associate Keywords" />
			</s:if>
			<s:if test="dataType == 'mapElement'">
				<ndar:dataElementChevron action="mapElementAction" chevron="Associate Keywords" />
			</s:if>

			<h2>Associate Keywords: <s:property value="currentDataElement.title" /></h2>
			<p>Associating keywords to the data element promotes reuse and improves the ability to search for data elements.</p>

			<div class="clear-float"></div>

			<h3>Add Keywords</h3>
			<div class="form-field">
				<label for="keywordSearchKey">Filter Keywords:</label> <input class="textfield" name="keywordSearchKey"
					id="keywordSearchKey" type="text" onkeyup="searchKeywords();" onkeydown="javascript: if (event.keyCode == 13){ }" />
					<div class="button no-float">
						<input type="button" value="Add Keyword" onclick="addNewKeyword();" />
						<div id="newKeywordDiv"></div>
					</div>
			</div>

			<div class="form-field">
				<label>Sort By:</label>
				<div class="checkbox-horizontal">
					<label> <input type="radio" name="sortRadio" id="nameRadioButton" onClick="sortOptions()" checked /> <span>Name</span>
					</label> <label> <input type="radio" name="sortRadio" id="freqRadioButton" onClick="sortOptions()" /> <span>Frequency</span>
					</label>
				</div>
			</div>
			<div class="form-field">
				<select id="hiddenKeywords" style="display: none;">
				</select>
				<s:optiontransferselect label="" leftTitle="Available Keywords"
	     rightTitle="Current Keywords" name="availableKeywordsone" doubleName="keywordForm.keywordList" id="availableKeywords"
					list="availableKeywords" doubleList="currentKeywords" listKey="id + '_' + count + '_' + keyword"
					doubleListKey="id + '_' + count + '_' + keyword" listValue="keyword + '     (' + count + ')'"
					doubleListValue="keyword + '     (' + count + ')'" allowAddAllToLeft="false" allowAddAllToRight="false"
					allowSelectAll="false" allowUpDownOnLeft="false" allowUpDownOnRight="false" buttonCssClass="leftRightButton margin-left margin-right"
					cssClass="form-field" doubleCssClass="form-field currentKeywords"
					cssStyle="width: 300px; height: 150px; float: left;" doubleCssStyle="width: 300px; height: 150px; float: left;"
					addToLeftLabel="<<"
					addToRightLabel=">>"
					addToRightOnclick="sortOptions()"
					addToLeftOnclick="sortOptions()"
					></s:optiontransferselect>

			</div>
			<div class="clear-left"></div>

			<br />
			<s:if test="isAdmin">
			
			<h3>Add Labels</h3>
			<div class="form-field">
				<label for="labelSearchKey">Filter Labels:</label> <input class="textfield" name="labelSearchKey"
					id="labelSearchKey" type="text" onkeyup="searchLabels();" onkeydown="javascript: if (event.keyCode == 13){ }" />
					<div class="button no-float">
						<input type="button" value="Add Label" onclick="addNewLabel();" />
						<div id="newLabelDiv"></div>
					</div>
			</div>

			<div class="form-field">
				<label>Sort By:</label>
				<div class="checkbox-horizontal">
					<label> <input type="radio" name="sortLabelRadio" id="nameLabelRadioButton" onClick="sortLabelOptions()" checked /> <span>Name</span>
					</label> <label> <input type="radio" name="sortLabelRadio" id="freqLabelRadioButton" onClick="sortLabelOptions()" /> <span>Frequency</span>
					</label>
				</div>
			</div>

			<div class="form-field">
				<select id="hiddenLabels" style="display: none;">
				</select>
				<s:optiontransferselect label=""  leftTitle="Available Labels"
	     rightTitle="Current Labels" name="availableLabels" doubleName="keywordForm.labelList" id="availableLabels"
					list="availableLabels" doubleList="currentLabels" listKey="id + '_' + count + '_' + keyword"
					doubleListKey="id + '_' + count + '_' + keyword" listValue="keyword + '     (' + count + ')'"
					doubleListValue="keyword+ '     (' + count + ')'" allowAddAllToLeft="false" allowAddAllToRight="false"
					allowSelectAll="false" allowUpDownOnLeft="false" allowUpDownOnRight="false" buttonCssClass="leftRightButton margin-left margin-right"
					cssClass="form-field" doubleCssClass="form-field currentLabels"
					cssStyle="width: 300px; height: 150px; float: left;" doubleCssStyle="width: 300px; height: 150px; float: left;"
					addToLeftLabel="<<"
					addToRightLabel=">>"
					addToRightOnclick="sortLabelOptions()"
					addToLeftOnclick="sortLabelOptions()"
					></s:optiontransferselect>
				</div>

			<div class="clear-left"></div>

			<br />
				</s:if>
		</s:form>
	
		<div class="form-field clear-left">
					<div class="button">
						<input type="button" value="Continue"
							onClick="javascript:selectAllCurrentKeywords(); selectAllCurrentLabels(); submitForm('${dataType}Action!editStandardDetails.action')" />
					</div>
					<s:if test="%{formType=='edit'}">
						<a class="form-link" href="javascript:selectAllCurrentKeywords(); selectAllCurrentLabels(); submitForm('${dataType}Action!review.action')">Review</a>
					</s:if>
					<a class="form-link" href="javascript:cancel()">Cancel</a>
				</div>

		<%-- <div class="form-field clear-left">
			<div class="button">
				<input type="button" value="Save & Finish"
					onClick="selectAllCurrentKeywords(); selectAllCurrentLabels(); submitForm('${dataType}Action!submit.action');" />
			</div>
			<a class="form-link" href="javascript:cancel()">Cancel</a>
		</div>--%>
	</div>
</div>

<script type="text/javascript">
	<s:if test="!inAdmin">
		<s:if test="%{formType == 'create'}">
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"dataElementLink"});
		</s:if>
		<s:else>
			setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"searchDataElementLink"});
		</s:else>
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataDictionaryModuleLink", "subnavigationLinkID":"defineDataToolsLink", "tertiaryLinkID":"manageDataElementsLink"});
	</s:else>
	
	$(document).ready(function() {
		sortOptions();
		<s:if test="isAdmin">
		sortLabelOptions();
		</s:if>
		
		
		
		// Wrap the left and right buttons in divs
		$(".leftRightButton").wrap("<div class=button />");
	});
	
	function addNewKeyword() {
		
		
		//var keyword = $.trim($('input[name="keywordNew"]').val());
		var keyword = $.trim($("#keywordSearchKey").val());
		
		var options = $('.currentKeywords');
		
		// Uses an ajax call instead of a post and serializes the form data so that the call gets picked up by struts validation
		$.ajax({
			cache : false,
			url : "keywordInterface${dataType}ValidationAction!createKeyword.ajax",
			data : $("form").serializeArray(),
			success : function (data) {
				
				
				if (data.keyword == undefined) {
					
					$("#newKeywordDiv").html(data);
				}
				else {
					
					options.append($("<option />", { value : data.uri + "_" + data.count + "_" + data.keyword }).html(data.keyword + " (" + data.count + ")"));
					$("#keywordSearchKey")[0].value = "";
					$('.error-message').hide();
					
					
					moveAllOptions(
							$("#hiddenKeywords")[0], 
							$("#availableKeywords")[0], false, '');
				}
				// Sort the select boxes
				sortOptions();
			}
		});
	}
	
	
	function addNewLabel() {
		
		//var keyword = $.trim($('input[name="keywordNew"]').val());
		var keyword = $.trim($("#labelSearchKey").val());
		
		var options = $('.currentLabels');
		
		// Uses an ajax call instead of a post and serializes the form data so that the call gets picked up by struts validation
		$.ajax({
			cache : false,
			url : "labelInterface${dataType}ValidationAction!createLabel.ajax",
			data : $("form").serializeArray(),
			success : function (data) {
				if (data.keyword == undefined) {
					
					$("#newLabelDiv").html(data);
				}
				else {
					
					options.append($("<option />", { value : data.uri + "_" + data.count + "_" + data.keyword }).html(data.keyword + " (" + data.count + ")"));
					$("#labelSearchKey")[0].value = "";
					$('.error-message').hide();
					moveAllOptions(
							$("#hiddenLabels")[0], 
							$("#availableLabels")[0], false, '');
				}
				// Sort the select boxes
				sortLabelOptions();
			}
		});
	}
	

	//searches for keywords
	function searchKeywords()
	{
		var searchData = $('#keywordSearchKey').val();	//Get the text box where the user is typing in search letters
		
		// The moveOptions function is defined in optiontransferselect.js
		moveOptions($("#availableKeywords")[0], $("#hiddenKeywords")[0], false,
			function(opt) {
				if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1)
				{
					return false;
				}
				else
				{
					return true;
				}
		});
		moveOptions($("#hiddenKeywords")[0], $("#availableKeywords")[0], false,
			function(opt) {
				if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1)
				{
					return true;
				}
				else
				{
					return false;
				}		
		});
   		sortOptions();
	}
	
	
	//searches for keywords
	function searchLabels()
	{
		var searchData = $('#labelSearchKey').val();	//Get the text box where the user is typing in search letters
		
		// The moveOptions function is defined in optiontransferselect.js
		moveOptions($("#availableLabels")[0], $("#hiddenLabels")[0], false,
			function(opt) {
				if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1)
				{
					return false;
				}
				else
				{
					return true;
				}
		});
		moveOptions($("#hiddenLabels")[0], $("#availableLabels")[0], false,
			function(opt) {
				if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1)
				{
					return true;
				}
				else
				{
					return false;
				}		
		});
   		sortLabelOptions();
	}
	
	//Sort options in the lists based on name or frequency
	function sortOptions()
	{

		
		if ($("#nameRadioButton")[0].checked)
		{
			$("#avaiableKeywords").html($("#availableKeywords option").sort(function (a, b) {
				var aValue = a.value.split('_', 3)[2].toUpperCase();
				var bValue = b.value.split('_', 3)[2].toUpperCase();
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
			$(".currentKeywords").html($(".currentKeywords option").sort(function (a, b) {
				var aValue = a.value.split('_', 3)[2].toUpperCase();
				var bValue = b.value.split('_', 3)[2].toUpperCase();
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
		}
		else if ($("#freqRadioButton")[0].checked)
		{
			$("#availableKeywords").html($("#availableKeywords option").sort(function (a, b) {
			    var aValue = parseInt(a.value.split('_', 3)[1]);
			    var bValue = parseInt(b.value.split('_', 3)[1]);
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
			$(".currentKeywords").html($(".currentKeywords option").sort(function (a, b) {
			    var aValue = parseInt(a.value.split('_', 3)[1]);
			    var bValue = parseInt(b.value.split('_', 3)[1]);
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
		}
	}
	
	
	
	//Sort label options in the lists based on name or frequency
	function sortLabelOptions()
	{
		

		if ($("#nameLabelRadioButton")[0].checked)
		{
			$("#avaiableLabels").html($("#availableLabels option").sort(function (a, b) {
				var aValue = a.value.split('_', 3)[2].toUpperCase();
				var bValue = b.value.split('_', 3)[2].toUpperCase();
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
			$(".currentLabels").html($(".currentLabels option").sort(function (a, b) {
				var aValue = a.value.split('_', 3)[2].toUpperCase();
				var bValue = b.value.split('_', 3)[2].toUpperCase();
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
		}
		else if ($("#freqLabelRadioButton")[0].checked)
		{
			$("#availableLabels").html($("#availableLabels option").sort(function (a, b) {
			    var aValue = parseInt(a.value.split('_', 3)[1]);
			    var bValue = parseInt(b.value.split('_', 3)[1]);
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
			$(".currentLabels").html($(".currentLabels option").sort(function (a, b) {
			    var aValue = parseInt(a.value.split('_', 3)[1]);
			    var bValue = parseInt(b.value.split('_', 3)[1]);
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
		}
	}
	
	function selectAllCurrentKeywords() {
		 var list = $('.currentKeywords')[0];
		 
		 //If the list contains no options, insert one with the value: 'empty'
		 if (list.length ==0)
		 {
			
			 $(list).append($("<option />", { value : "empty" }));
		 }
		 for (var i = 0; i < list.options.length; i++) 
		   {
			
			 valueSplit = list.options[i].value.split("_");
			 if(valueSplit[1] != 'null' && valueSplit[2] != 'null') {
 	   
		    	list.options[i].selected = true;
			 }
		   }
	}
	
	function selectAllCurrentLabels() {
		
		<s:if test="isAdmin">
		 var list = $('.currentLabels')[0];
		 
		 //If the list contains no options, insert one with the value: 'empty'
		 if (list.length ==0)
		 {
			 $(list).append($("<option />", { value : "empty" }));
		 }
		 for (var i = 0; i < list.options.length; i++) 
		   {
			 valueSplit = list.options[i].value.split("_");
			 if(valueSplit[1] != 'null' && valueSplit[2] != 'null') {
//	 	    alert(list.options[i].value)
		    list.options[i].selected = true;
			 }
		   }
		</s:if>
	}
	
	//calls clear session to clear the data in session upon cancel
	function cancel() {
		var dataType = '<s:property value="dataType"/>';
		if(dataType=="mapElement") { 
			window.location = "dataStructureElementAction!moveToElements.action";
		} else if(dataType=="dataElement") {
			<s:if test="%{formType == 'create'}">
				window.location = "searchDataElementAction!list.action";
			</s:if>
			<s:else>
				window.location = "dataElementAction!view.action?dataElementId=<s:property value='currentDataElement.id' />";
			</s:else>
		}		
	}
	
	
</script>