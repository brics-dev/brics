package gov.nih.tbi.dictionary.model.conversion;

import gov.nih.tbi.dictionary.model.NativeTypeConverter;

public class DoubleConverter implements NativeTypeConverter<Double> {

    public Double getNativeValue(String _strVal) {
        return Double.valueOf(_strVal);
    }

}
