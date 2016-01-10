import java.io.DataOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Socket;

import Helpers.InReqHttpParser;
import logic.crawlerLogic;
import logic.serverLogic;
public class httpRequestProccesser implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;

	// Constructor
	public httpRequestProccesser(Socket socket) throws Exception {
		this.socket = socket;
	}

	// Implement the run() method of the Runnable interface.
	public void run() {
		try {
			processRequest();
			
		} catch (Exception e) {
			System.out.println("A request had an error we couldn't recover from.");
			System.out.println(e);
			System.out.println();
		}
	}

	private void processRequest() throws Exception {
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		InputStream is = socket.getInputStream();
		InReqHttpParser parser = new InReqHttpParser(is);
		serverLogic logic = new serverLogic(parser, os, is);
		parser.parseRequest();
		
		
		if (parser.getStatus() != 200) {
			
			logic.ERR(parser.getStatus());
			// Parsing error 400.
			//Parsing error logic.
			
			System.out.println("error accured while running method");
		} else {
			
			try {
			Method method = logic.getClass().getMethod(parser.getMethod());
			method.invoke(logic);
			
			} catch (NoSuchMethodException e){
				//No such method. 501.
				logic.ERR(501);
				
			} catch (Exception e) {
				System.out.println("error accured while running method");	
				//TODO:  error 500.
				logic.ERR(500);
			}
		}
		

		// Close streams and socket.
		os.close();
		socket.close();
	}

}
