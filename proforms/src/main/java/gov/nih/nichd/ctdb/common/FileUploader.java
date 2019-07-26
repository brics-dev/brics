package gov.nih.nichd.ctdb.common;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;


/**
 * FileUploader handles all writes to the file system
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class FileUploader {
    /**
     * Uploads a file based on the given file object, path, and filename
     *
     * @param file The file object to write
     * @param path The path to write the file out to
     * @param name The filename used for writing
     * @return The name of the file written.
     * @throws FileUploadInvalidTypeException thrown if the filename is of invalid type.  User should be prompted to upload
     *                                        another file.
     * @throws FileUploadNotFoundException    thrown if the file uploaded is of size zero.  This can happen if user inputed a file
     *                                        name that does not exist on the user's machine.  User should be prompted to upload another file.
     * @throws FileUploadException            thrown if any other error occurs while writing the file out to the system.  This is a system error.
     */
    public static String upload(File file, String path, String name, List<String> fileTypes) throws FileUploadException {
        if (name == null) {
            return null;
        }

        if (name.length() == 0) {
            return null;
        }

        if (file == null || file.exists() ){
            FileUploadNotFoundException e = new FileUploadNotFoundException("Unable to upload file.  Please make sure the file exist. " + name);
            e.setFileName(name);
            throw e;
        }

        Iterator<String> iterator = fileTypes.iterator();
        if (iterator.hasNext()) {
            int lastIndex = name.lastIndexOf('.');
            if (lastIndex == -1) {
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }

            String fileType = name.substring(lastIndex + 1, name.length());
            if (fileType == null) {
                // name ends with a period.
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }

            boolean match = false;
            while (iterator.hasNext()) {
                String validType = iterator.next();
                if (fileType.equalsIgnoreCase(validType)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }
        }

        //  Write to disk
        String pathWithSeparator;
        if (path.charAt(path.length() - 1) != File.separatorChar) {
            pathWithSeparator = path + File.separator;
        } else {
            pathWithSeparator = path;
        }

        String nameFileWritten;
        try {
            // create folder if not exists
            String foldername = pathWithSeparator.substring(0, pathWithSeparator.length() - 1);
            File folder = new File(foldername);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    throw new FileUploadException("Unable to create the folder: " + foldername);
                }
            } else if (!folder.isDirectory()) {
                throw new FileUploadException("There is an existing file with the same name as the specified folder: " + foldername);
            }

            // write to file
            nameFileWritten = pathWithSeparator + name;
            FileInputStream stream = new FileInputStream(file);
            //InputStream stream = file.getInputStream();
            OutputStream out = new FileOutputStream(nameFileWritten);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            bytesRead = stream.read(buffer, 0, 8192);
            while (bytesRead != -1) {
                out.write(buffer, 0, bytesRead);
                bytesRead = stream.read(buffer, 0, 8192);
            }
            out.close();
        } catch (Exception e) {
            throw new FileUploadException("Error writing file " + e.getMessage(), e);
        }
        return nameFileWritten;
    }
    

    /**
     * Uploads a file based on the given file object, path, and filename
     *
     * @param file The file object to write
     * @param path The path to write the file out to
     * @param name The filename used for writing
     * @return The name of the file written.
     * @throws FileUploadInvalidTypeException thrown if the filename is of invalid type.  User should be prompted to upload
     *                                        another file.
     * @throws FileUploadNotFoundException    thrown if the file uploaded is of size zero.  This can happen if user inputed a file
     *                                        name that does not exist on the user's machine.  User should be prompted to upload another file.
     * @throws FileUploadException            thrown if any other error occurs while writing the file out to the system.  This is a system error.
     */
    public static String uploadFile(File file, String path, String name, List<String> fileTypes) 
    		throws FileUploadException, IOException {
        if (name == null || name.length() == 0) {
            return null;
        }

        if (file == null || file.length() == 0) {
            FileUploadNotFoundException e = new FileUploadNotFoundException(
            		"Unable to upload file.  Please make sure the file exist. " + name);
            e.setFileName(name);
            throw e;
        }

        if (fileTypes != null && !fileTypes.isEmpty()) {
            int lastIndex = name.lastIndexOf('.');
            if (lastIndex == -1) {
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }

            // name ends with a period.
            String fileType = name.substring(lastIndex + 1, name.length());
            if (fileType == null) {
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }

            boolean match = false;
            for (String validType : fileTypes) {
                if (fileType.equalsIgnoreCase(validType)) {
                    match = true;
                    break;
                }
            }
            
            if (!match) {
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }
        }

        //  Write to disk
        String pathWithSeparator;
        if (path.charAt(path.length() - 1) != File.separatorChar) {
            pathWithSeparator = path + File.separator;
        } else {
            pathWithSeparator = path;
        }

        String nameFileWritten;
        InputStream in = null;
        OutputStream out = null;
        
        try {
            // create folder if not exists
            String foldername = pathWithSeparator.substring(0, pathWithSeparator.length() - 1);
            File folder = new File(foldername);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    throw new FileUploadException("Unable to create the folder: " + foldername);
                }
            } else if (!folder.isDirectory()) {
                throw new FileUploadException("There is an existing file with the same name as the specified folder: " + foldername);
            }

            // write to file
            nameFileWritten = pathWithSeparator + name;
            in = new FileInputStream(file);
            out = new FileOutputStream(nameFileWritten);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            bytesRead = in.read(buffer, 0, 8192);
            while (bytesRead != -1) {
                out.write(buffer, 0, bytesRead);
                bytesRead = in.read(buffer, 0, 8192);
            }
            out.close();
        } catch (Exception e) {
            throw new FileUploadException("Error writing file " + e.getMessage(), e);
        } finally {
        	in.close();
        	out.close();
        }
        
        return nameFileWritten;
    }

    
    public static String uploadForExportImport(BufferedImage buffImage, String path, String name, List fileTypes) throws FileUploadException {
        if (name == null) {
            return null;
        }

        if (name.length() == 0) {
            return null;
        }
        
        
        String extension;
        if(name.endsWith("gif")){
        	extension = "gif";
        }else{
        	extension = "jpg";
        }



        Iterator<String> iterator = fileTypes.iterator();
        if (iterator.hasNext()) {
            int lastIndex = name.lastIndexOf('.');
            if (lastIndex == -1) {
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }

            String fileType = name.substring(lastIndex + 1, name.length());
            if (fileType == null) {
                // name ends with a period.
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }

            boolean match = false;
            while (iterator.hasNext()) {
                String validType = iterator.next();
                if (fileType.equalsIgnoreCase(validType)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                throw new FileUploadInvalidTypeException("Invalid file name. " + name);
            }
        }

        //  Write to disk

        String pathWithSeparator;
        if (path.charAt(path.length() - 1) != File.separatorChar) {
            pathWithSeparator = path + File.separator;
        } else {
            pathWithSeparator = path;
        }

        String nameFileWritten;
        try {
            // create folder if not exists
            String foldername = pathWithSeparator.substring(0, pathWithSeparator.length() - 1);
            File folder = new File(foldername);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    throw new FileUploadException("Unable to create the folder: " + foldername);
                }
            } else if (!folder.isDirectory()) {
                throw new FileUploadException("There is an existing file with the same name as the specified folder: " + foldername);
            }

            // write to file
            nameFileWritten = pathWithSeparator + name;
            nameFileWritten = pathWithSeparator + name;
            ImageIO.write(buffImage, extension ,new File(nameFileWritten));
        } catch (Exception e) {
            throw new FileUploadException("Error writing file " + e.getMessage(), e);
        }
        return nameFileWritten;
    }
    
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			os.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return out.toByteArray();
    }

}
