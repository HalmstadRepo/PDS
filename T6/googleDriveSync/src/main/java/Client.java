import java.io.File;

public class Client {
    private final DriveService driveService;
    private final FileService fileService;

    private String pathFolder;

    public Client(DriveService driveService, FileService fileService) {
        this.driveService = driveService;
        this.fileService = fileService;
        pathFolder = "testFolder/";
    }

    public void run(){
        File directory = new File(pathFolder);

        // get files in local directory
        File[] files = fileService.gatherFilesInDirectory(directory);

        // backup new files or files that differs from backup
        driveService.backup(files);
    }
}
