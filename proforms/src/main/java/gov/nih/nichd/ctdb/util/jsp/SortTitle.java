package gov.nih.nichd.ctdb.util.jsp;

/**
 * SortTitle holds jsp utilites that assist in the display of table tilte columns
 *  that must indicate sort.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SortTitle
{


    /**
     * For a table column title that contains a sort indicator
     * <code>alignImage</code> will ensure the following placement of the indicator.
     * if the title is a multi-word title, the image will appear on the same line
     * as the last word of the tile, allowing line breaks between the words in the
     * title. If the title is a single word title, the image will appear on the same line.
     *
     * @param titleIn The column title
     * @param sortImage The image to append
     * @return  The new title with the image included correctly
     */
    public static String alignImage(String titleIn, String sortImage)
    {
        try
        {
            int startIndex = 0;
            int endIndex = titleIn.lastIndexOf("</a>");
            for(int i = endIndex; i > 0; i--)
            {
                if(titleIn.charAt(i) == '>')
                {  // found beginning of actual description
                    startIndex = i;
                    break;
                }
            }
            String titleText = titleIn.substring(startIndex + 1, endIndex);
            titleText.trim();
            String appendedText;
            int spaceIndex = titleText.lastIndexOf(' ');
            if(spaceIndex > 1)
            {
                appendedText = titleText.substring(0, spaceIndex) + " <div class=\"nowrapTitle\">" + titleText.substring(spaceIndex + 1, titleText.length()) + sortImage + "</div>";
                //return titleIn.substring(0, spaceIndex) + "<div nowrap>" + titleIn.substring(spaceIndex+1, titleIn.length()) + "</div>";
            }
            else
            {
                appendedText = "<div nowrap>" + titleText + sortImage + "</div>";
            }
            return titleIn.substring(0, startIndex + 1) + appendedText + titleIn.substring(endIndex, titleIn.length());
        }
        catch(Exception e)
        { // catch anything that goes wrong
            return titleIn + sortImage;
        }
    }
}
