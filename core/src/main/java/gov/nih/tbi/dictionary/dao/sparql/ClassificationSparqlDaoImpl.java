
package gov.nih.tbi.dictionary.dao.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDFS;

import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.dao.ClassificationSparqlDao;
import gov.nih.tbi.dictionary.model.hibernate.Classification;

@Repository
public class ClassificationSparqlDaoImpl extends GenericSparqlDaoImpl<Classification> implements
        ClassificationSparqlDao
{

    @Override
    public List<Classification> getAll()
    {

        Query query = QueryConstructionUtil.getClassificationQuery();
        ResultSet results = querySelect(query);
        List<Classification> classifications = new ArrayList<Classification>();

        while (results.hasNext())
        {
            QuerySolution row = results.next();
            Classification classification = parseClassificationRow(row);
            classifications.add(classification);
        }

        return classifications;
    }

    /**
     * @inheritDoc
     */
    public Map<String, Classification> getClassificationMap()
    {

        Query classificationQuery = QueryConstructionUtil.getClassificationQuery();
        ResultSet results = querySelect(classificationQuery);
        Map<String, Classification> classificationMap = new HashMap<String, Classification>();

        while (results.hasNext())
        {
            QuerySolution row = results.next();
            Classification classification = parseClassificationRow(row);
            classificationMap.put(classification.getUri(), classification);
        }

        return classificationMap;
    }

    @Override
    public Classification get(String uri)
    {

        Query query = QueryConstructionUtil.getClassificationQuery();
        ElementGroup body = (ElementGroup) query.getQueryPattern();
        ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

        if (block == null)
        {
            block = new ElementTriplesBlock();
            body.addElement(block);
        }

        block.addTriple(Triple.create(RDFConstants.CLASSIFICATION_VARIABLE, RDFS.Nodes.isDefinedBy,
                NodeFactory.createURI(uri)));

        ResultSet results = querySelect(query);

        if (results.hasNext()) // if classification with specified uri exists
        {
            QuerySolution row = results.next();
            return parseClassificationRow(row);
        }

        return null;
    }

    public boolean exists(Classification classification)
    {

        if (classification == null)
        {
            throw new NullPointerException("Why are you checking if a null classification exists?!");
        }

        if (classification.getName() == null)
        {
            throw new NullPointerException("Name for classification should never be null");
        }

        Query existsQuery = QueryFactory.make();
        existsQuery.setQueryAskType();

        ElementTriplesBlock block = new ElementTriplesBlock();
        ElementGroup body = new ElementGroup();
        body.addElement(block);
        existsQuery.setQueryPattern(body);

        block.addTriple(Triple.create(RDFConstants.CLASSIFICATION_VARIABLE, RDFS.label.asNode(),
                NodeFactory.createLiteral(classification.getName())));

        return virtuosoStore.queryAsk(existsQuery);
    }

    public void delete(Classification classification)
    {

        // TODO: write this later
    }

    @Override
    public Classification save(Classification classification)
    {

        if (exists(classification))
        {
            delete(classification);
        }

        UpdateDataInsert updateInsert = new UpdateDataInsert(
                QueryConstructionUtil.generateClassificationTriples(classification));
        UpdateRequest request = UpdateFactory.create();
        request.add(updateInsert);
        virtuosoStore.update(request);

        return classification;
    }

    private Classification parseClassificationRow(QuerySolution row)
    {

        String uri = rdfNodeToString(row.get(RDFConstants.CLASSIFICATION_VARIABLE.getName()));
        String name = rdfNodeToString(row.get(RDFConstants.NAME_VARIABLE.getName()));
        Boolean canCreate = RDFConstants.TRUE.equals(rdfNodeToString(row.get(RDFConstants.CLASSIFICATION_VARIABLE
                .getName()))) ? Boolean.TRUE : Boolean.FALSE;

        return new Classification(uri, name, true, canCreate);
    }
}
