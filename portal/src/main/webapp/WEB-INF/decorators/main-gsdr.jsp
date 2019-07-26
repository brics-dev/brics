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
<title><decorator:title /> : Global Stroke Data Repository</title>

<link href="<s:url value='/config/gsdr/style.css'/>" rel="stylesheet" type="text/css" media="all" />
<!--[if IE 9]>    <link href="<s:url value='/config/ninds/gradientIE9.css'/>" rel="stylesheet" type="text/css" media="all" /> <![endif]-->
<link href="<s:url value='/config/gsdr/images/favicon.ico'/>" rel="icon" />

<jsp:include page="/common/script-includes.jsp" />
<decorator:head />

<jsp:include page="/config/gsdr/google-analytics.jsp" />

</head>
<body>
<jsp:include page="/common/envBanner.jsp" />
	<div id="skiplinkholder">
			<p>
				<a id="skiplink" href="#skiptarget" onclick="document.getElementById('skiptarget').focus();" class="visuallyhidden focusable">Skip to Main Content</a>
			</p>
	</div>	
	<div id="page-container">
		
	<s:if test="eFormModule eq null">
			<jsp:include page="/config/gsdr/header.jsp" />
	</s:if>
	<s:elseif test="eFormModule eq false">
			<jsp:include page="/config/gsdr/header.jsp" />
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
		
		<p id="skiptargetholder" class="visuallyhidden">
			<a id="skiptarget" name="skiptarget" class="skip" tabindex="0" href="#skiptarget">Start of main content</a>
		</p>
		
		<div id="content-wrapper">
			<div class="content">
				<s:if test="eFormModule eq true">
					<jsp:include page="/common/messages.jsp" />
				</s:if>
				<decorator:body />
			</div>
		</div>
		<div id="footerDiv">
			<jsp:include page="/config/gsdr/footer.jsp" />
		</div>
	</div>
</body>
</html>