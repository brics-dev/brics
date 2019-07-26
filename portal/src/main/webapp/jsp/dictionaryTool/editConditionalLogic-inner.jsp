<%@include file="/common/taglibs.jsp"%>
<s:bean name="gov.nih.tbi.dictionary.model.ConditionalLogicForm" var="conditionalLogicForms" />

<s:form id="theForm" cssClass="validate" validate="true" action="conditionalLogicValidationAction" method="post">
<s:token />
	<p class="form-field">
		If&nbsp;
		<s:select list="mapElementList" listKey="id" listValue="mapElementNameWithGroup"
			name="conditionalLogicForm.mapElementId" value="selectedMapElement.id" headerKey=""
			headerValue="- Select a Data Element -" />
		&nbsp;is
		<c:if test="${operatorList == null}">
			<select class="short-select" disabled="disabled">
				<option>=</option>
			</select>
		</c:if>
		<c:if test="${operatorList != null}">
			<s:select cssClass="short-select" list="operatorList" listKey="id" listValue="name"
				name="conditionalLogicForm.operator" value="conditionalLogicForm.operator.id" />
		</c:if>

		<c:choose>
			<c:when test="${type == 'text'}">
				<s:textfield cssClass="textfield" maxlength="200" name="conditionalLogicForm.value" escapeHtml="true" escapeJavaScript="true" />
			</c:when>
			<c:when test="${type == 'file'}">
				<s:textfield cssClass="textfield" maxlength="200" name="conditionalLogicForm.value" escapeHtml="true" escapeJavaScript="true" />
			</c:when>
			<c:when test="${type == 'select'}">
				<s:select list="selectedMapElement.valueRangeList" listKey="valueRange" listValue="valueRange"
					value="conditionalLogicForm.value" name="conditionalLogicForm.value" />
			</c:when>
			<c:when test="${type == 'date'}">
				<s:textfield cssClass="date-picker small textfield" name="conditionalLogicForm.value" escapeHtml="true" escapeJavaScript="true" />
			</c:when>
			<c:otherwise>
				<s:textfield cssClass="textfield" disabled="true" escapeHtml="true" escapeJavaScript="true" />
			</c:otherwise>
		</c:choose>
		<br /> <br /> Then ${currentMapElement.name} is <strong>conditionally ${conditionType}</strong>.
	</p>

	<div class="button">
		<input id="save-button" type="button" onClick="submitCondition()" value="Save" />
	</div>
	<a id="cancel-link" href="#" class="form-link">Cancel</a>
</s:form>
</form>
<script type="text/javascript">
	
$(document).ready(function() {
	$("#theForm_conditionalLogicForm_mapElementId").change(function() {
		
		var mapElementId = $("#theForm_conditionalLogicForm_mapElementId").val();
		
		$.post(	"conditionalLogicAction!viewInner.ajax",
			{ mapElementId:mapElementId }, 
			function (data) {
				$("#main").html(data);
			}
		);
	});
	
	<c:if test="${type == 'date'}">
		$(".date-picker").each(function() {
			$(this).datepicker({ 
				buttonImage: "/portal/images/brics/common/icon-cal.gif", 
				buttonImageOnly: true ,
				buttonText: "Select Date", 
				changeMonth: true,
				changeYear: true,
				duration: "fast",
				gotoCurrent: true,
				hideIfNoPrevNext: true,
				showOn: "both",
				showAnim: "blind",
				yearRange: '-120:+5'
			});
		}).attr("readonly", "readonly");
	</c:if>
	

	
	$("#cancel-link").click(function() {
		$.fancybox.close();
	});
});

function submitCondition() {
	$.ajax({
		type: "POST",
		cache: false,
		url: "conditionalLogicValidationAction!submit.ajax",
		data: $("form").serializeArray(),
		success: function(data) {
			if (data == "success")
			{
				window.location.replace("dataStructureAction!moveToElements.action");
			}
			else
			{
				$.fancybox(data);
			}
		}
	});
}
</script>