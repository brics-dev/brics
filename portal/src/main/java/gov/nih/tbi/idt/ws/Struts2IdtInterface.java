package gov.nih.tbi.idt.ws;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.google.gson.JsonObject;

public class Struts2IdtInterface extends IdtInterface {
	public Struts2IdtInterface() throws InvalidColumnException {
		Map<String, String[]> parameterMap = ServletActionContext.getRequest().getParameterMap();
		request.loadParams(translateParameters(parameterMap));
	}
	
	private JsonObject translateParameters(Map<String, String[]> parameterMap) {
		JsonObject output = new JsonObject();
	
		Iterator keyIterator = parameterMap.keySet().iterator();
		while (keyIterator.hasNext()) {
			String key = (String) keyIterator.next();
			String value = parameterMap.get(key)[0].toString();
			if (request.isValidArrayTypeKey(key)) {
				addArrayTypeElement(output, key, value);
			}
			if (request.isValidObjectTypeKey(key)) {
				addObjectTypeElement(output, key, value);
			}
			else {
				addKeyValueElement(output, key, value);
			}
		}
		
		return output;
	}
	
	public String output() {
		HttpServletResponse response = ServletActionContext.getResponse();
		String data = "{}";
		try {
			data = toJson();
			response.setContentType("application/json");
		    response.getWriter().write(data);
		}
		catch (IOException e) {
			// don't return anything if we fail here
		}
		return data;
	}
}
