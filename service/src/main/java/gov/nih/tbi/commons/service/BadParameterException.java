package gov.nih.tbi.commons.service;

import gov.nih.tbi.commons.model.QueryParameter;

/**
 * Created by amakar on 8/30/2016.
 */
public class BadParameterException extends Exception {

    private QueryParameter param;
    private String valAsStr;

    public BadParameterException(QueryParameter _param, String _valStr, String _msg) {

        super(_msg);
        this.param = _param;
        this.valAsStr = _valStr;
    }

    public BadParameterException(QueryParameter _param, String _valStr) {

        this.param = _param;
        this.valAsStr = _valStr;
    }
}
