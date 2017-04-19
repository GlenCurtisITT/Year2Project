package services;

import models.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by conno on 15/04/2017.
 */
public class Serializer {

    public static void serialize(Object object) throws IOException{
        final String FILE = "public/Files/" + object.getClass().getName().substring(7).toLowerCase() + "s.gz";
        try(FileOutputStream fo = new FileOutputStream(FILE);
            GZIPOutputStream gzipOut = new GZIPOutputStream(new BufferedOutputStream(fo));
            ObjectOutputStream oo = new ObjectOutputStream(gzipOut);){
            oo.writeObject(object);
        }
    }

    public static List<Appointment> readAppointmentArchive(String recordId){
        final String FILE = "public/Files/appointments.gz";
        List<Appointment> result = new ArrayList<>();
        Appointment appointment = null;
        try (FileInputStream fin = new FileInputStream(FILE);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                appointment = (Appointment) ois.readObject();
                if(appointment.getPatientRecord().getRecordId().equals(recordId)){
                    result.add(appointment);
                    appointment.insert();
                }
            }
        }catch (ClassNotFoundException e) {
            result = null;
        }catch (EOFException e) {
            return result;
        }catch (IOException e) {
            result = null;
        }
        return result;
    }

    public static List<Prescription> readPrescriptionArchive(String mrn){
        final String PRESCRIPTIONFILE = "public/Files/prescriptions.gz";
        List<Prescription> prescriptionResult = new ArrayList<>();
        Prescription pres = null;
        try (FileInputStream fin = new FileInputStream(PRESCRIPTIONFILE);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                pres = (Prescription) ois.readObject();
                if(pres.getPatient().getMrn().equals(mrn)){
                    prescriptionResult.add(pres);
                    pres.insert();
                }
            }
        }catch (ClassNotFoundException e) {
            prescriptionResult = null;
        }catch (EOFException e) {
            return prescriptionResult;
        }catch (IOException e) {
            prescriptionResult = null;
        }
        return prescriptionResult;
    }

    public static List<Chart> readChartArchive(String mrn, String recordId){
        final String CHARTFILE = "public/Files/charts.gz";
        List<Chart> chartResult = new ArrayList<>();
        Chart c = null;
        try (FileInputStream fin = new FileInputStream(CHARTFILE);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                c = (Chart) ois.readObject();
                chartResult.add(c);
            }
        }catch (ClassNotFoundException e) {
            chartResult = null;
        }catch (EOFException e) {
            for(Chart chart : chartResult){
                if(chart.getP() != null){
                    if(!chart.getP().getMrn().equals(mrn)){
                        chartResult.remove(chart);
                    }
                }
                else{
                    if(!chart.getPatientRecord().getRecordId().equals(recordId)){
                        chartResult.remove(chart);
                    }
                }
            }
        }catch (IOException e) {
            chartResult = null;
        }
        return chartResult;
    }

    public static Patient readPatientArchive(String mrn){
        final String FILENAME = "public/Files/patients.gz";
        final String BILLFILE = "public/Files/bills.gz";
        Bill b = new Bill();
        Patient p = new Patient();
        Patient patientResult = null;
        try (FileInputStream fin = new FileInputStream(FILENAME);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                p = (Patient) ois.readObject();
                if(p.getMrn().equals(mrn)){
                    patientResult = p;
                    try (FileInputStream in = new FileInputStream(BILLFILE);
                         GZIPInputStream is = new GZIPInputStream(in);
                         ObjectInputStream ins = new ObjectInputStream(new BufferedInputStream(is))){{
                             b = (Bill) ins.readObject();
                             if(b.getP().getMrn().equals(mrn)){
                                 b.insert();
                                 patientResult.insert();
                             }
                    }}catch (IOException e) {
                    }
                    return patientResult;
                }
            }
        }catch (ClassNotFoundException e) {
            patientResult = null;
        }catch (IOException e) {
            patientResult = null;
        }
        return null;
    }

    public static PatientRecord readPatientRecordArchive(String recordId){
        final String FILENAME = "public/Files/PatientRecords.gz";
        PatientRecord p = null;
        PatientRecord patientResult = null;
        try (FileInputStream fin = new FileInputStream(FILENAME);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                p = (PatientRecord) ois.readObject();
                if(p.getRecordId().equals(recordId)){
                    patientResult = p;
                    patientResult.insert();
                    return patientResult;
                }
            }
        }catch (ClassNotFoundException e) {
            patientResult = null;
        }catch (IOException e) {
            patientResult = null;
        }
        return null;
    }

}
