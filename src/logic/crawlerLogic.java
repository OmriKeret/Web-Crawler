package logic;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helpers.Analyzer;
import Helpers.Downloader;
import Helpers.HttpRequestFormater;
import Helpers.OutReqHttpParser;
import Helpers.Statistics;
import Helpers.urlUtils;
import Threads.customThreadPool;
import Threads.syncTaskQueue;
import config.configService;

public class crawlerLogic {
	final static String HTTP_VERSION = "1.1";
	private static crawlerLogic instance;
	private customThreadPool downloadersThreadPool;
	private customThreadPool analyzersThreadPool;
	private configService conf;
	private Statistics statistics;
	private boolean isRunning;
	private syncTaskQueue<String> urlQueue;
	private syncTaskQueue<String> documentQueue;

	private String urlToCrawl;

	private crawlerLogic() {
		conf = 	configService.getInstance();
	}

	@SuppressWarnings("resource")
	public synchronized void run(String urlToCrawl, boolean shouldRunPortScan, boolean shouldDisrespactRobot) throws Exception {
		try {
		
		// Init
		statistics = new Statistics();
		HttpRequestFormater requestParse = new HttpRequestFormater();
		
		// Create queues.
		urlQueue = new syncTaskQueue<String>(Integer.parseInt(conf.getProp("maxDownloaders")));
		documentQueue = new syncTaskQueue<String>(Integer.parseInt(conf.getProp("maxAnalyzers")));

		// Create thread pools.
		downloadersThreadPool = new customThreadPool(Integer.parseInt(conf.getProp("maxDownloaders")));
		analyzersThreadPool = new customThreadPool(Integer.parseInt(conf.getProp("maxAnalyzers")));
		
		//TODO: robot txt?
		isRunning = true;
		try {
			urlQueue.insert(urlUtils.processUrl(urlToCrawl));
		} catch (InterruptedException e1) {
			
		}
		
		// Handle robot.txt
		String request = requestParse.getGetRequest(urlUtils.processUrl(urlUtils.getHost(urlToCrawl)) + "Robots.txt", HTTP_VERSION);
		int port = urlUtils.getPort(urlToCrawl) == -1 ? 80 : urlUtils.getPort(urlToCrawl); 
		Socket socket = new Socket(urlUtils.getHost(urlToCrawl), port);
		DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream()); 
		List<String> addToQueueUrls = new ArrayList<String>();
		InputStream inFromServer = socket.getInputStream();
		
		// Write the request.
		long sentRequestTime = System.currentTimeMillis();
		outToServer.writeBytes(request);  

		// Now we should receive the HTML file.
		OutReqHttpParser parser = new OutReqHttpParser(inFromServer);
		parser.parseResponse();
		
		// RTT average data.
		statistics.increaseNumberOfWaitingTimeRTT(parser.getTimeRecivedResponse() - sentRequestTime);
		statistics.increaseNumberOfRequestsRTT();
		
		if (parser.getStatusCode() == 200) {
			String html = parser.getBody();
			Pattern robotTxtContentPattern = Pattern.compile("(User-agent|user-agent): \\*((.|\\n|\\r)*?)(User-agent|$|user-agent)");
			Pattern allowedPattern = Pattern.compile("(\\nAllow:|^Allow:|\\nallow:|^allow:)[ ]*(.*?)(\\n|$|\\r)");
			Pattern disallowedPattern = Pattern.compile("(\\nDisAllow:|^DisAllow:|\\ndisallow:|^disallow:)[ ]*(.*?)(\\n|$|\\r)");
			Matcher robotContent = robotTxtContentPattern.matcher(html);
			
			// Loop on all of our robot user agent areas.
			while (robotContent.find()) {
				String content = robotContent.group(2);
				Matcher allowLines = allowedPattern.matcher(content);
				Matcher disAllowLines = disallowedPattern.matcher(content);
				
				while (allowLines.find()) {
					if (!addToQueueUrls.contains(allowLines.group(2))) {
						addToQueueUrls.add(allowLines.group(2));	
					}
					
				}
				
				// Add disallows to statistics so we would know what urls to ignore
				while (disAllowLines.find()) {
					
					if (!shouldDisrespactRobot) {
						statistics.addDisallowed(disAllowLines.group(2));
						
					} else {
						
						// In case we disrespact robot text we will add the urls to anilaizition.
						if (!addToQueueUrls.contains(disAllowLines.group(2))) {
							addToQueueUrls.add(disAllowLines.group(2));	
						}
					}
				}
			}
			
		}
		
		
		// Create Downloaders.
		for (int i = 0; i <  Integer.parseInt(conf.getProp("maxDownloaders")); i++) {
			Downloader downloader = new Downloader(urlQueue, documentQueue, statistics);
			try {
				
				downloadersThreadPool.execute(downloader);
			} catch (Exception e) {
				System.out.println(" Error executing downloader thread");
				e.printStackTrace();
			}
		}
		
		try {
			
			// Create analyzers.
			for (int i = 0; i <  Integer.parseInt(conf.getProp("maxAnalyzers")); i++) {
				
				Analyzer analyzer = new Analyzer(urlQueue, documentQueue, statistics, urlToCrawl);
				try {
					analyzersThreadPool.execute(analyzer);
				} catch (Exception e) {
					System.out.println("Error executing downloader thread");
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			
			downloadersThreadPool.shutdown();
			analyzersThreadPool.shutdown();
			urlQueue.initiateShutDown();
			documentQueue.initiateShutDown();
			
			// Probabley rootDomain wasn't good
			System.out.println("Problem initiating analyzers");
			throw new Exception("Problem initating analyzers");

		}
		
		// Add robots.txt allowed (or disallowed) routes.
		for (String urlToAdd : addToQueueUrls) {
			if (urlUtils.partOfDomain(urlToCrawl, urlToAdd)) {
				urlQueue.insert(urlToAdd);	
			}
		}
		
		//TODO: Check if robots text works
		
		// Do port scan from 1 to 1024.
		if (shouldRunPortScan) {
			for (int i = 1; i <= 1024; i++) {
				
					try {
						Socket clientSocket = new Socket(urlUtils.getHost(urlToCrawl), i);
						statistics.addOpenPort(i);
						clientSocket.close();
						
					} catch (IOException e) {
						// Port is closed.
					
					}
			}
		}
		
		// Wait for processing to finish.
		while (urlQueue.getNumberOfWaitingThreads() != Integer.parseInt(conf.getProp("maxDownloaders")) 
				|| documentQueue.getNumberOfWaitingThreads() != Integer.parseInt(conf.getProp("maxAnalyzers"))) {
			try {
				wait(500);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		

		// Stops the threads
		downloadersThreadPool.shutdown();
		analyzersThreadPool.shutdown();
		urlQueue.initiateShutDown();
		documentQueue.initiateShutDown();

		//TODO: save into file
		
		
		
		// Finished initiation
		// TODO: port scanning
		// TODO: insert rootURL
		// TODO: ROBOT txt
		isRunning = false;
		
		} catch (Exception e) {
			
			downloadersThreadPool.shutdown();
			analyzersThreadPool.shutdown();
			urlQueue.initiateShutDown();
			documentQueue.initiateShutDown();
			throw new Exception();
		}
	}
	
	/**
	 * Getter to singleton
	 * @return
	 */
	public static synchronized crawlerLogic getInstance() {
		if (instance == null) {
			instance = new crawlerLogic();
		}
		return instance;
	}
	
	// Getter to is running.
	public boolean isCrawlerRunning() {
		return isRunning;
	}

}
