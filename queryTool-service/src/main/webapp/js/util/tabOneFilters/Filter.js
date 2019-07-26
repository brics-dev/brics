/**
 * Sets up a new filter on either studies or forms
 * 
 * @attr collection: the collection to filter (the collection that should be reduced)
 * @attr listOfVisible: the list that acts upon the filter (see example).  This must be
 * either an array of models or an object with key = uri and value = model
 * 
 * @example For text search upon forms, the collection is the forms collection and the
 * 		listOfVisible is the results of the server's search.
 */
QT.Filter = function(collection, listOfVisible) {
	this.init(collection, listOfVisible);
	if (!this.name) {
		this.name = "filter";
	}
};
	QT.Filter.prototype.init = function(collection, listOfVisible) {
		this.collection = collection;
		if (_.isArray(listOfVisible)) {
			this.listOfVisible = this.arrToObj(listOfVisible);
		}
		else {
			this.listOfVisible = listOfVisible;
		}
		
		// just a capability check for browsers since we use this in this.hashCode()
		if (!Date.now) {
			Date.now = function(){return new Date().getTime();}
		}
	};
	/**
	 * Takes an array of models and returns an array of only those that pass
	 * the criteria of this.check()
	 * 
	 * @param listOfVisible (optional) the list of visible to run the filter on
	 */
	QT.Filter.prototype.each = function(listOfVisible) {
		var output = {};	
		if (_.isArray(this.listOfVisible)) {
			listOfVisible = this.arrToObj(this.listOfVisible);
		}
		listOfVisible = this.listOfVisible;
		
		var length = this.collection.length;
		var model = null;
		for (var i = 0; i < length; i++) {
			model = this.collection.at(i);
			if (this.check(model, this.collection, listOfVisible)) {
				output[model.get("uri")] = model;
			}
		}
		return output;
	};
	
	/**
	 * Translate an array of models into an object with key = model.get("uri") and
	 * value = model
	 * 
	 * @param arrayOfObjects
	 * @returns object version of array
	 */
	QT.Filter.prototype.arrToObj = function(arrayOfObjects) {
		var outputObj = {};
		var length = arrayOfObjects.length;
		for (var i = 0; i < length; i++) {
			var element = arrayOfObjects[i];
			outputObj[element.get("uri")] = element;
		}
		return outputObj;
	};
	
	/**
	 * Checks a single input model against the listOfVisible
	 */
	QT.Filter.prototype.check = function(model, fullObjMap, listOfVisible) {
		return typeof listOfVisible[model.get("uri")] !== "undefined";
	};
	
	/**
	 * Used to compare another filter to this one
	 * @param filter the other filter
	 */
	QT.Filter.prototype.equals = function(filter) {
		if (typeof filter === "undefined") {
			return false;
		}
		return this.hashCode() == filter.hashCode();
	};
	
	QT.Filter.prototype.hashCode = function() {
		var output = this.name + ":";
		var keys = Object.keys(this.listOfVisible);
		output += keys.length + ":";
		output += this.collection.length + ":";
		// if the length of visible is greater than 5, we assume that the filters are going
		// to be different so just give a random number to the end
		if (this.listOfVisible.length > 5) {
			output += Date.now()
		}
		else {
			// otherwise, actually hash the collection
			
			// in case we haven't done this before
			if (!String.hashCode) {
				String.prototype.hashCode = function(){
					var hash = 0;
					if (this.length == 0) return hash;
					for (i = 0; i < this.length; i++) {
						char = this.charCodeAt(i);
						hash = ((hash<<5)-hash)+char;
						hash = hash & hash; // Convert to 32bit integer
					}
					return hash;
				}
			}
			
			var joined = keys.join();
			output += joined.hashCode();
		}
		return output;
	};

// from backbone - inherit the extend function
QT.Filter.extend = Backbone.History.extend;