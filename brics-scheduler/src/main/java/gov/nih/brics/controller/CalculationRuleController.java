package gov.nih.brics.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import gov.nih.brics.job.calculationRuleJob;
import gov.nih.tbi.dictionary.model.CalculationRule;

@Controller
@RequestMapping("calculationRule")
@Scope("request")
public class CalculationRuleController {
	
	private static Logger logger = Logger.getLogger(CalculationRuleController.class);
	
	@Autowired
	private calculationRuleJob calculationRuleJob;
	
	@RequestMapping(method = RequestMethod.GET)
	public String getCreateForm(Model model) {
		return "calculationRule/welcome";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@Scope("request")
	public void create(HttpServletResponse  response) throws IOException {

		if (logger.isInfoEnabled() == true) {
			logger.info("triggering calculationRule job");
		}
		
		response.setHeader("Content-Disposition", "attachment;filename=allEformCalculationRules.csv");
		response.setHeader("Content-Type", "text/csv");
 
		ArrayList<String> rows = new ArrayList<String>();
		rows.add("Eform Name,Section Name, Question Name, Calculation");
		rows.add("\n");
			
		List<CalculationRule> rules = calculationRuleJob.getCalculationRules();
 
		for (int i = 0; i < rules.size(); i++) {
			
			String row = "";
			row += "\""+rules.get(i).getEform_Name()+"\",";
			row +="\""+rules.get(i).getSection_Name()+"\",";
			row += "\""+rules.get(i).getQuestion_Name()+"\",";
			row +="\""+rules.get(i).getCalculationRule()+"\"";
			rows.add(row);
			rows.add("\n");
		}
 
		Iterator<String> iter = rows.iterator();
		while (iter.hasNext()) {
			String outputString = (String) iter.next();
			response.getOutputStream().print(outputString);
		}
 
		response.getOutputStream().flush();	

	}

}
