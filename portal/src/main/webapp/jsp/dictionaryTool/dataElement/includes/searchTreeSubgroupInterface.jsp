<%@include file="/common/taglibs.jsp"%>

<s:if test="%{subgroupOptions.size > 1}">

<b>Sub-Diseases</b>
			<ul class="leaf">	
	<s:iterator value="subgroupOptions">
			<li>
				<s:checkbox id="subgroup%{key}" cssClass="subgroupCheckBox" name="selectedSubgroups" 
						fieldValue="%{value}" value="%{value in sessionCriteria.supgroups}"  /> 
				<s:label for="subgroup%{key}"><s:property value="key" /></s:label>
			</li>
		</s:iterator>
		</ul>

<%-- <s:select id="subgroupSelection" name="subgroupSelection" list="subgroupOptions" listKey="subgroupName" listValue="subgroupName" headerKey="" headerValue="ANY" value="subgroup" cssClass="large"/>&nbsp;subgroup.
 --%>
</s:if>
