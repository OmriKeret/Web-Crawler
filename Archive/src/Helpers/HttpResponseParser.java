package Helpers;

import java.util.Hashtable;

public class HttpResponseParser {
	final static String CRLF = "\r\n";
	final static String[] contentType = {"text/html", "image", "icon","application/octet-stream"};
	private Hashtable<Integer,String> responsesStatusLine;
	private String contentTypeLine, contentLengthLine, chunckedHeader;
	private int responseCode;
	public HttpResponseParser (int _responseCode, int type, long contentLength) {
		
		//Initilize status line dictionary.
		responsesStatusLine = new Hashtable<Integer, String>();
		responsesStatusLine.put(200, "HTTP/1.1 200 OK" + CRLF);
		responsesStatusLine.put(404, "HTTP/1.1 404 Not Found" + CRLF);
		responsesStatusLine.put(501, "HTTP/1.1 501 Not Implemented" + CRLF);
		responsesStatusLine.put(400, "HTTP/1.1 400 Bad Request" + CRLF);
		responsesStatusLine.put(500, "HTTP/1.1 500 Internal Server Error" + CRLF);
		responseCode = _responseCode;
		contentTypeLine = "Content-Type: " + contentType[type] + CRLF;
		contentLengthLine = "Content-Length: " + Long.toString(contentLength) + CRLF;
		chunckedHeader = "transfer-encoding: chunked" + CRLF;
	}
	
	
	public String getStatusLine() {
		return responsesStatusLine.get(responseCode);
	}


	public String getContentTypeLine() {
		return contentTypeLine;
	}


	public String getContentLengthLine() {
		return contentLengthLine;
	}


	public String getChunkHeader() {
		return chunckedHeader;
	}

	
}
