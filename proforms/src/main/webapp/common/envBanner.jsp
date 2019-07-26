<%@ page import="javax.servlet.http.HttpServletRequest,
 				 gov.nih.nichd.ctdb.util.common.SysPropUtil" %>

<%
	String url = ((HttpServletRequest)pageContext.getRequest()).getRequestURL().toString();
%>
<%-- This if statement is for Josh's support of the CNRM dashboard --%>
<%
if (url.indexOf("local") != -1) {
%>
<s:property value="%{#url}" />

<div id="envBannerHolderV"></div>
<div id="envBannerWrapperV">

<div id="envBannerTransV"></div>
<div id="envBannerTextV">
<div class="bannerTextCenterV">
<!--  Please be aware you are currently in your local Development Environment. -->

<p>L</p>
<p>O</p>
<p>C</p>
<p>A</p>
<p>L</p>


</div>
</div>
<div class="css-vertical-text">Build Version:<%=SysPropUtil.getProperty("proforms.iteration.revision") %></div>
</div>

	
<%
}
if (url.indexOf("dev") != -1) {
%>


<div id="envBannerHolderV"></div>
<div id="envBannerWrapperV">

<div id="envBannerTransV"></div>
<div id="envBannerTextV">
<div class="bannerTextCenterV">
<!--  Please be aware you are currently in the Development Environment.-->
<p>D</p>
<p>E</p>
<p>V</p>
<p>E</p>
<p>L</p>
<p>O</p>
<p>P</p>
<p>M</p>
<p>E</p>
<p>N</p>
<p>T</p>
</div>
</div>
<div class="css-vertical-text">Build Version:<%=SysPropUtil.getProperty("proforms.iteration.revision") %></div>
</div>

	
<%
}
if (url.indexOf("demo") != -1) {
%>

<div id="envBannerHolderV"></div>
<div id="envBannerWrapperV">

<div id="envBannerTransV"></div>
<div id="envBannerTextV">
<div class="bannerTextCenterV">
<!--  Please be aware you are currently in the Demonstration Environment. -->
<p>D</p>
<p>E</p>
<p>M</p>
<p>O</p>
</div>
</div>
<div class="css-vertical-text">Build Version:<%=SysPropUtil.getProperty("proforms.iteration.revision") %></div>
</div>

	
<%
}
if (url.indexOf("stage") != -1) {
%>

	<div id="envBannerHolderV"></div>
	<div id="envBannerWrapperV">
	
	<div id="envBannerTransV"></div>
	<div id="envBannerTextV">
	<div class="bannerTextCenterV">
	<!--  Please be aware you are currently in the Staging Environment. -->
	<p>S</p>
	<p>T</p>
	<p>A</p>
	<p>G</p>
	<p>I</p>
	<p>N</p>
	<p>G</p>
	</div>
	</div>
	<div class="css-vertical-text">Build Version:<%=SysPropUtil.getProperty("proforms.iteration.revision") %></div>
	</div>
<%
}
%>