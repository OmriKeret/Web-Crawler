package logic;



import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import Helpers.InReqHttpParser;
import Helpers.HttpResponseFormater;
import config.configService;


public class serverLogic {
	final static String CRLF = "\r\n";
	private InReqHttpParser parser;
	private DataOutputStream outputStream;
	private InputStream inputStream;
	private configService conf;
	private crawlerLogic cawler;

	/**
	 * construct a logic object.
	 * 
	 * @param _headers
	 * @param _params
	 * @param _method
	 * @param _path
	 */
	public serverLogic(InReqHttpParser _parser, DataOutputStream _outputStream, InputStream _inputStream) {
		parser = _parser;
		outputStream = _outputStream;
		conf = configService.getInstance();
		inputStream = _inputStream;
		cawler = crawlerLogic.getInstance();
	}
	
	/**
	 * General error handle function.
	 * @param status
	 * @throws IOException
	 */
	public void ERR(int status) throws IOException {
		int contentType = 0; // Text.
		HttpResponseFormater responseParser;
		String responseMsg = getResponseMsg(status);
		responseParser = new HttpResponseFormater(status, contentType, responseMsg.length());

		// Trace.
		System.out.println("Response Header:");
		System.out.print(responseParser.getStatusLine());
		System.out.print(responseParser.getContentTypeLine());
		System.out.print(responseParser.getContentLengthLine());

		// Send the status line.
		outputStream.writeBytes(responseParser.getStatusLine());

		// Send the content type line.
		outputStream.writeBytes(responseParser.getContentTypeLine());

		// Send content length.
		outputStream.writeBytes(responseParser.getContentLengthLine());

		// Send a blank line to indicate the end of the header lines.
		outputStream.writeBytes(CRLF);

		// Stream bytes from string
		outputStream.writeBytes(responseMsg);
	}

	/**
	 *  HTTP GET REQUEST
	 * @throws IOException
	 */
	public void GET() throws IOException {
		int contentType = 0, currentRequestStatus = parser.getStatus();
		HttpResponseFormater responseParser;
		File f = null;
		boolean found = false;
		contentType = getContentType();
		String responseMsg = "", path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		lligealPath = lligealPath.replaceAll("/", "\\\\");

		if (lligealPath.equals("\\")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}

		lligealPath = lligealPath.replaceAll("^\\\\", "");
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
		if (f.isDirectory()) {
			lligealPath = lligealPath + "\\" + conf.getProp("defaultPage");
			fileFullPath = conf.getProp("root") + lligealPath;
			f = new File(fileFullPath);
			contentType = 0;
			
		}
		found = f.exists() && !f.isDirectory();
		long length = 0;

		if (!found) {
			currentRequestStatus = 404;
			contentType = 0;
			responseMsg = getResponseMsg(currentRequestStatus);
			length = responseMsg.length();
			
		} else {
			length = f.length();
		}

		responseParser = new HttpResponseFormater(currentRequestStatus, contentType, length);
		

		System.out.println("Response Header:");
		System.out.print(responseParser.getStatusLine());
		System.out.print(responseParser.getContentTypeLine());
		if (parser.isChunk()) {
			System.out.print(responseParser.getChunkHeader());
		} else {
			System.out.print(responseParser.getContentLengthLine());	
		}
		
		// Send the status line.
		outputStream.writeBytes(responseParser.getStatusLine());

		// Send the content type line.
		outputStream.writeBytes(responseParser.getContentTypeLine());

		if (parser.isChunk()) {
			
			// If chunk is required.
			outputStream.writeBytes(responseParser.getChunkHeader());
			
		} else {
			
			// Send content length.
			outputStream.writeBytes(responseParser.getContentLengthLine());
		}

		// Send a blank line to indicate the end of the header lines.
		outputStream.writeBytes(CRLF);

		if (f != null && found) {
			// Stream bytes from file
			try {
				readFromFileToStream(f, outputStream, parser.isChunk());
			} catch (Exception e) {
				System.out.println("Error accured while reading file");
				System.out.println(e);
			}

		} else {
			// Stream bytes from string
			outputStream.writeBytes(responseMsg);
		}

	}
	
	/**
	 *  HTTP POST REQUEST
	 * @throws IOException
	 */
	public void POST() throws IOException {
		// TODO: Get Cookie & Create authorization cookie
		
		int contentType = 0, currentRequestStatus = parser.getStatus();
		long length = 0;
		HttpResponseFormater responseParser;
		File f = null;
		boolean found = false, shouldCrawl = false;
		contentType = getContentType();
		String responseMsg = "", path = parser.getPath(), dynamicHtml = "";
		String lligealPath = path.replaceAll("\\/..\\/", "");
		lligealPath = lligealPath.replaceAll("/", "\\\\");
		if (lligealPath.equals("\\")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		lligealPath = lligealPath.replaceAll("^\\\\", "");
		shouldCrawl = lligealPath.equals("crawl.html");
		//TODO: what to do if should crawl?
		
		if (shouldCrawl && parser.containsCrawlToProp()) {
			// TODO: status page if running.
			boolean isRunning = cawler.isCrawlerRunning();
			cawler.run(parser.getCrawlTo(), parser.shouldPortScan());
			//TODO: wait till end.
			dynamicHtml =
					"<html>"
					+ "<head>"
					+ "<title>INFO</title>"
					+ "</head>"
					+ "<body>"
					+ parser.getParams()
					+ "</body>"
					+ "</html>";
			length = dynamicHtml.length();
			contentType = 0;

		} else {
			String fileFullPath = conf.getProp("root") + lligealPath;
			f = new File(fileFullPath);
			if (f.isDirectory()) {
				lligealPath = lligealPath + "\\" + conf.getProp("defaultPage");
				fileFullPath = conf.getProp("root") + lligealPath;
				f = new File(fileFullPath);
				contentType = 0;
				
			}
			found = f.exists() && !f.isDirectory();

	
			if (!found) {
				currentRequestStatus = 404;
				contentType = 0;
				responseMsg = getResponseMsg(currentRequestStatus);
				length = responseMsg.length();
				
			} else {
				length = f.length();
			}
		}

		responseParser = new HttpResponseFormater(currentRequestStatus, contentType, length);
		

		System.out.println("Response Header:");
		System.out.print(responseParser.getStatusLine());
		System.out.print(responseParser.getContentTypeLine());
		if (parser.isChunk()) {
			System.out.print(responseParser.getChunkHeader());
		} else {
			System.out.print(responseParser.getContentLengthLine());	
		}
		
		
		// Send the status line.
		outputStream.writeBytes(responseParser.getStatusLine());

		// Send the content type line.
		outputStream.writeBytes(responseParser.getContentTypeLine());

		if (parser.isChunk()) {
			
			// If chunk is required.
			outputStream.writeBytes(responseParser.getChunkHeader());
			
		} else {
			
			// Send content length.
			outputStream.writeBytes(responseParser.getContentLengthLine());
		}

		// Send a blank line to indicate the end of the header lines.
		outputStream.writeBytes(CRLF);

		if (shouldCrawl) {
			//TODO: wait for crawl to end.
			outputStream.writeBytes(dynamicHtml);
		} else {
			if (f != null && found) {
				// Stream bytes from file
				try {
					readFromFileToStream(f, outputStream, parser.isChunk());
				} catch (Exception e) {
					System.out.println("Error accured while reading file");
					System.out.println(e);
				}
	
			} else {
				// Stream bytes from string
				outputStream.writeBytes(responseMsg);
			}
		}

	}
	
	/**
	 *  HTTP TRACE REQUEST
	 * @throws IOException
	 */
	public void TRACE() throws IOException {
		int contentType = 0, currentRequestStatus = parser.getStatus(); // Text.
		HttpResponseFormater responseParser;
		String responseMsg = parser.getHeaders();
		responseParser = new HttpResponseFormater(currentRequestStatus, contentType, responseMsg.length());

		// Trace.
		System.out.println("Response Header:");
		System.out.print(responseParser.getStatusLine());
		System.out.print(responseParser.getContentTypeLine());
		System.out.print(responseParser.getContentLengthLine());

		// Send the status line.
		outputStream.writeBytes(responseParser.getStatusLine());

		// Send the content type line.
		outputStream.writeBytes(responseParser.getContentTypeLine());

		// Send content length.
		outputStream.writeBytes(responseParser.getContentLengthLine());

		// Send a blank line to indicate the end of the header lines.
		outputStream.writeBytes(CRLF);

		// Stream bytes from string
		outputStream.writeBytes(responseMsg);
	}
	
	/**
	 *  HTTP HEAD REQUEST
	 * @throws IOException
	 */
	public void HEAD() throws IOException {
		int contentType = 0, currentRequestStatus = parser.getStatus();
		HttpResponseFormater responseParser;
		File f = null;
		boolean found = false;
		contentType = getContentType();
		String responseMsg = "", path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		lligealPath = lligealPath.replaceAll("/", "\\\\");
		if (lligealPath.equals("\\")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		lligealPath = lligealPath.replaceAll("^\\\\", "");
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
		if (f.isDirectory()) {
			lligealPath = lligealPath + "\\" + conf.getProp("defaultPage");
			fileFullPath = conf.getProp("root") + lligealPath;
			f = new File(fileFullPath);
			contentType = 0;
			
		}
		found = f.exists() && !f.isDirectory();
		long length = 0;

		if (!found) {
			currentRequestStatus = 404;
			contentType = 0;
			responseMsg = getResponseMsg(currentRequestStatus);
			length = responseMsg.length();
			
		} else {
			length = f.length();
		}

		responseParser = new HttpResponseFormater(currentRequestStatus, contentType, length);
		

		System.out.println("Response Header:");
		System.out.print(responseParser.getStatusLine());
		System.out.print(responseParser.getContentTypeLine());
		
		if (parser.isChunk()) {
			System.out.print(responseParser.getChunkHeader());
		} else {
			System.out.print(responseParser.getContentLengthLine());	
		}
		
		// Send the status line.
		outputStream.writeBytes(responseParser.getStatusLine());

		// Send the content type line.
		outputStream.writeBytes(responseParser.getContentTypeLine());

		if (parser.isChunk()) {
			
			// If chunk is required.
			outputStream.writeBytes(responseParser.getChunkHeader());
			
		} else {
			
			// Send content length.
			outputStream.writeBytes(responseParser.getContentLengthLine());
		}

		// Send a blank line to indicate the end of the header lines.
		outputStream.writeBytes(CRLF);


	}

	// Bonus section.
	/**
	 *  HTTP OPTIONS REQUEST
	 * @throws IOException
	 */
	public void OPTIONS() throws IOException {
		int contentType = 0, currentRequestStatus = parser.getStatus();
		HttpResponseFormater responseParser;
		File f = null;
		boolean found = false;
		contentType = 0;
		String path = parser.getPath(), allow = "";
		String lligealPath = path.replaceAll("\\/..\\/", "");
		lligealPath = lligealPath.replaceAll("/", "\\\\");
		if (lligealPath.equals("\\")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		lligealPath = lligealPath.replaceAll("^\\\\", "");
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
		if (f.isDirectory()) {
			lligealPath = lligealPath + "\\" + conf.getProp("defaultPage");
			fileFullPath = conf.getProp("root") + lligealPath;
			f = new File(fileFullPath);
			contentType = 0;
			
		}
		found = f.exists() && !f.isDirectory();
		long length = 0;

		if (!found && !lligealPath.equals("*")) {
			// Allow nothing
			allow = "Allow: NONE";
		} else {
			allow = "Allow: OPTIONS, GET, HEAD, TRACE, POST" + CRLF;
		}

		responseParser = new HttpResponseFormater(currentRequestStatus, contentType, length);
		

		System.out.println("Response Header:");
		System.out.print(responseParser.getStatusLine());
		System.out.print(responseParser.getContentTypeLine());
		System.out.print(responseParser.getContentLengthLine());
		
		// Send the status line.
		outputStream.writeBytes(responseParser.getStatusLine());

		// Send content length.
		outputStream.writeBytes(allow);

		// Send a blank line to indicate the end of the header lines.
		outputStream.writeBytes(CRLF);


	}

	
	/**
	 *  HTTP PUT REQUEST
	 * @throws IOException
	 */
	public void PUT() throws IOException {
		int contentType = 0, currentRequestStatus = parser.getStatus();
		HttpResponseFormater responseParser;
		File f = null;
		boolean found = false;
		contentType = 0;
		String path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		lligealPath = lligealPath.replaceAll("/", "\\\\");
		if (lligealPath.equals("\\")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		lligealPath = lligealPath.replaceAll("^\\\\", "");
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
		if (f.isDirectory()) {
			lligealPath = lligealPath + "\\" + conf.getProp("defaultPage");
			fileFullPath = conf.getProp("root") + lligealPath;
			f = new File(fileFullPath);
			contentType = 0;
			
		}
		found = f.exists() ;
		long length = parser.getContentLength();
		currentRequestStatus = found ? 200 : 201;
		if (!f.isDirectory()) {
			try {
				writeToFileFromStream(f, inputStream, length);
			} catch (Exception e) {
				System.out.println("Error accured while reading file");
				System.out.println(e);
				currentRequestStatus = 500;
			}
		}

		responseParser = new HttpResponseFormater(currentRequestStatus, contentType, length);
		

		System.out.println("Response Header:");
		System.out.print(responseParser.getStatusLine());
		
		// Send the status line.
		outputStream.writeBytes(responseParser.getStatusLine());

		// Send a blank line to indicate the end of the header lines.
		outputStream.writeBytes(CRLF);


	}

	/**
	 * HTTP DELETE REQUEST
	 * @throws IOException
	 */
	public void DELETE() throws IOException {
		int contentType = 0, currentRequestStatus = parser.getStatus();
		HttpResponseFormater responseParser;
		File f = null;
		boolean found = false;
		contentType = 0;
		String path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		lligealPath = lligealPath.replaceAll("/", "\\\\");
		if (lligealPath.equals("\\")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		lligealPath = lligealPath.replaceAll("^\\\\", "");
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
		if (f.isDirectory()) {
			lligealPath = lligealPath + "\\" + conf.getProp("defaultPage");
			fileFullPath = conf.getProp("root") + lligealPath;
			f = new File(fileFullPath);
			contentType = 0;
			
		}
		found = f.exists() ;
		long length = parser.getContentLength();

		if (!f.isDirectory() && found) {
			try {
				Files.delete(f.toPath());
			} catch (Exception e) {
				System.out.println("Error accured while deleting file");
				System.out.println(e);
				currentRequestStatus = 500;
			}
			
		} else {
			currentRequestStatus = 400;
		}

		responseParser = new HttpResponseFormater(currentRequestStatus, contentType, length);
		

		System.out.println("Response Header:");
		System.out.print(responseParser.getStatusLine());
		
		// Send the status line.
		outputStream.writeBytes(responseParser.getStatusLine());

		// Send a blank line to indicate the end of the header lines.
		outputStream.writeBytes(CRLF);


	}
	
	
	/**
	 *  Helper method to write into files
	 * @param file
	 * @param reader
	 * @param length
	 * @throws IOException
	 */
	private void writeToFileFromStream(File file, InputStream reader, long length) throws IOException {
		// 
		FileOutputStream fos = new FileOutputStream(file);
		int readed = reader.read();
		int i = 0;
		// read until the end of the stream.
		while ((i < length) && readed != -1) {
			fos.write(readed);
			readed = reader.read();
			i++;
		}
		fos.close();
	}

	// Helper method to get content type.
	private int getContentType() {
		return parser.isText() ? 0 : parser.isImage() ? 1 : parser.isIcon() ? 2 : 3;
	}

	/**
	 * Helper method to read from file into stream.
	 * @param file
	 * @param outputStream
	 * @throws IOException
	 */
	private void readFromFileToStream(File file, DataOutputStream outputStream, boolean isChuncked) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		int chunckLengh = 30;
		int numberOfReadBytes = 0;
		byte[] bFile = isChuncked ? new byte[chunckLengh] : new byte[(int) file.length()];
		
		
		// read until the end of the stream.
		while (fis.available() != 0) {
			numberOfReadBytes = fis.read(bFile, 0, bFile.length);
			if (isChuncked && numberOfReadBytes != -1) {
				outputStream.writeBytes(Integer.toHexString(numberOfReadBytes) + CRLF);
				outputStream.write(bFile, 0, numberOfReadBytes);
				outputStream.writeBytes(CRLF);
			} else {
				// Not chuncked.
				outputStream.write(bFile, 0, bFile.length);
			}
		}
		if (isChuncked) {
			// Indicate last chunck was sent.
			outputStream.writeBytes("0" + CRLF);
			outputStream.writeBytes(CRLF);
		}
		fis.close();
	}

	/**
	 * Helper method to recive response msg from request status code.
	 * @param currentRequestStatus
	 * @return
	 */
	private String getResponseMsg(int currentRequestStatus) {
		if (currentRequestStatus == 200) {
			return "200 OK";
		} else if (currentRequestStatus == 404) {
			return "404 Not Found";
		} else if (currentRequestStatus == 501) {
			return "501 Not Implemented";
		} else if (currentRequestStatus == 400) {
			return "400 Bad Request";
		} else {
			return "500 Internal Server Error";
		}
	}

}
