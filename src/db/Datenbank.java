package db;

import javax.swing.plaf.nimbus.State;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by KNapret on 04.03.2016.
 */
public class Datenbank {
    private static Datenbank datenbank;

    private static Connection conn;

    private Datenbank() {

    }

    public static Datenbank getInstance() throws ClassNotFoundException, SQLException {

        if (datenbank == null) {

            //Datenbanktreiber laden
            Class.forName("org.postgresql.Driver");
            datenbank = new Datenbank();
        }

        //verbindung aufbauen

        boolean renew = conn == null;

        if (!renew)

            try {
                if (conn.isClosed())
                    renew = true;
            } catch (SQLException e) {
                renew = true;
            }

        if (renew) {
            String host = "localhost";
            int port = 5432;
            String database = "test";


            try {
                Socket socket = new Socket(host, port);
                socket.close();
            } catch (IOException e) {
                throw new SQLException(
                        "Server nicht erreichbar!", "08001", e);
            }


            String url = "jdbc:postgresql://" + host + ":" + port + "/";
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "root");


            try {
                conn = DriverManager.getConnection(url, props);
                conn.close();
            } catch (SQLException e) {
                throw new SQLException(
                        "Zugriff verweigert!", e.getSQLState(), e);
            }

            try {
                conn = DriverManager.getConnection(url + database, props);

            } catch (SQLException e) {
                throw new SQLException(
                        "Datenbank nicht gefunden!", e.getSQLState(), e);
            }

        }


        return datenbank;
    }

    public static Datenbank getInstance(String dbName) throws ClassNotFoundException, SQLException{

        String host = "localhost";
        int port = 5432;

        String url = "jdbc:postgresql://"+host+":"+port+"/";
        Properties props = new Properties();
        props.setProperty("user","postgres");
        props.setProperty("password", "root");

        try{
            conn = DriverManager.getConnection(url,props);
            conn.createStatement().executeUpdate(
                    "CREATE DATABASE "+dbName);
            conn.close();
        }
        catch (SQLException e){
            throw  new SQLException("Zugriff verweigert", e.getSQLState(),e);
        }
        try{
            getInstance();
        }catch(ClassNotFoundException e){}

        einlesenScript();

        return datenbank;

    }

    public void createTable(String tableName) throws SQLException {

        //try catch hier eigentlich nicht, da man sicherstellen sollte,
        //dass die benötigte Tabelle exakt so existiert, wie sie benötigt wird
        //ggf. kann die SQLExeption genau untersucht werden...

        Statement stmt = conn.createStatement();

        try {
            stmt.executeUpdate(

                    "CREATE TABLE " + tableName + " (" +
                            "name VARCHAR," +
                            "number INT," +
                            "PRIMARY KEY  (name)" +
                            ")"

            );
        } catch (SQLException e) {


        }


    }

    private static void einlesenScript(){
        try(BufferedReader br = new BufferedReader(
                new FileReader(
                        Datenbank.class.getResource("init.sql").getFile()))) {


            String sqlInstruction ="";
            String zeile;
            while ((zeile = br.readLine())!=null){
                zeile = zeile.split("--")[0];
                sqlInstruction += zeile+" ";
                if(sqlInstruction.trim().endsWith(";")){
                    Statement stmt = conn.createStatement();
                    stmt.execute(sqlInstruction);
                }

            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ;


    }

    /**
     * Diese Methode löscht eine Tabelle, falls sie
     * existiert, andernfalls tut sie nichts.
     *
     * @param tablename Der Name der Tabelle, die
     *                  gelöscht werden soll.
     * @throws SQLException Wenn beim Erstellen der
     *                      Verbindung ein Fehler auftritt.
     */

    public void dropIfExists(String tablename) throws SQLException {

        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate(
                    "DROP TABLE " + tablename

            );
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void insertOrUpdate(String name, int number) throws SQLException {

        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate(
                    "INSERT INTO test VALUES ('" + name + "', " + number + ")"
            );
        } catch (SQLException e) {

            stmt.executeUpdate(
                    "UPDATE test" +
                            " SET number=" + number +
                            " WHERE name='" + name + "'"
            );
        }

    }

    public void printTable(String tableName) throws SQLException {

        Statement stmt = conn.createStatement();
        try {
            ResultSet r = stmt.executeQuery(
                    "SELECT name, number FROM " + tableName
            );

            ResultSetMetaData rm = r.getMetaData();
            // Tabellenkopf -> Spaltennamen
            int col = rm.getColumnCount();
            int[] max = new int[col];

            List<List<String>> tabelle = new ArrayList<>(col);

            for (int i = 0; i < col; i++) {
                List<String> spalte = new ArrayList<>();
                String s = rm.getColumnLabel(i + 1).toUpperCase();
                spalte.add(s);
                tabelle.add(spalte);
                max[i] = s.length();
            }

            //Tabelleneinträge

            if (r.next()) {
                do {
                    for (int i = 0; i < col; i++) {
                        String s = r.getString(i + 1);
                        if (s == null) s = "null";
                        tabelle.get(i).add(s);
                        max[i] = Math.max(max[i], s.length());
                    }

                } while (r.next());
                r.close();
                //Datenausgabe:

                for (int i = 0; i < tabelle.get(0).size(); i++) {
                    String s = "";
                    for (int j = 0; j < col; j++)
                        s += "|" + String.format("%-" + max[j] + "s",
                                tabelle.get(j).get(i));
                    System.out.println(s + "|");
                }

            }


        } catch (SQLException e) {
            System.out.println("Abfrage nicht möglich!");
        }


    }

    public void createTableMitBlob(String tableName) throws SQLException {
        //Binärdateien wie Bilder, musik etc


        Statement stmt = conn.createStatement();

        try {
            stmt.executeUpdate(

                    "CREATE TABLE " + tableName + " (" +
                            "name VARCHAR," +
                            "blob BYTEA," +
                            "PRIMARY KEY  (name)," +
                            "FOREIGN KEY  (name) REFERENCES test(name)" +
                            ")"

            );
        } catch (SQLException e) {


        }

    }

    public void insertOrUpdateBlob(String name, FileInputStream fis) throws SQLException {

        Statement stmt = conn.createStatement();
        ResultSet r = stmt.executeQuery(

                "SELECT name "+
                        "FROM blob"+
                        " WHERE name='"+name+"'"


        );

        if(r.next()){
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE blob" +
                            " SET blob = ?" +
                            " WHERE name='" + name + "'");

            ps.setBinaryStream(1, fis);
            ps.executeUpdate();
        }else {

            PreparedStatement ps = conn.prepareStatement(

                    "INSERT INTO blob VALUES (?,?)"

            );


                ps.setString(1, name); //zahlen beziehen sich auf die ? im preparedStatement
                ps.setBinaryStream(2, fis);
                ps.executeUpdate();


        }



    }

    public InputStream getBlob(String name) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rS = stmt.executeQuery(

                "SELECT blob" +
                        " FROM blob" +
                        " WHERE name='" + name + "'"
        );

        if (rS.next())
            return rS.getBinaryStream(1);

        //bei mehreren erwarteten Ergebnissen ist doWhile notwendig
        return null;
    }
}
