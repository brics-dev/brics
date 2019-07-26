<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<%@ page contentType="text/html; charset=UTF-8" session="false"%>
<jsp:directive.include file="includes/top.jsp" />
<c:set var="portalUrl"><spring:eval expression="@applicationProperties.getProperty('portal.url')" /></c:set>
<c:if test="${not pageContext.request.secure}">
	<div class="errors">
		<h2><spring:message code="screen.nonsecure.title" /></h2>
		<p><spring:message code="screen.nonsecure.message" /></p>
	</div>
</c:if>
<div id="bad-build-msg" class="errors" style="display: none" >
	<p>The Portal site is currently undergoing a site-wide upgrade. Please refrain from logging into the site unil Tuesday, Jan 15, 2013. If you have any concerns, please contact the Operations team. Thank you.
	</p> 
</div>
<div id="browser-msg" class="errors" style="display: none">
	<p>We have detected that you are using an unsupported browser, please use one of the recommended browsers listed below.</p>
	<p>We recommend using one of the following supported browsers and versions for accessing this system: Internet Explorer 9+, 
				Firefox 9+, Chrome</p>
</div>
<div id="cookiesDisabled" class="errors" style="display:none;">
    <h2><spring:message code="screen.cookies.disabled.title" /></h2>
    <p><spring:message code="screen.cookies.disabled.message" /></p>
</div>

<c:if test="${not empty registeredService}">
    <c:set var="registeredServiceLogo" value="images/webapp.png"/>
    <c:set var="registeredServiceName" value="${registeredService.name}"/>
    <c:set var="registeredServiceDescription" value="${registeredService.description}"/>

    <c:choose>
        <c:when test="${not empty mduiContext}">
            <c:if test="${not empty mduiContext.logoUrl}">
                <c:set var="registeredServiceLogo" value="${mduiContext.logoUrl}"/>
            </c:if>
            <c:set var="registeredServiceName" value="${mduiContext.displayName}"/>
            <c:set var="registeredServiceDescription" value="${mduiContext.description}"/>
        </c:when>
        <c:when test="${not empty registeredService.logo}">
            <c:set var="registeredServiceLogo" value="${registeredService.logo}"/>
        </c:when>
    </c:choose>

    <div id="serviceui" class="serviceinfo" style="display: none">
        <table>
            <tr>
                <td><img src="${registeredServiceLogo}"></td>
                <td id="servicedesc">
                    <h1>${fn:escapeXml(registeredServiceName)}</h1>
                    <p>${fn:escapeXml(registeredServiceDescription)}</p>
                </td>
            </tr>
        </table>
    </div>
    <p/>
</c:if>

  <div class="box fl-panel" id="login">
			<form:form method="post" id="fm1" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
                  <form:errors path="*" cssClass="errors" id="msg" element="div" htmlEscape="false" />
                <!-- <spring:message code="screen.welcome.welcome" /> -->
                    <h2>Enter your Username and Password</h2>
                    <div class="row fl-controls-left">
                        <label for="username" class="fl-label">Username:</label>
						<c:if test="${not empty sessionScope.openIdLocalId}">
						<strong>${sessionScope.openIdLocalId}</strong>
						<input type="hidden" id="username" name="username" value="${sessionScope.openIdLocalId}" />
						</c:if>

						<c:if test="${empty sessionScope.openIdLocalId}">
						<spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
						<form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" />
						</c:if>
                    </div>
                    <div class="row fl-controls-left">
                        <label for="password" class="fl-label"><spring:message code="screen.welcome.label.password" /></label>
		                <%--
		                NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
		                "autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
		                information, see the following web page:
		                http://www.technofundo.com/tech/web/ie_autocomplete.html
		                --%>
						<spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
						<form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
						<span id="capslock-on" style="display:none;"><p><img src="images/warning.png" valign="top"> <spring:message code="screen.capslock.on" /></p></span>
                    </div>
                    <div class="row btn-row">
						<input type="hidden" name="lt" value="${loginTicket}" />
						<input type="hidden" name="execution" value="${flowExecutionKey}" />
						<input type="hidden" name="_eventId" value="submit" />

                        <input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" type="submit" />
                        <input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" type="reset" />
                    </div>
            </form:form>
          </div>
            <div id="sidebar">
            	<p>
						<c:choose>
						<c:when test="${fn:contains(pageContext.request.requestURL, 'pdbp' )}">
							<a href="<c:out value="${portalUrl}" />/portal/publicAccounts/accountInfoAction!create.action">Request A New Account</a>
						</c:when>
						<c:otherwise>
							<a href="<c:out value="${portalUrl}" />/portal/publicAccounts/createAction!create.action">Request A New Account</a>
						</c:otherwise>
					
					</c:choose> | <a
						href="<c:out value="${portalUrl}" />/portal/publicAccounts/userNameRecoveryValidationAction!input.action">Forgot your username?</a> | <a
						href="<c:out value="${portalUrl}" />/portal/publicAccounts/promptpasswordRecoveryValidationAction!input.action">Forgot your password?</a>
				</p>
                <p class="fl-panel fl-note fl-bevel-white fl-font-size-80"><spring:message code="screen.welcome.security" /></p>
	  			<div class="text-block fl-panel">
					<h4><strong>Warning Notice</strong></h4>
					<p><em>This is a U.S. Government computer system, which may be accessed and used only for authorized Government
					business by authorized personnel. Unauthorized access or use of this computer system may subject violators to
					criminal, civil, and/or administrative action.</em></p>
					<p><em>All information this computer system may be intercepted, recorded, read, copied, and disclosed by and to 
					authorized personnel for official purposes, including criminal investigations. Such information includes sensitive data encrypted 
					to comply with confidentiality and privacy requirements. Access or use of this computer system by any person, whether authorized or unauthorized,
					constitutes consent to these terms. There is no right of privacy in this system.</em></p>
				</div>
            </div>
<jsp:directive.include file="includes/bottom.jsp" />

<script type="text/javascript">

	
	$(document).ready(function(){
		//Supported browsers include IE9+, Firefox 9+, Chrome
		var supported = true;

		if( BrowserDetect.browser == "Explorer" && BrowserDetect.version < 9)
			{ supported = false; }
		else if( BrowserDetect.browser == "Firefox" && BrowserDetect.version < 9)
			{ supported = false; }
		else if( BrowserDetect.browser == "OmniWeb/" ||  BrowserDetect.browser == "Safari" ||  BrowserDetect.browser == "Opera" 
			||  BrowserDetect.browser == "iCab" ||  BrowserDetect.browser == "KDE" ||  BrowserDetect.browser == "Camino"
			||  BrowserDetect.browser == "Netscape" ||  BrowserDetect.browser == "Mozilla" )
		{ supported = false; }
			
		if( !supported)
			{ $("#browser-msg").show(); }
		
	});
</script>
