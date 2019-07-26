package gov.nih.tbi;

import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.query.ResultSet;

import junit.framework.TestCase;

public class VirtuosoTest extends TestCase
{
    @Autowired
    private VirtuosoStore virtuosoStore;
    
    public void testStore()
    {
        assertNotSame(virtuosoStore, null);
    }
    
    public void testQuery()
    {
        ResultSet rs = virtuosoStore.querySelect("Select * where { ?s ?p ?o } LIMIT 15", MetadataStore.REASONING);
        assertEquals(rs.hasNext(), true);
    }
}
