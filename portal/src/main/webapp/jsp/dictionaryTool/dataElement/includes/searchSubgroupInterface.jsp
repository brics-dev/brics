<%@include file="/common/taglibs.jsp"%>

<s:if test="%{subgroupOptions.size > 1}">
in <s:select id="subgroupSelection" name="subgroupSelection" list="subgroupOptions" listKey="subgroupName" listValue="subgroupName" headerKey="" headerValue="ANY" value="subgroup" cssClass="large"/>&nbsp;subgroup.
</s:if>
<s:else>
.
</s:else>