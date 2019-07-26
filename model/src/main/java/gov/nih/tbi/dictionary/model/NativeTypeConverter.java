package gov.nih.tbi.dictionary.model;

/**
 * Used to get the native, comparable value from a String representation of a value
 * when String is the lowest commond denominator for storage or display of different types.
 * This is introduced for sorting data element permissible values, which are stored
 * in the DB as Strings but are linked to either alphanumeric (String) or numeric (Double)
 * data type. However, it can be used in other implementations as well.
 *
 * Created by amakar on 9/30/2016.
 */

public interface NativeTypeConverter<T extends Comparable> {

    T getNativeValue(String _strVal);
}
