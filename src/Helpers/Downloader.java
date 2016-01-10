package Helpers;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Threads.syncTaskQueue;
import config.configService;

public class Downloader implements Runnable {
	final static String CRLF = "\r\n";
	final static String HTTP_VERSION = "1.1";
	private syncTaskQueue<String> urlQueue;
	private syncTaskQueue<String> documentQueue;
	HttpRequestFormater requestParse;
	private configService conf;
	Pattern acceptedImagePattern, acceptedVideoPattern, acceptedDocuments, isHtml;
	Matcher matcher;
	Statistics statistics;
	
	/**
	 *  Constructor.
	 */
	public Downloader(syncTaskQueue<String> _urlQueue, syncTaskQueue<String> _documentQueue, Statistics _statistics) {
		conf = configService.getInstance();
		urlQueue = _urlQueue;
		documentQueue = _documentQueue;	
		buildPatterns();
		requestParse = new HttpRequestFormater();
		statistics = _statistics;
		
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
	}

	public void run() {
		try {
			String currentUrl;
			long sentRequestTime;
			int port;
			// Do forever:
			// 1. Take a link to download
			// 2. Check if should download link
			// 3. Enqueue downloaded document.

			while ((currentUrl = urlQueue.remove()) != null) {
				
				if (urlUtils.startWith(currentUrl, statistics.getDisallowed())){
					// f the current URL is not allowed.
					continue;
				}
				
				Socket clientSocket = null;
				try {
					
					// Initiate TCP connection.
					port = urlUtils.getPort(currentUrl) == -1 ? 80 : urlUtils.getPort(currentUrl); 
					clientSocket = new Socket(urlUtils.getHost(currentUrl), port);
					DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
					InputStream inFromServer = clientSocket.getInputStream();
					
					//TODO: handle url of defult page.
					if (isHtml(currentUrl)) {
						// Download document.

						// Helper function that Parse the desired request.
						String request = requestParse.getGetRequest(currentUrl, HTTP_VERSION);
						
						// Write the request.
						sentRequestTime = System.currentTimeMillis();
						outToServer.writeBytes(request);  

						// Now we should receive the HTML file.
						OutReqHttpParser parser = new OutReqHttpParser(inFromServer);

						parser.parseResponse();
						
						// RTT average data.
						statistics.increaseNumberOfWaitingTimeRTT(parser.getTimeRecivedResponse() - sentRequestTime);
						statistics.increaseNumberOfRequestsRTT();
						
						// If the status code isn't 200 then we don't have any page.
						if (parser.getStatusCode() != 200 ) {
							clientSocket.close();
							continue;
						}
						
						// updating page sizes statistics.
						statistics.increaseSizeOfPages((int)parser.getContentLength());
						
						// Insert html to analyze queue.
						String body = parser.getBody();
						if (body != null) {
							documentQueue.insert(body);
							System.out.println("added " + currentUrl + " HTML file to analyze queue");
						}
						
						clientSocket.close();
						
					} else {
						// Do an head request
						
						// Helper function that Parse the desired request.
						String request = requestParse.getHeadRequest(currentUrl, HTTP_VERSION);
						
						// Write the request.
						sentRequestTime = System.currentTimeMillis();
						outToServer.writeBytes(request);  

						// Now we should receive the HTML file.
						OutReqHttpParser parser = new OutReqHttpParser(inFromServer);
						
						// Parse the response
						parser.parseResponse();
						
						// RTT average data.
						statistics.increaseNumberOfWaitingTimeRTT(parser.getTimeRecivedResponse() - sentRequestTime);
						statistics.increaseNumberOfRequestsRTT();
						
						// If the status code isn't 200 then we don't have any page.
						if (parser.getStatusCode() != 200 ) {
							clientSocket.close();
							continue;
						}
						
						// Update the statistics.
						if (isVideo(currentUrl)){
							// Increase video total size
							statistics.increaseSizeOfVideos((int)parser.getContentLength());
							
						} else if (isImage(currentUrl)) {
							// Increase image total size
							statistics.increaseSizeOfImages((int)parser.getContentLength());
							
						} else if (isDoc(currentUrl)) { 
							// Increase docs total size
							statistics.increaseSizeOfDocs((int)parser.getContentLength());
							
						}
						
						clientSocket.close();
							
					}
					
				} catch (Exception e) {
					// Error while parsing request.
					System.out.println("Error while downlding, stoped downloading");
					try {
						clientSocket.close();
					} catch (IOException e1) {

					}

				}


			}

		} catch (InterruptedException e) {

			System.out.println("Finished downloading.");
		}	
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
