package Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Statistics {
	
	// Statistics.
	private int numberOfPages = 0;
	private int totalSizeInBytesOfPages = 0;
	private int numberOfVideos = 0;
	private int numberOfImages = 0;
	private int numberOfDocuments = 0;
	private int numberOfLinks = 0;
	private int numberOfExternals = 0;
	private int otherDomains = 0;
	private List<String> inSiteCrawled;
	private List<String> outSideDomain;
	private List<String> disallowedUrl;
	private List<Integer> openPorts;
	private int numberOfRequestsRTT = 0;
	private long totalWaitingTimeRTT = 0;
	
	// Size of files
	private int sizeOfPages = 0;
	private int sizeOfVideos= 0;
	private int sizeOfImages = 0;
	private int sizeOfDocs = 0;
	private Pattern urlPattern;
	
	public Statistics() {
		inSiteCrawled = new ArrayList<String>();
		outSideDomain = new ArrayList<String>();
		disallowedUrl = new ArrayList<String>();
		openPorts = new ArrayList<Integer>();
		urlPattern = Pattern.compile(".*((http[s]*:[/]+[.]*)|(www[.]))(.+?)(:.+?)*[/](.*)");
	}
	
	
	/**
	 * Add URL to already crawled URLs list.
	 * @param url
	 * @return true if the url wasn't crawled yet and false otherwise. 
	 */
	public synchronized boolean addIOutSideDomain(String url) {
		Matcher tempMatcher;
	
		tempMatcher = urlPattern.matcher(url);
		if (tempMatcher.find()) {
			url = tempMatcher.group(4);
		}
			
		if (!outSideDomain.contains(url)) {
			outSideDomain.add(url);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Add URL to already crawled URLs list.
	 * @param url
	 * @return true if the url wasn't crawled yet and false otherwise. 
	 */
	public synchronized boolean addInSiteCrawledUrl(String url) {
		if (!inSiteCrawled.contains(url)) {
			inSiteCrawled.add(url);
			return true;
		}
		return false;
	}
	
	// Increase the total number of pages 
	public synchronized void increaseNumberOfPages() {
		numberOfPages++;
	}
	
	public synchronized void totalSizeOfPages(int size) {
		totalSizeInBytesOfPages += size;
	}
	
	// Increase the total number of videos 
	public synchronized void increaseNumberOfVideos() {
		numberOfVideos++;
	}
	
	// Increase the total number of images 
	public synchronized void increaseNumberOfImages() {
		numberOfImages++;
	}
	
	// Increase the total number of documents 
	public synchronized void increaseNumberOfDocuments() {
		numberOfDocuments++;
	}
	
	// Increase the total number of links 
	public synchronized void increaseNumberOfLinks() {
		numberOfLinks++;
	}
	
	// Increase the total number of externals 
	public synchronized void increaseNumberOfExternals() {
		numberOfExternals++;
	}
	
	// Increase the total number of externals 
	public synchronized void increaseNumberOfotherDomains() {
		otherDomains++;
	}
	
	// Add crawled domain to crawled domains.
	public synchronized void addCrawledDomain(String domain) {
		if (inSiteCrawled.contains(domain)) {
			inSiteCrawled.add(domain);
		}
	}
	
	// Increase the total number of RequestsRTT.
	public synchronized void increaseNumberOfRequestsRTT() {
		numberOfRequestsRTT++;
	}
	
	// Increase the total number of Waiting time RTT.
	public synchronized void increaseNumberOfWaitingTimeRTT(long l) {
		totalWaitingTimeRTT = totalWaitingTimeRTT + l;
	}

	public int getSizeOfPages() {
		return sizeOfPages;
	}

	public synchronized void increaseSizeOfPages(int size) {
		this.sizeOfPages += size;
	}

	public int getSizeOfVideos() {
		return sizeOfVideos;
	}

	public synchronized void increaseSizeOfVideos(int sizeOfVideos) {
		this.sizeOfVideos += sizeOfVideos;
	}

	public int getSizeOfImages() {
		return sizeOfImages;
	}

	public synchronized void increaseSizeOfImages(int sizeOfImages) {
		this.sizeOfImages += sizeOfImages;
	}

	public int getSizeOfDocs() {
		return sizeOfDocs;
	}

	public synchronized void increaseSizeOfDocs(int sizeOfDocs) {
		this.sizeOfDocs += sizeOfDocs;
	}


	public synchronized void addOpenPort(int i) {
		openPorts.add(i);
		
	}


	public synchronized void addDisallowed(String group) {
		disallowedUrl.add(group);
		
	}


	public ArrayList<String> getDisallowed() {
		return (ArrayList<String>) disallowedUrl;
	}
	
}
