package sync;

import java.util.HashMap;

/*
 * Data container for client files
 */
public class ClientData {
	private HashMap<String, FileData> files;
	
	public ClientData() {
		files = new HashMap<String, FileData>();
	}

	public HashMap<String, FileData> getFiles() {
		return files;
	}
}
