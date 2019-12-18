package sync;

/*
 * Data container for file details
 */
public class FileData {
	private String name;
	private String checksum;
	
	public FileData(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}


	public String getChecksum() {
		return checksum;
	}


	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}


	@Override
	public String toString() {
		return getName();
	}
}
