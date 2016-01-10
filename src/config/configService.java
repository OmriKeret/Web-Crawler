package config;


import java.util.*;
import java.io.*;

public class configService {
	// Singleton class to hold properties.

	private static configService instance;
	private static Properties prop;
	String[] images;
	String[] videos;
	String[] docs;
	
	private configService() {
		try {
			File testFile = new File("");
			String currentPath = testFile.getAbsolutePath() + "//config.ini";
			prop = new Properties();
			prop.load(new FileInputStream(currentPath));
			formatImages();
			formatVideos();
			formatDocs();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	// Create an array of trimed format docs.
	private void formatDocs() {
 		String docTypes = prop.getProperty("documentExtensions");
		String[] spliteDocs = docTypes.split(",");
		docs = new String[spliteDocs.length];
		
		for (int i = 0; i < docs.length; i++) {
			docs[i] = spliteDocs[i].trim();
		}
		
	}

	// Create an array of trimed format videos.
	private void formatVideos() {
		String videoTypes = prop.getProperty("videoExtensions");
		String[] splitedVideos = videoTypes.split(",");
		videos = new String[splitedVideos.length];
		
		for (int i = 0; i < videos.length; i++) {
			videos[i] = splitedVideos[i].trim();
		}
	}

	// Create an array of trimed format images.
	private void formatImages() {
		String imageTypes = prop.getProperty("imageExtensions");
		String[] splitedImages = imageTypes.split(",");
		images = new String[splitedImages.length];
		
		for (int i = 0; i < images.length; i++) {
			images[i] = splitedImages[i].trim();
		}
		
	}

	public static synchronized configService getInstance() {
		if (instance == null) {
			instance = new configService();
		}
		return instance;
	}

	// Getter to formats.
	public String[] getFormat(String s) {
		if (s.equals("imageExtensions")) {
			return images;
		} else if (s.equals("videoExtensions")) {
			return videos;
		} else if (s.equals("documentExtensions")) {
			return docs;
		}
		return null;
	}
	
	public String getProp(String p) {
		return prop.getProperty(p);
	}

}
