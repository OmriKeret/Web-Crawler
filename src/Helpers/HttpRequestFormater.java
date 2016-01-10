package Helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestFormater {
	final static String CRLF = "\r\n";
	private Pattern urlPattern;
	private Matcher urlMatcher;
	
	public HttpRequestFormater() {
		urlPattern = Pattern.compile(".*((http[s]*:[/]+[.]*)|(www[.]))(.+?)(:.+?)*[/](.*)");
	}
	
	/**
	 * Functino recive filePath (url) and http version and returns a string of the required request.
	 * @param filePath
	 * @param version
	 * @return
	 */
	public String getGetRequest(String filePath, String version) {
//		GET / HTTP/1.0
//		Host: www.ynet.co.il
		String result = "GET " + urlUtils.getFileRoute(filePath) + " HTTP/" + version + CRLF;
		result = result + "Host: " + urlUtils.getHost(filePath) + CRLF + CRLF;
		return result;
	}
    

	public String getHeadRequest(String filePath, String version) {
		//	HEAD / HTTP/1.0
		//	Host: www.ynet.co.il
		String result = "HEAD " + urlUtils.getFileRoute(filePath) + " HTTP/" + version + CRLF;
		result = result + "Host: " + urlUtils.getHost(filePath) + CRLF + CRLF;
		return result;
	}

}
