package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;

public class MetaStudyDataIdtListDecorator extends IdtDecorator {

	public String getDataNameLink() {
		
		MetaStudyData msd = (MetaStudyData) this.getObject();
				
		String output;
		if (msd.getSavedQuery() != null) {
			output = "<a class=tdLink href=\"javascript:viewSavedQuery(\'" + msd.getSavedQuery().getId() + "\')\">" + msd.getSavedQuery().getName() + "</a>";
		} else {
			if (msd.getId() != null && (msd.getUserFile() != null && msd.getUserFile().getId() != null)) {

				String link = "metaStudyExportAction!download.action?metaStudyDataId=" + msd.getId() + "&metaStudyId=" + msd.getMetaStudy().getId();
				output = "<a class=tdLink href=\"" + link + "\">" + msd.getName() + "</a>";
			} else {
				output = msd.getName();
			}
		}
		
		return output;
	}
	
}
