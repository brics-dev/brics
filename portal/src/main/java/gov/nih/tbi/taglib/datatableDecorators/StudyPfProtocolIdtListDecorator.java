package gov.nih.tbi.taglib.datatableDecorators;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cxf.common.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.tbi.commons.model.hibernate.Address;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.StudySite;

public class StudyPfProtocolIdtListDecorator extends IdtDecorator {

	public String getProtocolName() {
		String pfProtocolStr = (String) this.getObject();
		JsonParser jsonParser = new JsonParser();
		JsonObject pfProtocolJsonObj = jsonParser.parse(pfProtocolStr).getAsJsonObject();

		return pfProtocolJsonObj.get("protocolName").toString().replaceAll("^\"|\"$", "");
	}

	public String getProtocolNumber() {
		String pfProtocolStr = (String) this.getObject();
		JsonParser jsonParser = new JsonParser();
		JsonObject pfProtocolJsonObj = jsonParser.parse(pfProtocolStr).getAsJsonObject();

		return pfProtocolJsonObj.get("protocolNumber").toString().replaceAll("^\"|\"$", "");
	}
	
	public String getProtoESignature() {
		String pfProtocolStr = (String) this.getObject();
		JsonParser jsonParser = new JsonParser();
		JsonObject pfProtocolJsonObj = jsonParser.parse(pfProtocolStr).getAsJsonObject();

		return pfProtocolJsonObj.get("protoESignature").toString().replaceAll("^\"|\"$", "");
	}

	public String getClosedByFullName() {
		String pfProtocolStr = (String) this.getObject();
		JsonParser jsonParser = new JsonParser();
		JsonObject pfProtocolJsonObj = jsonParser.parse(pfProtocolStr).getAsJsonObject();

		return pfProtocolJsonObj.get("closingUserFullName").toString().replaceAll("^\"|\"$", "");
	}

	public String getClosingOutDate() {
		String pfProtocolStr = (String) this.getObject();
		JsonParser jsonParser = new JsonParser();
		JsonObject pfProtocolJsonObj = jsonParser.parse(pfProtocolStr).getAsJsonObject();

		return pfProtocolJsonObj.get("closingOutDate").toString().replaceAll("^\"|\"$", "");
	}
}

