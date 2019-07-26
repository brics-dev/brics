<%@ tag body-content="scriptless"%>
<%@ attribute name="action" required="true"%>
<%@ attribute name="chevron" required="false" type="java.lang.String"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<ul class="workflow">
	<li class="<c:if test="${chevron == 'Details'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToDetails.action');">1. Details</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Documentation'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToDocumentation.action');">2. Documentation</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Data'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToData.action');">3. Data Artifact</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Keyword'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToKeyword.action');">4. Keywords and Labels</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Preview'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!moveToPreview.action');">5. Preview</a>
	</span></li>
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
		
		if($('[name="metaStudyKeywordForm"]').length){
			selectAllCurrentFields();
		}
		
		if (action) {
			theForm.action = action;
		}
		theForm.submit();
	}
</script><%@ tag language="java" pageEncoding="ISO-8859-1"%>
