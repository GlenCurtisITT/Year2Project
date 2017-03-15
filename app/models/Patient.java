package models;

import javax.persistence.*;
import com.avaje.ebean.Model;
import models.users.Consultant;
import play.data.format.Formats;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.io.Serializable;

/**
 * Created by wdd on 03/03/17.
 */
@Entity
public class Patient extends Model implements Serializable{
    @Id
    private String mrn;
    private String fName;
    private String lName;
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
    private String prevIllnesses;

    @ManyToOne()    //signifies relationship with Consultant table
    @JoinColumn(name = "idNum")    //name of column which links tables
    Consultant c;

    @OneToMany(mappedBy = "p")
    private List<Appointment> appointments = new ArrayList<>();

    public Patient() {

    }

    public Patient(String fname, String lname, String ppsNumber, Date dob, String address, String email, String homePhone, String mobilePhone, String nokFName, String nokLName, String nokAddress, String nokNumber, boolean medicalCard, String prevIllness) {
        this.mrn = genMrn();
        this.fName = fname;
        this.lName = lname;
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
        this.prevIllnesses = prevIllness;
        this.c = null;
    }

    public static Patient create(String fname, String lname, String ppsNumber, Date dob, String address, String email, String homePhone, String mobilePhone, String nokFName, String nokLName, String nokAddress, String nokNumber, boolean medicalCard, String prevIllness){
        Patient patient = new Patient(fname, lname, ppsNumber, dob, address, email, homePhone, mobilePhone, nokFName, nokLName, nokAddress, nokNumber, medicalCard, prevIllness);
        patient.save();
        return patient;
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
        }while(!check);
        String numberAsString = Integer.toString(randNum);
        return numberAsString;
    }

    public String getMrn() {
        return mrn;
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

    public String getPrevIllnesses() {
        return prevIllnesses;
    }

    public void setPrevIllnesses(String prevIllnesses) {
        this.prevIllnesses = prevIllnesses;
    }
}
