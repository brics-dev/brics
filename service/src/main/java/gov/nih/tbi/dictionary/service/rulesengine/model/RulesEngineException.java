
package gov.nih.tbi.dictionary.service.rulesengine.model;

import java.lang.reflect.InvocationTargetException;

public class RulesEngineException extends Exception
{

    private static final long serialVersionUID = 1L;
    String exceptionMessage;

    public RulesEngineException(String exceptionMessage)
    {

        super(exceptionMessage);
        this.exceptionMessage = exceptionMessage;
    }

    public RulesEngineException(RulesEngineException exception)
    {

        super(exception);
        this.exceptionMessage = exception.getMessage();
    }
    //InvocationTargetException
    public RulesEngineException(InvocationTargetException exception)
    {

        super(exception);
        this.exceptionMessage = exception.getMessage();
    }
    public RulesEngineException(IllegalAccessException exception)
    {

        super(exception);
        this.exceptionMessage = exception.getMessage();
    }
    public RulesEngineException(IllegalArgumentException exception)
    {

        super(exception);
        this.exceptionMessage = exception.getMessage();
    }


    public String getExceptionMessage()
    {

        return exceptionMessage;
    }
}
