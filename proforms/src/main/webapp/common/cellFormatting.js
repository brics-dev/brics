


// the object for cell formatting information
function CellFormatting() {
    var formid ;
    var row;
    var col;
    var height;
    var width;
    var padding;
    var wrap;
    var bgcolor;
    var algin;
    var valign;
    var rowspan;
    var colspan;
    this.getHeight = getHeight;
    this.getWidth = getWidth;
    this.getPadding = getPadding;
    this.getRowspan = getRowspan;
    this.getColspan = getColspan;

}

function getHeight () {
    if (this.height == -2147483648 || this.height == 'undefined') {
        return "";
    } else {
        return this.height;
    }
}

function getWidth () {
    if (this.width == -2147483648 || this.width == 'undefined') {
        return "";
    } else {
        return this.width;
    }
}

function getPadding () {
    if (this.padding == -2147483648 || this.padding == 'undefined') {
        return "";
    } else {
        return this.padding;
    }
}


function getRowspan () {
    if (this.rowspan == -2147483648 || this.rowspan == 'undefined') {
        return "";
    } else {
        return this.rowspan;
    }
}

function getColspan () {
    if (this.colspan == -2147483648 || this.colspan == 'undefined') {
        return "";
    } else {
        return this.colspan;
    }
}



// ASSUMES THE EXISTANCE OF AN ARRAY NAMED "formattingObjs" full of CellFormatting objects
function getCellFormatting (row, col) {

    for (i = 0; i < formattingObjs.length; i++) {
     //alert ('iterating formatting objs row : ' + formattingObjs[i].row + ' col ' + formattingObjs[i].col);
        if (formattingObjs[i].row == row && formattingObjs[i].col == col) {

            return formattingObjs[i];
        }
    }
    return "NoFormattingFound";
}
