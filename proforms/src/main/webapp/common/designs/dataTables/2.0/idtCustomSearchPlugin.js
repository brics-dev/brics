(function($, window, document, undefined) {
	
    var pluginName = "idtCustomSearch",
        filterData = {},
        defaults = {
            searchList: null,
            idtTableId: '',
            idtConfigs: null,
            toolTipList: null
        };

    function IdtPlugin(element, options) {
        this.element = $(element);
        this.$elem = $(this.element);
        this.settings = $.extend({}, defaults, options);
        this._filterData = filterData;
        this._defaults = defaults;
        this._name = pluginName;
        this.init();
    }

    IdtPlugin.prototype = {

        options: function(option, val) {
            this.settings[option] = val;
        },
        extendData: function(obj) {
            $.extend(this._filterData, obj);
            this._runTable(this._filterData);
        },
        clear: function() {
            var that = this;
            $(this.element).find('input').each(function() {
                var name = $(this).attr('name'),
                	id = $(this).attr('id');
                
                switch(this.type) {
                    case 'checkbox':
                    	$(this).prop('checked', false);
                        that._filterData[name] = '';
                        break;
                    case 'radio':
                        if($("label[for='"+$(this).attr('id')+"']").text() === 'All') {
                            $(this).prop('checked', true);
                            that._filterData[name] = $("input[name="+name+"]:checked").val();
                        }
                        break;
                }

            })
            that._runTable(that._filterData);
        },
        reset: function() {
            var that = this;
            $(this.element)[0].reset();
            $(this.element).find('input').each(function() {
            	var name = $(this).attr('name');
                switch(this.type) {
                    case 'checkbox':
                        $this = $(this);
                        var checkedValues = $("input[name="+name+"]:checked").map(function() {
                            return this.value;
                        }).get();
                        that._filterData[name] = checkedValues.toString();
                        break;
                    case 'radio':
                        // $("input[name="+name+"]:radio").prop('checked', function () {
                        //     return this.getAttribute('checked') == 'checked';
                        // });
                        var checkedValue = $("input[name="+name+"]:checked").val();
                        that._filterData[name] = checkedValue;
                        break;
                       
                }

            });
            //case select dropDown
            $(this.element).find('select').each(function() {
            	var name = $(this).attr('name');
                var checkedValue = $("select[name="+name+"]:selected").val();
                if(!checkedValue) {
                	checkedValue = '';
                }
                that._filterData[name] = checkedValue;         	   
            })
            
            that._runTable(that._filterData);

        },
        init: function() {
            var that = this;
            if (this.settings.searchList !== null || this.settings.searchList !== 'undefined') {
                (this.settings.searchList).forEach(function(search) {
                    if (search == null) {
                        return;
                    }
                    switch (search.type) {
	                    case 'null':
	                        break;
	                    case 'radio':
	                        that._createRadioButton(that.element, search.defaultValue, search.containerId, search.legend, search.options, search.name, search.dynamicTitle, search.render);
	                        break;
	                    case 'checkbox':
                            that._createCheckBox(that.element, search.defaultValues, search.containerId, search.legend, search.options, search.name, search.dynamicTitle, search.render, search.eventCallback);
	                        break;
	                    case 'select':
	                        that._createDropDown(that.element, search.defaultValue, search.containerId, search.legend, search.options, search.name);
	                        break;	                        
	                     
                    }
                });
            }
            
            this._showStandardizationTooltips();
            
            var configs = {
                    searching: false,
                    ajaxMethod: "POST",
                    destroy: true,
                    pageLength: 25,
                    filterData: that._filterData,
                    filtering: false
            };

            $.extend(configs, this.settings.idtConfigs);
            
            $("#" + this.settings.idtTableId).idtTable(configs);

            return this;
        },
        _createRadioButton: function($el, defaultValue, containerId, legend, options, aName, dynamicTitle, callback) {
            var that = this,
                $input,
                currentValue,
                form = '<div id=' + containerId + ' class="filter facet-form-field"><fieldset><legend><strong>' + legend + '</strong></legend><ul>';
            if (callback && typeof(callback) == 'function' && !options) {
                $input = $(form + '</ul></fieldset></div>');
                currentValue = callback.call(this, containerId, aName, $input, filterData);
            }else{
                var iLength = options.length;
                for (var j = 0; j < iLength; j++) {
                    if (typeof(options[j]) == 'object') {
                    	var checked = '', title = '';
                        if (unescape(options[j].value) == defaultValue) {
                            checked = 'checked = checked';
                            currentValue = defaultValue;
                        }
                        if(options[j].title) title = 'title="' + unescape(options[j].title) + '"';
                        form += '<li><input type="radio" ' + checked + '  name="' + aName + '" id="' + unescape(options[j].id) + '" value="' + unescape(options[j].value) + '"/><label for="' + unescape(options[j].id) + '"'+ title +'>' + unescape(options[j].label) + '</label></li>';

                    }
                }

                $input = $(form + '</ul></fieldset></div>');
            }
            
            if(dynamicTitle == true) {
                $input.find('label').addClass('dynamicTitle');
            }     

            $el.append($input);
            that._filterData[aName] = unescape(currentValue);

            $('#' + containerId + ' li').on("click", "input[name=" + aName + "]:radio", function(e) {
                //e.preventDefault();
                that._filterData[aName] = $(this).val();
                $(this).prop('checked');
                that._runTable(that._filterData);

            });
        },
        _createCheckBox: function($el, defaultValues, containerId, legend, options, aName, dynamicTitle, callback, eventCallback) {
            var that = this,
                currentValues = [],
                checked = '',
                $input,
                form = '<div id=' + containerId + ' class="filter facet-form-field"><fieldset><legend><strong>' + legend + '</strong></legend><ul>';
            if (callback && typeof(callback) == 'function' && !options) {
                $input = $(form + '</ul></fieldset></div>');
                currentValues = callback.call(this, containerId, aName, $input, filterData);

            } else {
                var iLength = options.length;
                for (var j = 0; j < iLength; j++) {
                    if (typeof(options[j]) == 'object') {
                    	var checked = '', title = '';
                        if (defaultValues && Array.isArray(defaultValues)) {
                            if ($.inArray(unescape(options[j].value), defaultValues) !== -1) {
                                currentValues = defaultValues;
                                checked = 'checked = checked';
                            }
                        } else {
                            if (options[j].checked && options[j].checked == true) {
                                checked = 'checked = checked';
                                currentValues.push(options[j].value);
                            }
                        }
                        if(options[j].title) title = 'title="' + unescape(options[j].title) + '"';
                        form += '<li><input type="checkbox" ' + checked + '  name="' + aName + '" id="' + unescape(options[j].id) + '" value="' + unescape(options[j].value) + '"/><label for="' + unescape(options[j].id) + '"'+ title +'>' + unescape(options[j].label) + '</label></li>';

                    }
                }
                $input = $(form + '</ul></fieldset></div>');
            }

            if(dynamicTitle == true) {
                $input.find('label').addClass('dynamicTitle');
            }

            $el.append($input);

            that._filterData[aName] = currentValues.toString();

            $($input).on("click", "input[name=" + aName + "]:checkbox", function(e) {
                var checkedValues = $("input[name=" + aName + "]:checked").map(function() {
                    return this.value;
                }).get();
                var values = checkedValues.filter(function(item, pos) {
                    return checkedValues.indexOf(item) == pos;
                });

                $(this).prop('checked');
                that._filterData[aName] = checkedValues.toString();
                if(eventCallback) {
                   eventCallback.call(this, that._filterData);
                }
                that._runTable(that._filterData);

            });
        },
        _createDropDown: function($el, defaultValue, containerId, legend, options, aName) {
            var that = this,
            currentValue = '',
            checked = '',
            $input,
            form = '<div id=' + containerId + ' class="filter facet-form-field"><fieldset><legend><strong>' + legend + '</strong></legend><ul><li><select style="width:auto;" name=' + aName + '>',
            iLength = options.length;
            for (var j = 0; j < iLength; j++) {
                if (typeof(options[j]) == 'object') {
                	var selected = '', title = '';
                    if (unescape(options[j].value) == defaultValue) {
                        selected = 'selected = selected';
                        currentValue = defaultValue;
                    }
                    form += '<option ' + selected + '  value="' + unescape(options[j].value) + '">' + unescape(options[j].label) + '</option>';
                }
            }
            
            $input = $(form + '</li></ul></fieldset></div>');
            $el.append($input);
            that._filterData[aName] = unescape(currentValue);
            
            $($input).on("change", "select[name=" + aName + "]", function(e) {
            	
                that._filterData[aName] = $(this).val();
                $(this).prop('selected');
                that._runTable(that._filterData);

            });      
            
        },
        _showStandardizationTooltips: function() {
        	var toolTipList = this.settings.toolTipList;
            if(toolTipList && Array.isArray(toolTipList)){
	            $.each($('.dynamicTitle'), function() {
	                var $this = $(this);
	                toolTipList.map(function(el) {
	                    if(el.title == $this.attr('title')) {
	                        $this.attr('title',el.description);
	        	            // we are adding qtip to add the styling to the tooltip
	                        $this.qtip({
	        	        		style : {
	        	        			classes : "ui-tooltip-green ui-tooltip-shadow"
	        	        		},
	        	        		position: {
	        	        	        my: 'top left',  // Position my top left...
	        	        	        at: 'bottom left'// , // at the bottom right of...
	        	        	        // target: $(this) // my target
	        	        	    }
	        	        	})	                   
	                    }
	                });
	            });
            }
        },
        _runTable: function() {
            var oTableId = this.settings.idtTableId;
            var oTable = $('#' + oTableId).idtApi('getTableApi').settings();
            var options = $('#' + oTableId).idtApi('getOptions');
            options.filterData = this._filterData;
            if (options.serverSide == true) {
                oTable.clearPipeline().draw();
            } else {
                oTable.ajax.reload();
            }
        }
    };

    $.fn[pluginName] = function(options) {
           // get the arguments
        var args = $.makeArray(arguments),
            after = args.slice(1);

        return this.each(function () {

            // check if there is an existing instance related to element
            var instance = $.data(this, pluginName);

            if (instance) {
                if (instance[options]) {
                    instance[options].apply(instance, after);
                } else {
                    $.error('Method ' + options + ' does not exist on Plugin');
                }
            } else {
                // create the plugin
                var plugin = new IdtPlugin(this, options);

                // Store the plugin instance on the element
                $.data(this, pluginName, plugin);
                return plugin;
            }
        });

    };


})(jQuery, window, document);

