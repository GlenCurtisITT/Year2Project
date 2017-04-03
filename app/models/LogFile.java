package models;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

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

    public static List<String> readAllLogEntries(){
        File inFile = new File(fileLocation);
        ArrayList<String> logEntries = new ArrayList<>();
        try(Scanner in = new Scanner(inFile)){
            while(in.hasNextLine()){
                logEntries.add(in.nextLine());
            }
        }catch (IOException e){

        }
        return logEntries;
    }

    public static List<String> readTodaysLogEntries(){
        File inFile = new File(fileLocation);
        ArrayList<String> logEntries = new ArrayList<>();
        //Get the current date.
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String todaysDate = dateFormat.format(date);

        try(Scanner in = new Scanner(inFile)){
            while(in.hasNextLine()){
                String s = in.nextLine();
                //Get the date from the current log entry.
                String lineDate = s.substring(1, 11);
                //Check if date in log entry is the same as current date.
                if(lineDate.equals(todaysDate)){
                    //If it is the same add to array list.
                    logEntries.add(s);
                }
            }
        }catch (IOException e){

        }
        return logEntries;
    }

    public static void deleteLogFile(){
        File inFile = new File(fileLocation);
        inFile.delete();
    }
}
