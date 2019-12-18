package sync;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import com.google.gson.Gson;

public class FileClient {
	public static void main(String[] args) {
        new FileClient().run();
    }

	private WebTarget webTarget;
	private String pathFolder;
	private int clientId;
	private Gson gson;
	
	public FileClient() {
		gson = new Gson();
		ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
		URI uri = UriBuilder.fromUri("http://localhost:8080/sync/rest/FileService").build();
		webTarget = client.target(uri);
		
		pathFolder = "testFolder/";
		clientId = 0;
	}
	
	private void run() {
		// Create user on server
		//createUser(clientId);
		
		// Gather files that need backup
		String[] files = gatherFilesInFolder(pathFolder); 
		
		FileData[] filesInNeedOfBackup = getFilesInNeedOfBackup(pathFolder,files);
		System.out.println("Files in need of backup:");
		System.out.println(Arrays.toString(filesInNeedOfBackup));
		
		
		// Backup
		backup(filesInNeedOfBackup);
		System.out.println("\nFile backup done!\n");
		
		// Comparison
		filesInNeedOfBackup = getFilesInNeedOfBackup(pathFolder, files);
		System.out.println("Files in need of backup:");
		System.out.println(Arrays.toString(filesInNeedOfBackup));
		
	}

	/*
	 * Backup files to server
	 * @param files to backup
	 */
	private void backup(FileData[] files) {
		// Convert to JSON and base64
		String json = gson.toJson(files);
		String encoded = Encoder.toBase64(json);
		
		// POST files to server
		Response response = webTarget
				.path("files")
				.path("backup")
        		.path(String.format("%o", clientId))
        		.path(encoded)
        		.request()
        		.post(Entity.entity(encoded, MediaType.APPLICATION_JSON));
		
		if (response.getStatusInfo().toEnum() != Status.OK) {
			System.out.println("Could not backup files!");
		}
	}

	/*
	 * Get files that are new/different to the backup server
	 * @param files to check
	 * @return files that differ
	 */
	private FileData[] getFilesInNeedOfBackup(String folder, String[] files){
		List<FileData> fileDatas = new ArrayList<FileData>();
		// Create FileData with name and checksum
		for (String file : files) {
			FileData fileData = new FileData(file);
			try {
				String path = folder + file;
				String checksum = FileUtility.getMD5Checksum(path);
				fileData.setChecksum(checksum);
				fileDatas.add(fileData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Convert to JSON and base64
		String json = gson.toJson(fileDatas);
		String encoded = Encoder.toBase64(json);
		
		// Get file dif from server
		Response response = webTarget
				.path("files")
				.path("difference")
        		.path(String.format("%o", clientId))
        		.path(encoded)
        		.request()
        		.get();
	
		if (response.getStatusInfo().toEnum() == Status.OK) {
	        // Parse response from server
			String responseString = response.readEntity(String.class);
			String decoded = Encoder.fromBase64(responseString);
			FileData[] retrievedFiles = gson.fromJson(decoded, FileData[].class);
	        
			return retrievedFiles;
		}
		else {
			System.out.println("Could not retrieve file difference!");
			return null;
		}
	}
	
	// Gather files from directory
	private String[] gatherFilesInFolder(String directory) {
		File fileDirectory = new File(pathFolder);
		String[] files = fileDirectory.list();
	
		return files;
	}

	// Create user with id on server
	private void createUser(long id) {
		Response response = webTarget
        		.path("user")
        		.path("create")
        		.path(String.format("%o", id))
        		.request()
        		.post(Entity.entity(id, MediaType.APPLICATION_JSON));
	}
}
