<%@ tag body-content="scriptless"%>
<%@ attribute name="action" required="true"%>
<%@ attribute name="method" required="false"%>
<%@ attribute name="lastButtonSet" required="false"%>
<%@ attribute name="shortNameNeededSet" required="false"%>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="form-field">
	<c:if test="${lastButtonSet == null || lastButtonSet == false}">
		<c:choose>
	      	<c:when test="${shortNameNeededSet == true}">
				<div class="button">
					<c:if test='${method != ""}'>
						<input type="button" value="Change Short Name" onclick="javascript: submitDataStructureForm('${action}!${method}.action');" />
					</c:if>
					<c:if test='${method == ""}'>
						<input type="button" value="Change Short Name" onclick="javascript: submitDataStructureForm('${action}.action');" />
					</c:if>
				</div>
			</c:when>
			<c:otherwise>
				<div class="button">
					<c:if test='${method != ""}'>
						<input type="button" value="Continue" onclick="javascript: submitDataStructureForm('${action}!${method}.action');disableCancelLink();" />
					</c:if>
					<c:if test='${method == ""}'>
						<input type="button" value="Continue" onclick="javascript: submitDataStructureForm('${action}.action');disableCancelLink();" />
					</c:if>
				</div>
				<s:if test="%{!sessionDataStructure.newStructure}">
					<a class="form-link" href="#" onClick="javascript: submitDataStructureForm('${action}!moveToReview.action');">Review</a>
				</s:if>
			</c:otherwise>
		</c:choose>
	</c:if>
	<c:if test="${lastButtonSet == true}">
		<div class="button">
			<input type="button" value="Finish" onclick="javascript: submitDataStructureForm('${action}!saveAndFinish.action');" />
		</div>
	</c:if>

	<div id="createCancelId">
		<a class="form-link" href="#" onClick="parent.location='dataStructureAction!cancel.action'">Cancel</a>
	</div>
</div>

<script type="text/javascript">
	//generic submit form method, submits the given form with the given action
	//also disables any buttons so that the user cannot perform a 'double' submit
	function submitDataStructureForm(action) {
		
		theForm = document.forms["dataStructureForm"];
		
		var disableButtons = document.getElementsByTagName('input');
		var i;
		var length = disableButtons.length;
		for (i = 0; i < length; i++) {
			if (disableButtons[i].type == 'button') {
				disableButtons[i].disabled = true;
			}
		}
		if (action) {
			theForm.action = action;
		}
		theForm.submit();
	}
	function disableCancelLink(){
		theCancelLink =  $("#createCancelId").children("a");
		theCancelLink.removeAttr("onclick").attr("href", "javascript:void(0)");
		theCancelLink.css("color", "grey");
	}
</script>