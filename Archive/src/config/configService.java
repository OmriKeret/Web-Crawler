package config;

import java.util.*;
import java.io.*;

public class configService {
	// Singleton class to hold properties.

	private static configService instance;
	private static Properties prop;

	private configService() {
		try {
			File testFile = new File("");
			String currentPath = testFile.getAbsolutePath() + "/src/config/config.ini";
			prop = new Properties();
			prop.load(new FileInputStream(currentPath));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static synchronized configService getInstance() {
		if (instance == null) {
			instance = new configService();
		}
		return instance;
	}

	public String getProp(String p) {
		return prop.getProperty(p);
	}

}
