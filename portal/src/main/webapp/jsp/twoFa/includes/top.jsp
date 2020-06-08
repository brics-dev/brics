<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="styleKey"><spring:eval expression="@applicationProperties.getProperty('modules.style.key')" /></c:set>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
	<head>
        <c:choose>
           <c:when test="${fn:contains(styleKey, 'cnrm' )}">
				<spring:theme code="cnrm.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />
				<title>CNRM Login</title>
           </c:when>
           <c:when test="${fn:contains(styleKey, 'nti' )}">
				<spring:theme code="nti.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />
				<title>NTRR Login</title> 
           </c:when>
           <c:when test="${fn:contains(styleKey, 'pdbp' )}">
				<spring:theme code="pdbp.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />
				<title>PDBP Login</title> 
           </c:when>
           <c:when test="${fn:contains(styleKey, 'fitbir' )}">
				<spring:theme code="fitbir.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />
				<title>FITBIR Login</title> 
           </c:when>
           <c:when test="${fn:contains(styleKey, 'cistar' )}">
				<spring:theme code="cistar.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />     
				<title>CISTAR Login</title> 
           </c:when>
           <c:when test="${fn:contains(styleKey, 'ninds' )}">
				<spring:theme code="ninds.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />     
				<title>NINDS Login</title> 
           </c:when>
           <c:when test="${fn:contains(styleKey, 'gsdr' )}">
				<spring:theme code="gsdr.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />     
				<title>GSDR Login</title> 
           </c:when>
           <c:when test="${fn:contains(styleKey, 'eyegene' ) || fn:contains(styleKey, 'nei' )}">
				<spring:theme code="eyegene.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />     
				<title>NEI BRICS Login</title> 
           </c:when>
            <c:when test="${fn:contains(styleKey, 'cdrns' )}">
				<spring:theme code="cdrns.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />     
				<title>cdRNS Login</title> 
           </c:when>
           <c:otherwise>
           		<spring:theme code="brics.custom.css.file" var="customCssFile" />
				<link type="text/css" rel="stylesheet" href="<c:url value="${customCssFile}" />" />     
				<title>BRICS Login</title> 
	       </c:otherwise>
           
        </c:choose>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="icon" href="<c:url value="/${styleKey}/favicon.ico" />" type="image/x-icon" /> 
	</head>
	
	<body id="cas">
		<c:set var="portalUrl"><spring:eval expression="@applicationProperties.getProperty('portal.url')" /></c:set>
    	
		
    	<div id="page-container">
            <div id="header" class="flc-screenNavigator-navbar fl-navbar fl-table">
            	<div class="content">
	  				<c:choose>
			           <c:when test="${fn:contains(styleKey, 'cnrm' )}">
							<a href="<c:out value="${portalUrl}" />" id="logo" class="float-left"><em class="alt">CNRM Data Repository</em></a>
			           </c:when>
			           <c:when test="${fn:contains(styleKey, 'nti' )}">
							<a href="<c:out value="${portalUrl}" />" id="logo" class="float-left"><em class="alt">National Trauma Research Repository (NTRR)</em></a>
			           </c:when>
			           <c:when test="${fn:contains(styleKey, 'pdbp' )}">
							<a href="<c:out value="${portalUrl}" />" id="logo" class="float-left"><em class="alt">Parkinson's Disease Biomarkers Program (PDBP)</em></a>
			           </c:when>
			           <c:when test="${fn:contains(styleKey, 'fitbir' )}">
							<a href="<c:out value="${portalUrl}" />" id="logo" class="float-left"><em class="alt">Federal Interagency Traumatic Brain Injury Research (FITBIR) Informatics System</em></a>
			           </c:when>
			           <c:when test="${fn:contains(styleKey, 'cistar' )}">
							<a href="<c:out value="${portalUrl}" />" id="logo" class="float-left"><em class="alt">Clinical Informatics System for Trials and Research (CiSTAR)</em></a>
			           </c:when>
			           <c:when test="${fn:contains(styleKey, 'ninds' )}">
							<a href="http://commondataelements.ninds.nih.gov/" id="logo" class="float-left"><em class="alt">National Institute of Neurological Disorders and Stroke (NINDS) Data Dictionary</em></a>
			           </c:when>
			            <c:when test="${fn:contains(styleKey, 'gsdr' )}">
							<a href="${portalUrl}" id="logo" class="float-left"><em class="alt">Global Stroke Data Repository</em></a>
			           </c:when>
			           <c:when test="${fn:contains(styleKey, 'eyegene' ) || fn:contains(styleKey, 'nei' )}">
							<a href="<c:out value="${portalUrl}" />" id="logo" class="float-left"><em class="alt">NEI BRICS</em></a>
			           </c:when>
			            <c:when test="${fn:contains(styleKey, 'cdrns' )}">
							<a href="<c:out value="${portalUrl}" />" id="logo" class="float-left"><em class="alt"> Common Data Repository for Nursing Science (cdRNS)</em></a>
			           </c:when>
			           <c:otherwise>
			           	<a href="<c:out value="${portalUrl}" />" id="logo" class="float-left"><em class="alt"> Biomedical Research Informatics Computing System (BRICS) </em></a>
			           </c:otherwise>
			           
			         
	        		</c:choose>
	        		 <c:if test = "${inLogout}">
 						<h2 class="button-right"><a id="loginUrl" href="#" ><button>Log In</button></a></h2>
 					</c:if>
            	</div>
            	
 
 				<!-- <input type=button id="loginUrl" value="Log In" location.href="#"/> -->
            </div>
            <div id="content-wrapper">	
            	<div class="content">
		