
/* Global variables to hold the grid images
*/
var img_5x5;
var img_8x8;
var img_10x10;
var img_15x15;
var img_20x20;

/*  Loads the grid images onto the page for use
*/
function loadGrids () {
	img_5x5 = new Image;
	img_5x5.src = "images/5x5_large.gif";
	img_8x8=new Image;
	img_8x8.src = "images/8x8_large.gif";

	img_10x10=new Image;
	img_10x10.src = "images/10x10_large.gif";

	img_15x15=new Image;
	img_15x15.src = "images/15x15_large.gif";

	img_20x20=new Image;
	img_20x20.src = "images/20x20_large.gif";
}

/* Changes the grid size to match the image size
*
*/
function doGridOverlay () {

	document.getElementById('theGrid').height = document.getElementById('theImage').height;
	document.getElementById('theGrid').width = document.getElementById('theImage').width;
}

/* Switches the grid as selected by the user
*/
function swapGrid() {

	if (document.getElementById('grids').value==1) {
		document.getElementById('theGrid').src = img_5x5.src;
	} else if (document.getElementById('grids').value==2) {
		document.getElementById('theGrid').src = img_10x10.src;
	}
	doGridOverlay();
}

/* global vars to keep track of image orginal size and the height/width ration
*/
var yProportion;
var origHeight;
var origWidth;

/* finds the ratio of height / width for use in reszing
 * MUST CALL WHEN PAGE LOADS
 */
function findImgProportions () {
	origHeight = document.getElementById('theImage').height;
	origWidth = document.getElementById('theImage').width;
	yProportion =  origHeight / origWidth;
}

/* Resets image to original size
*/
function reset() {
	document.getElementById ('theImage').width=origWidth;
	document.getElementById ('theImage').height=origHeight;
	doGridOverlay();
}

/* Decreases the size of the image
*/
function shrinkImg(factor) {
	document.getElementById('theImage').width -= (1 * factor);
	document.getElementById('theImage').height -= (yProportion * factor);
	doGridOverlay();

}

/* increases the size of the image
*/
function growImg(factor) {
	document.getElementById('theImage').width += (1 * factor);
	document.getElementById('theImage').height += (yProportion * factor);
	doGridOverlay();

}

/* size the image to users pre-saved specifications
*/
function adjustSize() {
    document.getElementById('theImage').width = document.getElementById('width').value;
    document.getElementById('theImage').height = document.getElementById('height').value;
}

function saveSize() {
       document.getElementById('width').value  =  document.getElementById('theImage').width;
       document.getElementById('height').value = document.getElementById('theImage').height;
}