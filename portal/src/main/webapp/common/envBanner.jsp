<%@include file="/common/taglibs.jsp"%>
<%-- This if statement is for Josh's support of the CNRM dashboard --%>
<c:if test="${fn:contains(pageContext.request.requestURL, 'local' )}">

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
<div class="css-vertical-text">Build Version:<s:property value="%{deploymentVersion}" /></div>
</div>

	
</c:if>

<c:if test="${fn:contains(pageContext.request.requestURL, 'dev' )}">

<div id="envBannerHolderV"></div>
<div id="envBannerWrapperV">

<div id="envBannerTransV"></div>
<div id="envBannerTextV">
<div class="bannerTextCenterV">
<!--  Please be aware you are currently in the Development Environment.-->
<p>D</p>
<p>E</p>
<p>V</p>
</div>
</div>
<div class="css-vertical-text">Build Version:<s:property value="%{deploymentVersion}" /></div>
</div>

	
</c:if>

<c:if test="${fn:contains(pageContext.request.requestURL, 'demo' )}">

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
<div class="css-vertical-text">Build Version:<s:property value="%{deploymentVersion}" /></div>
</div>

	
</c:if>



<c:if test="${fn:contains(pageContext.request.requestURL, 'stage' )}">

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
<p>E</p>
</div>
</div>
<div class="css-vertical-text">Build Version:<s:property value="%{deploymentVersion}" /></div>
</div>

	
</c:if>

<c:if test="${fn:contains(pageContext.request.requestURL, 'uat' )}">

<div id="envBannerHolderV"></div>
<div id="envBannerWrapperV">

<div id="envBannerTransV"></div>
<div id="envBannerTextV">
<div class="bannerTextCenterV">
<!--  Please be aware you are currently in the UAT Environment. -->
<p>U</p>
<p>A</p>
<p>T</p>
</div>
</div>
<div class="css-vertical-text">Version:<s:property value="%{buildID}" /></div>
</div>

	
</c:if>