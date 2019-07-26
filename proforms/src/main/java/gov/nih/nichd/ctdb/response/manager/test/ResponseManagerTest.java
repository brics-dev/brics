package gov.nih.nichd.ctdb.response.manager.test;


public class ResponseManagerTest {/*
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		  // rcarver - setup the jndi context and the datasource
        try {
            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");            
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");
           
            // Construct DataSource
            PoolingDataSource ds = new PoolingDataSource();
            ds.setDataSourceName("Proforms Datasource");
            ds.setServerName("localhost");
            ds.setPortNumber(5432);
            ds.setDatabaseName("proforms_data");
            ds.setUser("REPLACED");
            ds.setPassword("REPLACED");
            ds.setMaxConnections(10);
            new InitialContext().rebind("DataSource", ds);
            
            ic.bind("java:/comp/env/jdbc/nameofmyjdbcresource", ds);
        } catch (NamingException ex) {
        	ex.printStackTrace();
            //Logger.getLogger(ResponseManagerDao.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	*//**
	 * Junit test case to test the count of my collections list count
	 * @throws CtdbException
	 * @throws BusinessManagerAccessException
	 * @throws UnknownBusinessManagerException
	 * @throws ConnectionFactoryException
	 *//*
	@Test
	public void testGetAllPreviousCollDataEntrySummary() throws CtdbException, BusinessManagerAccessException, UnknownBusinessManagerException, ConnectionFactoryException {
		Connection con = null;
		try {
		    DataSource source = (DataSource)new InitialContext().lookup("DataSource");
		    con = source.getConnection();
		    int actual = ResponseManagerDao.getInstance(con).getAllPreviousCollDataEntrySummary(new AdministeredFormResultControl(), 1, 203, new HashMap()).size();
		    int expected = 1;
		    assertEquals(expected, actual);
		} catch(SQLException e) {
		} catch(NamingException e) {
		} finally {
		    if(con != null) {
		        try {con.close();}catch(SQLException e) {}
		    }
		}
	}
	
	*//**
	 * Junit test case to test whether allow multiple collections instances flag is check or not in the a particular form
	 * @throws NamingException
	 * @throws SQLException
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 *//*
	@Test
	public void testAllowMultipleCollectionInstancesRadio() throws NamingException, SQLException, ObjectNotFoundException, CtdbException {
		Connection con = null;
		DataSource source = (DataSource)new InitialContext().lookup("DataSource");
		con = source.getConnection();
		Form f = FormManagerDao.getInstance(con).getForm(384);
		boolean actual = f.isAllowMultipleCollectionInstances();
		boolean expected = true;
		assertEquals(actual,expected);
		
	  }
 
*/}

