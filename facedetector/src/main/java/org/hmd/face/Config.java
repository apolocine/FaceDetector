package org.hmd.face;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {

	private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

	private static final String CONFIG_FILE = "files" + File.separator + "config" + File.separator
			+ "config.properties";
	private static Properties properties;

	static {
		properties = new Properties();
		addDefaultProperties();
		// APPROACH 1.
//    		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		// APPROACH 2.
//	        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		// APPROACH 3.
//    		ClassLoader classLoader = Config.getClass().getClassLoader();
		try (
//	        		InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
				InputStream inputStream = new FileInputStream(CONFIG_FILE)

		// APPROACH 1.
//	        		InputStream inputStream = Config.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);

		// APPROACH 2.
//	        		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//	        		InputStream inputStream = classLoader.getResourceAsStream(CONFIG_FILE)
//
//	        		// APPROACH 3.
//	        		ClassLoader classLoader = getClass().getClassLoader();
//	        		InputStream inputStream = classLoader.getResourceAsStream(CONFIG_FILE);

		) {

			properties.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Config(String configFileName) {
		if (properties == null) {
			properties = new Properties();
		}
		loadProperties(configFileName);
	}

	public Config() {
		if (properties == null) {
			properties = new Properties();
		}
		loadProperties(CONFIG_FILE);
	}

	private void loadProperties(String configFileName) {

		try (
				// InputStream input =
				// AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)
				InputStream input = new FileInputStream(configFileName)) {
			properties.load(input);
		} catch (FileNotFoundException e) {
			// Si le fichier n'existe pas, ajoutez des propriétés génériques
			addDefaultProperties();
		} catch (IOException e) {
			e.printStackTrace();
			// Gérer les erreurs de lecture du fichier de configuration
		}
	}

	private static void addDefaultProperties() {
		// Ajoutez des propriétés génériques
		properties.setProperty("directory", "C:\\Users\\DELL\\Documents\\0APng");

		properties.setProperty("db.sgbd", "jdbc:mysql://localhost:3383/");

		properties.setProperty("db.url", "jdbc:mysql://localhost:3383/dbfacedetector");
		properties.setProperty("db.user", "root");
		properties.setProperty("db.password", "");

//        properties.setProperty("db.url", "jdbc:h2:./data/test;DB_CLOSE_DELAY=-1");
//        properties.setProperty("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
//        properties.setProperty("db.user", "sa");
//        properties.setProperty("db.password", "");

		properties.setProperty("db.name", "dbfacedetector");
		properties.setProperty("db.tb.name", "tb_utilisateur");

		properties.setProperty("username", "drmdh@msn.com");
		properties.setProperty("password", "azery@26");
		properties.setProperty("medecinUtilisateur", "Dr Hamid MADANI");
		properties.setProperty("adresse.cabinet", "\\ Cit\\u00E9 50 Logements Mesra Mostaganem");

		properties.setProperty("main-class", "org.amia.playground.PrinterManager");

		properties.setProperty("login-main-class", "org.amia.play.ihm.LoginFormMain");

		properties.setProperty("sql-file-path", "files\\install.sql");
		properties.setProperty("sql-dump-file", "files\\dumpfile.sql");
		properties.setProperty("barcodes-directory", "files\\barcodes\\");
		properties.setProperty("numbers-directory", "files\\numbers\\");
		properties.setProperty("amia-url", "http://localhost:8083/amia");
		properties.setProperty("paterne-extracteid", "_(\\d+)\\.");
		properties.setProperty("php-scriptpath", "/tickets/script.php");

		properties.setProperty("photos-directorypath", "files/photos/aquired/");

		// "files" + File.separator + "photos" + File.separator + "search" +File.separator + "photoSRC.png";
		properties.setProperty("search-photos-path", "files/photos/search/photoSRC.png");

		File file = new File(CONFIG_FILE);

		// ne pas forcer l'initialisation du fichier s'il existe déja
		// Sauvegardez les propriétés dans le fichier
		if (!file.exists()) {
			saveProperties();
		}

	}

	public static void saveProperties() {
		try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
			properties.store(output, null);
		} catch (IOException ex) {
			ex.printStackTrace();
			// Gérer les erreurs de sauvegarde du fichier de configuration
		}
	}

	// Méthodes pour obtenir et définir des propriétés
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static void setProperty(String key, String value) {
		properties.setProperty(key, value);
		saveProperties();
	}

	// Ajoutez la méthode pour obtenir l'objet Properties
	public static Properties getProperties() {
		return properties;
	}

	public static String getDatabaseTestURL() {
		return properties.getProperty("db.url.test");
	}

	public static String getDatabaseURL() {
		return properties.getProperty("db.url");
	}

	public static String getDatabaseUser() {
		return properties.getProperty("db.user");
	}

	public static String getDatabasePassword() {
		return properties.getProperty("db.password");
	}

	public static String getDatabaseName() {
		return properties.getProperty("db.name");
	}

	public static String getDatabaseUserTablename() {
		return properties.getProperty("db.tb.name");
	}

	// Ajoutez d'autres méthodes selon les besoins, par exemple pour parcourir les
	// propriétés existantes
	public static String getSGBDURL() {
		// TODO Auto-generated method stub
		return properties.getProperty("db.sgbd");
	}

	public static String getRooyPath() {
		// TODO Auto-generated method stub
		return properties.getProperty("directory");
	}

	public static String getNameMainClazz() {
		// TODO Auto-generated method stub
		return properties.getProperty("main-class");
	}

	public static String getSqlFilePathToInstall() {
		// TODO Auto-generated method stub
		return properties.getProperty("sql-file-path");
	}

	public static String getSqlDumpFilePath() {
		// TODO Auto-generated method stub
		return properties.getProperty("sql-dump-file");
	}

	// org.amia.play.ihm.LoginFormMain
	public static String getLoginClassNameMainClazz() {

		return properties.getProperty("login-main-class");
	}

	public static String getBarcodesDirectory() {
		// TODO Auto-generated method stub

		return properties.getProperty("barcodes-directory");
	}

	public static String getNumberssDirectory() {
		// TODO Auto-generated method stub

		return properties.getProperty("numbers-directory");
	}

	public static String getBaseAmiaUrl() {
		// TODO Auto-generated method stub

		return properties.getProperty("amia-url");
	}

	public static String getPaterneExtracteID() {

		return properties.getProperty("paterne-extracteid");
	}

	public static String getPhpScriptPath() {
		return properties.getProperty("php-scriptpath");
	}

	public static String getPhotosDirectoryPath() {
//		"files" + File.separator + "photos" + File.separator + "aquired" + File.separator;

		return properties.getProperty("photos-directorypath");
	}

	public static String getSearchPhotoPath() {
		// "files" + File.separator + "photos" + File.separator + "search" +
		// File.separator + "photoSRC.png";
		return properties.getProperty("search-photos-path");
	}

}