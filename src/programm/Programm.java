package programm;

import db.Datenbank;

import java.sql.SQLException;

/**
 * Created by KNapret on 04.03.2016.
 */
public class Programm {

    public Programm(){

        Datenbank db = null;

        try {
            db =
                    Datenbank.getInstance();
        } catch (ClassNotFoundException e) {
            System.out.println("Datenbanktreiber nicht gefunden");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        try{
            //db.dropIfExists("test");
            db.createTable("test");
            db.createTableMitBlob("blob");


            db.insertOrUpdate("Napret", 1234);
            db.insertOrUpdate("Fleuren", 4561);
            db.insertOrUpdate("Kevin", 6789);
            db.insertOrUpdate("Jochen",12419864);

            db.printTable("test");




        } catch (SQLException e){
            e.printStackTrace();
        }


    }
}
