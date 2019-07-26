package gov.nih.tbi.repository.xml;

import java.util.List;
import java.util.Map;

public interface XmlValidator {

	public Boolean validate(String value);

	public Map<String, Boolean> validate(List<String> values);
}
