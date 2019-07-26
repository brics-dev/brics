<%@page contentType="text/html;charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<html>
<head>
	<META  http-equiv="Content-Type"  content="text/html;charset=UTF-8">
	<title><fmt:message key="welcome.title" /></title>
	<link rel="stylesheet"
		href="<c:url value="/resources/blueprint/screen.css" />"
		type="text/css" media="screen, projection">
	<link rel="stylesheet"
		href="<c:url value="/resources/blueprint/print.css" />" type="text/css"
		media="print">
	<!--[if lt IE 8]>
			<link rel="stylesheet" href="<c:url value="/resources/blueprint/ie.css" />" type="text/css" media="screen, projection">
		<![endif]-->
</head>
	<body>
		<div class="container">
			<h1>
				<fmt:message key="scheduler.title" />
			</h1>
			<p>
				You can manually trigger the delete download dataset job here
			</p>	
			<hr>
			<form:form method="post">
				<input type="submit" value="Trigger Delete Download Dataset Job"/>
			</form:form>
		</div>
	</body>
</html>