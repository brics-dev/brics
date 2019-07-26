<%@include file="/common/taglibs.jsp"%>

<div class="form-field">
	<label for="domainSelection">Domain: </label>
	<s:select id="domainSelection" name="domainSelection" list="domainOptions" value="domain" headerKey="" headerValue="- All -" />
</div>
	
<div id="subDomain" class="form-field">
</div>


<script type="text/javascript">
$('document').ready(function() 
	{ 
		changeSubDomains();
	}
);

$('#domainSelection').change(function() 
{
   changeSubDomains();
});


</script>