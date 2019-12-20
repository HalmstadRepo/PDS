import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class FileServiceTest {
    private static String FILE_PATH_FOLDER = "src/test/resources/";

    @Test
    public void gatherFilesInDirectory() {
        FileService fileService = new FileService();
        Assert.assertNotNull(fileService);

        File folder = new File(FILE_PATH_FOLDER);
        Assert.assertNotNull(folder);

        var files = fileService.gatherFilesInDirectory(folder);
        Assert.assertNotEquals(0, files.length);

        String checksum;
        for (File file : files) {
            checksum = "";
            try {
                String s = file.getAbsolutePath();
                checksum = FileService.getMD5Checksum(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Assert.assertNotEquals("", checksum);
        }
    }
}
