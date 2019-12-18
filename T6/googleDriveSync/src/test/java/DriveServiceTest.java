
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.model.File;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class DriveServiceTest {
    private static String FILE_PATH_FOLDER = "src/test/resources/";
    private static String PHOTO_1 = "photo1.jpg";
    private static String PHOTO_2 = "photo2.jpg";

    @Test
    public void createAndDelete() {
        DriveService driveService = new DriveService();
        Assert.assertNotNull(driveService);

        java.io.File fileInput = new java.io.File(String.format("%s/%s", FILE_PATH_FOLDER, PHOTO_1));
        File file = driveService.createFile(fileInput);
        Assert.assertNotNull(file);

        boolean contains = driveService.containsId(file.getId());
        Assert.assertTrue(contains);

        driveService.deleteFile(file.getId());
        contains = driveService.containsId(file.getId());
        Assert.assertFalse(contains);
    }
   

    @Test
    public void credentials() {
        DriveService driveService = new DriveService();
        Assert.assertNotNull(driveService);

        Credential c = driveService.getCredentials();

        Assert.assertNotNull(c);
    }
}