package gov.nih.tbi.dictionary.model.restful;

/**
 * Created by amakar on 1/11/2017.
 */
public interface ListableEntity<T extends ListItem> {
    T getListItem();
}
