<%@include file="/common/taglibs.jsp"%>

<c:import var="xmlDoc" url="${grantUrl}" charEncoding="UTF8"/>
<c:import var="xslDoc" url="grantInfo.xsl" />
<x:transform doc="${xmlDoc}" xslt="${xslDoc}" />