import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileService {

    // checksum
    // https://www.rgagnon.com/javadetails/java-0416.html
    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    // https://www.rgagnon.com/javadetails/java-0416.html
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] bytes = createChecksum(filename);
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    /**
     * Gather files in local directory
     *
     * @param directory to gather files from
     * @return array of files found in directory
     */
    public File[] gatherFilesInDirectory(File directory) {
        if (directory == null) {
            return null;
        }
        String[] filePaths = getFiles(directory.getAbsolutePath());
        if (filePaths == null) {
            return null;
        }
        Arrays.sort(filePaths);

        File[] files = new File[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            // create new file from filepath
            String f = filePaths[i];
            files[i] = new File(f);
        }
        return files;
    }

    // Gather files from directory
    private String[] getFiles(String directory) {
        try (Stream<Path> walk = Files.walk(Paths.get(directory))) {
            return walk
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .toArray(String[]::new);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
