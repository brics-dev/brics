<%@include file="/common/taglibs.jsp"%>
<title>Create Meta Study</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/metaStudyNavigation.jsp" />
	<h1 class="float-left">Meta Study</h1>
	<!--begin #center-content -->
	<div id="main-content">
		<h2>Create Meta Study</h2>			
			<s:form id="theForm" name="metaStudyKeywordForm" action="metaStudyAction" method="post">
			<s:token />
			<ndar:editMetaStudyChevron action="metaStudyAction" chevron="Keyword" />
			<s:if test="hasActionErrors()">
				<div class="error-message">
					<s:actionerror />
				</div>
			</s:if>
			<div class="clear-float"></div>

			<h3>Add Keywords</h3>
			<div class="form-field">
				<label for="keywordSearchKey">Filter Keywords:</label> <input class="textfield" name="keywordSearchKey" maxlength="55"
					id="keywordSearchKey" type="text" onkeyup="searchKeywords();" onkeydown="javascript: if (event.keyCode == 13){ }" />
					<div class="button no-float">
						<input type="button" value="Add Keyword" onclick="addNewKeyword();" />
						<div id="newKeywordDiv"></div>
					</div>
			</div>

			<div class="form-field">
				<label>Sort By</label>
				<div class="checkbox-horizontal">
					<label> <input type="radio" name="sortRadio" id="nameRadioButton" onClick="sortOptions()" checked/> <span>Name</span> </label>
					<label> <input type="radio" name="sortRadio" id="freqRadioButton" onClick="sortOptions()" /> <span>Frequency</span> </label>
				</div>
			</div>
			<div class="form-field">
				<select id="hiddenKeywords" style="display: none;">
				</select>
				<s:optiontransferselect label="" leftTitle="Available Keywords"
	     rightTitle="Current Keywords" name="availableKeywordsone" doubleName="metaStudyKeywordForm.keywordsList" id="availableKeywords"
					list="availableKeywords" doubleList="currentKeywords" listKey="count + '_' + keyword"
					doubleListKey="count + '_' + keyword" listValue="keyword + '     (' + count + ')'"
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
			<s:if test="isMetaStudyAdmin">
			
			<h3>Add Labels</h3>
			<div class="form-field">
				<label for="labelSearchKey">Filter Labels</label> <input class="textfield" name="labelSearchKey"
					id="labelSearchKey" type="text" onkeyup="searchLabels();" onkeydown="javascript: if (event.keyCode == 13){ }" />
					<div class="button no-float">
						<input type="button" value="Add Label" onclick="addNewLabel();" />
						<div id="newLabelDiv"></div>
					</div>
			</div>

			<div class="form-field">
				<label>Sort By</label>
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
	     rightTitle="Current Labels" name="availableLabels" doubleName="metaStudyKeywordForm.labelsList" id="availableLabels"
					list="availableLabels" doubleList="currentLabels" listKey="count + '_' + label"
					doubleListKey="count + '_' + label" listValue="label + '     (' + count + ')'"
					doubleListValue="label+ '     (' + count + ')'" allowAddAllToLeft="false" allowAddAllToRight="false"
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
				
			<div class="form-field" style="display: inline-block;">
				<div class="button">
					<input type="button" value="Back" 
					onClick="javascript:selectAllCurrentFields(); submitForm('metaStudyAction!moveToData.action')" />
				</div>
			</div>

			<div class="form-field inline-right-button">
				<div class="button btn-primary" style="margin-right: 10px;">
					<input type="button" value="Create & Finish" 
						onclick="javascript:selectAllCurrentFields(); createMetaStudy()" />
				</div>
				<div class="button" style="margin-right: 5px;">
					<input type="button" value="Preview" 
					onClick="javascript:selectAllCurrentFields(); submitForm('metaStudyAction!moveToPreview.action')" />
				</div>
				<a class="form-link" href="javascript:void(0)" onclick="javascript:cancelCreation()">Cancel</a>
			</div>
		</s:form>
		
	    <div class="ibisMessaging-dialogContainer"></div>
	</div>
</div>

<script type="text/javascript" src="/portal/js/metastudy/metaStudy.js"></script>
<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"metaStudyModuleLink", "subnavigationLinkID":"metaStudyLink", "tertiaryLinkID":"createMetaStudyLink"});
	
	$(document).ready(function() {
		sortOptions();
		<s:if test="isMetaStudyAdmin">
		sortLabelOptions();
		</s:if>
		
		// Wrap the left and right buttons in divs
		$(".leftRightButton").wrap("<div class=button />");
	});
	
	
	function selectAllCurrentFields() {
		selectAllCurrentKeywords(); 
		
		<s:if test="isMetaStudyAdmin">
			selectAllCurrentLabels();
		</s:if>
	}
	
</script>