package gov.nih.tbi.dictionary.model.conversion;

import gov.nih.tbi.dictionary.model.NativeTypeConverter;

/**
 * Created by amakar on 9/30/2016.
 */
public class LongConverter implements NativeTypeConverter<Long> {

    public Long getNativeValue(String _strVal) {
        return Long.valueOf(_strVal);
    }
}
