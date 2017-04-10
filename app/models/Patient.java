package models;

import javax.persistence.*;
import com.avaje.ebean.Model;
import models.users.Consultant;
import play.data.format.Formats;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by wdd on 03/03/17.
 */
@Entity
public class Patient extends Model implements Serializable{
    @Id
    private String mrn;
    private String fName;
    private String lName;
    private Boolean gender;
    private String ppsNumber;
    @Formats.DateTime(pattern="yyyy/dd/MM")
    private Date dob;
    private String address;
    private String email;
    private String homePhone;
    private String mobilePhone;
    private String nokFName;
    private String nokLName;
    private String nokAddress;
    private String nokNumber;
    private Boolean medicalCard;
    private String illness;

    @ManyToOne()    //signifies relationship with Consultant table
    @JoinColumn(name = "idNum")    //name of column which links tables
    private Consultant c;

    @ManyToOne()    //signifies relationship with Ward table
    @JoinColumn(name = "wardId")    //name of column which links tables
    private Ward ward;

    @ManyToOne()
    @JoinColumn(name = "standbyId")
    private StandbyList sl;

    @OneToMany(mappedBy = "p")
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "p")
    private List<Chart> charts = new ArrayList<>();

    @OneToMany (mappedBy = "patient")
    private List<Prescription> prescriptionList = new ArrayList<>();

    public Patient() {

    }

    public Patient(String fname, String lname, Boolean gender, String ppsNumber, Date dob, String address, String email, String homePhone, String mobilePhone, String nokFName, String nokLName, String nokAddress, String nokNumber, boolean medicalCard, String prevIllness) {
        this.mrn = genMrn();
        this.fName = fname;
        this.lName = lname;
        this.gender = gender;
        this.ppsNumber = ppsNumber;
        this.dob = dob;
        this.address = address;
        this.email = email;
        this.homePhone = homePhone;
        this.mobilePhone = mobilePhone;
        this.nokFName = nokFName;
        this.nokLName = nokLName;
        this.nokAddress = nokAddress;
        this.nokNumber = nokNumber;
        this.medicalCard = medicalCard;
        this.illness = prevIllness;
        this.c = null;

    }

    public static Patient create(String fname, String lname, Boolean gender, String ppsNumber, Date dob, String address, String email, String homePhone, String mobilePhone, String nokFName, String nokLName, String nokAddress, String nokNumber, boolean medicalCard, String prevIllness){
        Patient patient = new Patient(fname, lname, gender, ppsNumber, dob, address, email, homePhone, mobilePhone, nokFName, nokLName, nokAddress, nokNumber, medicalCard, prevIllness);
        Chart chart = new Chart(patient);
        patient.setChart(chart);
        patient.save();
        chart.save();
        return patient;
    }

    public void assignConsultant(Consultant c){
        this.c = c;
        c.addPatient(this);
        this.save();
        c.save();
    }

    public Consultant getC() {
        return c;
    }

    public void popAppointments(){
        appointments.clear();
        List<Appointment> appoints = Appointment.findAll();
        for(Appointment a: appoints){
            if(a.getP().getMrn().equals(this.getMrn())){
                appointments.add(a);
            }
        }
    }

    public String getFormattedDOB(Date a){
        return new SimpleDateFormat("dd MMM yyyy").format(a);
    }

    public String getFormattedDOBForUpdate(Date a){
        return new SimpleDateFormat("yyyy-dd-MM").format(a);
    }

    public static Finder<String, Patient> find = new Finder<String, Patient>(Patient.class);

    public static List<Patient> findAll(){
        return Patient.find.all();
    }

    public static Patient getPatientById(String id){
        if(id == null)
            return null;
        else
            return find.byId(id);
    }

    private static String genMrn(){
        Random rand = new Random();
        List<Patient> allpatients = findAll();
        List<Patient> archivedPatients = Patient.readAllArchive();
        int randNum = 0;
        boolean check = true;
        do{
            randNum = rand.nextInt((99999999 - 10000001) + 1) + 10000001;
            check = true;
            for(Patient a: allpatients){
                if(a.find.byId(Integer.toString(randNum)) != null) {
                    check = false;
                }
            }
            if(archivedPatients != null) {
                for (Patient a : archivedPatients) {
                    if (a.getMrn() == (Integer.toString(randNum))) {
                        check = false;
                    }
                }
            }
        }while(!check);
        String numberAsString = Integer.toString(randNum);
        return numberAsString;
    }

    public void serialize() throws IOException {
        final String FILENAME = "public/Files/patients.gz";
        try(FileOutputStream fo = new FileOutputStream(FILENAME);
            GZIPOutputStream gzipOut = new GZIPOutputStream(new BufferedOutputStream(fo));
            ObjectOutputStream oo = new ObjectOutputStream(gzipOut);) {
            oo.writeObject(this);
        }
    }

    public static Patient readArchive(String mrn){
        final String FILENAME = "public/Files/patients.gz";
        Patient p = new Patient();
        Patient patientResult = null;
        try (FileInputStream fin = new FileInputStream(FILENAME);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                p = (Patient) ois.readObject();
                if(p.getMrn().equals(mrn)){
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

    public static List<Patient> readAllArchive(){
        final String FILENAME = "public/Files/patients.gz";
        List<Patient> patients = new ArrayList<>();
        try (FileInputStream fin = new FileInputStream(FILENAME);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                patients.add((Patient)ois.readObject());
            }
        }catch (EOFException e) {
            return patients;
        }catch (ClassNotFoundException e) {
            patients = null;
        }catch (IOException e) {
            patients = null;
        }
        return patients;
    }

    public List<Prescription> getPrescriptionList() {
        return prescriptionList;
    }

    public void setPrescriptionList(List<Prescription> p){
        prescriptionList = p;
    }

    public void setPrescription(Prescription p) {
        this.prescriptionList.add(p);
    }

    public void removeWard(){
        this.ward = null;
    }

    public StandbyList getSl() {
        return sl;
    }

    public void setSl(StandbyList sl) {
        this.sl = sl;
    }

    public void setMrn(String mrn) {
        this.mrn = mrn;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public Chart getCurrentChart() {
        return charts.get(charts.size() - 1);
    }

    public Chart getBillingChart() {
        List<Chart> billingCharts = charts.stream().filter(c -> c.getDateOfAdmittance() != null).collect(Collectors.toList());

        if(billingCharts.size() != 0) {
            final Iterator<Chart> itr = billingCharts.iterator();
            Chart lastElement = itr.next();

            while (itr.hasNext()) {
                lastElement = itr.next();
            }

            return lastElement;
        }
        else{
            return getCurrentChart();
        }
    }

    public List<Chart> getCharts(){
        return charts;
    }

    public void setChart(Chart chart) {
        this.charts.add(chart);
    }

    public void setChartList(List<Chart> charts){
        this.charts = charts;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public Ward getWard() {
        return ward;
    }

    public String getMrn() {
        return mrn;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fname) {
        this.fName = fname;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lname) {
        this.lName = lname;
    }

    public String getPpsNumber() {
        return ppsNumber;
    }

    public void setPpsNumber(String ppsNumber) {
        this.ppsNumber = ppsNumber;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getNokFName() {
        return nokFName;
    }

    public void setNokFName(String nokFName) {
        this.nokFName = nokFName;
    }

    public String getNokLName() {
        return nokLName;
    }

    public void setNokLName(String nokLName) {
        this.nokLName = nokLName;
    }

    public String getNokAddress() {
        return nokAddress;
    }

    public void setNokAddress(String nokAddress) {
        this.nokAddress = nokAddress;
    }

    public String getNokNumber() {
        return nokNumber;
    }

    public void setNokNumber(String nokNumber) {
        this.nokNumber = nokNumber;
    }

    public Boolean getMedicalCard() {
        return medicalCard;
    }

    public void setMedicalCard(Boolean medicalCard) {
        this.medicalCard = medicalCard;
    }

    public String getIllness() {
        return illness;
    }

    public void setIllness(String illness) {
        this.illness = illness;
    }
}
