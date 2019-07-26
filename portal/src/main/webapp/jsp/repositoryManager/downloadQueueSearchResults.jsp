<%@include file="/common/taglibs.jsp"%>

<s:set var="page" value="page" />
<s:set var="numPages" value="numPages" />

<s:if test="!userQueue.size==0">
	<div class="filter-results clear-float">
		<div class="form-field">
			<span class="pagination"><em><s:property value="numSearchResults" /> Results Found</em></span> <span> Page <s:property
					value="page" /> of <s:property value="numPages" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</span> <span class="pagination-arrows"> <s:if test="#page != 1">
					<a href="javascript: pagination.page = 1; search();"><img alt="First"
						src="<s:url value='/images/brics/common/icon_first.png' />" /></a>
					<a href="javascript: pagination.page--; search();"><img alt="Prev"
						src="<s:url value='/images/brics/common/icon_back.png' />" /></a>
				</s:if> <s:else>
					<img alt="First" src="<s:url value='/images/brics/common/icon_first_disabled.png' />" />
					<img alt="Prev" src="<s:url value='/images/brics/common/icon_back_disabled.png' />" />
				</s:else> <input class="tiny" maxLength="100" type="text" value="${page}" id="paginationJump"
				onkeydown="javascript: if (event.keyCode == 13){ checkPageField(${numSearchResults} / ${pageSize}); return false;}" />
				<s:if test="#page < #numPages">
					<a href="javascript: pagination.page++; search();"><img alt="Next"
						src="<s:url value='/images/brics/common/icon_forward.png' />" /></a>
					<a href="javascript: pagination.page = <s:property value="#numPages" />; search();"><img
						alt="Last" src="<s:url value='/images/brics/common/icon_last.png' />" /></a>
				</s:if> <s:else>
					<img alt="Next" src="<s:url value='/images/brics/common/icon_forward_disabled.png' />" />
					<img alt="Last" src="<s:url value='/images/brics/common/icon_last_disabled.png' />" />
				</s:else>
			</span>
		</div>
	</div>
</s:if>
<s:else>
	<div class="display-error">
		<br />
		<p>Your download queue is empty.</p>
	</div>
</s:else>