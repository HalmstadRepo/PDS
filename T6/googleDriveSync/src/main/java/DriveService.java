import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriveService {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private Drive drive;
    private NetHttpTransport HTTP_TRANSPORT;

    public DriveService() {
        try {
            // Init service
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Credential credentials = getCredentials();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Backup local files to drive
     *
     * @param files local files to sync
     */
    public void backup(java.io.File[] files) {
        // sync every file
        for (java.io.File file : files) {
            System.out.println(String.format("Syncing file: %s", file.getName()));
            // get file from drive
            File fileDrive = getFileByName(file.getName());

            boolean needBackup = false;

            // if no backup exists
            if (fileDrive == null) {
                needBackup = true;
            } else try {
                // check if checksum differs
                String checksumLocal = FileService.getMD5Checksum(file.getAbsolutePath());
                String checksumDrive = fileDrive.getMd5Checksum();
                if (!checksumDrive.equals(checksumLocal)) {
                    needBackup = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // backup if changes
            if (needBackup) {
                System.out.println(String.format("File required sync: %s", file.getName()));
                // create a new file
                File fileBackup = createFile(file);

                // delete old file if backup success and had a prev version of file
                if (fileBackup != null && fileDrive != null) {
                    deleteFile(fileDrive.getId());
                }
            }
        }
    }

    /**
     * Check if drive contains file with id
     *
     * @param id to check for
     * @return true if found otherwise false
     */
    public boolean containsId(String id) {
        return getAllFiles().stream().anyMatch(t -> t.getId().equals(id));
    }

    /**
     * Create drive file from local file
     *
     * @param file create drive version of
     * @return created drive file
     */
    public File createFile(java.io.File file) {
        try {
            // get mime type and create file metadata
            String mimeType = getMIMEType(file);
            FileContent mediaContent = new FileContent(mimeType, file);
            File fileMetadata = new File();
            fileMetadata.setName(file.getName());

            // request to create file
            File fileDrive = drive.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            return fileDrive;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Permanently delete a file, skipping the trash.
     *
     * @param fileId ID of the file to delete.
     */
    public void deleteFile(String fileId) {
        try {
            drive.files().delete(fileId).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
    }

    /**
     * Get all files from drive
     *
     * @return list of all files
     */
    public List<File> getAllFiles() {
        List<File> list = new ArrayList<>();
        String pageToken = null;
        try {
            // iterate each page in drive
            do {
                FileList result = drive.files().list()
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, md5Checksum)")
                        .setPageToken(pageToken)
                        .execute();

                // add all files to result
                list.addAll(result.getFiles());

                pageToken = result.getNextPageToken();
            } while (pageToken != null);

        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
        return list;
    }

    /**
     * Get credentials used in drive api
     *
     * @return credentials if no errors, otherwise null
     */
    public Credential getCredentials() {

        int port = 8888;
        // get local credentials
        java.io.File initialFile = new java.io.File("credentials.json");
        InputStream in = null;
        GoogleClientSecrets clientSecrets = null;
        FileDataStoreFactory factory = null;
        GoogleAuthorizationCodeFlow flow = null;

        try {
            in = new FileInputStream(initialFile);

            // Load client secrets.
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            var fileTokens = new java.io.File(TOKENS_DIRECTORY_PATH);
            factory = new FileDataStoreFactory(fileTokens);

            // Build flow and trigger user authorization request.
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(factory)
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(port).build();
            Credential c = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            return c;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get drive file by name
     *
     * @param name of file
     * @return drive file with name, null if none found
     */
    public File getFileByName(String name) {
        List<File> files = getAllFiles();
        var v = files.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst();
        return v.orElse(null);

    }

    /**
     * Get MIME type of a given file
     *
     * @param file to get MIME for
     * @return MIME type
     */
    private String getMIMEType(java.io.File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        return URLConnection.guessContentTypeFromStream(is);
    }

}
