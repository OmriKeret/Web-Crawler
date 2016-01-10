package Helpers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Threads.syncTaskQueue;
import config.configService;

public class Analyzer implements Runnable {

	private String rootPageUrl, rootDomain;
	private syncTaskQueue<String> urlQueue;
	private syncTaskQueue<String> documentQueue;
	private configService conf;
	Pattern acceptedImagePattern, acceptedVideoPattern, acceptedDocuments, isHtml, hrefPattern, urlPattern,
		shouldAdddelmiterPattern, shouldAddRootDomainPattern;
	Matcher matcher;
	
	Statistics statistics;
	
	
	/**
	 *  Constructor.
	 * @throws Exception 
	 */
	public Analyzer(syncTaskQueue<String> _urlQueue, syncTaskQueue<String> _documentQueue, Statistics _statistics, String _rootDomain) throws Exception {
		Matcher tempMatcher;
		conf = configService.getInstance();
		urlQueue = _urlQueue;
		documentQueue = _documentQueue;	
		buildPatterns();
		statistics = _statistics;
		
		rootDomain = urlUtils.getHost(_rootDomain);
		rootPageUrl = "http://" + rootDomain;
	}
	
	/**
	 * Build the Regexp patterns to check if a URL is a document of some type.
	 */
	private void buildPatterns() {
		StringBuilder imagePattern = new StringBuilder();
		StringBuilder videoPattern = new StringBuilder();
		StringBuilder docsPattern = new StringBuilder();
		int i = 0;
		for (String str : conf.getFormat("imageExtensions")) {
			// If its the first round don't push | to the regex.
			if (i == 0) {
				imagePattern.append(str + "$");
			} else {
				imagePattern.append("|" + str + "$");
			}
			i++;
		}
		i = 0;
		for (String str : conf.getFormat("videoExtensions")) {
			// If its the first round don't push | to the regex.
			if (i == 0) {
				videoPattern.append(str + "$");
			} else {
				videoPattern.append("|" + str + "$");
			}
			i++;
		}
		i = 0;
		for (String str : conf.getFormat("documentExtensions")) {
			// If its the first round don't push | to the regex.
			if (i == 0) {
				docsPattern.append(str + "$");
			} else {
				docsPattern.append("|" + str + "$");
			}
			i++;
		}
		
		 acceptedImagePattern = Pattern.compile(imagePattern.toString());
		 acceptedVideoPattern = Pattern.compile(videoPattern.toString());
		 acceptedDocuments = Pattern.compile(docsPattern.toString());
		 isHtml =  Pattern.compile("html$|asp$");
		 hrefPattern = Pattern.compile("(<a.*?href=\\\"(.+?)\\\".*?>)|(<img src=\\\"(.+?)\\\".*?>)");
		 urlPattern = Pattern.compile(".*((http[s]*:[/]+[.]*)|(www[.]))(.+?)(:.+?)*[/](.*)");
		
		 shouldAdddelmiterPattern =  Pattern.compile("[/][^/]+\\.[^.]+$");
		 shouldAddRootDomainPattern = Pattern.compile("(.*[www][.].*|http[s]*:\\/\\/.*)");;
	}
	
	@Override
	public void run() {
		try {
			String currentHtml;
			Matcher matcher, tempMatcher;
			String currentLink, temp;
			boolean isPartOfRootDomain;
			boolean isHref;
			// Do forever:
			// 1. Take a link to download
			// 2. Check if should download link
			// 3. Enqueue downloaded document.

			while ((currentHtml = documentQueue.remove()) != null) {
				isPartOfRootDomain = true;
				
				// Check all hrefs.
				matcher = hrefPattern.matcher(currentHtml);
				while (matcher.find()) {
					
					 if (matcher.group(2) != null) {
						 //Href.
						 currentLink = matcher.group(2);
						 statistics.increaseNumberOfLinks();
						 isHref = true;
						 if (matcher.group(2).startsWith("#")) {
							 // In page url.
							 continue;
						 }
					 } else {
						 // Image.
						 currentLink = matcher.group(4);
						 isHref = false;
						 if (isImage(currentLink)) {
							
							// Increase number of images.
							statistics.increaseNumberOfImages();
						}
					 }
					
					// Handles url of type: www.ynet.co.il
					currentLink = urlUtils.processUrl(currentLink);
					
					// Check if the URL is part of the root domain.
					isPartOfRootDomain = urlUtils.partOfDomain(rootDomain, currentLink);
					
					// Check if link was processed.
					if (isPartOfRootDomain && statistics.addInSiteCrawledUrl(currentLink)) {
					
						urlQueue.insert(currentLink);
						
						// Update the statistics.
						if (isHtml(currentLink)) {
							statistics.increaseNumberOfPages();
							
						} else if (isVideo(currentLink)){
							
							// Increase number of videos.
							statistics.increaseNumberOfVideos();
							
						} else if (isDoc(currentLink)) { 
							
							// Increase number of docs.
							statistics.increaseNumberOfDocuments();
						} 
						
					} else if(!isPartOfRootDomain) {
						statistics.addIOutSideDomain(currentLink);
					}
					
				}
					
			}


			

		} catch (InterruptedException e) {

			System.out.println("Finished Analyzing");
		}	
	}
	
	/**
	 * Helper function which process the URL in order to help us use it later on.
	 * @param currentLink
	 * @return
	 */
	private String processUrl(String currentLink) {
		String result = currentLink;
		 
		matcher = shouldAdddelmiterPattern.matcher(currentLink);
		if (!matcher.find()) {
			result = currentLink  + "/";
		} 
		
		matcher = shouldAddRootDomainPattern.matcher(currentLink);
		if (!matcher.find()) {
			result = rootPageUrl + currentLink;
		}
		
		return result;
	}

	/**
	 * Recives an URL and returns if the destination is an html file.
	 * @param currentUrl
	 * @return
	 */
	private boolean isHtml(String currentUrl) {
		matcher = isHtml.matcher(currentUrl);
		return matcher.find();
	}
	
	/**
	 * Recives an URL and returns if the destination is an video file.
	 * @param currentUrl
	 * @return
	 */
	private boolean isVideo(String currentUrl) {
		matcher = acceptedVideoPattern.matcher(currentUrl);
		return matcher.find();
	}
	
	/**
	 * Recives an URL and returns if the destination is an image file.
	 * @param currentUrl
	 * @return
	 */
	private boolean isImage(String currentUrl) {
		matcher = acceptedImagePattern.matcher(currentUrl);
		return matcher.find();
	}
	
	/**
	 * Recives an URL and returns if the destination is an doc file.
	 * @param currentUrl
	 * @return
	 */
	private boolean isDoc(String currentUrl) {
		matcher = acceptedDocuments.matcher(currentUrl);
		return matcher.find();
	}

}
