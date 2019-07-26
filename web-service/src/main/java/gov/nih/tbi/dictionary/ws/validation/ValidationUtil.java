
package gov.nih.tbi.dictionary.ws.validation;

import gov.nih.tbi.ModelConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class ValidationUtil
{

    public static boolean isRowRef(String token)
    {

        return token.startsWith(ValidationConstants.VALUE_ROW_REFERENCE);
    }

    public static boolean isColRef(String token)
    {

        return token.startsWith(ValidationConstants.VALUE_COLUMN_REFERENCE);
    }

    public static boolean isOperator(String token)
    {

        boolean isOperator = false;
        for (int i = 0; i < ValidationConstants.CONSTRAINT_OPERATORS.length; i++)
        {
            String operator = ValidationConstants.CONSTRAINT_OPERATORS[i];
            if (token.equalsIgnoreCase(operator))
            {
                isOperator = true;
                break;
            }
        }
        return isOperator;
    }

    // might be able to move to class if only used in one
    public static boolean isNumberOperator(String token)
    {

        boolean isOperator = false;
        for (int i = 0; i < ValidationConstants.CONSTRAINT_NUMBER_OPERATORS.length; i++)
        {
            String operator = ValidationConstants.CONSTRAINT_NUMBER_OPERATORS[i];
            if (token.equalsIgnoreCase(operator))
            {
                isOperator = true;
                break;
            }
        }
        return isOperator;
    }

    public static boolean isRangeOperator(String token)
    {

        boolean isOperator = false;
        for (int i = 0; i < ValidationConstants.CONSTRAINT_RANGE_OPERATORS.length; i++)
        {
            String operator = ValidationConstants.CONSTRAINT_RANGE_OPERATORS[i];
            if (token.equalsIgnoreCase(operator))
            {
                isOperator = true;
                break;
            }
        }
        return isOperator;
    }

    // When rebuilding ranges the ; must be put back between items
    public static Vector<String> tokenizeRange(String range)
    {

        Vector<String> tokenList = new Vector<String>();
        if (range != null && !range.isEmpty())
        {
            for (String s : range.split(ValidationConstants.VALUE_DELIMITER))
            {
                tokenList.add(s.trim());
            }
        }
        return tokenList;
    }

    // escape character \
    public static Vector<String> tokenizeConstraint(String constraint)
    {

        boolean operator = false;
        // int spaces = 0;
        Vector<String> tokenList = new Vector<String>();
        if (constraint != null)
        {
            int pos = 0;
            char c, c2;
            String s;
            StringBuffer sb;
            while (pos < constraint.length())
            {
                c = readChar(constraint, pos);
                if (c == ' ')
                {
                    pos++;
                    // spaces++;
                }
                else
                    if (c == ';')
                    {
                        s = String.valueOf(c);
                        pos++;
                        operator = true;
                        // spaces = 0;
                        tokenList.add(s);
                    }
                    else
                        if (c == '(' || c == ')' || c == '=' || c == '~')
                        { // || c == '[' || c == ']'
                            s = String.valueOf(c);
                            pos++;
                            operator = true;
                            // spaces = 0;
                            tokenList.add(s);
                        }
                        else
                            if (c == '<' || c == '>')
                            {
                                sb = new StringBuffer();
                                s = String.valueOf(c);
                                sb.append(s);
                                pos++;
                                if (pos < constraint.length())
                                {
                                    c = readChar(constraint, pos);
                                    if (c == '=')
                                    {
                                        s = String.valueOf(c);
                                        sb.append(s);
                                        pos++;
                                    }
                                }
                                operator = true;
                                // spaces = 0;
                                tokenList.add(sb.toString());
                            }
                            else
                                if (c == '!')
                                {
                                    sb = new StringBuffer();
                                    s = String.valueOf(c);
                                    sb.append(s);
                                    pos++;
                                    if (pos < constraint.length())
                                    {
                                        c = readChar(constraint, pos);
                                        if (c == '=' || c == '~')
                                        {
                                            s = String.valueOf(c);
                                            sb.append(s);
                                            pos++;
                                        }
                                    }
                                    operator = true;
                                    // spaces = 0;
                                    tokenList.add(sb.toString());
                                }
                                else
                                {
                                    sb = new StringBuffer();
                                    try
                                    {
                                        c2 = readChar(constraint, pos + 1);
                                        if ((c == '|' && c2 == '|') || (c == '&' && c2 == '&'))
                                        {
                                            s = String.valueOf(c) + String.valueOf(c2);
                                            sb.append(s);
                                            pos = pos + 2;
                                            operator = true;
                                            // spaces = 0;
                                            tokenList.add(sb.toString());
                                            continue; // Operator, move on to next token!
                                        }
                                        else
                                        {
                                            boolean escaped = false;
                                            while (pos < constraint.length() && (!isTokenStart(c, c2) || escaped))
                                            {
                                                if (c == '\\' && !escaped)
                                                {
                                                    escaped = true;
                                                }
                                                else
                                                {
                                                    escaped = false;
                                                    s = String.valueOf(c);
                                                    sb.append(s);
                                                }
                                                pos++;
                                                try
                                                {
                                                    if (pos < constraint.length())
                                                    {
                                                        c = readChar(constraint, pos);
                                                        c2 = readChar(constraint, pos + 1);
                                                    }
                                                }
                                                catch (IndexOutOfBoundsException e)
                                                {
                                                    c2 = ' '; // Guarantees that the last two in the while list eval to
                                                              // true
                                                }
                                            }
                                        }
                                    }
                                    catch (IndexOutOfBoundsException e)
                                    {
                                        s = String.valueOf(c);
                                        sb.append(s);
                                        pos++;
                                    }

                                    String token = sb.toString().trim();

                                    if (!operator && !tokenList.isEmpty())
                                    {
                                        String last = tokenList.lastElement();
                                        // for (int i =0; i < spaces; i++){
                                        last = last.concat(" ");
                                        // }
                                        token = last + token;
                                        tokenList.remove(tokenList.size() - 1);
                                    }

                                    operator = false;
                                    // spaces = 0;
                                    tokenList.add(token);
                                }

            }
        }

        return tokenList;
    }

    static private boolean isTokenStart(char c, char c2)
    {

        return (c == '<' || c == '>' || c == '=' || c == '!' || c == ' ' || c == '(' || c == ')' || c == '~'
                || c == ';' || // c == '[' || c == ']' ||
                (c == '|' && c2 == '|') || (c == '&' && c2 == '&'));
    }

    static private char readChar(String exp, int pos)
    {

        char c;
        c = exp.charAt(pos);
        return c;
    }

    public static boolean not(boolean eval)
    {

        return !(eval);
    }

    public static boolean equals(String refData, String value, String type)
    {

        if (type.equalsIgnoreCase("Integer"))
        {
            int i = Integer.parseInt(refData);
            int j = Integer.parseInt(value);
            return (i == j);
        }
        else
            if (type.equalsIgnoreCase("Float"))
            {
                float i = Float.parseFloat(refData);
                float j = Float.parseFloat(value);
                return (i == j);
            }
            else
                if (type.equalsIgnoreCase("Date"))
                {
                    Date i = parseDate(refData);
                    Date j = parseDate(value);
                    return (i.equals(j));
                }
                else
                {
                    return value.equalsIgnoreCase(refData);
                }
    }

    public static boolean greater(String refData, String value, String type)
    {

        if (type.equalsIgnoreCase("Integer"))
        {
            int i = Integer.parseInt(refData);
            int j = Integer.parseInt(value);
            return (i > j);
        }
        else
            if (type.equalsIgnoreCase("Float"))
            {
                float i = Float.parseFloat(refData);
                float j = Float.parseFloat(value);
                return (i > j);
            }
            else
                if (type.equalsIgnoreCase("Date"))
                {
                    Date i = parseDate(refData);
                    Date j = parseDate(value);
                    return (i.after(j));
                }
                else
                {
                    throw new UnsupportedOperationException(
                            "Greater then operation is only supported for numeric types.");
                }
    }

    public static boolean less(String refData, String value, String type)
    {

        if (type.equalsIgnoreCase("Integer"))
        {
            int i = Integer.parseInt(refData);
            int j = Integer.parseInt(value);
            return (i < j);
        }
        else
            if (type.equalsIgnoreCase("Float"))
            {
                float i = Float.parseFloat(refData);
                float j = Float.parseFloat(value);
                return (i < j);
            }
            else
                if (type.equalsIgnoreCase("Date"))
                {
                    Date i = parseDate(refData);
                    Date j = parseDate(value);
                    return (i.before(j));
                }
                else
                {
                    throw new UnsupportedOperationException("Less then operation is only supported for numeric types.");
                }
    }

    // Helper to to find data format in list that works
    public static Date parseDate(String dateString)
    {

        SimpleDateFormat dateForm;

        Date date = null;
        for (String dateFormat:ModelConstants.UNIVERSAL_DATE_FORMATS)
        {
            try
            {
                dateForm = new SimpleDateFormat(dateFormat);
                dateForm.setLenient(false);
                date = dateForm.parse(dateString);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                
                if(year < 1000)
                {
                    return null;
                }
                
                if (date != null)
                {
                    break;
                }
            }
            catch (Exception e)
            {
                //this is just a failing format
            }
        }

        return date;
    }

}
