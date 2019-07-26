<script type="text/x-handlebars-template" id="hamburgerTemplate">
    <div class="tableHamburger"><div></div><div></div><div></div></div>
 </script>
<script id="dataTableTemplate" type="text/x-handlebars-template">
<div id="tableContainer" style="position:relative"></div>
			

</script>
<script type="text/x-handlebars-template" id="pagerTemplate">
 <ul class="pagination">
{{#each links}}

<li>{{{this}}}<li>
{{/each}}
</ul>
 
</script>
<script>
//TODO: where do i pit this close the menu
$(document)
		.ready(
				function() {

//close the menu
$(document)
		.mouseup(
				function(e) {

					// this is used to close the hamburger
					// menu
					var container = $(".actionContainer");

					if (!container.is(e.target) // if the
							// target of
							// the click
							// isn't the
							// container...
							&& container.has(e.target).length === 0) // ...
					// nor
					// a
					// descendant of the
					// container
					{

						container.slideUp("slow");
						container.remove();

					}

				});
				});
				
</script>