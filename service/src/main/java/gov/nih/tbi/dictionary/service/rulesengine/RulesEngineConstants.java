
package gov.nih.tbi.dictionary.service.rulesengine;

public class RulesEngineConstants
{

    /**************************************************************************************
     * 
     * Rules Engine Error Messages
     * 
     *************************************************************************************/

    public final static String RULES_ENGINE_ERROR = "Rules Engine Error: ";
    public final static String INVALID_OPERATOR = RULES_ENGINE_ERROR + "Invalid Operator Use";
    public final static String INVALID_SEVERITY_LEVEL = RULES_ENGINE_ERROR + "Invalid Severity Level";
    public final static String INVALID_LIST_OBJECT_KEY = RULES_ENGINE_ERROR
            + " Invalid Key Value; Either Key Value Doesn't Exist or was Misspelled.";
    public final static String INVALID_SUBFIELD_RULES = RULES_ENGINE_ERROR
            + "SubFields or Rules are not allowed with Fields containing Lists";
    public final static String RULES_ENGINE_OPERATION_ERROR = "Operation Error: ";
    public final static String ERROR_EVALUATOR_DNE = RULES_ENGINE_ERROR
            + " Comparison cannot be performed, Evaluator Does not exist";
    public final static String RULES_ENGINE_EVALUATE_ERROR = RULES_ENGINE_ERROR
            + "Field cannot be evaluated. Field Name: ";
    public final static String RULES_ENGINE_COMPARE_ERROR = RULES_ENGINE_ERROR
            + "Compare cannot be completed; Unknown Class Type. Class: ";

    public final static String REPEATABLE_GROUP_THRESHOLD_FAILURE = RULES_ENGINE_ERROR
            + "Failed XML Load For Repeatable Group Rules.";

    public final static String DE_OPTIONALITY_FAILURE = RULES_ENGINE_ERROR
            + "Failed XML Load For Data Element Optionality Rules.";
    public static final String RULES_ENGINE_CHANGES = "RULES_ENGINE_CHANGES";
    public static final String RULES_ENGINE_OPERAND_SEPARATOR="--";

    public String formatFieldName(String name)
    {

        String newFieldName = name;
        CharSequence cs = ".";
        if (name.contains(cs))
        {
            newFieldName = name.substring(0, name.indexOf('.'));
        }
        int capLetters = detectCapitalLettersInFieldName(newFieldName);
        while (capLetters != -1)
        {
            newFieldName = addSpacesInFieldName(newFieldName, capLetters);
            capLetters = detectCapitalLettersInFieldName(newFieldName);
        }
        return newFieldName;

    }

    public int detectCapitalLettersInFieldName(String name)
    {

        for (int i = name.length() - 1; i >= 0; i--)
        {
            // This is to look for camelCase letters
            if (i != 0 && Character.isUpperCase(name.charAt(i)) && (name.charAt(i - 1) != ' '))
            {
                return i;
            }
        }
        return -1;
    }

    public String addSpacesInFieldName(String name, int spaceLocal)
    {

        StringBuilder newFieldName = new StringBuilder();
        for (int i = name.length() - 1; i >= 0; i--)
        {
            if (i == spaceLocal)
            {
                newFieldName.append(name.subSequence(0, (i)));
                newFieldName.append(" ");
                newFieldName.append(name.subSequence(i, name.length()));
            }
        }
        return newFieldName.toString();
    }

}
