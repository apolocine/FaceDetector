package org.hmd.face.emp; 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
 
public class DatabaseHelper {
    private Connection connection;

    
    /**
     * 
     * 
    CREATE TABLE IF NOT EXISTS faces (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    features MEDIUMBLOB
);


     * 
     * 
     */
    public DatabaseHelper() {
        // Établir la connexion avec la base de données SQLite
//        String url = "jdbc:sqlite:path_to_your_db.db";
        String url = "jdbc:mysql://localhost:3306/dbfacedetector";//Config.getDatabaseURL();
        String user = "root";//Config.getDatabaseUser();
        String password  = "";//Config.getDatabasePassword();

        
		 
        try {
//            connection = DriverManager.getConnection(url);
//        	   connection = DatabaseManager.getConnection();
        	 connection = DriverManager.getConnection(url, user, password);
         
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean insertFace(String name, byte[] features) {
        String sql = "INSERT INTO faces(name, features) VALUES(?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setBytes(2, features);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Ajouter d'autres méthodes au besoin pour interagir avec la base de données

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
