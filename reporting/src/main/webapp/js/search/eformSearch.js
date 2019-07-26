function eformSearch() {

	var action = "eFormSearchAction!search.ajax";
	
	var ownerValue = $('input[name="owner"]:checked').val();

	// Ajax call your search
	$.fancybox.showActivity();
	$.ajax(action, {
		"type": 	"POST",
		"data": 	{
					"ownerId": ownerValue,
					},
		"success": 	function(data) {
						$("#eFormResultsId").html(data);
						$("#eFormResultsId").find('script').each(function(i) {
			            	if ($(this).attr("type") != "text/json") {
			            		eval($(this).text());
			            	}
			            });
			            $.fancybox.hideActivity();
			            buildDataTables();
					},
		"error":	function(data) {
						$.fancybox.hideActivity();
					}
	});
}



