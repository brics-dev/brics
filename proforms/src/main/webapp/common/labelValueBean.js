
function LabelValueBean (Label, Value) {
    this.label = Label;
    this.value = Value;
    
}

LabelValueBean.prototype.setLabel = function (str) {
    this.label = str;
}

LabelValueBean.prototype.setValue = function (str) {
    this.value = str;
}


LabelValueBean.prototype.getLabel = function () {
    return this.label;
}

LabelValueBean.prototype.getValue = function () {
    return this.value;
}