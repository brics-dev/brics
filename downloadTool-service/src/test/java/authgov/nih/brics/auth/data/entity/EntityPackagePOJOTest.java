package authgov.nih.brics.auth.data.entity;

import org.testng.annotations.Test;

import gov.nih.brics.auth.util.GetterSetterVerifier;

/**
 * Test all getters and setters in this package.
 * 
 * @author Ryan Powell
 */
public class EntityPackagePOJOTest {
	@Test
	public void testAllGettersAndSettersInThisPackage() throws Exception {
		GetterSetterVerifier.testGettersAndSettersInPackage(this.getClass().getPackage().getName(), this.getClass());
	}
}
