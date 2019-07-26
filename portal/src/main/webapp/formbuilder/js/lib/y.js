function tooltip() {
	$("a[title][id!='layoutButton'],label[title]").qtip({
		style : {
			classes : "ui-tooltip-green ui-tooltip-shadow",
			width: "inherit"
		},
		position: {
	        my: 'top left',  // Position my top left...
	        at: 'bottom left'//, // at the bottom right of...
	        //target: $(this) // my target
	    }
	});
	$("a#layoutButton").qtip({
		style : {
			classes : "ui-tooltip-green ui-tooltip-shadow"
		},
		position: {
	        my: 'bottom center',
	        at: 'bottom right',
	        adjust: {
	        	x: 5,
	        	y: 10
	        }
	    }
	})
	
}

