package Core;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static Logger instance;

    // Log current time and message
    public void log(String s) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date) + ": " + s);
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
}