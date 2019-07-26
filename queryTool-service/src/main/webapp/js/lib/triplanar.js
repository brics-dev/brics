var axialCanvas    = document.getElementById('AxialCanvas');
var sagittalCanvas = document.getElementById('SagittalCanvas');
var coronalCanvas  = document.getElementById('CoronalCanvas');
var axialCTX		= axialCanvas.getContext('2d');
var sagittalCTX = sagittalCanvas.getContext('2d');
var coronalCTX	= coronalCanvas.getContext('2d');
var raf;
var runningCrossHairs = null;
var mouseStartX = 0;
var mouseStartY = 0;
var mouseStartRect = new MyRect(0, 0, 0, 0);
var myButton = 0;
var is4D = false;
var imageTime = 3;
var maxImageTime = 1;
var intervalID;
var animDelay = 100;
var rangeSlider = document.getElementById('time4D');
var playButton = document.getElementById('play_Button');
var timeControls = document.getElementById('timeControls');

var tfx = new Float32Array(4);
var tfy = new Float32Array(4);
var lutIndex = new Uint8ClampedArray(256);
var lutR = new Uint8ClampedArray(256);
var lutG = new Uint8ClampedArray(256);
var lutB = new Uint8ClampedArray(256);
var datasetRoot = "manix/manix_";

function MyRect(x, y, width, height){
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
}
MyRect.prototype.top = function() {
	return this.y;
}
MyRect.prototype.bottom = function() {
	return this.y + this.height;
}
MyRect.prototype.left = function() {
	return this.x;
}
MyRect.prototype.right = function() {
	return this.x + this.width;
}
MyRect.prototype.copy = function(r) {
	this.x = r.x;
	this.y = r.y;
	this.width = r.width;
	this.height = r.height;
}
MyRect.prototype.inside = function(x, y) {
	return (x >= this.x && y >= this.y &&
			x <= this.right() && y <= this.bottom());
}

function CrossHairs(canvas, ctx, imageBase, numImages, dimX, dimY, maxImageTime) {
	this.dimX = dimX;
	this.dimY = dimY;
	this.x = Math.round(dimX/2);
	this.y = Math.round(dimY/2);
	this.imgIndex = Math.round(numImages/2);
	this.colorVert = "yellow";
	this.colorHoriz = "green";
	this.canvas = canvas;
	this.ctx = ctx;
	this.imageBase = imageBase;
	this.numImages = numImages;
	this.images = new Array(numImages * (is4D ? maxImageTime : 1));
	this.imageRect = imgScaleRect(canvas.width, dimX, dimY); 
}

function imgScaleRect(canvasSize, width, height) {
	if (width === height ) {
		return new MyRect(0, 0, canvasSize, canvasSize);
	} else if (height > width) {
		return new MyRect((1.0 - (width/height)) * canvasSize * 0.5, 0, (width/height) * canvasSize, canvasSize);
	} else {
		return new MyRect(0, (1.0 - (height/width)) * canvasSize * 0.5, canvasSize, (height/width) * canvasSize);
	}
}

function getImageSrc(index, base, is4D, time) {
	var urlObj = urlObject(window.location.href);
	var indexDisk = index + 1;
	var padStr = (indexDisk < 10 ? "00" : (indexDisk < 100 ? "0": ""));
	
	var studyName = urlObj.parameters.studyName;
	var datasetName = urlObj.parameters.datasetName;
	var triplanarName = urlObj.parameters.triplanarName;
	
	return "service/triplanar/downloadTriplanarImg?studyName=" + studyName + "&datasetName=" + datasetName + "&triplanarName=" + triplanarName + "&imageName=" + datasetRoot + base + ( is4D ? "_t" + time.toString() : "" ) + 
			"_" + padStr + indexDisk.toString();
	
	//return datasetRoot + base + ( is4D ? "_t" + time.toString() : "" ) + "_" + padStr + indexDisk.toString() + ".jpg";
}

// Given numImages per timeslice, and the index in a time slice, get flattened array index. 
function getImageIndex(index, numImages, is4D, time) {
	return (is4D ? numImages * time + index : index);
}

// slider callback - show a specific time, with validation, and remember. 
function showTime(myTime) {
  imageTime = Math.round(Math.min(maxImageTime, Math.max(0, myTime)));
	if (rangeSlider.value != imageTime) rangeSlider.value = imageTime;
	draw();
}

// animation callback - increment time. 
function incrTime() {
	showTime((imageTime + 1) % (maxImageTime + 1));
}

// 
function playPause() {
	if (!intervalID) {
		intervalID = setInterval(incrTime, animDelay);
		play_Button.src="images/triplanar/pause.gif"
	} else {
		clearInterval(intervalID);
		intervalID = undefined;
		play_Button.src="images/triplanar/play.gif"
	}
}

function setDelay(direction) {
	animDelay += direction * 20;
	// max speed of 60 fps. 
	animDelay = Math.max(17, animDelay);
	if (intervalID) {
		//restart with new speed. 
		playPause();
		playPause();
	}
}

function changeSrc(id, newSrc) {
	var myImg = document.images[id];
	myImg.src="images/triplanar/" + newSrc + ".gif";
}

CrossHairs.prototype = {
	constructor : CrossHairs,
	draw: function() {
		var ctx = this.ctx;
		if (!this.imageRect) return;
		var rect = this.imageRect;
		var canvX = Math.round(Math.min(rect.right(), Math.max(rect.left(), this.getCanvasCrossHairX())));
		var canvY = Math.round(Math.min(rect.bottom(), Math.max(rect.top(), this.getCanvasCrossHairY())));
		//console.log(canvX + " " + canvY + " " + Math.round(rect.top())  + " " + Math.round(rect.bottom()));
		ctx.strokeStyle = this.colorVert;
		ctx.beginPath();
		ctx.moveTo(canvX + 0.5, Math.round(rect.top()) + 0.5);
		ctx.lineTo(canvX + 0.5, Math.round(rect.bottom() - 1) + 0.5);
		ctx.stroke();

		ctx.strokeStyle = this.colorHoriz;
		ctx.beginPath();
		ctx.moveTo(Math.round(rect.left()) + 0.5,  canvY + 0.5);
		ctx.lineTo(Math.round(rect.right() - 1) + 0.5, canvY + 0.5);
		ctx.stroke();
	},
	loadImg: function (index, doDraw) {
		doDraw = doDraw || false;
		var imageIndex = getImageIndex(index, this.numImages, is4D, imageTime);
		if (!this.images[imageIndex]) {
			this.images[imageIndex] = new Image();
			var ch = this;
			this.images[imageIndex].onload = function () {
				if (!ch.imageRect) ch.imageRect = imgScaleRect(ch.canvas.width, ch.images[imageIndex].width, ch.images[imageIndex].height);
				if (doDraw) draw();
				console.log("loaded "+ ch.imageBase + imageIndex  + " " + doDraw);
			};
			this.images[imageIndex].src = getImageSrc(index, this.imageBase, is4D, imageTime);
		}
	},
	drawImg: function (index) {
		if (!index) {
			index = this.imgIndex;
		} else {
			index = Math.round(Math.min(this.numImages - 1, Math.max(0, index)));
			this.imgIndex = index;
		}
		var imageIndex = getImageIndex(index, this.numImages, is4D, imageTime);
		if (!this.images[imageIndex]) {
			this.loadImg(index);
		} else {
			//avoid trying to draw a non-loaded image. 
		  if (this.images[imageIndex].complete && this.imageRect) {
				var imageRect = this.imageRect;
				clear(this.canvas, this.ctx);
				this.ctx.drawImage(this.images[imageIndex], Math.round(imageRect.x), Math.round(imageRect.y), Math.round(imageRect.width), Math.round(imageRect.height));
				filterImg(this.ctx, this.canvas.width, this.canvas.height, this.imageRect);
			}
		}
	},
	getCanvasCrossHairX: function () {
	  var clampX = Math.min(this.dimX - 1, Math.max(0, this.x));
		var t = clampX / (this.dimX - 1);
		return Math.round(this.imageRect.left() + (this.imageRect.width - 1) * t);
	},
	getCanvasCrossHairY: function () {
	  var clampY = Math.min(this.dimY - 1, Math.max(0, this.y));
		var t = clampY / (this.dimY - 1);
		return Math.round(this.imageRect.top() + (this.imageRect.height - 1) * t);
	}, 
	reset: function () {
		var ch = this;
		var imageIndex = getImageIndex(ch.imgIndex, this.numImages, is4D, imageTime);
		ch.imageRect = imgScaleRect(ch.canvas.width, ch.images[imageIndex].width, ch.images[imageIndex].height);
		this.x = Math.round(this.dimX/2);
		this.y = Math.round(this.dimY/2);
		this.imgIndex = Math.round(this.numImages/2);
	}
}

// declare crossHair managers.
var crossHairsAx	= null;
var crossHairsSag = null;
var crossHairsCor = null;

function clear( canvas, ctx, trail ) {
	if ( trail )
	{
		ctx.fillStyle = 'rgba(0,0,0,0.4)';
	}
	else
	{
		ctx.fillStyle = 'rgba(0,0,0,1.0)';
	}
	ctx.fillRect(0,0,canvas.width,canvas.height);
}

function draw() {
	crossHairsAx.drawImg();
	crossHairsSag.drawImg();
	crossHairsCor.drawImg();
	crossHairsAx.draw();
	crossHairsSag.draw();
	crossHairsCor.draw();

	//raf = window.requestAnimationFrame(draw);
}

function reset() {
	crossHairsAx.reset();
	crossHairsSag.reset();
	crossHairsCor.reset();
	
  calcWinLevTransferFunction(256, 128, tfx, tfy);
	generateLut(lutIndex, tfx, tfy);
  handleLUT(0);
	
	draw();
}

function CanvasScale(factor) {
	axialCTX.canvas.width += factor * Math.round(axialCTX.canvas.width*0.20);
	axialCTX.canvas.height += factor * Math.round(axialCTX.canvas.height*0.20);
	sagittalCTX.canvas.width += factor * Math.round(sagittalCTX.canvas.width*0.20);
	sagittalCTX.canvas.height += factor * Math.round(sagittalCTX.canvas.height*0.20);
	coronalCTX.canvas.width += factor * Math.round(coronalCTX.canvas.width*0.20);
	coronalCTX.canvas.height += factor * Math.round(coronalCTX.canvas.height*0.20);

	crossHairsAx.reset();
	crossHairsSag.reset();
	crossHairsCor.reset();
	draw();
}

// From Mipav, JDialogWinLevel.java
function calcWinLevTransferFunction(win, lev, tfx, tfy) {
	if (tfx == null || tfx.length != 4) {
		throw ("Transfer function x component not set up correctly.");
	}
	if (tfy == null || tfy.length != 4) {
		throw ("Transfer function y component not set up correctly.");
	}
	
	var img_min = 0.0;
	var img_max = 255.0;
	
	// first point always at lower left
	tfx[0] = img_min;
	tfy[0] = 255;
	
	if (win <= 0) {
		win = 1;
	}
	
	tfx[2] = lev + (win / 2.0);

	if (tfx[2] > img_max) {
		tfy[2] = 255.0 * (tfx[2] - img_max) / win;
		tfx[2] = img_max;
	} else {
		tfy[2] = 0.0;
	}

	tfx[1] = lev - (win / 2.0);

	if (tfx[1] < img_min) {
		tfy[1] = 255.0 - (255.0 * (img_min - tfx[1]) / win);
		tfx[1] = img_min;
	} else {
		tfy[1] = 255.0;
	}
	
	// last point always at upper right of histogram
	tfx[3] = img_max;
	tfy[3] = 0;
}

// from Mipav, TransferFunction.java
function getRemappedValue(inputValue, height, tfx, tfy) {

	var i;
	var slope = 0;
	var ptX1, ptX2, ptY1, ptY2;

	if (tfx === null || tfy === null) {
		return 0;
	}

	for (i = 0; i < (tfx.length - 1); i++) {
		ptX1 = tfx[i];
		ptX2 = tfx[i + 1];
		ptY1 = (height - 1) - tfy[i];
		ptY2 = (height - 1) - tfy[i + 1];


		if ((inputValue >= ptX1) && (inputValue <= ptX2)) {

			if ((ptX2 - ptX1) != 0) {
				slope = (ptY2 - ptY1) / (ptX2 - ptX1);
			} else {
				slope = 0;
			}

			return ptY1 + (slope * (inputValue - ptX1));
		}
	}

	return 0;
}

function generateLut(lutOut, tfx, tfy) {
	var i = 0;
	for (i = 0; i < lutOut.length; i++) {
		lutOut[i] = getRemappedValue(i, 256, tfx, tfy);
	}
}

function filterImg(imgCtx, width, height, rect) {
	// get all canvas pixel data
	var imageData = imgCtx.getImageData(0, 0, width, height);
	for (y = 0; y < height; y++) {
		pos = y * width * 4;
		for (x = 0; x < width; x++) {
			if (rect.inside(x, y)) {
				//incoming image data shifted with window/level or full remap
				// then remapped value is looked up in LUT. 
				imageData.data[pos] = lutR[lutIndex[imageData.data[pos]]];
				imageData.data[pos+1] = lutG[lutIndex[imageData.data[pos+1]]];
				imageData.data[pos+2] = lutB[lutIndex[imageData.data[pos+2]]];
				imageData.data[pos+3] = 255;
			} else {
				imageData.data[pos] = 0;
				imageData.data[pos+1] = 0;
				imageData.data[pos+2] = 0;
				imageData.data[pos+3] = 255;
			}
			pos += 4;
		}
	}
	imgCtx.putImageData(imageData, 0, 0);
}

// define the same LUTs as MIPAV
function getLutTfx(index, outArray) {
	// WARNING: Colors are inverted. Mipav defines LUTs so that y=0 is MAX, and y = height-1 is MIN
	// so here r=255 is black, and r = 0 is full red. Sigh. 
	
	//ModelLUT.java, makeXXXTransferFunctions
	var x, r, g, b;
	switch (index) {
	default:
	case 0: //gray
		x = new Uint8ClampedArray(2);
		r = new Uint8ClampedArray(2);
		x[0] = 0;
		r[0] = 255;
		x[1] = 255;
		r[1] = 0;
		b = g = r;
		break;
	case 1: //red
		x = new Uint8ClampedArray(2);
		r = new Uint8ClampedArray(2);
		g = new Uint8ClampedArray(2);
		x[0] = 0;
		r[0] = 255;
		g[0] = 255;
		x[1] = 255;
		r[1] = 0;
		g[1] = 255;
		b = g;
		break;
	case 2: //green
		x = new Uint8ClampedArray(2);
		r = new Uint8ClampedArray(2);
		g = new Uint8ClampedArray(2);
		x[0] = 0;
		r[0] = 255;
		g[0] = 255;
		x[1] = 255;
		r[1] = 255;
		g[1] = 0;
		b = r;
		break;
	case 3: //blue
		x = new Uint8ClampedArray(2);
		r = new Uint8ClampedArray(2);
		b = new Uint8ClampedArray(2);
		x[0] = 0;
		r[0] = 255;
		b[0] = 255;
		x[1] = 255;
		r[1] = 255;
		b[1] = 0;
		g = r;
		break;
	case 4: //grey red/blue
	  var size = 6;
		x = new Uint8ClampedArray(size);
		r = new Uint8ClampedArray(size);
		g = new Uint8ClampedArray(size);
		b = new Uint8ClampedArray(size);
		x[0] = 0;
		r[0] = 255;
		g[0] = 255;
		b[0] = 0;

		x[1] = 1;
		r[1] = 255;
		g[1] = 255;
		b[1] = 0;

		x[2] = 1;
		r[2] = 255;
		g[2] = 255;
		b[2] = 255;

		x[3] = 254;
		r[3] = 0;
		g[3] = 0;
		b[3] = 0;

		x[4] = 254;
		r[4] = 0;
		g[4] = 255;
		b[4] = 255;

		x[5] = 255;
		r[5] = 0;
		g[5] = 255;
		b[5] = 255;
		break;
	case 5: //hot metal
	  var size = 4;
		x = new Uint8ClampedArray(size);
		r = new Uint8ClampedArray(size);
		g = new Uint8ClampedArray(size);
		b = new Uint8ClampedArray(size);
		x[0] = 0;
		r[0] = 255;
		g[0] = 255;
		b[0] = 255;

		x[1] = 95;
		r[1] = 0;
		g[1] = 255;
		b[1] = 255;

		x[2] = 190;
		r[2] = 0;
		g[2] = 0;
		b[2] = 255;

		x[3] = 255;
		r[3] = 0;
		g[3] = 0;
		b[3] = 0;
		break;
	case 6: //spectrum
	  var size = 6;
		x = new Uint8ClampedArray(size);
		r = new Uint8ClampedArray(size);
		g = new Uint8ClampedArray(size);
		b = new Uint8ClampedArray(size);
		x[0] = 0;
		r[0] = 255;
		g[0] = 255;
		b[0] = 127;

		x[1] = 32;
		r[1] = 255;
		g[1] = 255;
		b[1] = 0;

		x[2] = 96;
		r[2] = 255;
		g[2] = 0;
		b[2] = 0;

		x[3] = 160;
		r[3] = 0;
		g[3] = 0;
		b[3] = 255;

		x[4] = 224;
		r[4] = 0;
		g[4] = 255;
		b[4] = 255;

		x[5] = 255;
		r[5] = 127;
		g[5] = 255;
		b[5] = 255;
		break;
	case 7: //cool hot
	  var size = 5;
		x = new Uint8ClampedArray(size);
		r = new Uint8ClampedArray(size);
		g = new Uint8ClampedArray(size);
		b = new Uint8ClampedArray(size);
		x[0] = 0;
		r[0] = 255;
		g[0] = 255;
		b[0] = 255;

		x[1] = 48;
		r[1] = 255;
		g[1] = 255;
		b[1] = 0;

		x[2] = 112;
		r[2] = 0;
		g[2] = 255;
		b[2] = 255;

		x[3] = 184;
		r[3] = 0;
		g[3] = 0;
		b[3] = 255;

		x[4] = 255;
		r[4] = 0;
		g[4] = 0;
		b[4] = 0;

		break;
	case 8: //skin
	  var size = 5;
		x = new Uint8ClampedArray(size);
		r = new Uint8ClampedArray(size);
		g = new Uint8ClampedArray(size);
		b = new Uint8ClampedArray(size);
		x[0] = 0;
		r[0] = 255;
		g[0] = 255;
		b[0] = 255;

		x[1] = 109;
		r[1] = 84;
		g[1] = 139;
		b[1] = 146;

		x[2] = 126;
		r[2] = 78;
		g[2] = 121;
		b[2] = 130;

		x[3] = 128;
		r[3] = 77;
		g[3] = 119;
		b[3] = 128;

		x[4] = 255;
		r[4] = 0;
		g[4] = 0;
		b[4] = 0;
		break;
	case 9: //bone
	  var size = 5;
		x = new Uint8ClampedArray(size);
		r = new Uint8ClampedArray(size);
		g = new Uint8ClampedArray(size);
		b = new Uint8ClampedArray(size);
		x[0] = 0;
		r[0] = 255;
		g[0] = 255;
		b[0] = 255;

		x[1] = 139;
		r[1] = 2;

		x[2] = 169;
		g[2] = 45;

		x[3] = 241;
		b[3] = 45;

		x[4] = 255;
		r[4] = 0;
		g[4] = 0;
		b[4] = 0;

		g[1] = ((g[2] - g[0])/(x[2]-x[0])) * x[1] + g[0];
		b[1] = ((b[3] - b[0])/(x[3]-x[0])) * x[1] + b[0];
		r[2] = ((r[4] - r[1])/(x[4]-x[1])) * x[2] + r[1];
		b[2] = ((b[3] - b[0])/(x[3]-x[0])) * x[2] + b[0];
		r[3] = ((r[4] - r[1])/(x[4]-x[1])) * x[3] + r[1];
		g[3] = ((g[4] - g[2])/(x[4]-x[2])) * x[3] + g[2];

		break;
	}
	outArray[0] = x;
	outArray[1] = r;
	outArray[2] = g;
	outArray[3] = b;
	
}

// switch to a new LUT
function handleLUT(index) {
	var x, r, g, b;
	var outArray = new Array(4);
	getLutTfx(index, outArray);
	x = outArray[0]; r = outArray[1]; g = outArray[2]; b = outArray[3]; 
	generateLut(lutR, x, r);
	generateLut(lutG, x, g);
	generateLut(lutB, x, b);
	x = null;
	r = null; g = null; b = null;
	draw();
}

// convert a clicked point into an image index
function getImageIndexX(x, rect, numImages) {
  var clampX = Math.min(rect.right(), Math.max(rect.left(), x));
	return Math.round(numImages * (x - rect.left())/(rect.width));
}

function getImageIndexY(y, rect, numImages) {
  var clampY = Math.min(rect.bottom(), Math.max(rect.top(), y));
	return Math.round(numImages * (y - rect.top())/(rect.height));
}

function mouseStart(e) {
	if (this === axialCanvas) runningCrossHairs = crossHairsAx;
	else if (this === sagittalCanvas) runningCrossHairs = crossHairsSag;
	else if (this === coronalCanvas) runningCrossHairs = crossHairsCor;
	var rect = runningCrossHairs.canvas.getBoundingClientRect();
	var x = e.clientX - rect.left;
	var y = e.clientY - rect.top;
	mouseStartX = x;
	mouseStartY = y;
	mouseStartRect.copy(runningCrossHairs.imageRect);
	//console.log(this.id + " start " + e.button + " " + (this === axialCanvas));
	myButton = e.button;
	return true;
}

function mouseEnd(e) {
	runningCrossHairs = null;
	//window.cancelAnimationFrame(raf);
	//console.log("end	 " + e.button);
	if (e.button == 2) {
		generateLut(lutIndex, tfx, tfy);
	}

	// No right-click menu:
	if (e.button == 2) return false;
	return true;
}

function mouseMove(e) {
	if (!runningCrossHairs) return;
	var rect = runningCrossHairs.canvas.getBoundingClientRect();
	// mousemove always sets e.button == 0 , even when another button is down. Use myButton...
	var x = e.clientX - rect.left;
	var y = e.clientY - rect.top;
	if (myButton == 0)
	{
		//console.log(e.button + " " + rect.top + " " + rect.left + " " + e.clientX);
	  //clicked point is in canvas coords. Convert to an image index using the image bounding rect. 
		runningCrossHairs.x = x;
		runningCrossHairs.y = y;
		runningCrossHairs.drawImg();
		if (runningCrossHairs === crossHairsAx) {
			var imgIndexX = getImageIndexX(x, crossHairsAx.imageRect, crossHairsSag.numImages);
			var imgIndexY = getImageIndexY(y, crossHairsAx.imageRect, crossHairsCor.numImages);
			crossHairsAx.x = imgIndexX;
			crossHairsAx.y = imgIndexY;
			crossHairsAx.drawImg();
			crossHairsSag.drawImg(imgIndexX);
			crossHairsSag.x = imgIndexY;
			crossHairsCor.drawImg(imgIndexY);
			crossHairsCor.x = imgIndexX;
		} else if (runningCrossHairs === crossHairsSag) {
			var imgIndexX = getImageIndexX(x, crossHairsSag.imageRect, crossHairsCor.numImages);
			var imgIndexY = getImageIndexY(y, crossHairsSag.imageRect, crossHairsAx.numImages);
			crossHairsSag.x = imgIndexX;
			crossHairsSag.y = imgIndexY;
			crossHairsSag.drawImg();
			crossHairsAx.drawImg(crossHairsAx.numImages - imgIndexY - 1);
			crossHairsAx.y = imgIndexX;
			crossHairsCor.drawImg(imgIndexX);
			crossHairsCor.y = imgIndexY;
		} else if (runningCrossHairs === crossHairsCor) {
			var imgIndexX = getImageIndexX(x, crossHairsCor.imageRect, crossHairsSag.numImages);
			var imgIndexY = getImageIndexY(y, crossHairsCor.imageRect, crossHairsAx.numImages);
			crossHairsCor.x = imgIndexX;
			crossHairsCor.y = imgIndexY;
			crossHairsCor.drawImg();
			crossHairsAx.drawImg(crossHairsAx.numImages - imgIndexY - 1);
			crossHairsAx.x = imgIndexX;
			crossHairsSag.drawImg(imgIndexX);
			crossHairsSag.y = imgIndexY;
		}
		crossHairsAx.draw();
		crossHairsSag.draw();
		crossHairsCor.draw();
	}
	else if (myButton == 1) 
	{
		var ctrlPressed=0;
		//var altPressed=0;
		//var shiftPressed=0;
		//shiftPressed=e.shiftKey;
		//altPressed  =e.altKey;
		ctrlPressed =e.ctrlKey;
		if (!ctrlPressed) {
			//zoom, vertical motion. 
			var diff = mouseStartY - y;
			if (diff == 0) {
				runningCrossHairs.imageRect.copy(mouseStartRect);
				return true;
			}
			// zoom 2x with move from center to edge. 
			var fullScale = (rect.bottom - rect.top) / 2; 
			var scale = (diff > 0) ? (diff / fullScale ) + 1 : (1 / ((-diff / fullScale) + 1));
			//console.log("diff " + diff + " scale " + scale);
			var imageRect = runningCrossHairs.imageRect;
			imageRect.width = mouseStartRect.width * scale;
			imageRect.height = mouseStartRect.height * scale;
			var centerX = mouseStartRect.x + mouseStartRect.width*0.5;
			var centerY = mouseStartRect.y + mouseStartRect.height*0.5;
			imageRect.x = centerX - imageRect.width * 0.5;
			imageRect.y = centerY - imageRect.height * 0.5;
			
			runningCrossHairs.drawImg();
			runningCrossHairs.draw();
		} else {
			// translation with CTRL held down.
			var diffX = x - mouseStartX;
			var diffY = y - mouseStartY;
			var imageRect = runningCrossHairs.imageRect;
			imageRect.x = mouseStartRect.x + diffX;
			imageRect.y = mouseStartRect.y + diffY;
			if ( window.console && console.log ){
			console.log(imageRect);
			}
			runningCrossHairs.drawImg();
			runningCrossHairs.draw();

		}
	}
	else if (myButton == 2) 
	{
		// Change the window/level of the LUT
		var lvl = 1.0 * y;
		var win = 2.0 * x;
		
		if (intervalID) {
			// Changing the LUT is too hard during animation. Stop. 
			playPause();
		}

		calcWinLevTransferFunction(win, lvl, tfx, tfy);
		generateLut(lutIndex, tfx, tfy);

		draw();
		// draw the window-level line:
		//var ctx = crossHairsAx.ctx;
		//ctx.strokeStyle = crossHairsAx.colorVert;
		//ctx.beginPath();
		//ctx.moveTo(tfx[1] + 0.5, tfy[1] + 0.5);
		//ctx.lineTo(tfx[2] + 0.5, tfy[2] + 0.5);
		//ctx.stroke();
	}
	return true;
}

var axialImg0 = new Image();
var sagittalImg0 = new Image();

// Once image dimensions and is4D are known, we can create and load our crossHair objects, and hook up mouse events. 
function onLoad() {
  // === check for image completion, get dimensions.
	if (!axialImg0.complete || !sagittalImg0.complete) return;
	var dimX = axialImg0.width,
		dimY = axialImg0.height,
		dimZ = sagittalImg0.height;
	if (sagittalImg0.width != dimY) {
		alert("Inconsistent image dimensions, expect incomplete view of volume data.");
	}
  //=== create crossHair objects
	crossHairsAx	= new CrossHairs(axialCanvas, axialCTX, "axial", dimZ, dimX, dimY, maxImageTime);
	crossHairsAx.images[0] = axialImg0;

	crossHairsSag = new CrossHairs(sagittalCanvas, sagittalCTX, "sagittal", dimX, dimY, dimZ, maxImageTime);
	crossHairsSag.images[0] = sagittalImg0;
	crossHairsSag.colorHoriz = "red";
	crossHairsSag.colorVert = "green";

	crossHairsCor = new CrossHairs(coronalCanvas, coronalCTX, "coronal", dimY, dimX, dimZ, maxImageTime);
	crossHairsCor.colorHoriz = "red";
	
	//=== hook up mouse events.

	// handle mouseup events outside the canvas. 
	//var body = document.getElementsByTagName("body")[0];
	// body doesn't work because it doesn't extend past the canvas to the bottom of the window. 
	// window still doesn't get mouseup outside the window. Seems like an OK compromise. 
	window.addEventListener("mouseup", mouseEnd, false);

	axialCanvas.addEventListener("mousemove", mouseMove, false);
	axialCanvas.addEventListener("mousedown", mouseStart);
	// don't want "mouseout" because it won't start dragging again when we re-enter the window. 
	//axialCanvas.addEventListener("mouseout", mouseEnd);
	axialCanvas.addEventListener("mouseup", mouseEnd);

	sagittalCanvas.addEventListener("mousemove", mouseMove, false);
	sagittalCanvas.addEventListener("mousedown", mouseStart);
	sagittalCanvas.addEventListener("mouseup", mouseEnd);

	coronalCanvas.addEventListener("mousemove", mouseMove, false);
	coronalCanvas.addEventListener("mousedown", mouseStart);
	coronalCanvas.addEventListener("mouseup", mouseEnd);
	
	//=== Load initial view.
	if (is4D) timeControls.style.display = 'block';
	else timeControls.style.display = 'none';
	
  var i = 0;
	crossHairsAx.loadImg(crossHairsAx.imgIndex, true);
	crossHairsSag.loadImg(crossHairsSag.imgIndex, true);
	crossHairsCor.loadImg(crossHairsCor.imgIndex, true);

  calcWinLevTransferFunction(256, 128, tfx, tfy);
	generateLut(lutIndex, tfx, tfy);
  handleLUT(0);
	draw();

	// pre-load testing. Hangs the whole interface while images load. 
	// Quick for local files, ~2 sec. ?? for web server ??
	for (i = 0; i < crossHairsAx.numImages; i++) {
		crossHairsAx.loadImg(i, false);
	}
	for (i = 0; i < crossHairsSag.numImages; i++) {
		crossHairsSag.loadImg(i, false);
	}
	for (i = 0; i < crossHairsCor.numImages; i++) {
		crossHairsCor.loadImg(i, false);
	}
	draw();

}

//Need to retrieve image dimensions by loading the first images. 
function preLoad() {
	axialImg0.onload = function() {
		if (sagittalImg0.complete) onLoad();
	}
	axialImg0.src = getImageSrc(0, "axial", is4D, imageTime);
	sagittalImg0.onload = function() {
		if (axialImg0.complete) onLoad();
	}
	sagittalImg0.src = getImageSrc(0, "sagittal", is4D, imageTime);
}

// callback for dataset selector. 
function changeDataset(imagePrefix, is4DImage, timeSlices) {
	
	if (is4DImage && timeSlices > 1) {
		is4D = true;
		maxImageTime = timeSlices - 1;
		if (isNaN(maxImageTime)) {
			is4D = false;
			maxImageTime = 1;
		} else {
			rangeSlider.max = maxImageTime;
		}
		
		imageTime = Math.min(maxImageTime, imageTime);
	} else {
		is4D = false;
	}
	
	datasetRoot = imagePrefix;
	if ( window.console && console.log ){
	console.log(datasetRoot + " " + is4D + " " + maxImageTime);
	}
	if (intervalID && !is4D) {
		//stop if not 4D. 
		playPause();
	}
	preLoad();
}