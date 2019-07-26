<%@include file="/common/taglibs.jsp"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<div id="header">
	<div class="content">
		<a id="logo" href='http://commondataelements.ninds.nih.gov/'><em class="alt">NINDS Common Data Elements: Harmonizing Information. Streamlining Research.</em></a>
		<div id="login-button" class="float-right"></div>
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
