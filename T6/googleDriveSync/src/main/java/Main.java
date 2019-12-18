public class Main {
    public static void main(String[] args){
        new Main().run();
    }

    private void run() {
        DriveService driveService = new DriveService();
        FileService fileService = new FileService();

        Client c = new Client(driveService, fileService);
        c.run();
    }
}
