

function JsResponse () {
    this.deOneAnswer = "";
    this.deTwoAnswer = "";
    this.finalAnswer = "";
    this.questionId = null;
    this.editAnswer = "";
    this.changeReason = "";
    this.edited = false;
    this.responseId = null;
    this.sectionId = null;
    this.qText = "";

}

JsResponse.prototype.init = function (answer1, answer2, finalanswer, questionid, responseid) {
    this.deOneAnswer = answer1;
    this.deTwoAnswer = answer2;
    this.finalAnswer = finalanswer;
    this.questionId = questionid;
    this.resposneId = responseid;
}


JsResponse.prototype.isEdited = function () {
    if (this.edited != false) {
        return true;
    } else {
        return false;
    }
}

JsResponse.prototype.setAnswer1 = function (ans) { this.deOneAnswer = ans; }
JsResponse.prototype.getAnswer1 = function () { return this.deOneAnswer; }

JsResponse.prototype.setAnswer2 = function (ans) { this.deTwoAnswer = ans; }
JsResponse.prototype.getAnswer2 = function () { return this.deTwoAnswer; }

JsResponse.prototype.setFinalAnswer = function (ans) { this.finalAnswer = ans; }
JsResponse.prototype.getFinalAnswer = function () { return this.finalAnswer; }

JsResponse.prototype.setQuestionId = function (ans) { this.questionId = ans; }
JsResponse.prototype.getQuestionId = function () { return this.questionId; }

JsResponse.prototype.setEditAnswer = function (ans) { this.editAnswer = ans; }
JsResponse.prototype.getEditAnswer = function () { return this.editAnswer; }

JsResponse.prototype.setChangeReason = function (ans) { this.changeReason = ans; }
JsResponse.prototype.getChangeReason = function () { return this.changeReason; }

JsResponse.prototype.setResponseId = function (ans) { this.responseId = ans; }
JsResponse.prototype.getResponseId = function () { return this.responseId; }

JsResponse.prototype.setEdited = function (ans) { this.edited = ans; }


JsResponse.prototype.setQText = function (ans) { this.qText = ans; }
JsResponse.prototype.getQText = function () { return this.qText; }

