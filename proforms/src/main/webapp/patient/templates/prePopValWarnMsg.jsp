<script id="prePopWarnContent" type="text/x-handlebars-template">
	<h2>WAIT!</h2>
	<div style="text-align: left;">
		<p>
			The following data elements have been selected for pre-population for this visit 
			type, but do not yet have a valid value to pre-populate for this subject: 
		</p>
		<ul>
			{{#each prePopArray}}
				<li>{{title}}</li>
			{{/each}}
		</ul>
		<br/>
		<p>Would you like to schedule the visit without pre-populating these data elements?</p>
		<br/>
	</div>
</script>