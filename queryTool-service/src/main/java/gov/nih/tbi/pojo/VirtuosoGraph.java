package gov.nih.tbi.pojo;

import java.io.Serializable;

import javax.sql.DataSource;

import virtuoso.jena.driver.VirtGraph;


/**
 * Extension of {@link virtuoso.jena.driver.VirtGraph} in order to actually implement the String, DataSource
 * constructor.
 * 
 * @author Bill Puschmann
 * 
 */
public class VirtuosoGraph extends VirtGraph implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2520829561876968948L;

	/**
     * As of 2014-06-16, the {@link VirtGraph#VirtGraph(String, DataSource)} does not honor the specified graph name.
     * Regardless of the String provided, the resulting object is bound to the VIRT:DEFAULT graph.
     * 
     * This class was written to provide access to the protected graphName object.
     * 
     * @param graph
     * @param ds
     */
    public VirtuosoGraph(String graph, DataSource ds)
    {

        super(graph, ds);
        graphName = graph;

    }

    /**
     * Once a constructor is created in an extended class, like this one
     * {@link VirtuosoGraph#VirtuosoGraph(String, DataSource)}, then all constructors must be declared
     * 
     * This constructor is called in our code, and so much be included here.
     * 
     * @param db_graph
     * @param db_url
     * @param db_user
     * @param db_pass
     */
    public VirtuosoGraph(String db_graph, String db_url, String db_user, String db_pass)
    {

        super(db_graph, db_url, db_user, db_pass);
    }

    /**
     * The original graphName is protected
     */
    public String getGraphName()
    {

        return graphName;
    }
}
