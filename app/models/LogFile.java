package models;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFile {
    private static final String fileLocation = "public/Files/log.txt";

    public static void writeToLog(String s){
        try(FileWriter fw = new FileWriter(fileLocation, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            //Get the current date and time.
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String stringDate = "[" + dateFormat.format(date) + "]" + " - ";
            out.println(stringDate + s);

        }catch(IOException e){

        }
    }
}
