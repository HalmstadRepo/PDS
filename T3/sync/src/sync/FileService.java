package sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

@Singleton
@Path("FileService")
public class FileService {
	private HashMap<Long, ClientData> clientMap;
	private Gson gson;
    
	public FileService() {
    	clientMap = new HashMap<Long, ClientData>();
    	gson = new Gson();
    }
	
	// Create user with id
	@POST
    @Path("/user/create/{clientId}")
    public Response createUser(@PathParam("clientId") long clientId) {
		if (!clientMap.containsKey(clientId)) {
			clientMap.put(clientId, new ClientData());
		}
		
		return Response
				.ok("User created!", MediaType.APPLICATION_JSON)
				.build();
    }	
	
	// Backup all given files for user with id
	@POST
    @Path("/files/backup/{clientId}/{files}")
    public Response createUser(@PathParam("clientId") long clientId,
    		@PathParam("files") String serializedData) {
		// If no user with id
		if (!clientMap.containsKey(clientId)) {
			return Response
					.serverError()
					.entity("User with id not found.")
					.build();
		}
		// Validate data
		if (serializedData == null || serializedData.isEmpty()) {
			return Response
					.serverError()
					.entity("Invalid files")
					.build();
		}
		
		// Convert received data to FileData[]
		String json = Encoder.fromBase64(serializedData);
		try {
			FileData[] files = gson.fromJson(json, FileData[].class);
			HashMap<String, FileData> clientFiles = clientMap.get(clientId).getFiles(); 

			// Add all files to client data
			if (files != null && files.length > 0) {
				for (FileData file : files) {
					clientFiles.put(file.getName(), file);
				}	
			}
		
			return Response
					.ok("Backup success", MediaType.APPLICATION_JSON)
					.build();
		}
		catch (Exception e) {
			return Response
					.serverError()
					.entity("Could not parse files: "+e)
					.build();
		}
    }	
	
	// Get files that are new/different than client data
	@GET
    @Path("/files/difference/{clientId}/{files}")
    public Response getDifference(@PathParam("clientId") long clientId,
    		@PathParam("files") String serializedData) {
		
		// If no user with id
		if (!clientMap.containsKey(clientId)) {
			return Response
					.serverError()
					.entity("User with id not found.")
					.build();
		}
		// Validate data
		if (serializedData == null || serializedData.isEmpty()) {
			return Response
					.serverError()
					.entity("Invalid files")
					.build();
		}
		
		// Convert received data to FileData[]
		String json = Encoder.fromBase64(serializedData);
		try {
			FileData[] files = gson.fromJson(json, FileData[].class);
			
			HashMap<String, FileData> clientFiles = clientMap.get(clientId).getFiles(); 
			List<FileData> fileDifferenceList = new ArrayList();
			
			if (files != null && files.length > 0) {
			
				// Check each received file
				for (FileData file : files) {
					String fileName = file.getName();
					if (!clientFiles.containsKey(fileName)) {
						// Add if no current record
						fileDifferenceList.add(file);
					}
					else {
						// If record exists, compare checksum
						FileData fileDataClient = clientFiles.get(fileName);
						if (!fileDataClient.getChecksum().equals(file.getChecksum())) {
							fileDifferenceList.add(file);
						}
					}
				}	
			}
		
			// Convert to JSON and base64 and return different files
			FileData[] filesInNeedOfBackup = new FileData[fileDifferenceList.size()];
			filesInNeedOfBackup = fileDifferenceList.toArray(filesInNeedOfBackup);
	        
			String jsonResponse = gson.toJson(filesInNeedOfBackup);
			String response = Encoder.toBase64(jsonResponse);
			
			return Response
					.ok(response, MediaType.APPLICATION_JSON)
					.build();
		}
		catch (Exception e) {
			return Response
					.serverError()
					.entity("Could not parse files: "+e)
					.build();
		}
    }
}
