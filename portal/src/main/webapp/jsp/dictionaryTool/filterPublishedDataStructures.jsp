<%@include file="/common/taglibs.jsp"%>

<s:set var="publishedDataStructurePage" value="page" />
<s:set var="publishedDataStructureNumPages" value="numPages" />

<s:if test="!dsDataStructureList.size==0">
	<div class="filter-results clear-float">
		<div class="form-field">
			<span class="pagination"><em><s:property value="numSearchResults" /> Results Found</em></span> <span> Page <s:property
					value="page" /> of <s:property value="numPages" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</span> <span class="pagination-arrows"> <s:if test="#publishedDataStructurePage != 1">
					<a href="javascript: publishedDataStructurePagination.page = 1; publishedDataStructureSearch();"><img
						alt="First" src="/portal/images/brics/common/icon_first.png" /></a>
					<a href="javascript: publishedDataStructurePagination.page--; publishedDataStructureSearch();"><img alt="Prev"
						src="/portal/images/brics/common/icon_back.png" /></a>
				</s:if> <s:else>
					<img alt="First" src="/portal/images/brics/common/icon_first_disabled.png" />
					<img alt="Prev" src="/portal/images/brics/common/icon_back_disabled.png" />
				</s:else> <input class="tiny" maxLength="100" type="text" value="${page}" id="publishedDataStructurePaginationJump"
				onkeydown="javascript: if (event.keyCode == 13){ publishedDataStructureCheckPageField(${numSearchResults} / ${pageSize}); return false;}" />
				<s:if test="#publishedDataStructurePage < #publishedDataStructureNumPages">
					<a href="javascript: publishedDataStructurePagination.page++; publishedDataStructureSearch();"><img alt="Next"
						src="/portal/images/brics/common/icon_forward.png" /></a>
					<a
						href="javascript: publishedDataStructurePagination.page = <s:property value="#publishedDataStructureNumPages" />; publishedDataStructureSearch();"><img
						alt="Last" src="/portal/images/brics/common/icon_last.png" /></a>
				</s:if> <s:else>
					<img alt="Next" src="/portal/images/brics/common/icon_forward_disabled.png" />
					<img alt="Last" src="/portal/images/brics/common/icon_last_disabled.png" />
				</s:else>
			</span>
		</div>
	</div>

	<table class="display-data full-width" cellSpacing="0" cellPadding="0">
		<tr>
			<!-- 	An array of the table headers. The first value in each pair is the name of the header and the second value is the string that will be used to sort the hibernate results -->
			<s:iterator var="name"
				value="{{'title', 'title'},{'version', 'version'}, {'disease', 'disease'}, {'status', 'status'}, {'modified date', 'modifiedDate'}}">
				<s:if test='#name[0] == "disease"'>
					<th>disease</th>
				</s:if>
				<s:else>
				<th><a href="javascript:publishedDataStructureSetSort('<s:property value='#name[1]' />')"> <s:property
							value="#name[0]" />
				</a> <s:if test="#name[1] == sort">
						<s:if test="ascending">
							<img src='<s:url value="/images/brics/common/icon-down.png"/>'>
						</s:if>
						<s:else>
							<img src='<s:url value="/images/brics/common/icon-up.png"/>'>
						</s:else>
					</s:if></th>
			</s:else>
			</s:iterator>
		</tr>
		<s:iterator var="publishedDataStructure" value="dsDataStructureList" status="rowstatus">
			<s:if test="#rowstatus.odd == true">
				<tr>
			</s:if>
			<s:else>
				<tr class="stripe">
			</s:else>
			<td><a class="" target="_blank" href="${modulesAccountURL}publicData/dataStructureAction!view.action?dataStructureId=${publishedDataStructure.id}&publicArea=true"><s:property value="#publishedDataStructure.title" /></a></td>
			
			<td><s:property value="version" /></td>
			<td><s:property value="diseaseStructureString" /></td>
			<td><s:property value="status.type" /></td>
			<td><ndar:dateTag value='${modifiedDate}' /></td>
			</tr>
		</s:iterator>
	</table>
</s:if>
<s:else>
	<div class="display-error">
		<br />
		<p>No results were found.</p>
	</div>
</s:else>
