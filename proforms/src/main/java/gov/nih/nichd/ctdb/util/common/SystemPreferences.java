package gov.nih.nichd.ctdb.util.common;

import java.io.Serializable;

public class SystemPreferences implements Serializable {
	private static final long serialVersionUID = -1617069116072870871L;

	public SystemPreferences() {}
	
	public boolean isPiiAllowed() {
		return SysPropUtil.getProperty("guid_with_non_pii").equals("0");
	}
	
	public String getFooterUrl() {
		return "/common/c-" + SysPropUtil.getProperty("template.global.appName") + "/footer.jsp";
	}
	
	public String getCommonImageBaseUrl() {
		return SysPropUtil.getProperty("app.webroot") + "/common/c-" + 
				SysPropUtil.getProperty("template.global.appName") + "/images";
	}
	
	public String get(String property) {
		return SysPropUtil.getProperty(property);
	}
}