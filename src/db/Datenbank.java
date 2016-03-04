package db;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by KNapret on 04.03.2016.
 */
public class Datenbank {
    private static Datenbank datenbank;

    private static Connection conn;

    private Datenbank(){

    }

    public static Datenbank getInstance() throws ClassNotFoundException, SQLException {

        if(datenbank==null){

            //Datenbanktreiber laden
            Class.forName("org.postgresql.Driver");
            datenbank = new Datenbank();
        }

        //verbindung aufbauen

        boolean renew = conn == null;

        if(!renew)

            try {
                if(conn.isClosed())
                    renew = true;
            } catch (SQLException e){
                renew = true;
            }

        if(renew){
            String host = "localhost";
            int port = 5432;
            String database = "test";


            try {
                Socket socket = new Socket(host,port);
                socket.close();
            } catch (IOException e) {
                throw new SQLException(
                        "Server nicht erreichbar!", "08001",e);
            }


            String url = "jdbc:postgresql://"+host+":"+port+"/";
            Properties props = new Properties();
            props.setProperty("user","postgres");
            props.setProperty("password","root");


            try{
                conn = DriverManager.getConnection(url,props);
                conn.close();
            }catch (SQLException e){
                throw new SQLException(
                        "Zugriff verweigert!",e.getSQLState(),e);
            }

            try{
                conn = DriverManager.getConnection(url + database,props);

            }catch (SQLException e){
                throw new SQLException(
                        "Datenbank nicht gefunden!",e.getSQLState(),e);
            }

        }








        return datenbank;
    }

    public void createTable(String tableName) throws SQLException {

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(

                "CREATE TABLE "+tableName+" ("+
                        "name VARCHAR,"+
                        "number INT,"+
                        "PRIMARY KEY  (name)"+
                        ")"

        );


    }
}
