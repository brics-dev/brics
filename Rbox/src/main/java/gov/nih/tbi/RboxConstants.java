package gov.nih.tbi;

import org.springframework.beans.factory.annotation.Value;

public class RboxConstants {
	
	public static final String DEFAULT_R_EXECUTABLE = "C:/Program Files/R/R-3.2.4revised/bin/Rscript.exe";
	public static final String R_EXECUTABLE = "/usr/bin/Rscript";
	public static final String RBOX_SERVICE_URL = "rbox/v1/script/";
	public static final String LOCAL_HOST = "http://localhost:8082/";
	public static final String IMAGE_ENCODE_PREFIX = "data:image/png;base64,";
	public static final String INSTALL_COMMAND = "install.packages";
	public static final String SAVE_COMMAND = "ggsave";
	
	@Value("#{applicationProperties['rbox.exec.path']}")
	private String rExecutablePath;
	
	public String getRExecutablePath() {

		if (rExecutablePath == null || rExecutablePath.isEmpty()) {
			return DEFAULT_R_EXECUTABLE;
		} else {
			return rExecutablePath;

		}
	}

}
