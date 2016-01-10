package Helpers;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutReqHttpParser {
	final static String CRLF = "\r\n";
	private BufferedReader reader;
	private String body , version, statusCode, description;
	private Hashtable<String, String> headers;
	private final String[] acceptedImageFormat = { "bmp", "gif", "png", "jpg" };
	private final String[] acceptedTextFormat = { "txt", "html"};
	private final String[] acceptedIconFormat = { "ico"};
	private long timeRecivedResponse;
	
	public OutReqHttpParser(InputStream stream) {
		reader = new BufferedReader(new InputStreamReader(stream));
		headers = new Hashtable<String, String>();
		body = "";

	}

	/**
	 * Parses the first line of the request. including the url, method and
	 * version. returns the status code of the request.
	 * @throws Exception 
	 */
	public void parseResponse() throws Exception {

		parseFirstLine();
		parseHeaders();
		if (headers != null) {
			parseBody();
		}


	}

	private void parseBody() throws IOException {
		StringBuilder bodyBuilder = new StringBuilder();
		String line;

		line = reader.readLine();

		// Read the entire body.
		while ( line != null) {
			bodyBuilder.append(line);
			line = reader.readLine(); 
		}

		body = bodyBuilder.toString();

		// Printing body to console.
		System.out.println("Recived body:");
		System.out.println(body);
	}

	/**
	 * Parses the first line of the requeset. including the Url, method and
	 * version.
	 * @throws Exception 
	 */
	private void parseFirstLine() throws Exception {
		String line;
		line = reader.readLine();
		timeRecivedResponse = System.currentTimeMillis();
		// Printing first line to console.
		System.out.println("Recived header:");
		System.out.println(line);

		if (line == null || line.length() == 0) {
			throw new Exception("No header");
		}


		Pattern firstLinePattern = Pattern.compile("(.+? )([0-9]+ )(.*)");
		Matcher equationMatcher = firstLinePattern.matcher(line);

		if (equationMatcher.find()) {

			version = equationMatcher.group(1);
			statusCode = equationMatcher.group(2);
			description = equationMatcher.group(3);
		}
	}


	/**
	 * Parse the headers of the http request.
	 * 
	 * 
	 */
	private void parseHeaders() throws IOException {
		String line;
		int index;

		line = reader.readLine();

		while (!line.equals("")) {

			// Priniting header to console
			System.out.println(line);

			index = line.indexOf(':');
			if (index < 0) {
				break;
			} else {
				headers.put(line.substring(0, index).toLowerCase().trim(), line.substring(index + 1).trim());
			}
			line = reader.readLine();
		}

	}

	/**
	 * Checks if the file requested is an image.
	 * 
	 * @return
	 */
	public boolean isImage(String path) {
		Pattern imagePattern = Pattern.compile(".*\\.(.+?$)");
		Matcher imagePatternMatcher = imagePattern.matcher(path);
		boolean result = false;

		// If the path includes a file check if it is an image.
		if (imagePatternMatcher.find()) {
			String fileType = imagePatternMatcher.group(1);
			for (String format : acceptedImageFormat) {
				result = result || format.equals(fileType);
			}
		}

		return result;
	}

	/**
	 * Checks if the file requested is text file.
	 * 
	 * @return
	 */
	public boolean isText(String path) {
		Pattern imagePattern = Pattern.compile(".*\\.(.+?$)");
		Matcher imagePatternMatcher = imagePattern.matcher(path);
		boolean result = false;

		// If the path includes a file check if it is an image.
		if (imagePatternMatcher.find()) {
			String fileType = imagePatternMatcher.group(1);
			for (String format : acceptedTextFormat) {
				result = result || format.equals(fileType);
			}
		}

		return result;
	}


	public boolean isIcon(String path) {
		Pattern imagePattern = Pattern.compile(".*\\.(.+?$)");
		Matcher imagePatternMatcher = imagePattern.matcher(path);
		boolean result = false;

		// If the path includes a file check if it is an image.
		if (imagePatternMatcher.find()) {
			String fileType = imagePatternMatcher.group(1);
			for (String format : acceptedIconFormat) {
				result = result || format.equals(fileType);
			}
		}

		return result;
	}

	public String getHeaders() {
		StringBuilder builder = new StringBuilder();
		// Append first line
		builder.append(version + " " + statusCode + " " + description + CRLF);

		// Append headers
		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			builder.append(key + ": " + value + CRLF);
		}

		return builder.toString();
	}

	public String getBody() {
		return body;
	}
	
	public BufferedReader getReader() {
		return reader;
	}

	public long getContentLength() {
		if (headers.containsKey("content-length")) {
			return Integer.parseInt(headers.get("content-length"));
		} else {
			return 0;
		}
	}

	public boolean isChunk() {
		if (headers.containsKey("chunked")) {
			return headers.get("chunked").equals("yes");
		} else {
			return false;
		}
	}

	public long getTimeRecivedResponse() {
		return timeRecivedResponse;
	}

	public int getStatusCode() {
		return Integer.parseInt(statusCode);
	}

}
