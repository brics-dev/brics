

function TreeSelector (_treeName, _targetObject) {
    this.treeName = _treeName;
    this.targetObject = _targetObject;

}


TreeSelector.prototype.select = function (_selectId) {

    var options =  this.targetObject.options;
    for(i=0; i < options.length; i++ ){
        if (options[i].value == _selectId) {
            options[i].selected = true;
        } else {
            options[i].selected = false;
        }
    }

    document.getElementById('treemenuDiv').style.display = 'none';
    document.getElementById('treemenuBackground').style.display = 'none';


}