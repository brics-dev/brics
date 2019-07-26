var Sections = Backbone.Collection.extend({
	initialize : function() {
		this.model = Section;
	},
	
	
	/**
	 * Gets the repeatable parent of the passed section
	 * 
	 * @param Section section to find the parent of
	 * @returns Section repeatable parent if it exists, otherwise null
	 */
	getRepeatableParent : function(section) {
		// if a section is not repeatable, there is no parent
		if (section.get("isRepeatable") == false) {
			return null;
		}
		var parentDivId = section.get("repeatedSectionParent");
		if (parentDivId == null) {
			return null;
		}
		return this.findWhere({divId : parentDivId});
	},
	
	/**
	 * Gets an array of repeatable children of the passed Section
	 * 
	 * @param section section of which to get the children
	 * @returns Section[] array of sections children of this repeatable parent
	 */
	getRepeatableChildren : function(section) {
		if (section.get("isRepeatable") == false) {
			return [];
		}
		
		return this.where(
		                  {isRepeatable: true, repeatedSectionParent : section.get("divId")}
        				  );
	},
	
	/**
	 * Delete data elements from the given section and its repeatable children
	 * IF it has repeatable children.  This affects the questions of these
	 * sections.
	 * 
	 * @param section the section to remove data elements from (its questions)
	 * @param includeChildren (optional) include repeatable children?
	 */
	deleteDEs : function(section, includeChildren) {
		var questions = section.get("questions").models;
		if (typeof includeChildren === "undefined" || includeChildren) {
			var children = this.getRepeatableChildren(section);
			_.each(children, function(child) {
				questions = _.union(questions, child.get("questions").models);
			});
		}
		_.each(questions, function(question) {
			question.removeDE();
		});
	},
	
	/**
	 * Gets the last model in this collection
	 * @returns Section
	 */
	getLast : function() {
		return this.at(this.length - 1);
	},
	
	/**
	 * Gets a section model using its displayed section name.
	 * 
	 * Used mainly for auto-loading questions
	 * 
	 * @param name string name of the section
	 * @return Section model section if a match found, otherwise null
	 */
	getBySectionName : function(name) {
		return this.findWhere({name: name});
	}
});