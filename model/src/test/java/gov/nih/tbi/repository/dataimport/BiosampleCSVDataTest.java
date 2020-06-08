package gov.nih.tbi.repository.dataimport;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import gov.nih.tbi.ModelConstants;

public class BiosampleCSVDataTest {
	
	@Test
	public void calculateDaysSinceBaselineTest() {
		BiosampleCSVData data = new BiosampleCSVData();
		
		assertEquals(data.calculateDaysSinceBaseline("Baseline"), 0);
		
		assertEquals(data.calculateDaysSinceBaseline("12 months"), 365);
	}
	
	@Test
	public void calculateDaysSinceBaselineInvalidVisitTypeTest() {
		BiosampleCSVData data = new BiosampleCSVData();
		
		assertEquals(data.calculateDaysSinceBaseline("Test"), -1);
	}
	
	@Test
	public void updateGuidTest() {
		
		BiosampleCSVData data = new BiosampleCSVData();
		
		assertEquals(data.updateGuid("CSF-POOL-TEST"), "");
		assertEquals(data.updateGuid("PLASMA-POOL-TEST"), "");
		assertEquals(data.updateGuid("SERUM-POOL-TEST"), "");
		assertEquals(data.updateGuid("NIHAAAABBBB"), "NIHAAAABBBB");
		
		
	}

	@Test
	public void updateSteadyPDIIIVisitTypeTest() {
		BiosampleCSVData data = new BiosampleCSVData();
		
		assertEquals(data.updateSteadyPDIIIVisitType("Screening"), ModelConstants.VISIT_TYPE_BASELINE);
		assertEquals(data.updateSteadyPDIIIVisitType("RS1"), ModelConstants.VISIT_TYPE_BASELINE);
		assertEquals(data.updateSteadyPDIIIVisitType("RS2"), ModelConstants.VISIT_TYPE_BASELINE);
		assertEquals(data.updateSteadyPDIIIVisitType("V10"), ModelConstants.VISIT_TYPE_THIRTY_SIX_MONTHS);
	}
	
	@Test
	public void updateSteadyPDIIIVisitTypeTestInvalid() {
		BiosampleCSVData data = new BiosampleCSVData();
	
		assertEquals(data.updateSteadyPDIIIVisitType("SC1"), "");
	}
}
