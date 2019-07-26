package gov.nih.nichd.ctdb.common;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Apr 27, 2011
 * Time: 9:41:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectClass extends CtdbLookup {

    /**
	 * 
	 */
	private static final long serialVersionUID = 287337292558667227L;

	public ObjectClass() {
          // default constructor
      }

      /**
       * Overloaded constructor to set all lookup values
       *
       * @param id        The lookup ID
       * @param shortName The lookup short name
       */
      public ObjectClass(int id, String shortName) {
          this.setId(id);
          this.shortName = shortName;
      }

      /**
       * Overloaded constructor to set all lookup values
       *
       * @param id The lookup ID
       */
      public ObjectClass(int id) {
          this.setId(id);
      }

      /**
       * Overloaded constructor to set all lookup values
       *
       * @param id        The lookup ID
       * @param shortName The lookup short name
       * @param longName  The lookup long name
       */
      public ObjectClass(int id, String shortName, String longName) {
          this.setId(id);
          this.shortName = shortName;
          this.longName = longName;
      }

    
}
