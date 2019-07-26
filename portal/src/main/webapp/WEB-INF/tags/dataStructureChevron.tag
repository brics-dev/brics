<%@ tag body-content="scriptless"%>
<%@ attribute name="action" required="true"%>
<%@ attribute name="chevron" required="false" type="java.lang.String"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<ul class="workflow">
	<li class="<c:if test="${chevron == 'Edit Details'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToDetails.action');">1. Basic Information</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Upload Documentations'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToDocumentations.action');">2. Documentation</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Attach Elements'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToElements.action');">3. Data Elements</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Grant Permissions'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToPermissions.action');">4. Permissions</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Review Structure'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToReview.action');">5. Review</a>
	</span></li>
</ul>


<script type="text/javascript">
	//generic submit form method, submits the given form with the given action
	//also disables any buttons so that the user cannot perform a 'double' submit
	function submitChevronForm(action) {

		var theForm = document.forms["dataStructureForm"];	
			
		if (theForm==undefined)
		{
			//This is a fix to resolve the redirect from generic search page 
			var correctedAction=action.replace("Validation",""); 		
			self.location=correctedAction;
		}
		
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
</script>