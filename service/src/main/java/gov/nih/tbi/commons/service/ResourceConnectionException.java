package gov.nih.tbi.commons.service;

/**
 * Unchecked proxy for typically checked API-level exceptions related to external
 * dependency/resource connection, such as UnsupportedEncodingException, MalformedURLException,
 * URISyntaxException or even DB (SQL) exceptions. This (or similar proxy unchecked exceptions) should be
 * used instead of propagating the lower level checked exceptions to calling methods.
 *
 * Created by amakar on 9/29/2016.
 */

public class ResourceConnectionException extends RuntimeException {

    public ResourceConnectionException(String msg) {
        super(msg);
    }
}
