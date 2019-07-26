package gov.nih.nichd.ctdb.common;

import java.util.List;

/**
 * Base class for nichd ctdb domain and form assemblers.
 * Both static methods must be overridden by a subclass in order to perform valid assembly.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public abstract class CtdbAssembler {
    /**
     * Transforms a CtdbForm Object into a CtdbDomainObject object
     *
     * @param form The CtdbForm Object to transform into a CtdbDomainObject object
     * @return CtdbDomainObject
     * @throws AssemblerException Thrown if any error occurs while transforming the CtdbForm object into a CtdbDomainObject
     */
    public static CtdbDomainObject formToDomain(CtdbForm form) throws AssemblerException {
        throw new AssemblerException("This method must be implemented by a subclass for valid assembly.");
    }

    /**
     * Transforms a CtdbForm Object into a CtdbDomainObject object with validation of the input data
     *
     * @param form   The CtdbForm Object to transform into a CtdbDomainObject object
     * @param errors Holder for the errors during validation
     * @return CtdbDomainObject
     * @throws AssemblerException Thrown if any error occurs while transforming the CtdbForm object into a CtdbDomainObject
     */
    public static CtdbDomainObject validateFormToDomain(List<String> errors, CtdbForm form) throws AssemblerException {
        throw new AssemblerException("This method must be implemented by a subclass for valid assembly.");
    }

    /**
     * Transforms a CtdbDomainObject Object into a CtdbForm object
     *
     * @param domain The CtdbDomainObject to transform into a CtdbForm object
     * @param form   The CtdbForm object to use for transformation. This value is passed to this argument to retain
     *               values set outside the CTDB application by Struts.
     * @throws AssemblerException Thrown if any error occurs while transforming the CtdbDomainObject to a CtdbForm object
     */
    public static void domainToForm(CtdbDomainObject domain, CtdbForm form) throws AssemblerException {
        throw new AssemblerException("This method must be implemented by a subclass for valid assembly.");
    }
}
