package gov.nih.nichd.ctdb.attachments.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by CIT
 * User: engj
 * Date: November 21, 2012
 */
public class AttachmentIOUtil
{
	/**
	 * Streams the system file to the user's browser for download. This method will create an output stream from the
	 * response object, and an input stream from the system file. Then it will stream the file's contents to the
	 * user's browser up to 2KB at a time. If the File object references a file that no longer exists on the server's
	 * file system, that file will be ignored and no file streaming will occur.
	 * 
	 * @param sysFile - The system file to stream
	 * @param response - The response object to the user's browser
	 * @throws IOException When an error occurs while reading from the system file or streaming to the user's browser
	 * @throws SecurityException	If there was an access violation on the system file
	 */
	public void sendToBrowser(File sysFile, HttpServletResponse response) throws IOException, SecurityException
	{
		FileInputStream in = null;
		ServletOutputStream out = response.getOutputStream();
		byte[] buffer = new byte[2048];  // 2KB buffer
		int bytesRead = 0;
		
		// Check if the system file exists, and if so try to send the file to the browser
		if ( sysFile.exists() )
		{
			// Prepare response object for the out stream
			response.setHeader("Content-disposition", "attachment;filename=\"" + sysFile.getName() + "\"");
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Length", Long.toString(sysFile.length()));
			
			try
			{
				// Stream the system file to the user's browser
				in = new FileInputStream(sysFile);
				bytesRead = in.read(buffer);
				
				while ( bytesRead > 0 )
				{
					out.write(buffer, 0, bytesRead);
					out.flush();
					
					bytesRead = in.read(buffer);
				}
			}
			finally
			{
				out.close();
				
				if ( in != null )
				{
					in.close();
				}
			}
		}
		else
		{
			out.close();
		}
	}
	
	
	public void sendToBrowser(byte [] bArray, String filename, HttpServletResponse response) throws IOException
	{
		ServletOutputStream out = response.getOutputStream();
		
			// Prepare response object for the out stream
			response.setHeader("Content-disposition", "attachment;filename=\"" + filename + "\"");
			response.setContentType("application/download");
			response.setHeader("Content-Length", Long.toString(bArray.length));
			
			try
			{
				out.write(bArray);
				out.flush();
			}
			finally
			{
				out.close();
			}
	}
}
