package gov.nih.tbi.idt.ws;
/*
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.gson.JsonObject;
*/
public class CxfIdtInterface extends IdtInterface {
/* TODO Uncomment when CXF dependencies are introduced in ProFoRMS
	@Context
	private ServletContext sc;

	public CxfIdtInterface() throws InvalidColumnException {
		Message message = PhaseInterceptorChain.getCurrentMessage();
		HttpServletRequest httpRequest = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
		Map<String, String[]> parameterMap = httpRequest.getParameterMap();
		request.loadParams(translateParameters(parameterMap));
	}

	private JsonObject translateParameters(Map<String, String[]> parameterMap) {
		JsonObject output = new JsonObject();

		Iterator keyIterator = parameterMap.keySet().iterator();
		while (keyIterator.hasNext()) {
			String key = (String) keyIterator.next();
			String value = parameterMap.get(key)[0];
			if (request.isValidArrayTypeKey(key)) {
				addArrayTypeElement(output, key, value);
			}
			if (request.isValidObjectTypeKey(key)) {
				addObjectTypeElement(output, key, value);
			} else {
				addKeyValueElement(output, key, value);
			}
		}

		return output;
	}
	*/
}
