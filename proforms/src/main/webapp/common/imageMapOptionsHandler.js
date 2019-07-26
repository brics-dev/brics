
function ImageMapOptionsHandler () {

    this.rows = new Array();
    this.cols = new Array();
    this.add = add;
    this.remove = remove;
    this.getCols = getCols;
    this.print = print;
    this.getWebString = getWebString;

}


function add (row, col) {
    found = false;
    for (i = 0; i < this.rows.length; i++) {
        if (this.rows[i] == row) {
            found = true;
            someCols = this.cols[i];
            someCols[someCols.length] = col;
            this.cols[i] = someCols;
         }
    }
    if (! found) {
        num = this.rows.length;
        this.rows[num] = row;
        tmp = new Array();
        tmp[0] = col;
        this.cols[num] = tmp;
    }
}

function remove (row, col) {
 needsUpdating = false;
    for (i = 0; i < this.rows.length; i++) {
        if (this.rows[i] == row) {
            tmp = this.cols[i];
            newTmp = new Array();
            for (j = 0; j < tmp.length; j++) {
                if (tmp[j] != col) {
                    newTmp[newTmp.length] = tmp[j];
                 }
             }
             if (newTmp.length == 0) {
                needsUpdating = true;
             }
             this.cols[i] = newTmp;
             break;
         }
     }
     if (needsUpdating) {
        tmpRows = new Array();
        tmpCols = new Array();
        for (i = 0; i < this.rows.length; i++) {
            if (this.cols[i].length != 0) {
                tmpRows[tmpRows.length] = this.rows[i];
                tmpCols[tmpCols.length] = this.cols[i];
            }
        }
        this.rows = tmpRows;
        this.cols = tmpCols;
    }
}

function getCols (row) {
    for (i = 0; i < this.rows.length; i++) {
        if (this.rows[i] == row) {
            return this.cols[i];
        }
    }
}

function print () {
    msg = "the handler contains : \n";
    for (i = 0; i < this.rows.length; i++) {
        msg += " ROW: " + this.rows[i] + " -> Cols : " + this.cols[i] + " \n";
    }
    alert (msg);
}

function getWebString () {
    str = ""
    if (this.rows.length == 0){
        return "error";
    } else {
        for (i = 0; i < this.rows.length; i++) {
            theCols = this.cols[i];
            for (j=0; j < theCols.length; j++) {
                str += "(" +this.rows[i] + "," + theCols[j] + ")";
            }
        }
        return str;
    }
}



