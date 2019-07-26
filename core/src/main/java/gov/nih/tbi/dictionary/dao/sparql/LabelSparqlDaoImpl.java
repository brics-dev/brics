
package gov.nih.tbi.dictionary.dao.sparql;

import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.dao.LabelSparqlDao;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;

import java.util.ArrayList;
import java.util.List;

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

@Repository
public class LabelSparqlDaoImpl extends GenericSparqlDaoImpl<Keyword> implements LabelSparqlDao
{

    /**
     * Returns a label object by the given name. This label object contains a name, uri, and count. Returns null if
     * label does not exist
     * 
     * @param name
     * @return
     */
    public Keyword getByName(String name)
    {

        Query query = QueryConstructionUtil.getLabelQuery();
        ElementGroup body = (ElementGroup) query.getQueryPattern();
        ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

        if (block == null)
        {
            block = new ElementTriplesBlock();
            body.addElement(block);
        }

        block.addTriple(Triple.create(RDFConstants.LABEL_VARIABLE, RDFS.label.asNode(), NodeFactory.createLiteral(name)));

        ResultSet results = querySelect(query);
        if (results.hasNext())
        {
            return parseRow(results.next());
        }

        return null;
    }

    /**
     * Returns a list of labels that contain the given string. Search is case insensitive.
     * 
     * @param searchKey
     * @return
     */
    public List<Keyword> search(String searchKey)
    {

        Query query = QueryConstructionUtil.getLabelQuery();
        ElementGroup body = (ElementGroup) query.getQueryPattern();
        body.addElementFilter(QueryConstructionUtil.regexFilter(RDFConstants.LABEL_VARIABLE, searchKey));

        ResultSet results = querySelect(query);

        List<Keyword> labels = new ArrayList<Keyword>();

        while (results.hasNext())
        {
            labels.add(parseRow(results.next()));
        }

        return labels;
    }

    public Keyword parseRow(QuerySolution row)
    {
        String uri = rdfNodeToString(row.get(RDFConstants.LABEL_VARIABLE.toString()));
        String labelName = rdfNodeToString(row.get(RDFConstants.VALUE_VARIABLE.toString()));
        String countString = rdfNodeToString(row.get(RDFConstants.COUNT_VARIABLE.toString()));
        if (labelName == null || countString == null)
        {
            throw new NullPointerException("label or label count is null.");
        }

        Long count = Long.valueOf(countString);

        Keyword label = new Keyword();
        label.setUri(uri);
        label.setKeyword(labelName);
        label.setCount(count);

        return label;
    }

    @Override
    public List<Keyword> getAll()
    {

        Query query = QueryConstructionUtil.getLabelQuery();
        ResultSet results = querySelect(query);

        List<Keyword> labels = new ArrayList<Keyword>();

        while (results.hasNext())
        {
            Keyword label = parseRow(results.next());
            labels.add(label);
        }

        return labels;
    }

    @Override
    public Keyword get(String uri)
    {

        Query query = QueryConstructionUtil.getLabelQuery();
        ElementGroup body = (ElementGroup) query.getQueryPattern();
        ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

        if (block == null)
        {
            block = new ElementTriplesBlock();
            body.addElement(block);
        }

        block.addTriple(Triple.create(RDFConstants.LABEL_VARIABLE, RDFS.isDefinedBy.asNode(), NodeFactory.createLiteral(uri)));

        ResultSet results = querySelect(query);
        if (results.hasNext())
        {
            return parseRow(results.next());
        }

        return null;
    }

    @Override
    public Keyword save(Keyword label)
    {

        if (exists(label))
        {
            delete(label);
        }

        UpdateDataInsert updateInsert = new UpdateDataInsert(QueryConstructionUtil.generateLabelTriples(label));
        UpdateRequest request = UpdateFactory.create();
        request.add(updateInsert);
        virtuosoStore.update(request);
        return label;
    }

    public boolean exists(Keyword label)
    {

        if (label == null)
        {
            throw new NullPointerException("Why are you checking if a null label exists?!");
        }

        if (label.getUri() == null) // no uri means that this is a new label, thus does not exist in db
        {
            return false;
        }

        Query existsQuery = QueryFactory.make();
        existsQuery.setQueryAskType();

        ElementTriplesBlock block = new ElementTriplesBlock();
        ElementGroup body = new ElementGroup();
        body.addElement(block);
        existsQuery.setQueryPattern(body);

        block.addTriple(Triple.create(RDFConstants.LABEL_VARIABLE, RDFS.isDefinedBy.asNode(), NodeFactory.createURI(label.getUri())));

        return virtuosoStore.queryAsk(existsQuery);
    }

    private void delete(Keyword label)
    {

        String uri = label.getUri();

        String sparqlUpdate = "WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + uri + "> ?p ?o } WHERE { <" + uri + "> ?p ?o }";

        update(sparqlUpdate);
    }

}
