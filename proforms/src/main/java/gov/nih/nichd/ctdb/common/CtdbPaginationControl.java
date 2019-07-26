package gov.nih.nichd.ctdb.common;

/**
 * Created by Booz Allen Hamilton
 * Date: May 27, 2004
 */
public class CtdbPaginationControl {

    private int currPage;
    private int resultsPerPage;
    private int totalNumResults;
    private String typeOfResults;
    private String href;
    private String linkClass;

    public CtdbPaginationControl() {
        href = "Javascript: submitForm (replaceMe);";
    }


    public String getCurrentDisplay() {

        String result = this.totalNumResults + " " + typeOfResults;
        result += " found, displaying ";
        result += (currPage * resultsPerPage - resultsPerPage) + 1;
        int endResults = currPage * resultsPerPage;
        if (endResults > totalNumResults) {
            endResults = totalNumResults;
        }
        result += " to " + endResults;

        return result;
    }

    public String getFirstPrev() {

        String result = "[<a class='" + linkClass + "'  href=\"" + getHref(1) + "\" id='pageFirst' title='1'>First</a>";
        result += "/<a  class='" + linkClass + "'  href=\"" + getHref(currPage - 1) + "\" id='pageNext' title='" + (currPage - 1) + "'>Prev</a>]";
        if (currPage > 1) {
            return result;
        } else {
            return "[First/Prev]";
        }
    }

    public String getNextLast() {
        String result = "[<a  class='" + linkClass + "'  href=\"" + getHref(currPage + 1) + "\" id='pageNext' title='" + (currPage + 1) + "'>Next</a>";
        result += "/<a class='" + linkClass + "'  href=\"" + getHref(getNumPages()) + "\" id='pageLast' title='" + getNumPages() + "'>Last</a>]";
        if (currPage != getNumPages()) {
            return result;
        } else {
            return "[Next/Last]";
        }
    }

    public String getPages() {
        int numPages = getNumPages();
        String result = "";
        int i = 1;
        if (numPages > 4) {
            i = currPage;
        }
        int maxPages = currPage + 3;
        if (numPages < 4 || maxPages > numPages) {
            maxPages = numPages;
        }

        int pagesToPrint = maxPages - i + 1;
        if (pagesToPrint < 4 && numPages > 4) {
            i = currPage - 4;
        }
        if (i <= 0) {
            i = 1;
        }


        while (i <= maxPages) {
            if (i == currPage) {
                result += i + ", ";
            } else {
                result += "<a class='" + linkClass + "'  href=\"" + getHref(i) + "\" id='page" + i + "' title='" + i + "'>" + i + "</a>, ";
            }
            i++;
        }
        result = result.substring(0, result.length() - 2);
        return result;
    }


    private int getNumPages() {
        int num = totalNumResults / resultsPerPage;
        if (num * resultsPerPage < totalNumResults) {
            num++;
        }
        return num;
    }

    private String getHref(int num) {
        return href.replaceAll("replaceMe", Integer.toString(num));
    }


    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }

    public int getTotalNumResults() {
        return totalNumResults;
    }

    public void setTotalNumResults(int totalNumResults) {
        this.totalNumResults = totalNumResults;
    }

    public String getTypeOfResults() {
        return typeOfResults;
    }

    public void setTypeOfResults(String typeOfResults) {
        this.typeOfResults = typeOfResults;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getLinkClass() {
        return linkClass;
    }

    public void setLinkClass(String linkClass) {
        this.linkClass = linkClass;
    }
}
