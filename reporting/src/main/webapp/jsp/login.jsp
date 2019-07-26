<title>Log In</title>

<%@include file="/common/taglibs.jsp"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>

<script type="text/javascript" src='<s:url value="/js/browser-check.js"/>'></script>

<div class="clear-float">
	<h1 class="float-left">Account Management</h1>
</div>

<div class="border-wrapper wide">
	<div id="main-content">
		
		<h2> Login</h2>
			<p>Please provide your username and password to access this content.</p>
			
			<div id="browser-msg" class="default-error clear-both" style="display: none">
				<p>We have detected that you are using an unsupported browser, please use one of the recommended browsers listed below.<br>
				 We recommend using one of the following supported browsers and versions for accessing this system: Internet Explorer 9+, 
				Firefox 9+, Chrome</p> 
			</div>

			<s:if test="hasActionErrors()">
				<div class="default-error clear-both">
					<s:actionerror />
				</div>
			</s:if>

			<c:if test="${not empty param.login_error}">
				<div class="default-error clear-both">
					<p>
						<c:choose>
							<%-- Bad Credentials --%>
							<c:when test="${SPRING_SECURITY_LAST_EXCEPTION.message == 'Bad credentials'}">
							The username or password you entered is incorrect.
		
							<%
							    if (request.getSession().getAttribute("unlockDate") != null) {
							%>
								<br /> Your account is now locked, it will become unlocked at: <%=(new SimpleDateFormat("hh:mm:ss a z"))
										.format(request.getSession()
												.getAttribute("unlockDate"))%>
								<%
								    }
								%>
							</c:when>
							<%-- Account Not Active --%>
							<c:when test="${SPRING_SECURITY_LAST_EXCEPTION.message == 'User is disabled'}">
							Your account has not yet been approved by an admin.
						</c:when>
							<%-- Account Locked --%>
							<c:when test="${SPRING_SECURITY_LAST_EXCEPTION.message == 'User account is locked'}">
							Your account is locked, at <%=(new SimpleDateFormat("hh:mm:ss a z"))
									.format(request.getSession().getAttribute(
											"unlockDate"))%> it will be active.
						</c:when>
							<%-- Account Expired --%>
							<c:when test="${SPRING_SECURITY_LAST_EXCEPTION.message == 'User account has expired'}">
							Your account has expired either because it has been too long since your last log in, or your permission has not been re-approved.  Please contact the operations team for more information.
						</c:when>
							<c:otherwise>
							Your login attempt was not successful, try again.
							<br />
							Reason: 
							<c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}" />.
						</c:otherwise>
						</c:choose>
					</p>
				</div>
			</c:if>

			<form name="f" action="/portal/j_spring_security_check" method="POST">
				<div class="formElements">
					<div class="form-field">
						<label for="userNameField" class="required">Username <span class="required">* </span>
						</label> <input type="text" id="userNameField" name="j_username" class="textfield required"
							value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>' />
					</div>
					<div class="form-field">
						<label for="passwordField" class="required">Password <span class="required">* </span>
						</label>
						<s:password id="passwordField" name="j_password" cssClass="textfield required" />
					</div>
					<!-- 				<div class="form-field"> -->
					<!-- 					<label for="rememberCheckbox">Remember me: </label> -->
					<!-- 					<input id="rememberCheckbox" type="checkbox" name="_spring_security_remember_me"> -->
					<!-- 				</div> -->
				</div>

				<div class="login-buttons">
					<div class="button">
						<s:submit name="submit" value="Log In"></s:submit>
					</div>
				</div>
			</form>

			
			
			<div class="text-block">
				<h4>Need Assistance?</h4>
				<p>
					<s:if test="instanceType.name == 'PD'">
						<a href="/portal/publicAccounts/accountInfoAction!create.action">Request A New Account(PD)</a>
					</s:if>
					<s:else>
						<a href="/portal/publicAccounts/createAction!create.action">Request A New Account(NPD)</a>
					</s:else>
					 | <a
						href="/portal/publicAccounts/userNameRecoveryAction!input.action">Forgot your username?</a> | <a
						href="/portal/publicAccounts/passwordRecoveryAction!input.action">Forgot your password?</a>
				</p>
			</div>
			
			<div class="text-block">
				<p>				
				<em>We recommend using one of the following supported browsers and versions for accessing this system: Internet Explorer 9+, 
				Firefox 9+, Chrome</em>
				</p>
			</div>


			<div class="text-block">
				<h3>NIH Single Sign-On</h3>
				<p>We allow you to use your NIH Federal Identity to login to the system. While you will
					still need to request a local system account to store Profile details, you can use your NIH or Federation credentials for login purposes.
					Single Sign-on is supports credentials from: NIH AD, NIH External, HHS-issued PIV smart card, eRA
					Commons, InCommon federation, and OpenID Foundation credentials.</p>

				<p>Select the button below to use your single sign-on credentials.</p>
				<div class="button">
					<input type="button" onclick="window.location.href='/portal/sso/ssoAction!ssoLogin.action'"
						value="Single Sign-On Login">
				</div>	
			</div>

			<br />
			<br />
			<br />

			<div class="text-block">
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
</div>


<script type="text/javascript">
	setNavigation({
		"bodyClass" : "primary"
	});
	
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