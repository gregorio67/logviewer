package dymn.log.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.ibatis.io.Resources;


public class PropertiesUtil {
	
	private static Properties properties;
	private static Object sync = new Object();
	
	private static final String DEFAULT_PROPERTY_LOCATION = "properties/log.properties";

	public static void loadProperties() throws Exception {
		
		InputStream inputStream = null;
		if (properties == null) {
			synchronized(sync) {
				if (System.getenv("properties.location") != null) {
					inputStream = Resources.getResourceAsStream(System.getenv("properties.location"));
				}
				else {
					inputStream = Resources.getResourceAsStream(DEFAULT_PROPERTY_LOCATION);
				}
				properties = new Properties();
				properties.load(inputStream);				
			}
		}
	}

	public static String getString(String key) throws Exception {
		loadProperties();
		return properties.getProperty(key) != null ? String.valueOf(properties.getProperty(key)) : "";
	}

	public static int getInt(String key) throws Exception {
		loadProperties();
		return properties.getProperty(key) != null ? Integer.parseInt(String.valueOf(properties.getProperty(key))) : 0;
	}

	public static long getLong(String key) throws Exception {
		loadProperties();
		return properties.getProperty(key) != null ? Long.parseLong(String.valueOf(properties.getProperty(key))) : 0L;
	}

}
