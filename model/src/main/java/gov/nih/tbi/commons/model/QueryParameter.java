package gov.nih.tbi.commons.model;

/**
 * Created by amakar on 8/30/2016.
 */
public enum QueryParameter {

    DATASET_PREFIXED_ID("prefixedID");

    private String name;

    QueryParameter(String _name) {
        this.name = _name;
    }

    public String getName() {
        return this.name;
    }
}
