<%@include file="/common/taglibs.jsp"%>

<form id="theForm">
	<div class="flex-vertical">
	<br>
	<p>
	This action will send an automated email to the user providing next steps on necessary
	action to take to renew their account. You must also provide a more detailed comment <strong>to
	be sent to the user</strong>  and stored in the Account Action History in order to continue. The
	user will have access to their system privileges until the module in question expires.
	</p>
	<br><br>

	<c:forEach var="accountGuidanceEmail" items="${accountMessageTemplates}">
		
		<div class="flex-no-wrap flex-checkbox accountEmailsOption">
			<input type="checkbox" style="height: 1.5em"  name="accountGuidanceEmail" ${accountGuidanceEmail.defaultChecked ? 'checked' :''} />
			<label for="${accountGuidanceEmail.checkboxText}" class="no-float "><c:out value="${accountGuidanceEmail.checkboxText}"/></label>
		    <input type="hidden" id="accountEmailMsgId_<c:out value="${accountGuidanceEmail.id}"/>" class="hiddenId" value=<c:out value="${accountGuidanceEmail.id}"/> />
		    <c:if test="${accountGuidanceEmail.message==''}">
		        	<span class="required">* </span> <s:textarea class="accountEmailMsg"  cols="40" rows="5"/>
		    </c:if>
	    </div>
	    <br>
	    
	</c:forEach>
	
	</div>
</form>