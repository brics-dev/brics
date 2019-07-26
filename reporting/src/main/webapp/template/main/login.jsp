<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<script id="login" type="text/x-handlebars-template">

<div class="border-wrapper wide primary">
<header id="header" class="header">
		<div id="instanceHeaderOne" class="">
			<a href="javascript:;" class="bannerHome">
				<img name="" src="instances/default/images/header.png"></img>
			</a>
		</div>
		<div id="instanceHeaderTwo" class="">
			<span><b>BRICS</b>  Biomedical Research Informatics Computing System</span>
		</div>
		<div id="userMenu" class="">
			<span>Welcome <span class="username"></span> | <a href="javascript:;" class="logout"> Log Out </a></span>
		</div>
		<div class="clearfix"></div>
	</header>
<div id="main-content" class="mainContent tabContainer login-form">
		
		<h2> Login</h2>
			<p>Please provide your username and password to access this content.</p>
			
			<div id="browser-msg" class="default-error clear-both" style="display: none">
				<p>We have detected that you are using an unsupported browser, please use one of the recommended browsers listed below.<br>
				 We recommend using one of the following supported browsers and versions for accessing this system: Internet Explorer 9+, 
				Firefox 9+, Chrome</p> 
			</div>

			<form name="f" action="/query/j_spring_security_check" method="POST">
				<div class="formElements">
					<div class="form-field">
						<label for="userNameField" class="required">Username <span class="required">* </span>
						</label> 
						<input type="text" id="userNameField" name="j_username" class="textfield required" value='<c:if test="${not empty param.login_error}">
						<c:out value="${SPRING_SECURITY_LAST_USERNAME}"/>
							</c:if>' />
					</div>
					<div class="form-field">
						<label for="passwordField" class="required">Password <span class="required">* </span>
						</label>
						<input id="passwordField"  type="password" name="j_password" cssClass="textfield required">
					</div>
				</div>

				<div class="login-buttons">
					<div class="button">
						<input type="submit" value="Log In">
					</div>
				</div>
			</form>

			
			<div class="text-block">
				<p>				
				<em>We recommend using one of the following supported browsers and versions for accessing this system: Internet Explorer 9+, 
				Firefox 9+, Chrome</em>
				</p>
			</div>

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
</script>
<script type="text/javascript">
	
	/*$(document).ready(function(){
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
		
	});*/
</script>