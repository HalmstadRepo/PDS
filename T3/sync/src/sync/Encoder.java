package sync;

import java.nio.charset.Charset;

import org.apache.tomcat.util.codec.binary.Base64;

public class Encoder {
	// Convert to base64
	public static String toBase64(String text) {
		byte[] encoded = Base64.encodeBase64(text.getBytes());
		return new String(encoded);
	}
	
	// Convert from base64
	public static String fromBase64(String text) {
		byte[] decoded = Base64.decodeBase64(text.getBytes());
	    return new String(decoded, Charset.forName("UTF-8"));
	}
}
