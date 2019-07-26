




function setOpacity(elemid,  value ) {
 document.getElementById(elemid).style.opacity = value / 10;
 document.getElementById(elemid).style.filter = 'alpha(opacity=' + value * 10 + ')';
}

function fadeIn(id) {
    document.getElementById(id).style.filter = 'alpha(opacity=0)';
    document.getElementById(id).style.opacity = 0;
    document.getElementById(id).style.display = "block"
    for( var i = 0 ; i <= 100 ; i++ ){
        setTimeout( "setOpacity('"+id +"', " + (i / 10) + ')' , 3 * i );
    }
}

function fadeOut(id) {
 for( var i = 0 ; i <= 100 ; i++ ) {
   setTimeout( "setOpacity('"+id+"', " + (10 - i / 10) + ')' , 3 * i );
 }
    setTimeout("document.getElementById('"+id+"').style.display='none'", 900);
}