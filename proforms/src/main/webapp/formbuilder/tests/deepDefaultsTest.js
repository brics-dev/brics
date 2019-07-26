/**
 * test suite for the deep defaults function
 */
 _.deepDefaults = function(obj) {
	_.each(Array.prototype.slice.call(arguments, 1), function(source) {
		if (source) {
			for (var prop in source) {
				if (typeof source[prop] === "object") {
					obj[prop] = _.deepDefaults(obj[prop], source[prop]);
				}
				else {
					if (obj[prop] === void 0) obj[prop] = source[prop];
				}
			}
		}
	});
	return obj;
};


module("deepDefaults");
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
	
	_.deepDefaults(one, two);
	
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
	
	_.deepDefaults(six, seven, eight);
	
	
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