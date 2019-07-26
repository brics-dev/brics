<%@include file="/common/taglibs.jsp"%>
<%@ tag body-content="scriptless"%>
<%@ attribute name="action" required="true"%>
<%@ attribute name="chevron" required="false" type="java.lang.String"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<ul class="workflow">
	<li class="<c:if test="${chevron == 'Edit Details'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!editDetails.action');">1. Basic Information</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Documentations'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!editDocumentation.action');">2. Documentation</a>
	</span></li>
	<li
		class="<c:if test="${chevron == 'Define the Data'}">active-workflow</c:if> <c:if test="${isPublished}">completed-workflow</c:if>">
		<span> 
				<a href="javascript: submitChevronForm('${action}!editValueRange.action');">3. Attributes</a>
    	</span>
	</li>
	<li class="<c:if test="${chevron == 'Associate Keywords'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!editKeywords.action');">4. Keywords and Labels</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Step Four'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!editStandardDetails.action');">5. Details</a>
	</span></li>
	<li class="<c:if test="${chevron == 'Review'}">active-workflow</c:if>"><span> <a
			href="javascript: submitChevronForm('${action}!review.action');">6. Review</a>
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
		if (action) {
			theForm.action = action;
		}

		<s:if test="%{#inValueRangeForm != '' && #inValueRangeForm != null}">
		
		
			if(!verifyDomainPairing()){
				return;
			}; 
			if(!confirmDomainPairSelection()){ return; };
		</s:if>
		if($('[name="keywordForm"]').length){
			selectAllCurrentKeywords(); selectAllCurrentLabels();
			
		}
		
		//verify date range
		//until date cannot be before effdate
		if($('#effdate').val() != '' ){ 
			if(new Date($('#effdate').val()) > new Date($('#untildate').val())) {
				alert("The until date must be after the effective date.");
				for (a = 0; a < b; a++) {
					if (c[a].type == "button") {
						c[a].disabled = false
					}
				}
				
				 $('body').animate({scrollTop:$('#untildate').offset().top - 30}, 200);
				return false;
			} 
		}
		
		theForm.submit();
	}
</script>