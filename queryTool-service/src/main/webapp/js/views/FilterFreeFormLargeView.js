QT.FilterFreeFormLargeView = QT.GenericQueryFilterView.extend({

    events : {
        "click .filterToggle" : "toggleFilterBody",
        "click .filterClose" : "closeFilter"
    },

    initialize : function() {
        this.template = TemplateManager.getTemplate("filterFreeFormLarge");
        QT.FilterFreeFormLargeView.__super__.initialize.call(this);

    },

    render : function($container) {

        this.$el.html(this.template(this.model.attributes));
        $container.append(this.$el);
        QT.FilterFreeFormLargeView.__super__.render.call(this);
        this.populateFormStructureTitleAndElement();
    },

    fillData : function() {
        var freeFormVal = this.model.get("selectedFreeFormValue");
        if (freeFormVal) {
            this.$(".filterFreeFormTextBox").val(freeFormVal);
        }
    }

});