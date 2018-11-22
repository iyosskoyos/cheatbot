package com.chatbot.translate;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {
    private static final String HOST = "ec2-54-197-249-140.compute-1.amazonaws.com";
    private static final String USERNAME = "qsrokubscletbs";
    private static final String PASSWORD = "a8c2f03d1c14ca1291b5e6cf38f3f8a4a551bb32cc5895f4a2c085ade07726aa";

//    private static Connection getConnection() throws URISyntaxException, SQLException {
//        URI dbUri = new URI(System.getenv("DATABASE_URL"));
//
//        String username = dbUri.getUserInfo().split(":")[0];
//        String password = dbUri.getUserInfo().split(":")[1];
//        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
//
//        return DriverManager.getConnection(dbUrl, username, password);
//    }

//        private static Connection getConnection() throws  SQLException {
//            try{
//                Class.forName("org.postgresql.Driver");
//            }catch (ClassNotFoundException ex){
//                System.out.println("Error");
//                return null;
//            }
//            Connection connection = DriverManager.getConnection("jdbc:postgresql://" + HOST + "?sslmode=require", USERNAME, PASSWORD);
//           // System.out.println("berhasil");
//            return  connection;
//    }

    private static Connection getConnection() throws URISyntaxException, SQLException {
        Connection connection=null;

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://ec2-54-197-249-140.compute-1.amazonaws.com:5432/dap2hgf18m4g59", "qsrokubscletbs", "a8c2f03d1c14ca1291b5e6cf38f3f8a4a551bb32cc5895f4a2c085ade07726aa");
            System.out.println("Java JDBC PostgreSQL Example");
            // When this class first attempts to establish a connection, it automatically loads any JDBC 4.0 drivers found within
            // the class path. Note that your application must manually load any JDBC drivers prior to version 4.0.
//			Class.forName("org.postgresql.Driver");

            System.out.println("Connected to PostgreSQL database!");
            return connection;
        } /*catch (ClassNotFoundException e) {
			System.out.println("PostgreSQL JDBC driver not found.");
			e.printStackTrace();
		}*/ catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
        String dbUrl = "postgres://qsrokubscletbs:a8c2f03d1c14ca1291b5e6cf38f3f8a4a551bb32cc5895f4a2c085ade07726aa@ec2-54-197-249-140.compute-1.amazonaws.com:5432/dap2hgf18m4g59";

        String username ="qsrokubscletbs";

        String password="a8c2f03d1c14ca1291b5e6cf38f3f8a4a551bb32cc5895f4a2c085ade07726aa";

        return connection;
    }

    public static void main(String[] args) throws SQLException{
            Connection connection = null;
            try {
                connection = getConnection();
            }catch (SQLException ex){
                ex.printStackTrace();
                System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
                System.out.println("Connection Failed!");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        if (connection != null){
                System.out.println("You made it!");
            }else{
                System.out.println("Failed!");
            }
    }


}


