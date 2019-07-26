package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.commons.model.hibernate.Address;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.StudySite;

import org.apache.cxf.common.util.StringUtils;

import com.google.gson.Gson;

public class StudySiteIdtListDecorator extends IdtDecorator {

	public String getSiteName() {
		StudySite site = (StudySite) this.getObject();

		String siteName = site.getSiteName();
		if (site.isPrimary()) {
			siteName += " (Primary)";
		}
		return siteName;
	}

	public String getAddressLine() {
		StudySite site = (StudySite) this.getObject();
		Address address = site.getAddress();

		String output = "";
		if (address != null) {
			output = address.getAddress1();

			if (!StringUtils.isEmpty(address.getAddress2())) {
				output += ", " + address.getAddress2();
			}
		}

		return output;
	}

	public String getState() {
		StudySite site = (StudySite) this.getObject();
		if (site.getAddress() != null && site.getAddress().getState() != null) {
			return site.getAddress().getState().getCode();
		}

		return "";
	}

	public String getCountry() {
		StudySite site = (StudySite) this.getObject();
		if (site.getAddress() != null && site.getAddress().getCountry() != null) {
			return site.getAddress().getCountry().getName();
		}

		return "";
	}

	public String getRemoveLink() {

		StudySite site = (StudySite) this.getObject();
		Gson gson = new Gson();
		String jsonStr = gson.toJson(site);

		String output = "<a href='javascript:;' onclick='removeStudySite(" + jsonStr + ")'>Remove</a>";
		return output;
	}

}
