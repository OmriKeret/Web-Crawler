import java.util.*;

import Threads.customThreadPool;
import config.configService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class main {

	public static void main(String[] args) {

		ServerSocket socket;
		try {
			configService conf = configService.getInstance();
			customThreadPool threadPool = new customThreadPool(Integer.parseInt(conf.getProp("maxThreads")));
			socket = new ServerSocket(Integer.parseInt(conf.getProp("port")));
			System.out.println("server is initlized and listening on port:" + Integer.parseInt(conf.getProp("port")));
			// Process HTTP service requests in an infinite loop.
			while (true) {
				// Listen for a TCP connection request.
				Socket connection = socket.accept();
				System.out.println("server recived a request");
				// Construct an object to process the HTTP request message.
				httpRequestProccesser request = new httpRequestProccesser(connection);

				// Create a new thread to process the request.
				threadPool.execute(request);
			}
		} catch (Exception e) {
			System.out.println("We have an error we can't recover from:");
			System.out.println(e);
			System.out.println();
			System.out.println("Now server will shut down.");
		}
	}

}
