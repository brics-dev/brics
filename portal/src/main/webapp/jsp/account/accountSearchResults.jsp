<%@include file="/common/taglibs.jsp"%>

<s:set var="accountPage" value="page" />
<s:set var="accountNumPages" value="numPages" />

<!--   -->
<s:if test="!searchAccountList.size==0">
	<div class="filter-results clear-float">
		<div class="form-field">
			<span class="pagination"><em><s:property value="numSearchResults" /> Results Found</em></span> <span> Page <s:property
					value="page" /> of <s:property value="numPages" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</span> <span class="pagination-arrows"> <s:if test="#accountPage != 1">
					<a href="javascript: accountPagination.page = 1; accountSearch();"><img alt="First"
						src="<s:url value='/images/brics/common/icon_first.png' />" /></a>
					<a href="javascript: accountPagination.page--; accountSearch();"><img alt="Prev"
						src="<s:url value='/images/brics/common/icon_back.png' />" /></a>
				</s:if> <s:else>
					<img alt="First" src="<s:url value='/images/brics/common/icon_first_disabled.png' />" />
					<img alt="Prev" src="<s:url value='/images/brics/common/icon_back_disabled.png' />" />
				</s:else> <input class="tiny" maxLength="100" type="text" value="${page}" id="accountPaginationJump"
				onkeydown="javascript: if (event.keyCode == 13){ accountCheckPageField(${numSearchResults} / ${pageSize}); return false;}" />
				<s:if test="#accountPage < #accountNumPages">
					<a href="javascript: accountPagination.page++; accountSearch();"><img alt="Next"
						src="<s:url value='/images/brics/common/icon_forward.png' />" /></a>
					<a href="javascript: accountPagination.page = <s:property value="#accountNumPages" />; accountSearch();"><img
						alt="Last" src="<s:url value='/images/brics/common/icon_last.png' />" /></a>
				</s:if> <s:else>
					<img alt="Next" src="<s:url value='/images/brics/common/icon_forward_disabled.png' />" />
					<img alt="Last" src="<s:url value='/images/brics/common/icon_last_disabled.png' />" />
				</s:else>
			</span>
		</div>
		<ndar:accountTable accountList="${searchAccountList}" />
	</div>
	
	
</s:if>
<s:else>
	<div class="display-error">
		<br />
		<p>No results were found.</p>
	</div>
</s:else>

