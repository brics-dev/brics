/**
 * test suite for the deep defaults function
 */



module("jqExtend");
test("Basic Object Defaults Test", function() {
	var one = {
		"events" : {
			"click" : "goone",
			"click .edit" : "edit"
		}
	};
	
	var render = function() {return true;}
	var two = {
		"events" : {
			"click .save" : "save"
		},
		"name" : "decoratedOne",
		render : render
	};
	
	$.extend(true, one, two);
	
	//-----------------------------------
	deepEqual(one, {
		"events" : {
			"click" : "goone",
			"click .edit" : "edit",
			"click .save" : "save"
		},
		"name" : "decoratedOne",
		render: render
	});
	
	var six = {
			events : {
				"click" : "goone",
				"click .edit" : "edit"
			}
		};
	
	var seven = {
			events : {
				"click .save" : "save"
			},
			name : "decoratedOne"
		};
	
	var eight = {
		events : {
			"hover" : "hovered"
		}
	}
	
	$.extend(true, six, seven, eight);
	
	
	var testTwo = {
			events : {
				"click" : "goone",
				"click .edit" : "edit",
				"click .save" : "save",
				"hover" : "hovered"
			},
			name : "decoratedOne"
		};
	
	//deepEqual(five, testTwo);
	deepEqual(six,testTwo);
});