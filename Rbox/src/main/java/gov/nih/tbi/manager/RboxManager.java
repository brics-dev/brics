package gov.nih.tbi.manager;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.rcaller.rStuff.RCaller;
import com.github.rcaller.rStuff.RCode;

import gov.nih.tbi.RboxConstants;
import gov.nih.tbi.query.model.RboxResponse;

public class RboxManager {
	
	@Autowired
	private RboxConstants constants;
	
	public RboxManager(){
	}
	
	public RboxResponse executeRScript(String script, String data){
		
		System.out.println("TEST SCRIPT: " + script);
		System.out.println("TEST DATA: " + data);
		
		
		RCaller caller = new RCaller();
		caller.setRscriptExecutable(constants.getRExecutablePath());
		
		RboxResponse response = new RboxResponse();
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		
		if(!secureScript(script)){
			String errorMessage = "You do not have authorization to install R packages or save plots remotely.";
			
			response.setConsoleOutput(errorMessage);
			
			return response;
		}
		
		RCode code = new RCode();  
		code.clear();
		
		caller.cleanRCode();
		caller.setRCode(code);
		
		try{
		
			File plotFile = code.startPlot();
			


//			library("rjson")
//			json_file <- "http://api.worldbank.org/country?per_page=10&region=OED&lendingtype=LNX&format=json"
//			json_data <- fromJSON(paste(readLines(json_file), collapse=""))
			
			//import data here
			code.addRCode("data.str='" + data + "'");
			code.addRCode("df <- read.csv(text=data.str, header=TRUE, sep=',')");
			
			//import json data here
//			code.addRCode("library('rjson')");
//			System.out.println(data);
//			code.addRCode("data.str='" + data + "'");
//			code.addRCode("df <- fromJSON(data.str)");
			
			//add user's R script to code
			code.addRCode(script);
			
			code.endPlot();
	    	
			caller.redirectROutputToStream(ostream);
			long RCodeRunStart = System.currentTimeMillis();
			caller.runOnly();
			long RCodeRunStop = System.currentTimeMillis();
			
			long RCodeRunTime = RCodeRunStop - RCodeRunStart;
			System.out.println("R code execution time (s): " + RCodeRunTime/1000);
			
			Image image = code.getPlot(plotFile).getImage();
			String encodedImage = encodeImage(image);
			
			response.setGraphImage(encodedImage);
		
		}
		catch(Exception e){
			
			e.printStackTrace();
			
		}
		finally{
			
			try{
				String consoleOut = ostream.toString();
				ostream.flush();
				
				response.setConsoleOutput(consoleOut);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return response;
	}
	
	private String encodeImage(Image image){
		
		BufferedImage bimage = getBufferedImage(image);
		
		String imageString = null;  
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
  
        try {  
            ImageIO.write(bimage, "png", bos);  
            byte[] imageBytes = bos.toByteArray();  
  
            imageString = Base64.encodeBase64String(imageBytes);
  
            bos.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(RboxConstants.IMAGE_ENCODE_PREFIX);
        sb.append(imageString);
        imageString = sb.toString();
        
        return imageString;
	}
	
	private BufferedImage getBufferedImage(Image in){
		
        int w = in.getWidth(null);
        int h = in.getHeight(null);
        int type = BufferedImage.TYPE_INT_RGB;
        
        BufferedImage out = new BufferedImage(w, h, type);
        Graphics2D g2 = out.createGraphics();
        g2.drawImage(in, 0, 0, null);
        g2.dispose();
        
        return out;
    }
	
	private boolean secureScript(String script){
		
		boolean secure = true;
		
		if(script.contains(RboxConstants.INSTALL_COMMAND) || script.contains(RboxConstants.SAVE_COMMAND)){
			secure = false;
		}
		
		return secure;
		
	}

}
