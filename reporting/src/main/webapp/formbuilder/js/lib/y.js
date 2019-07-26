function tooltip() {
	$("a[title],label[title]").qtip({
		style : {
			classes : "ui-tooltip-green ui-tooltip-shadow"
		},
		position: {
	        my: 'top left',  // Position my top left...
	        at: 'bottom left'//, // at the bottom right of...
	        //target: $(this) // my target
	    }
	})
}

