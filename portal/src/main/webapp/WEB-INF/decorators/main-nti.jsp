<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>


<!--[if lte IE 8]>    <html class="lt-ie9" xmlns="http://www.w3.org/1999/xhtml" lang="en"> <![endif]-->

<!--[if gt IE 8]><!--> <html xmlns="http://www.w3.org/1999/xhtml" lang="en"> <!--<![endif]-->

<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge" />
<title><decorator:title /> : NTRR : National Trauma Research Repository</title>

<link href="<s:url value='/config/nti/style.css'/>" rel="stylesheet" type="text/css" media="all" />
<link href="<s:url value='/config/nti/images/favicon.ico'/>" rel="icon" />
<jsp:include page="/config/nti/google-analytics.jsp" />
<jsp:include page="/common/script-includes.jsp" />
<decorator:head />

</head>
<body>
	<!-- Google Tag Manager (noscript) -->
	<noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-K2MLJ55"
	height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
	<!-- End Google Tag Manager (noscript) -->
	
	<jsp:include page="/common/envBanner.jsp" />
	<div id="page-container">
	<s:if test="eFormModule eq null">
			<jsp:include page="/config/nti/header.jsp" />
	</s:if>
	<s:elseif test="eFormModule eq false">
			<jsp:include page="/config/nti/header.jsp" />
	</s:elseif>
	<s:else>
		
	</s:else>

		<!-- Only show if you come from base action so URLs will go to the correct location -->
		<s:if test="user neq null">
		<s:set var="nameSpace" value="nameSpace"/>
		<s:if test="%{#nameSpace !='publicData'}">
			<jsp:include page="/common/navigation.jsp" />
		</s:if>
		</s:if>
		





		<div id="content-wrapper">
			<div class="content">
				<s:if test="eFormModule eq true">
					<jsp:include page="/common/messages.jsp" />
				</s:if>
				<decorator:body />
			</div>
		</div>
		<div id="footerDiv">
			<jsp:include page="/config/nti/footer.jsp" />
		</div>
	</div>
</body>
</html>