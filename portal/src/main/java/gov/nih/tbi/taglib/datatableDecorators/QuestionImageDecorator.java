package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

import javax.activation.MimetypesFileTypeMap;
import org.apache.taglibs.display.Decorator;
import org.apache.xml.security.utils.Base64;

public class QuestionImageDecorator extends Decorator {
	
	QuestionDocument questionDoc;

    public String getCheckboxDec(){
    	QuestionDocument quesDoc = (QuestionDocument) this.getObject();
        String currentName = quesDoc.getQuestionDocumentPk().getFileName();
    	
        return "<input type='checkbox' name='namesToDelete' value='" + currentName + "'  id='"+currentName+"' style='text-align:center;'/>";
    }

    public String getNumberDec() throws Exception  {
        int idx = this.getListIndex();
        QuestionDocument quesDoc = (QuestionDocument) this.getObject();
        String currentName = quesDoc.getQuestionDocumentPk().getFileName();
        String currentFile = getImageFileByQuestionDoc(quesDoc);

        StringBuffer html = new StringBuffer(800);

        html.append("<a href='" + currentFile + "' id='"+currentName+"' target='_blank' style='text-align:center;'>");
        html.append(idx + 1);
        html.append("</a>");

        return html.toString();
    }

    public String getThumbnailDec() throws Exception {
    	
    	QuestionDocument quesDoc = (QuestionDocument) this.getObject();
    	String currentName = quesDoc.getQuestionDocumentPk().getFileName();
    	String currentFile = getImageFileByQuestionDoc(quesDoc);

        StringBuffer html = new StringBuffer(800);
        html.append("<a href='" + currentFile + "' id='"+currentName+"' target='_blank' style='text-align:center;'>");
        html.append("<img class='qgraphics qFraphic_" +this.getListIndex() + "'" + " height='60' width='60' border='0' src='" + currentFile + "'/>");
        html.append("</a>");

        return html.toString();
    }
    
    private String getImageFileByQuestionDoc (QuestionDocument qd) throws Exception {
    	SftpClient client = SftpClientManager.getClient(qd.getUserFile().getDatafileEndpointInfo());
    	
		byte[] questionDocumentBytes = client.downloadBytes(qd.getUserFile().getName(), qd.getUserFile().getPath());
		String fileBytesEncoded = new String(Base64.encode(questionDocumentBytes));
		
		MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
		String mimeTypeOfFile = mimetypesFileTypeMap.getContentType(qd.getUserFile().getName());
		
		String imgFileStr = "data:" + mimeTypeOfFile + ";base64," + fileBytesEncoded;
		
		return imgFileStr;
    }

}
