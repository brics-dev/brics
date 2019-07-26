<%@include file="/common/taglibs.jsp"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<div id="header">
	<div class="content">
		
	<s:if test='modulesPublicURL != ""'>
		<a id="logo" class="float-left" href='<s:url value="%{modulesPublicURL}"/>'>
	</s:if>
	<s:else>
		<a id="logo" class="float-left" href='<s:url value="/"/>'>
	</s:else>
		<em class="alt">NEI BRICS</em></a>
		<div id="login-button" class="float-right"></div>
		<p class="visuallyhidden">Data Management Resource</p>
	</div>
</div>

<script type="text/javascript">
$(document).ready(function() {
	$.post("baseAction!loginCheck.ajax", {},
		function(data) {
			if (document.getElementById("login-button") != null)
			{
				document.getElementById("login-button").innerHTML = data;
				$('#login-link a').click(function () { 
					$(this).toggleClass("expanded");
					$("#login-panel").slideToggle(); 
					return false;
				});
			}
	});
});
</script>
<!-- end of #header -->
