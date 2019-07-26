<%@include file="/common/taglibs.jsp"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<div class="lightbox-content-wrapper">
	<h3>Add Saved Query</h3>
	
	<s:form id="addSavedQueryForm" class="validate" method="post" action="metaStudyDataAction!uploadData">
		
		<s:hidden id="isEditingData"  name="isEditingData" />
		<div class="form-field">
			<label for="selectQueryBtn" class="required">Saved Query <span class="required">* </span>
			</label>
			<input id="selectQueryBtn" type="button" value="Select" style="padding: 2px 10px" 
					onClick="javascript:openSelectSavedQueryDialog()" />&nbsp;
					
			<s:if test="savedQueryName != null && savedQueryName != ''"> 
				<s:property value="savedQueryName" />
			</s:if>
			<s:else>
				No saved query selected. 
			</s:else>
			
			<s:hidden name="savedQueryId" />
			<s:hidden name="savedQueryName" />
			<s:hidden name="editSavedQueryId" />
			<s:fielderror fieldName="savedQueryName" />
		</div>
		
		<div class="form-field">
			<label class="required">Type&nbsp;&nbsp;</label>Saved Query
		</div>
		
		<div class="form-field">
			<label for="dataDescription" class="required">Description <span class="required">* </span>
			</label>
			<s:textarea id="dataDescription" name="dataDescription" cssClass="textfield required"
					cols="30" rows="5" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="dataDescription" />
		</div>
		
		<div class="form-field">
			<div class="button">
				<input type="button" class="submit" value="Save" onclick="addSavedQueryData()" />
			</div>
			<a class="form-link" href="#" onclick="$.fancybox.close();">Cancel</a>
		</div>
			
	</s:form>
</div>


<script type="text/javascript">

	$(document).ready(function() {
		$("#dataDescription").bind("keyup", function() { 
			checkTextareaMaxLength(this.id, 1000); 
		});
	});
	
	function openSelectSavedQueryDialog() {
		var isEditingData = $('#isEditingData').val();
		
		$.ajax({
			type: "POST",
			cache: false,
			url: "metaStudyDataAction!selectSavedQueryDialog.ajax",
			data: { 'isEditingData': isEditingData },
			success: function(data) {
				$.fancybox({
					autoDimensions: false,
					content : data,
					width : 900,
					height: 'auto',
					onComplete : function() {
						$("#fancybox-content > div").width(901);
						$("#fancybox-content").height('auto');

					}
				});
			}
		});
	}
</script>


