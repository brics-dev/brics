<%@include file="/common/taglibs.jsp"%>

	<h2>Associate Keywords</h2>
	<p>Associating keywords to the study promotes reuse and improves the search capability.</p>
	
	<div class="form-field">
		<label for="keywordSearchKey">Filter Keywords:</label> 
		<input class="textfield" name="keywordSearchKey" id="keywordSearchKey" type="text" maxlength="55" 
				onkeyup="searchKeywords();" onkeydown="javascript: if (event.keyCode == 13) { }" />
		<div class="button no-float">
			<input type="button" value="Add Keyword" onclick="addNewKeyword();" />
			<div id="newKeywordDiv"></div>
		</div>
		<s:fielderror fieldName="keywordSearchKey" />
	</div>

	<div class="form-field">
		<label>Sort By:</label>
		<div class="checkbox-horizontal">
			<label> <input type="radio" name="sortRadio" id="nameRadioButton" onClick="sortOptions()" checked /> <span>Name</span></label> 
			<label> <input type="radio" name="sortRadio" id="freqRadioButton" onClick="sortOptions()" /> <span>Frequency</span></label>
		</div>
	</div>
	<div class="form-field">
		<select id="hiddenKeywords" style="display: none;"></select>
		<s:optiontransferselect label="" leftTitle="Available Keywords" rightTitle="Current Keywords" name="availableKeywordsone" 
			doubleName="sessionStudy.keywordList" id="availableKeywords"
				list="availableKeywords" doubleList="currentKeywords" listKey="count + '_' + keyword"
				doubleListKey="count + '_' + keyword" listValue="keyword + '     (' + count + ')'"
				doubleListValue="keyword + '     (' + count + ')'" allowAddAllToLeft="false" allowAddAllToRight="false"
				allowSelectAll="false" allowUpDownOnLeft="false" allowUpDownOnRight="false" buttonCssClass="leftRightButton margin-left margin-right"
				cssClass="form-field" doubleCssClass="form-field currentKeywords"
				cssStyle="width: 300px; height: 150px; float: left; overflow-y:scroll;" doubleCssStyle="width: 300px; height: 150px; float: left;"
				addToLeftLabel="<<" addToRightLabel=">>"
				addToRightOnclick="sortOptions()" addToLeftOnclick="sortOptions()">
		</s:optiontransferselect>
	</div>
	<div class="clear-left"></div>
	
<script type="text/javascript">
	$(document).ready(function() {
		sortOptions();
		
		// Wrap the left and right buttons in divs
		$(".leftRightButton").wrap("<div class=button />");
	});
	
	
	function addNewKeyword() {
		var keyword = $.trim($("#keywordSearchKey").val());
		var options = $('.currentKeywords');
		
		$.ajax({
			cache : false,
			url : "keywordValidationAction!createKeyword.ajax",
			data : $("form").serializeArray(),
			success : function (data) {
				if (data.keyword == undefined) {
					$("#keywords").html(data);
				} else {
					options.append($("<option />", { value: data.count + "_" + data.keyword }).html(data.keyword + " (" + data.count + ")"));
					$("#keywordSearchKey")[0].value = "";
					$('.error-message').hide();
					
					moveAllOptions(
						$("#hiddenKeywords")[0], 
						$("#availableKeywords")[0], false, '');
				}

				sortOptions();
			}
		});
	}

	// searches for keywords
	function searchKeywords() {
		var searchData = $('#keywordSearchKey').val();	
		
		// The moveOptions function is defined in optiontransferselect.js
		moveOptions($("#availableKeywords")[0], $("#hiddenKeywords")[0], false,
			function(opt) {
				if (opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1) {
					return false;
				} else {
					return true;
				}
		});
		
		moveOptions($("#hiddenKeywords")[0], $("#availableKeywords")[0], false,
			function(opt) {
				if(opt.value.toUpperCase().indexOf(searchData.toUpperCase()) != -1) {
					return true;
				} else {
					return false;
				}		
		});
   		sortOptions();
	}
	
	
	// Sort options in the lists based on name or frequency
	function sortOptions() {
		
		if ($("#nameRadioButton")[0].checked) {
			$("#availableKeywords").html($("#availableKeywords option").sort(function (a, b) {
				var aValue = a.value.split('_', 2)[1].toUpperCase();
				var bValue = b.value.split('_', 2)[1].toUpperCase();
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
			$(".currentKeywords").html($(".currentKeywords option").sort(function (a, b) {
				var aValue = a.value.split('_', 2)[1].toUpperCase();
				var bValue = b.value.split('_', 2)[1].toUpperCase();
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
			
		} else if ($("#freqRadioButton")[0].checked) {
			$("#availableKeywords").html($("#availableKeywords option").sort(function (a, b) {
			    var aValue = parseInt(a.value.split('_', 2)[0]);
			    var bValue = parseInt(b.value.split('_', 2)[0]);
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
			$(".currentKeywords").html($(".currentKeywords option").sort(function (a, b) {
			    var aValue = parseInt(a.value.split('_', 2)[0]);
			    var bValue = parseInt(b.value.split('_', 2)[0]);
			    return aValue == bValue ? 0 : aValue < bValue ? -1 : 1;
			}));
		}
	}
	
	function selectAllCurrentKeywords() {
		var list = $('.currentKeywords')[0];
		 
		// If the list contains no options, insert one with the value: 'empty'
		if (list.length == 0) {
			$(list).append($("<option />", { value : "empty" }));
		}
		 
		for (var i = 0; i < list.options.length; i++) {
			valueSplit = list.options[i].value.split("_");
			if (valueSplit[1] != 'null' && valueSplit[2] != 'null') {
				list.options[i].selected = true;
			}
		}
	}
	
</script>
