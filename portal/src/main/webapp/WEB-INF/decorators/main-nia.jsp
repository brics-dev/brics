<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>


<!--[if lte IE 8]>    <html class="lt-ie9" xmlns="http://www.w3.org/1999/xhtml"> <![endif]-->

<!--[if gt IE 8]><!--> <html xmlns="http://www.w3.org/1999/xhtml"> <!--<![endif]-->

<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<title><decorator:title /> : NIA GUID : National Institute on Aging and Data Repository Global Unique Identifier</title>

<link href="<s:url value='/config/nia/style.css'/>" rel="stylesheet" type="text/css" media="all" />
<link href="<s:url value='/config/nia/images/favicon.ico'/>" rel="icon" />

<jsp:include page="/common/script-includes.jsp" />
<decorator:head />

</head>
<body>
<jsp:include page="/common/envBanner.jsp" />
	<div id="page-container">
		<jsp:include page="/config/nia/header.jsp" />

		<!-- Only show if you come from base action so URLs will go to the correct location -->
		<s:if test="user neq null">
			<jsp:include page="/common/navigation.jsp" />
		</s:if>
		<div id="content-wrapper">
			<div class="content">
				<decorator:body />
			</div>
		</div>
		<div id="footerDiv">
			<jsp:include page="/config/nia/footer.jsp" />
		</div>
	</div>
</body>
</html>