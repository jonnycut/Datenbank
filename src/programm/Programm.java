package programm;

import db.Datenbank;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.BufferPoolMXBean;
import java.sql.SQLException;

/**
 * Created by KNapret on 04.03.2016.
 */
public class Programm {
    Datenbank db = null;
    public Programm(){



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
            db.insertOrUpdate("Jochen", 12419864);



            db.printTable("test");

            bildSpeichern("Napret","bild.jpg");
            bildSpeichern("Fleuren","fuYou.jpg");

            bildAnzeigen("Napret");

            bildAnzeigen("Fleuren");

            bildSpeichern("Fleuren","bild.jpg");

            bildAnzeigen("Fleuren");







        } catch (SQLException e){
            e.printStackTrace();
        }


    }

    public void bildAnzeigen(String name) throws SQLException {

        try {
            Icon icon = new ImageIcon(
                    ImageIO.read(db.getBlob(name))
            );
            JOptionPane.showMessageDialog(
                    null, "Tolles Bild aus DB",
                    "Bildanzeige",
                    JOptionPane.INFORMATION_MESSAGE,
                    icon
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bildSpeichern(String name, String file) throws SQLException{

        try {
            FileInputStream fis = new FileInputStream(file);
            db.insertOrUpdateBlob(name,fis);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
