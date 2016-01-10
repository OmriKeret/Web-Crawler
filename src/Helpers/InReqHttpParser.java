package Helpers;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InReqHttpParser {
	final static String CRLF = "\r\n";
	private BufferedReader reader;
	private String method, path, ver, body, origPath;
	private Hashtable<String, String> headers, params;
	private int ret = 200;
	private final String[] acceptedImageFormat = { "bmp", "gif", "png", "jpg" };
	private final String[] acceptedTextFormat = { "txt", "html"};
	private final String[] acceptedIconFormat = { "ico"};
	
	public InReqHttpParser(InputStream stream) {
		reader = new BufferedReader(new InputStreamReader(stream));
		method = "";
		path = "";
		origPath = "";
		headers = new Hashtable<String, String>();
		params = new Hashtable<String, String>();
		ver = "";
		body = "";

	}

	/**
	 * Parses the first line of the request. including the url, method and
	 * version. returns the status code of the request.
	 */
	public int parseRequest() {

		try {
			ret = parseFirstLine();
			parseHeaders();
			if (headers != null) {
				boolean parseBody = headers.containsKey("content-length") && !method.equals("PUT");
				if (parseBody) {
					int bodyLength = Integer.parseInt(headers.get("content-length"));
					parseBody(bodyLength);
				}
			}
			
		} catch (IOException e) {
			ret = 500;
			e.printStackTrace();
		} catch (NumberFormatException e) {
			ret = 400;
		}  catch (Exception e) {
			ret = 500;
			e.printStackTrace();
		}

		return 0;
	}

	private void parseBody(int bodyLength) throws IOException {
		StringBuilder bodyBuilder = new StringBuilder();
		Pattern parameterEquactionPattern = Pattern.compile("(.+?=.*?)(&|$)"),
				parameterBreakPattern = Pattern.compile("(.+?)=(.*)");
		Matcher equationMatcher, parameterMatcher;
		String line;

		// Reads the entire body as stated in the contentLength attribute.
		for (int i = 0; i < bodyLength; i++) {
			bodyBuilder.append((char) reader.read());
		}
		body = bodyBuilder.toString();
		
		// Printing body to console.
		System.out.println("Recived body:");
		System.out.println(body);
		
		equationMatcher = parameterEquactionPattern.matcher(body);

		// For each equation separated by '&' extract parameter and value.
		while (equationMatcher.find()) {
			line = equationMatcher.group(1);
			parameterMatcher = parameterBreakPattern.matcher(line);
			if (parameterMatcher.find()) {
				params.put(parameterMatcher.group(1), parameterMatcher.group(2));
			}
		}
	}

	/**
	 * Parses the first line of the requeset. including the Url, method and
	 * version.
	 */
	private int parseFirstLine() throws IOException {
		String firstLine[], line;
		line = reader.readLine();
		
		// Printing first line to console.
		System.out.println("Recived header:");
		System.out.println(line);
		
		if (line == null || line.length() == 0)
			return 400;
		if (Character.isWhitespace(line.charAt(0))) {

			// starting whitespace, return bad request
			return 400;
		}

		firstLine = line.split("\\s");
		if (firstLine.length != 3) {
			return 400;
		}
		if (firstLine[2].indexOf("HTTP/") == 0 && firstLine[2].indexOf('.') > 5) {
			ver = firstLine[2].substring(5);
		} else {
			return 400;
		}

		method = firstLine[0];
		origPath = firstLine[1];
		getPathAndParams(firstLine[1]);
		return 200;
	}

	/**
	 * Get a url and extract the path and params.
	 * 
	 * @param url
	 */
	private void getPathAndParams(String url) {
		
		int index;
		String[] urlParams, temp;
		index = url.indexOf('?');
		if (index < 0) {
			path = url;
		} else {
			path = url.substring(0, index);
			urlParams = url.substring(index + 1).split("&");

			for (int i = 0; i < urlParams.length; i++) {
				temp = urlParams[i].split("=");
				if (temp.length == 2) {
					// Extract the data.
					params.put(temp[0], temp[1]);

				} else if (temp.length == 1 && urlParams[i].indexOf('=') == urlParams[i].length() - 1) {
					// in Case of [param]= we insert the empty string.
					params.put(temp[0], "");
				}
			}
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
				ret = 400;
				break;
			} else {
				headers.put(line.substring(0, index).toLowerCase().trim(), line.substring(index + 1).trim());
			}
			line = reader.readLine();
		}
		
		// One line to seperate from body or response header
		System.out.println();
	}

	/**
	 * Checks if the file requested is an image.
	 * 
	 * @return
	 */
	public boolean isImage() {
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
	public boolean isText() {
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

	public String getMethod() {
		return method;
	}
	
	public String getPath() {
		return path;
	}
	
	public int getStatus() {
		return ret;
	}

	public boolean isIcon() {
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
		builder.append(method + " " + origPath + " " + ver + CRLF);
		
		// Append headers
		for (Entry<String, String> entry : headers.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    builder.append(key + ": " + value + CRLF);
		}
		
		return builder.toString();
	}
	
	public String getParams() {
		StringBuilder builder = new StringBuilder();
		
		// Append headers
		for (Entry<String, String> entry : params.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    builder.append(key + ": " + value + "<br>");
		}
		
		return builder.toString();
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

	public boolean isAllowedToCrawl() {
		
		if (params.containsKey("cameFromMainPage")) {
			return params.get("cameFromMainPage").equals("true");
		} else {
			return false;
		}
	}

	public boolean containsCrawlToProp() {
		if (params.containsKey("crawlTo")) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getCrawlTo()
	{
		return params.get("crawlTo");
	}

	public boolean shouldPortScan() {
		if (params.containsKey("portScan")) {
			return params.get("portScan").equals("true");
		} else {
			return false;
		}
	}
}
