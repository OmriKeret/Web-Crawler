package Helpers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class urlUtils {
	private static Pattern urlPattern = Pattern.compile(".*((http[s]*:[/]+[.]*)|(www[.]))(.+?)(:.+?)*[/](.*)");
	private static Pattern shouldAdddelmiterPattern =  Pattern.compile("[/][^/]+\\.[^.]+$");
	private static Pattern shouldAddRootDomainPattern = Pattern.compile("(.*[www][.].*|http[s]*:\\/\\/.*)");
	
	/**
	 * Helper function to recive host from url.
	 * @param filePath
	 * @return
	 */
	public static String getHost(String url) {
		Matcher urlMatcher = urlPattern.matcher(url);
		String result = null;
		if (urlMatcher.find()) {
			if (urlMatcher.group(3) != null) {
				result = urlMatcher.group(3) + urlMatcher.group(4);
			} else {
				result = urlMatcher.group(4);
			}
		}
		return result;
	}
	
	public static String getFileRoute(String filePath) {
		Matcher urlMatcher = urlPattern.matcher(filePath);
		String result = null;
		if (urlMatcher.find()) {
			result = urlMatcher.group(6);
		}
		return result;
	}
	
	public static int getPort(String filePath) {
		Matcher urlMatcher = urlPattern.matcher(filePath);
		String result = null;
		int port = -1;
		if (urlMatcher.find()) {
			result = urlMatcher.group(5);
			if (result != null && result.length() > 0) {
				result = result.substring(1); // remove :
				port = Integer.parseInt(result);
			}
		}
		
		return port;
	}
	
	/**
	 * Helper function which process the URL in order to help us use it later on.
	 * @param currentLink
	 * @return
	 */
	public static String processUrl(String currentLink) {
		String result = currentLink;
		int port = getPort(currentLink);
		Matcher matcher = shouldAdddelmiterPattern.matcher(currentLink);
		if (!matcher.find()) {
			result = currentLink  + "/";
		} 
		
		matcher = shouldAddRootDomainPattern.matcher(currentLink);
		if (!matcher.find()) {
			if (port != -1) {
				result = getHost(currentLink) + ":" + Integer.toString(port) + "/" + currentLink;
			} else {
				result = getHost(currentLink) + "/" + currentLink;
			}
		}
		
		return result;
	}
	
	public static boolean partOfDomain(String rootDomain, String OtherDomain) {
		String host = getHost(rootDomain);
		String otherHost = getHost(OtherDomain);
		if (host != null && otherHost !=null) {
			return host.equals(otherHost);
		}
		return false;
	}
	
	public static boolean startWith(String url, ArrayList<String> disallowUrl) {
		for (String route : disallowUrl) {
			if (processUrl(url).startsWith(processUrl(route))) {
				return true;
			}
		}
		return false;
	}
}
