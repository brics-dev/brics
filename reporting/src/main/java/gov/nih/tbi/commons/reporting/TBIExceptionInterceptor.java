
package gov.nih.tbi.commons.reporting;

import org.apache.log4j.Logger;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.ExceptionHolder;
import com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor;

/**
 * This is the exception interceptor that automatically logs any exception it intercepts.
 * 
 * @author Francis Chen
 * @author Raymond Anthony Trombadore
 */
public class TBIExceptionInterceptor extends ExceptionMappingInterceptor
{

    private static final long serialVersionUID = -7676879742427045733L;

    static Logger logger = Logger.getLogger(TBIExceptionInterceptor.class);

    protected void publishException(ActionInvocation invocation, ExceptionHolder exceptionHolder)
    {

        try
        {
            // log exception here
            if (exceptionHolder.getExceptionStack().toString() != null)
            {
                logger.error(invocation.getAction().toString() + "\n  CAUSED  \n"
                        + exceptionHolder.getExceptionStack().toString() + " WITH MESSAGE "
                        + exceptionHolder.getException().getMessage());
            }
            else
            {
                logger.error(invocation.getAction().toString() + "\n  CAUSED AN EXCEPTION");
            }
        }
        catch (Exception e)
        {
            logger.error("Exception in interceptor", e);
        }

        super.publishException(invocation, exceptionHolder);
    }

}
