package gov.nih.tbi.service.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class TriplanarQueue {
	private static final String INPROCESS = "In Process";
	private static final String READY = "READY";

	// key: triplanar file path, value: sftp location of the file
	private Map<String, String> triplanarMap;

	public TriplanarQueue() {
		triplanarMap = new ConcurrentHashMap<String, String>();
	}

	public void addReadyTriplanar(String triplanarFilePath) {
		triplanarMap.put(triplanarFilePath, READY);
	}

	public void addInProcessTriplanar(String triplanarFilePath) {
		triplanarMap.put(triplanarFilePath, INPROCESS);
	}

	public boolean isTriplanarExisting(String triplanarFilePath) {
		return triplanarMap.containsKey(triplanarFilePath);
	}

	public synchronized boolean isTriplanarInProcess(String triplanarFilePath) {
		return isTriplanarExisting(triplanarFilePath) && INPROCESS.equals(triplanarMap.get(triplanarFilePath));
	}

	public void emptyTriplanarQueue() {
		triplanarMap.clear();
	}

}
