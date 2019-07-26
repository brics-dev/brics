<%@include file="/common/taglibs.jsp"%>

<h3>Conditional Logic</h3>

<div id="main" class="lightbox-narrow"></div>

<s:fielderror fieldName="conditionalLogicForm.operator" />
<s:fielderror fieldName="conditionalLogicForm.mapElement" />
<s:fielderror fieldName="conditionalLogicForm.value" />
<s:if test="hasActionErrors()">
	<div class="form-error clear-both">
		<s:actionerror />
	</div>
</s:if>

<script type="text/javascript">
	
$(document).ready(function() {
	$.post(	"conditionalLogicAction!viewInner.ajax",
		{ 
			<s:if test="currentCondition.mapElement != null">
				mapElementId:${currentCondition.mapElement.id} 
			</s:if>
			<s:else>
				failedValidation:true
			</s:else>
		}, 
		function (data) {
			$("#main").html(data);
		}
	);
});
	
</script>
