package myduppic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Stream;

public class App {
    protected List<Path> readAllPicsToList(String rootPath) {
        try {
            final Stream<Path> paths = Files.walk(Paths.get(rootPath));
            final List<Path> lstPaths = paths.toList();
            paths.close();
            return lstPaths;
        } 
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean createFolderIfNecessary(final String rootPath, final String folderName) {        
        final java.io.File folder = new java.io.File(rootPath + "/" + folderName);

        if(!folder.exists()) {
            if(folder.mkdir()) {
                return true;
            }
            else {
                throw new RuntimeException("could not create folder: " + folderName);
            }
        }
        else {
            return false;
        }
    }

    protected void copyFile(final java.io.File sourceFile, final String destinationFileName, final String destinationRootPath, final String destinationFolderName) {
        // final java.io.File destinationFolder = new java.io.File(destinationRootPath + "/" + destinationFolderName);
        final java.io.File destinationFile = new java.io.File(destinationRootPath + "/" + destinationFolderName + "/" + destinationFileName);

        try(java.io.FileInputStream fis = new java.io.FileInputStream(sourceFile);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int length;

            while((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 00, length);
            }
        }
        catch(final IOException e) {
            e.printStackTrace();
            throw new RuntimeException("could not copy file: " + destinationFileName);
        }
    }

    protected String getFullName(final Path pic) {
        String fullName = ""+pic.toAbsolutePath();        
        fullName = fullName.substring(10, fullName.length());
        fullName = fullName.replace("\\", "_");

        return fullName;
    }

    protected String getHash(final java.io.File file, final MessageDigest digest) {
        String hash = "";

        try(java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            while((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        catch(java.io.IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
       
	    hash = sb.toString();
        return hash;
    }

    protected void processPics(final List<Path> allPics) {
        System.out.println("...number of pics: " + allPics.size());
        final String destinationRootPath ="/mysortedpics";
        final MessageDigest digest;
        
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch(final NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }

        for(final Path pic : allPics) {
            final java.io.File file = pic.toFile();            
            
            if(file.isFile()) {
                final String fullName = this.getFullName(pic);
                final String hash = this.getHash(file, digest);
                System.out.println("checking: " + fullName);
                
                if(this.createFolderIfNecessary(destinationRootPath, hash)) {
                    System.out.println("...new pic");
                }
                else {
                    System.out.println("...duplicate");
                }

                this.copyFile(pic.toFile(), fullName, destinationRootPath, hash);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("start dup pic");
        final String rootPath ="/mypics";
        final App app = new App();
        final List<Path> allPics = app.readAllPicsToList(rootPath);
        app.processPics(allPics);
        System.out.println("end dup pic");
    }
}