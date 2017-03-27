package models;

public class DateForCalendar {
    private String patientName;
    private String appointmentDate;
    private String appointmentId;

    public DateForCalendar(String patientName, String appointmentDate, String appointmentId){
        this.setPatientName(patientName);
        this.setAppointmentDate(appointmentDate);
        this.setAppointmentId(appointmentId);
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
}
