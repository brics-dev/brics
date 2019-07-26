package gov.nih.nichd.ctdb.question.domain;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by Booz Allen Hamilton
 * Date: Aug 24, 2004
 *
 */
public class ImageMapQuestion extends Question implements Cloneable {
	private static final long serialVersionUID = 3786669662897251279L;
	
	private String imageFileName;
    private int gridResolution = 1;
    private String gridFileName;
    private String height;
    private String width;
    private boolean showGrid = false;
    private List<ImageMapOption> options;
    private int imageMapId = Integer.MIN_VALUE;
    private QuestionImage mapHolder; //specific for imagemap
    
    public ImageMapQuestion() {
    	super();
    	this.formQuestionAttributes.setInstanceType(InstanceType.IMAGE_MAP_QUESTION);
    	this.type = QuestionType.IMAGE_MAP;
    }

	public QuestionImage getMapHolder() {
		return mapHolder;
	}

	public void setMapHolder(QuestionImage imageHolder) {
		this.mapHolder = imageHolder;
	}

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public int getGridResolution() {
        return gridResolution;
    }

    public void setGridResolution(int gridResolution) {
        this.gridResolution = gridResolution;
    }

    public String getHeight() {
        return height;
    }
    public double getHeightInt () {
        if (this.height.endsWith("px")) {
            return Double.parseDouble(height.substring(0, height.length() -2));
        } else {
            return Double.parseDouble(height);
        }
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }
    public double getWidthInt () {
        if (this.width.endsWith("px")) {
            return Double.parseDouble (width.substring(0, width.length() -2));
        } else {
            return Double.parseDouble(width);
        }
    }

    public void setWidth(String width) {
        this.width = width;
    }
    
    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public List<ImageMapOption> getOptions() {
        return options;
    }

    public void setOptions(List<ImageMapOption> options) {
        this.options = options;
    }

    public String getGridFileName() {
        if (this.gridFileName == null ||
                this.gridFileName.trim().equals(""))
        { // need to get the image file name
            this.gridFileName = this.determineGridFileName();
        }
        return gridFileName;
    }

    public void setGridFileName(String gridFileName) {
        this.gridFileName = gridFileName;
    }

    public int getImageMapId() {
        return imageMapId;
    }

    public void setImageMapId(int imageMapId) {
        this.imageMapId = imageMapId;
    }

    public InstanceType getInstanceType () {
        return InstanceType.IMAGE_MAP_QUESTION;
    }


    public String getImageMapHtml (int sectionId) {
        try {
            int resolution = this.getResolution (this.gridResolution);
            double cellWidth =this.getWidthInt()  / resolution;
            double cellHeight = this.getHeightInt() / resolution;

            String map = "<map name='theMap_S_"+sectionId+"_Q_"+this.getId()+"' id='theMap_S_"+sectionId+"_Q_"+this.getId()+"'>";

            for (Iterator<ImageMapOption> options = this.options.iterator(); options.hasNext(); ){
                ImageMapOption imo = options.next();

                for (Iterator<String> rows = imo.getCoordinates().keySet().iterator(); rows.hasNext(); ) {
                    String row = rows.next();

                    for (Iterator<String> cols = imo.getCoordinates().get(row).iterator(); cols.hasNext(); ){
                        String col = cols.next();
                        // must cast all image map coords to int for mac to display
                        int width1 = (int) ((Double.parseDouble(col)-1)*cellWidth);
                        int height1 = (int) ((Double.parseDouble(row)-1) * cellHeight);
                        int width2 = (int) (Double.parseDouble(col)*cellWidth);
                        int height2 = (int) (Double.parseDouble(row)*cellHeight);

                        map += "<area SHAPE=RECT style='border:2px solid black;' COORDS='"+ width1 + "," + height1 +
                                " " + width2 + "," + height2 + "' ";
                        map += "href=\"Javascript: enterCoordinates ('S_"+sectionId+"_Q_"+ this.getId() +"', "+ row +", "+ col +", '"+imo.getOption().trim() +"');onBlurElem(document.getElementById('imageMap_S_"+sectionId+"_Q_"+this.getId()+"'))\">";
                    }
                }

            }
            map += "</map>";
            return map;
        } catch (Exception e) {
            // the object must not be complete
            return "<map name='image map creation failure'>";
        }
    }

    private int getResolution (int resolution) {
        int result = 20;
        switch (resolution) {
            case 1 : result = 5; break;
            case 2 : result =  8; break;
            case 3 : result =  10; break;
            case 4 : result =  15; break;
        }
        return result;
    }

    private String determineGridFileName() {
        boolean rec = (this.getHeightInt() / this.getWidthInt() < 0.57);
        if (rec && this.getHeightInt() < 115) {
            return getSmallRecName();
        } else if (rec && this.getHeightInt() >= 115) {
            return getLargeRecName();
        } else if (!rec && this.getHeightInt() < 115) {
            return getSmallName();
        } else {
            return getLargeName();
        }
    }

    private String getLargeName() {
        String file = "20x20_large.gif";
        switch (this.getGridResolution()){
            case 1 : file = "5x5_large.gif";
                break;
            case 2 : file = "8x8_large.gif";
                break;
            case 3 : file = "10x10_large.gif";
                break;
            case 4 : file = "15x15_large.gif";
                break;
        }
        return file;
    }

    private String getLargeRecName() {
        String file = "20x20_large.gif";
        switch (this.getGridResolution()){
            case 1 : file = "5x5Rec_large.gif";
                break;
            case 2 : file = "8x8Rec_large.gif";
                break;
            case 3 : file = "10x10Rec_large.gif";
                break;
            case 4 : file = "15x15Rec_large.gif";
                break;
        }
        return file;
    }

    private String getSmallRecName() {
        String file = "20x20_small.gif";
        switch (this.getGridResolution()){
            case 1 : file = "5x5Rec_small.gif";
                break;
            case 2 : file = "8x8Rec_small.gif";
                break;
            case 3 : file = "10x10Rec_small.gif";
                break;
            case 4 : file = "15x15Rec_small.gif";
                break;
        }
        return file;
    }
    private String getSmallName() {
        String file = "20x20_small.gif";
        switch (this.getGridResolution()){
            case 1 : file = "5x5_small.gif";
                break;
            case 2 : file = "8x8_small.gif";
                break;
            case 3 : file = "10x10_small.gif";
                break;
            case 4 : file = "15x15_small.gif";
                break;
        }
        return file;
    }


    public void clone (Question q) {
        try {
            this.setName(q.getName());
            this.setType( q.getType());
            this.setText(q.getText());
            this.setImages(q.getImages());
            this.setGroupsAssociatedWith(q.getGroupsAssociatedWith());
            this.setTextDisplayed(q.isTextDisplayed());
            this.setLatestVersion(q.getLatestVersion());
            this.setDefaultValue(q.getDefaultValue());
            this.setAnswers(q.getAnswers());
            this.setVersion(q.getVersion());
            this.setShowGrid(((ImageMapQuestion)q).isShowGrid());
            this.setWidth(((ImageMapQuestion)q).getWidth());
            this.setHeight(((ImageMapQuestion)q).getHeight());
            this.setImageFileName(((ImageMapQuestion)q).getImageFileName());
            this.setImageMapId(((ImageMapQuestion)q).getImageMapId());
            this.setGridResolution(((ImageMapQuestion)q).getGridResolution());
            this.setOptions(((ImageMapQuestion)q).getOptions());
        } catch (Exception e) {

        }

    }

    public Document toXML(int sectionId, String bgColor, int floorColSpanTD, int widthTD) throws TransformationException {
        Document document = super.newDocument();
        Element root = super.initXML(document, "question");

        root.setAttribute("instanceType", this.getInstanceType().getDispValue());
        root.setAttribute("required", Boolean.toString(this.getFormQuestionAttributes().isRequired()));
        root.setAttribute("type", this.getType().getDispValue());
        root.setAttribute("hasSkipRule", Boolean.toString(this.formQuestionAttributes.hasSkipRule()));
        root.setAttribute("displayText", Boolean.toString(this.isTextDisplayed()));
        if (this.formQuestionAttributes.hasSkipRule()) {
            root.setAttribute("skipOperator", this.formQuestionAttributes.getSkipRuleOperatorType().getDispValue());
            root.setAttribute("skipRule", this.formQuestionAttributes.getSkipRuleType().getDispValue());
            root.setAttribute("skipEquals", this.formQuestionAttributes.getSkipRuleEquals());
        }

        root.setAttribute("imageHeight", this.getHeight());
        root.setAttribute("imageWidth", this.getWidth());

        Element nameNode = document.createElement("name");
        nameNode.appendChild(document.createTextNode(this.getName()));
        root.appendChild(nameNode);
        //bg-color
        Element bgColorNode = document.createElement("bgColor");
        bgColorNode.appendChild(document.createTextNode(bgColor));
        root.appendChild(bgColorNode);
        //colSpan for question td
        root.setAttribute("floorColSpanTD", String.valueOf(floorColSpanTD));
        //width of question td
        root.setAttribute("widthTD", String.valueOf(widthTD));
        
        //descriptionUp
        Element descriptionUpNode = document.createElement("descriptionUp");
        String descriptionUpString = this.getDescriptionUp()==null?"":this.getDescriptionUp();
        descriptionUpNode.appendChild(document.createTextNode(descriptionUpString));
        root.appendChild(descriptionUpNode);
        if(!descriptionUpString.equals("")){
        	root.setAttribute("upDescription", "true");        	
        }else{
        	root.setAttribute("upDescription", "false");
        }
        
        //descriptionDown
        Element descriptionDownNode = document.createElement("descriptionDown");
        String descriptionDownString = this.getDescriptionDown()==null?"":this.getDescriptionDown();
        descriptionDownNode.appendChild(document.createTextNode(descriptionDownString ));
        root.appendChild(descriptionDownNode);
        if(!descriptionDownString.equals("")){
        	root.setAttribute("downDescription", "true");        	
        }else{
        	root.setAttribute("downDescription", "false");
        }
        //   

        Element textNode = document.createElement("text");
        textNode.appendChild(document.createTextNode(this.getText()));
        root.appendChild(textNode);

        Element mapHtmlNode = document.createElement("mapHtml");
        mapHtmlNode.appendChild(document.createTextNode(this.getImageMapHtml(sectionId)));
        root.appendChild(mapHtmlNode);
        root.setAttribute("imageMapFileName", this.getImageFileName());

        root.setAttribute("displayGrid", Boolean.toString(this.isShowGrid()));
        root.setAttribute("gridFileName", this.getGridFileName());
        Element answersNode = document.createElement("answers");
        
        for(Iterator<ImageMapOption> sectionIterator = this.options.iterator(); sectionIterator.hasNext();)
        {
            ImageMapOption opt = sectionIterator.next();
            Document answerDom = opt.toXML();
            answersNode.appendChild(document.importNode(answerDom.getDocumentElement(), true));
        }
        root.appendChild(answersNode);


        if (this.getDefaultValue() != null) {
            Element defaultValueNode = document.createElement("defaultValue");
            defaultValueNode.appendChild(document.createTextNode(this.getDefaultValue()));
            root.appendChild(defaultValueNode);
        } else {
            Element defaultValueNode = document.createElement("defaultValue");
            defaultValueNode.appendChild(document.createTextNode(""));
            root.appendChild(defaultValueNode);
        }
        
        Document qAttrsDom = this.getFormQuestionAttributes().toXML();
        root.appendChild(document.importNode(qAttrsDom.getDocumentElement(), true));

        if (this.getImages() != null && !this.getImages().isEmpty()) {
            Element imagesNode = document.createElement("images");
            Element fileNameNode = null;
            for (Iterator<String> imageIterator = this.getImages().iterator(); imageIterator.hasNext();) {
                String imageFileName = imageIterator.next();
                fileNameNode = document.createElement("filename");
                fileNameNode.appendChild(document.createTextNode(imageFileName));
                imagesNode.appendChild(fileNameNode);
            }
            root.appendChild(imagesNode);
        }

        if (this.getFormQuestionAttributes().getQuestionsToSkip() != null && !this.getFormQuestionAttributes().getQuestionsToSkip().isEmpty()) {
            Element questionToSkipNode = document.createElement("questionsToSkip");
            StringBuffer qIds = new StringBuffer(20);
            for (Iterator<Question> questionIterator = this.getFormQuestionAttributes().getQuestionsToSkip().iterator(); questionIterator.hasNext();) {
                Question skipQuestion = questionIterator.next();
                qIds.append("'S_"+Integer.toString(skipQuestion.getSectionId())+"_Q_" + Integer.toString(skipQuestion.getId()) + "'");
                if (questionIterator.hasNext()) {
                    qIds.append(",");
                }
            }
            questionToSkipNode.appendChild(document.createTextNode(qIds.toString()));
            root.appendChild(questionToSkipNode);
        }
        // added by Ching Hneg
        Element questionSectionNode = document.createElement("questionSectionNode"); 
        questionSectionNode.appendChild(document.createTextNode(String.valueOf(sectionId)));
        root.appendChild(questionSectionNode);
        return document;
    }


        /**
     * Compare if this Object is equal to Object o through member-wise comparison.
     *
     * @param o Object the object to compare with
     * @return True if they are equal, false otherwise
     */
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof ImageMapQuestion))
        {
            return false;
        }

        final ImageMapQuestion question = (ImageMapQuestion) o;

        if(!this.getName().equals(question.getName()))
        {
            return false;
        }
        if(!this.getText().equals(question.getText()))
        {
            return false;
        }
        if(!this.getType().equals(question.getType()))
        {
            return false;
        }
        if(!(this.getFormQuestionAttributes().isRequired()== question.getFormQuestionAttributes().isRequired()))
        {
            return false;
        }

        if( this.options.size() != question.options.size())
        {
            return false;
        }
        else
        {
            boolean isEqual = true;
            for(int i = 0; i < this.options.size(); i++)
            {
                ImageMapOption option1 = (ImageMapOption) this.options.get(i);
                ImageMapOption option2 = (ImageMapOption) question.options.get(i);
                if( !option1.equals(option2) )
                {
                    isEqual = false;
                    break;
                }
            }

            if( !isEqual )
            {
                return false;
            }
        }

        return true;
    }

}
