<%@ tag body-content="scriptless"%>
<%@ attribute name="action" required="true"%>
<%@ attribute name="chevron" required="false" type="java.lang.String"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<ul class="workflow">
	<li class="<c:if test="${chevron == 'Edit Details'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToDetails.action');">1. Edit Details</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Manage Documentation'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToDocumentation.action');">2. Manage Documentation</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Manage Datasets'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToDataset.action');">3. Manage Datasets</a>
	</span></li>
	<c:if test="${isAdmin}">
		<li class="<c:if test="${chevron == 'Grant Permissions'}">active-workflow</c:if>"><span> <a
				href="javascript: submitChevronForm('${action}!moveToPermissions.action');">4. Grant Permissions</a>
		</span></li>
	</c:if>
</ul>


<script type="text/javascript">
	//generic submit form method, submits the given form with the given action
	//also disables any buttons so that the user cannot perform a 'double' submit
	function submitChevronForm(action) {
		
		var theForm = document.forms["theForm"];
		
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
</script><%@ tag language="java" pageEncoding="ISO-8859-1"%>
