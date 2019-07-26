package gov.nih.brics.spring.integration.ws;

import java.io.File;

import org.apache.log4j.Logger;

public class ExternalCommand {

	static Logger log = Logger.getLogger(ExternalCommand.class);

	private String command;
	private String pathToFile;

	public PostData executeCommand(PostData payloadArguments) {
		String fileName = pathToFile + File.separator + payloadArguments.getNewPath() + File.separator
				+ payloadArguments.getOriginalFile();
		String newName = payloadArguments.getNewPath() + File.separator + payloadArguments.getNewFile();

		log.info("Post data: " + payloadArguments.toString());
		log.info("Creating a triplanar image from " + fileName + " to " + newName + ".");

		try {
			Process p = new ProcessBuilder(command, fileName, newName).start();
			log.info("Waiting for triplanar generation process to stop...");
			int i = p.waitFor();
			log.info("The result of " + command + " on [" + fileName + "] to [" + newName + "] is: " + i);
		} catch (Exception e) {
			log.error("Error occured while generating a triplanar image for " + fileName + " to " + newName + ".", e);
		}

		return payloadArguments;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public String getPathToFile() {
		return pathToFile;
	}

	public void setPathToFile(String pathToFile) {
		this.pathToFile = pathToFile;
	}
}
