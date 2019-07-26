package gov.nih.tbi.taglib.datatableDecorators;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.common.util.DictionaryUtils;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

public class ListEformDataStructureDecorator extends Decorator {

	BasicEform basicEform;

	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);

		if (obj instanceof BasicEform) {
			basicEform = (BasicEform) obj;
		}

		return feedback;
	}

	public String getTitleViewOnlyLink() {
		String title = stringToEscape(basicEform.getTitle());
		String output = "<a href=\"/portal/dictionary/eformDetailedViewAction!viewFormDetail.ajax?eformId=" + basicEform.getId() + "\" target=\"_blank\">";
		output += stringToEscape(title);
		output += "</a>";
		return output;
	}

	public String getDescriptionEllipsis() {
		return basicEform.getDescription();
	}

	public String getModifiedDate() {
		SimpleDateFormat df = new SimpleDateFormat(ModelConstants.ISO_DATE_FORMAT);
			
        return df.format(DictionaryUtils.getMostCurrentDate(basicEform.getAllDates()));
	}
	
	/*
	 * escape HTML for display
	 */
	private String stringToEscape(String title){
		title = escapeHtml(title);
		return title;
	}
}
