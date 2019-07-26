/*Exmple...
e-Form name:{
	raw score: [T-score, Standard error]
}*/
var conversion_table = {
		"NINRBRICSSymPain":{
			"3":[30.7,4.5],
			"4":[36.3,3.1],
			"5":[40.2,3.0],
			"6":[43.5,3.0],
			"7":[46.3,3.0],
			"8":[49.4,2.9],
			"9":[52.1,2.8],
			"10":[54.5,2.9],
			"11":[57.5,3.1],
			"12":[60.5,3.1],
			"13":[64.1,3.8],
			"14":[67.4,4.2],
			"15":[71.7,5.0]

		},
		"NINRBRICSSymAnxiety":{
			"6":[39.1,5.9],
			"7":[45.9,3.4],
			"8":[48.8,2.9],
			"9":[50.9,2.6],
			"10":[52.7,2.4],
			"11":[54.2,2.3],
			"12":[55.6,2.2],
			"13":[56.9,2.2],
			"14":[58.2,2.2],
			"15":[59.4,2.2],
			"16":[60.7,2.2],
			"17":[62.0,2.2],
			"18":[63.3,2.2],
			"19":[64.6,2.2],
			"20":[66.0,2.2],
			"21":[67.3,2.2],
			"22":[68.6,2.2],
			"23":[70.0,2.2],
			"24":[71.3,2.2],
			"25":[72.7,2.2],
			"26":[74.1,2.2],
			"27":[75.6,2.3],
			"28":[77.4,2.4],
			"29":[79.4,2.7],
			"30":[82.7,3.5]
		},
		"NINRBRICSSymAffDepression":{
			"6":[38.4,5.8],
			"7":[45.2,3.4],
			"8":[48.3,2.8],
			"9":[50.4,2.4],
			"10":[52.0,2.2],
			"11":[53.4,2.1],
			"12":[54.7,2.0],
			"13":[55.9,2.0],
			"14":[57.0,1.9],
			"15":[58.2,1.9],
			"16":[59.3,2.0],
			"17":[60.5,2.0],
			"18":[61.7,2.0],
			"19":[62.9,2.0],
			"20":[64.2,2.0],
			"21":[65.5,2.0],
			"22":[66.7,2.0],
			"23":[68.0,2.0],
			"24":[69.3,2.0],
			"25":[70.6,2.0],
			"26":[72.0,2.0],
			"27":[73.4,2.0],
			"28":[75.0,2.1],
			"29":[76.9,2.4],
			"30":[80.3,3.5]
		},
		"NINRBRICSSymFatigue":{
			"6":[33.4,4.9],
			"7":[39.1,2.9],
			"8":[42.0,2.4],
			"9":[44.2,2.2],
			"10":[46.1,2.1],
			"11":[47.8,2.1],
			"12":[49.4,2.1],
			"13":[50.9,2.0],
			"14":[52.4,2.0],
			"15":[53.7,2.0],
			"16":[55.1,2.0],
			"17":[56.3,1.9],
			"18":[57.5,1.9],
			"19":[58.8,1.9],
			"20":[60.0,1.9],
			"21":[61.2,1.9],
			"22":[62.4,1.9],
			"23":[63.7,2.0],
			"24":[65.0,2.0],
			"25":[66.4,2.0],
			"26":[67.8,2.0],
			"27":[69.3,2.0],
			"28":[71.0,2.1],
			"29":[73.0,2.5],
			"30":[76.8,3.8]
		},
		"NINRBRICSSymPrntFatigue":{
			"10":[34.0,5.0],
			"11":[39.0,4.0],
			"12":[42.0,3.0],
			"13":[44.0,3.0],
			"14":[45.0,3.0],
			"15":[47.0,3.0],
			"16":[48.0,2.0],
			"17":[49.0,2.0],
			"18":[50.0,2.0],
			"19":[51.0,2.0],
			"20":[52.0,2.0],
			"21":[53.0,2.0],
			"22":[54.0,2.0],
			"23":[55.0,2.0],
			"24":[56.0,2.0],
			"25":[57.0,2.0],
			"26":[58.0,2.0],
			"27":[59.0,2.0],
			"28":[60.0,2.0],
			"29":[61.0,2.0],
			"30":[62.0,2.0],
			"31":[63.0,2.0],
			"32":[64.0,2.0],
			"33":[65.0,2.0],
			"34":[66.0,2.0],
			"35":[67.0,2.0],
			"36":[68.0,2.0],
			"37":[69.0,2.0],
			"38":[70.0,2.0],
			"39":[71.0,2.0],
			"40":[72.0,2.0],
			"41":[72.0,2.0],
			"42":[73.0,2.0],
			"43":[74.0,2.0],
			"44":[75.0,2.0],
			"45":[76.0,2.0],
			"46":[77.0,2.0],
			"47":[79.0,3.0],
			"48":[80.0,3.0],
			"49":[82.0,3.0],
			"50":[85.0,4.0]
		},
		"NINRBRICSSymPrntGlblHlth":{
			"7":[14.7,2.9],
			"8":[15.3,3.1],
			"9":[16.0,3.2],
			"10":[16.9,3.4],
			"11":[18.1,3.6],
			"12":[19.4,3.7],
			"13":[21.0,3.8],
			"14":[22.7,3.8],
			"15":[24.4,3.7],
			"16":[26.1,3.7],
			"17":[27.7,3.7],
			"18":[29.4,3.8],
			"19":[31.2,3.8],
			"20":[32.9,3.8],
			"21":[34.6,3.8],
			"22":[36.2,3.8],
			"23":[37.9,3.9],
			"24":[39.7,4.0],
			"25":[41.7,4.0],
			"26":[43.6,3.9],
			"27":[45.4,3.8],
			"28":[47.3,3.9],
			"29":[49.3,4.1],
			"30":[51.8,4.4],
			"31":[54.5,4.7],
			"32":[57.3,5.0],
			"33":[60.2,5.4],
			"34":[63.2,6.0],
			"35":[66.1,6.5]
		},
		"NINRBRICSSymPedFatigue":{
			"10":[30.3,5.5],
			"11":[34.3,4.7],
			"12":[36.9,4.4],
			"13":[39.0,4.1],
			"14":[40.9,3.9],
			"15":[42.5,3.8],
			"16":[44.0,3.7],
			"17":[45.4,3.6],
			"18":[46.7,3.5],
			"19":[47.9,3.5],
			"20":[49.1,3.4],
			"21":[50.2,3.4],
			"22":[51.3,3.4],
			"23":[52.4,3.4],
			"24":[53.5,3.4],
			"25":[54.5,3.4],
			"26":[55.6,3.4],
			"27":[56.6,3.4],
			"28":[57.6,3.4],
			"29":[58.6,3.3],
			"30":[59.6,3.3],
			"31":[60.6,3.3],
			"32":[61.6,3.3],
			"33":[62.6,3.3],
			"34":[63.6,3.3],
			"35":[64.6,3.3],
			"36":[65.6,3.3],
			"37":[66.7,3.3],
			"38":[67.7,3.3],
			"39":[68.7,3.3],
			"40":[69.8,3.3],
			"41":[70.9,3.3],
			"42":[72.0,3.4],
			"43":[73.2,3.4],
			"44":[74.4,3.4],
			"45":[75.7,3.5],
			"46":[77.0,3.6],
			"47":[78.5,3.6],
			"48":[80.2,3.7],
			"49":[82.0,3.7],
			"50":[84.0,3.5]
		},
		"NINRBRICSSymPedGlblHlth":{
			"7":[16.0,3.4],
			"8":[17.1,3.6],
			"9":[18.3,3.7],
			"10":[19.7,3.8],
			"11":[21.2,3.8],
			"12":[22.8,3.7],
			"13":[24.4,3.6],
			"14":[26.1,3.6],
			"15":[27.6,3.5],
			"16":[29.2,3.5],
			"17":[30.8,3.5],
			"18":[32.4,3.6],
			"19":[34.0,3.6],
			"20":[35.6,3.6],
			"21":[37.2,3.6],
			"22":[38.8,3.6],
			"23":[40.4,3.6],
			"24":[42.1,3.7],
			"25":[43.9,3.7],
			"26":[45.7,3.6],
			"27":[47.5,3.6],
			"28":[49.2,3.6],
			"29":[51.1,3.7],
			"30":[53.3,3.9],
			"31":[55.7,4.2],
			"32":[58.3,4.5],
			"33":[61.1,4.9],
			"34":[64.2,5.4],
			"35":[67.5,6.1]
		},
		"NINRBRICSSymCognitive":{
			"6":[23.13,4.25],
			"7":[26.64,3.28],
			"8":[28.55,3.05],
			"9":[30.18,2.84],
			"10":[31.58,2.72],
			"11":[32.85,2.64],
			"12":[34.04,2.59],
			"13":[35.17,2.57],
			"14":[36.28,2.57],
			"15":[37.37,2.57],
			"16":[38.45,2.57],
			"17":[39.53,2.58],
			"18":[40.63,2.59],
			"19":[41.74,2.6],
			"20":[42.87,2.62],
			"21":[44.04,2.63],
			"22":[45.23,2.64],
			"23":[46.47,2.67],
			"24":[47.77,2.71],
			"25":[49.17,2.79],
			"26":[50.72,2.94],
			"27":[52.49,3.14],
			"28":[54.69,3.51],
			"29":[57.6,4.04],
			"30":[63.17,5.75]
		},	
		"NINRBRICSSymSleepDist":{
			"6":[31.7,5.1],
			"7":[36.9,3.9],
			"8":[40.1,3.5],
			"9":[42.5,3.3],
			"10":[44.6,3.2],
			"11":[46.4,3.1],
			"12":[48.0,3.0],
			"13":[49.5,3.0],
			"14":[50.9,3.0],
			"15":[52.3,2.9],
			"16":[53.6,2.9],
			"17":[54.8,2.9],
			"18":[56.1,2.9],
			"19":[57.3,2.9],
			"20":[58.5,2.9],
			"21":[59.7,2.9],
			"22":[61.0,2.9],
			"23":[62.3,2.9],
			"24":[63.6,2.9],
			"25":[65.0,2.9],
			"26":[66.5,3.0],
			"27":[68.1,3.1],
			"28":[70.0,3.3],
			"29":[72.4,3.6],
			"30":[76.1,4.4]
		},
		"PhysicalHealthScoring":{
			"4":[16.2,4.8],
			"5":[19.9,4.7],
			"6":[23.5,4.5],
			"7":[26.7,4.3],
			"8":[29.6,4.2],
			"9":[32.4,4.2],
			"10":[34.9,4.1],
			"11":[37.4,4.1],
			"12":[39.8,4.1],
			"13":[42.3,4.2],
			"14":[44.9,4.3],
			"15":[47.7,4.4],
			"16":[50.8,4.6],
			"17":[54.1,4.7],
			"18":[57.7,4.9],
			"19":[61.9,5.2],
			"20":[67.7,5.9]
		},
		"MentalHealthScoring":{
			"4":[21.2,4.6],
			"5":[25.1,4.1],
			"6":[28.4,3.9],
			"7":[31.3,3.7],
			"8":[33.8,3.7],
			"9":[36.3,3.7],
			"10":[38.8,3.6],
			"11":[41.1,3.6],
			"12":[43.5,3.6],
			"13":[45.8,3.6],
			"14":[48.3,3.7],
			"15":[50.8,3.7],
			"16":[53.3,3.7],
			"17":[56.0,3.8],
			"18":[59.0,3.9],
			"19":[62.5,4.2],
			"20":[67.6,5.3]
		},
		"PROMISGlobalHealth":{}
}


var raw_score_name = 'PROMISRawScore';
var tScore_name = 'PROMISTScore';
var se_name = 'PROMISStandardError';
var theta_name = 'PROMISTheta';

function getConversionScore(dsName, rawScore){
	cTable = conversion_table[dsName];
	if (typeof cTable == 'undefined'){
		return -1;
	}
	if(typeof cTable[rawScore] == 'undefined'){
		return -1;
	}
	return cTable[rawScore];
}


function set_tScore_SE(){
	dsName = $('#dsName').val();
	cTable = conversion_table[dsName];
	if(typeof cTable != 'undefined'){
		eFormShortName = $('#shortName').val();
		if(dsName == 'PROMISGlobalHealth'){
			// PROMISGlobalHealth for testing
			// PROMISGlobalHealth
			// this is only for this specific form
			pRaw = $(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+raw_score_name+"']").val();
			if (pRaw !== ""){
				pScore = getConversionScore("PhysicalHealthScoring", pRaw);
				if(pScore != -1){
					$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+tScore_name+"']").attr('value',pScore[0]);
					$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+se_name+"']").attr('value',pScore[1]);
					theta = (pScore[0]-50)/10;
					theta = Math.round(theta*10000000000)/10000000000;
					$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+theta_name+"']").attr('value', theta);
				}else{
					$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+tScore_name+"']").attr('value','-99');
					$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+se_name+"']").attr('value','-99');
					$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+theta_name+"']").attr('value','-99');
				}
			}else{
				$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+tScore_name+"']").attr('value','-99');
				$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+se_name+"']").attr('value','-99');
				$(".sectionName:contains('PROMIS Physical Health Scoring')").parents('tbody').eq(0).find("[dename='"+theta_name+"']").attr('value','-99');
			}
			// ---
			mRaw = $(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+raw_score_name+"']").val();
			if (mRaw !== ""){
				mScore = getConversionScore("MentalHealthScoring", mRaw);
				if(mScore != -1){
					$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+tScore_name+"']").attr('value',mScore[0]);
					$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+se_name+"']").attr('value',mScore[1]);
					theta = (mScore[0]-50)/10;
					theta = Math.round(theta*10000000000)/10000000000;
					$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+theta_name+"']").attr('value',theta);
				}else{
					$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+tScore_name+"']").attr('value', '-99');
					$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+se_name+"']").attr('value', '-99');
					$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+theta_name+"']").attr('value', '-99');
				}
			}else{
				$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+tScore_name+"']").attr('value', '-99');
				$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+se_name+"']").attr('value', '-99');
				$(".sectionName:contains('PROMIS Mental Health Scoring')").parents('tbody').eq(0).find("[dename='"+theta_name+"']").attr('value', '-99');
			}
			
		}else{
			rawScore = $("input[dename='"+raw_score_name+"']").val();
			cScore = getConversionScore(eFormShortName, rawScore);
			if (cScore != -1){
				$("input[dename='"+tScore_name+"']").attr('value',cScore[0]);
				$("input[dename='"+se_name+"']").attr('value',cScore[1]);
				theta = (cScore[0]-50)/10;
				theta = Math.round(theta*10000000000)/10000000000;
				$("input[dename='"+theta_name+"']").attr('value',theta);
			}else{
				$("input[dename='"+tScore_name+"']").attr('value','-99');
				$("input[dename='"+se_name+"']").attr('value','-99');
				$("input[dename='"+theta_name+"']").attr('value','-99');
			}
		}
	}
}

function lock_autoScore_fileds(){
	$("input[dename='"+tScore_name+"']").attr("readonly", true);
	$("input[dename='"+se_name+"']").attr("readonly", true);
	$("input[dename='"+theta_name+"']").attr("readonly", true);
}


function userEntry(inputbox){
	var v = $('#'+inputbox.id).val();
	if(v != ""){
		$('#inputFalg_'+inputbox.id).val('true')
	}
}