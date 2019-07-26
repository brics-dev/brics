package gov.nih.brics.job;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.commons.service.EformManager;
import gov.nih.tbi.dictionary.model.CalculationRule;

@Component
@Scope("singleton")
public class calculationRuleJob {
	
	@Autowired
	EformManager eformManager;
	
	public List<CalculationRule> getCalculationRules(){
		
		return eformManager.getAllCalculationRules();
		
	}

}
