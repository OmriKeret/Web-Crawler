package logic;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import Helpers.HttpParser;
import Helpers.HttpResponseParser;
import config.configService;

public class serverLogic {
	final static String CRLF = "\r\n";
	private HttpParser parser;
	private DataOutputStream outputStream;
	private InputStream inputStream;
	private configService conf;

	/**
	 * construct a logic object.
	 * 
	 * @param _headers
	 * @param _params
	 * @param _method
	 * @param _path
	 */
	public serverLogic(HttpParser _parser, DataOutputStream _outputStream, InputStream _inputStream) {
		parser = _parser;
		outputStream = _outputStream;
		conf = configService.getInstance();
		inputStream = _inputStream;
	}
	
	/**
	 * General error handle function.
	 * @param status
	 * @throws IOException
	 */
	public void ERR(int status) throws IOException {
		int contentType = 0; // Text.
		HttpResponseParser responseParser;
		String responseMsg = getResponseMsg(status);
		responseParser = new HttpResponseParser(status, contentType, responseMsg.length());

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
		HttpResponseParser responseParser;
		File f = null;
		boolean found = false;
		contentType = getContentType();
		String responseMsg = "", path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		if (lligealPath.equals("/")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
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

		responseParser = new HttpResponseParser(currentRequestStatus, contentType, length);
		

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
		int contentType = 0, currentRequestStatus = parser.getStatus();
		HttpResponseParser responseParser;
		File f = null;
		boolean found = false;
		contentType = getContentType();
		String responseMsg = "", path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		if (lligealPath.equals("/")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
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

		responseParser = new HttpResponseParser(currentRequestStatus, contentType, length);
		

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
	 *  HTTP TRACE REQUEST
	 * @throws IOException
	 */
	public void TRACE() throws IOException {
		int contentType = 0, currentRequestStatus = parser.getStatus(); // Text.
		HttpResponseParser responseParser;
		String responseMsg = parser.getHeaders();
		responseParser = new HttpResponseParser(currentRequestStatus, contentType, responseMsg.length());

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
		HttpResponseParser responseParser;
		File f = null;
		boolean found = false;
		contentType = getContentType();
		String responseMsg = "", path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		if (lligealPath.equals("/")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
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

		responseParser = new HttpResponseParser(currentRequestStatus, contentType, length);
		

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
		HttpResponseParser responseParser;
		File f = null;
		boolean found = false;
		contentType = 0;
		String path = parser.getPath(), allow = "";
		String lligealPath = path.replaceAll("\\/..\\/", "");
		if (lligealPath.equals("/")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
		found = f.exists() && !f.isDirectory();
		long length = 0;

		if (!found && !lligealPath.equals("*")) {
			// Allow nothing
			allow = "Allow: NONE";
		} else {
			allow = "Allow: OPTIONS, GET, HEAD, TRACE, POST" + CRLF;
		}

		responseParser = new HttpResponseParser(currentRequestStatus, contentType, length);
		

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
		HttpResponseParser responseParser;
		File f = null;
		boolean found = false;
		contentType = 0;
		String path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		if (lligealPath.equals("/")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
		found = f.exists() ;
		long length = parser.getContentLength();

		if (!f.isDirectory()) {
			try {
				writeToFileFromStream(f, inputStream, length);
			} catch (Exception e) {
				System.out.println("Error accured while reading file");
				System.out.println(e);
				currentRequestStatus = 500;
			}
		}

		responseParser = new HttpResponseParser(currentRequestStatus, contentType, length);
		

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
		HttpResponseParser responseParser;
		File f = null;
		boolean found = false;
		contentType = 0;
		String path = parser.getPath();
		String lligealPath = path.replaceAll("\\/..\\/", "");
		if (lligealPath.equals("/")) {
			
			// Use default route.
			lligealPath = conf.getProp("defaultPage");
			contentType = 0;
		}
		String fileFullPath = conf.getProp("root") + lligealPath;
		f = new File(fileFullPath);
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

		responseParser = new HttpResponseParser(currentRequestStatus, contentType, length);
		

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
			if (isChuncked) {
				outputStream.writeBytes(Integer.toHexString(numberOfReadBytes) + CRLF);
				outputStream.write(bFile, 0, bFile.length);
				outputStream.writeBytes(CRLF);
			} else {
				// Not chuncked.
				outputStream.write(bFile, 0, bFile.length);
			}
		}
		if (isChuncked) {
			// Indicate last chunck was sent.
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
