package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;

import java.net.URLEncoder;

import org.apache.taglibs.display.Decorator;

public class MetaStudyDataListDecorator extends Decorator {

	@SuppressWarnings("deprecation")
	public String getSelectCheckbox() {
		
		MetaStudyData msd = (MetaStudyData) this.getObject();
		
		String msdName = URLEncoder.encode(msd.getName());
		Long msdId = msd.getId();
		String output = "<input  type=\"checkbox\" name=\"docCheckbox\" value=\"" + msdId + "_" + msdName + "\" />\n";
       	return output;
	}
	
	public String getDataNameLink() {
		
		MetaStudyData msd = (MetaStudyData) this.getObject();
				
		String output;
		if (msd.getSavedQuery() != null) {
			output = "<a href=\"javascript:viewSavedQuery(\'" + msd.getSavedQuery().getId() + "\')\">" + msd.getSavedQuery().getName() + "</a>";
		} else {
			if (msd.getId() != null && (msd.getUserFile() != null && msd.getUserFile().getId() != null)) {

				String link = "metaStudyExportAction!download.action?metaStudyDataId=" + msd.getId() + "&metaStudyId=" + msd.getMetaStudy().getId();
				output = "<a href=\"" + link + "\">" + msd.getName() + "</a>";
			} else {
				output = msd.getName();
			}
		}
		
		return output;
	}
	
	public String getDescriptionEllipsis() {
		MetaStudyData msd = (MetaStudyData) this.getObject();
		
		String desc = msd.getDescription();
		String descLine = "";
		if (desc.length() > PortalConstants.ELLIPSIS_CHARACTER_COUNT) {
			descLine = "<span class=\"descBeginning\">";
			descLine += desc.substring(0, PortalConstants.ELLIPSIS_CHARACTER_COUNT + 1);
			descLine += " <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">...</a></span>";
			descLine += "<span class=\"descAll\">";
			descLine += desc;
			descLine += " <a href=\"javascript:;\" class=\"ellipsisExpandCollapse\" onclick=\"ellipsisExpandCollapse(this)\">collapse</a></span>";
		}
		else {
			descLine = desc;
		}
		return descLine;
	}
	
}
