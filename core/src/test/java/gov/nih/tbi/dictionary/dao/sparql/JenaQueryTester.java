
package gov.nih.tbi.dictionary.dao.sparql;

import gov.nih.tbi.VirtuosoStore;
import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteInsert;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

public class JenaQueryTester
{

    private ApplicationContext ctx;
    private VirtuosoStore virtuosoStore;

    @BeforeMethod
    protected void setUp()
    {

        ctx = new ClassPathXmlApplicationContext("test-context.xml");
        virtuosoStore = ctx.getBean("virtuosoStore", VirtuosoStore.class);
    }

    @Test
    public void testSyntaxBuilder()
    {
    }
    
    @Test
    public void testGraph()
    {
        ElementGroup eg = new ElementGroup();
        ElementTriplesBlock block = new ElementTriplesBlock();
        eg.addElement(block);
        block.addTriple(Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.TYPE_VARIABLE));
        
        UpdateDeleteInsert updateDeleteInsert = new UpdateDeleteInsert();
        updateDeleteInsert.setElement(eg);
        updateDeleteInsert.getInsertQuads().add(Quad.create(NodeFactory.createURI(RDFConstants.GRAPH_URI), Triple.create(RDFConstants.URI_NODE, RDF.type.asNode(), RDFConstants.TYPE_VARIABLE)));
        updateDeleteInsert.setHasInsertClause(true);
        System.out.println(updateDeleteInsert);
    }
}
