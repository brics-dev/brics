package gov.nih.nichd.ctdb.question.domain;
public class VisualScaleExportImport {
	 	private int rangeStart;
	    private int rangeEnd;
	    private int width;
	    private String rightText;
	    private String leftText;
	    private String centerText;
	    private boolean showHandle;
	    private int version;
	    
	    public int getRangeStart() {
			return rangeStart;
		}
		public void setRangeStart(int rangeStart) {
			this.rangeStart = rangeStart;
		}
		public int getRangeEnd() {
			return rangeEnd;
		}
		public void setRangeEnd(int rangeEnd) {
			this.rangeEnd = rangeEnd;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public String getRightText() {
			return rightText;
		}
		public void setRightText(String rightText) {
			this.rightText = rightText;
		}
		public String getLeftText() {
			return leftText;
		}
		public void setLeftText(String leftText) {
			this.leftText = leftText;
		}
		public String getCenterText() {
			return centerText;
		}
		public void setCenterText(String centerText) {
			this.centerText = centerText;
		}
		public boolean isShowHandle() {
			return showHandle;
		}
		public void setShowHandle(boolean showHandle) {
			this.showHandle = showHandle;
		}
		public int getVersion() {
			return version;
		}
		public void setVersion(int version) {
			this.version = version;
		}
		

}
