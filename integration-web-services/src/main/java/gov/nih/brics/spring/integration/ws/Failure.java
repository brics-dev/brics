package gov.nih.brics.spring.integration.ws;

import org.apache.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class Failure {

	static Logger log = Logger.getLogger(Failure.class);

	public String handleError(Message<?> s) {
		if (s instanceof Message<?>) {
			Message<?> m = (Message<?>) s;
			MessageHeaders mh = m.getHeaders();
			for (String key : mh.keySet()) {
				log.info("\tkey: " + mh + "value: " + mh.get(key).toString()+"\n");
			}
		}
		log.info("Handle Error (Message<?>): " + s.getPayload().toString());
		return "Error Occurred: " + s.getPayload().toString();
	}
}

