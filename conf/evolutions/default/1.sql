# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table appointment (
  id                            varchar(255) not null,
  app_date                      timestamp,
  complete                      boolean,
  recordid                      varchar(255),
  mrn                           varchar(255),
  idnum                         varchar(255),
  equipid                       varchar(255),
  constraint pk_appointment primary key (id)
);
create sequence appointment_seq increment by 1;

create table bill (
  bill_id                       varchar(255) not null,
  amount                        double,
  is_paid                       boolean,
  constraint pk_bill primary key (bill_id)
);
create sequence bill_seq increment by 1;

create table chart (
  chart_id                      integer not null,
  current_ward                  varchar(255),
  date_of_admittance            timestamp,
  discharge_date                timestamp,
  meal_plan                     varchar(255),
  mrn                           varchar(255),
  recordid                      varchar(255),
  constraint pk_chart primary key (chart_id)
);
create sequence chart_seq increment by 1;

create table equipment (
  equip_id                      varchar(255) not null,
  type                          varchar(255),
  functional                    boolean,
  constraint pk_equipment primary key (equip_id)
);
create sequence equipment_seq start with 4 increment by 1;

create table medicine (
  medicine_id                   varchar(255) not null,
  name                          varchar(255),
  side_affects                  varchar(255),
  ingredients                   varchar(255),
  price_per_unit                double,
  unit_of_measurement           varchar(255),
  constraint pk_medicine primary key (medicine_id)
);
create sequence medicine_seq increment by 1;

create table patient (
  mrn                           varchar(255) not null,
  f_name                        varchar(255),
  l_name                        varchar(255),
  gender                        boolean,
  pps_number                    varchar(255),
  dob                           timestamp,
  address                       varchar(255),
  email                         varchar(255),
  home_phone                    varchar(255),
  mobile_phone                  varchar(255),
  nok_fname                     varchar(255),
  nok_lname                     varchar(255),
  nok_address                   varchar(255),
  nok_number                    varchar(255),
  medical_card                  boolean,
  illness                       varchar(255),
  idnum                         varchar(255),
  wardid                        varchar(255),
  standbyid                     varchar(255),
  billid                        varchar(255),
  constraint uq_patient_billid unique (billid),
  constraint pk_patient primary key (mrn)
);

create table patient_record (
  record_id                     varchar(255) not null,
  mrn                           varchar(255),
  constraint uq_patient_record_mrn unique (mrn),
  constraint pk_patient_record primary key (record_id)
);
create sequence patient_record_seq increment by 1;

create table prescription (
  prescription_id               varchar(255) not null,
  frequency                     varchar(255),
  dosage                        integer,
  paid                          boolean,
  medicineid                    varchar(255),
  mrn                           varchar(255),
  constraint pk_prescription primary key (prescription_id)
);
create sequence prescription_seq increment by 1;

create table standby_list (
  standby_id                    varchar(255) not null,
  current_occupancy             integer,
  wardid                        varchar(255),
  constraint uq_standby_list_wardid unique (wardid),
  constraint pk_standby_list primary key (standby_id)
);
create sequence standby_list_seq increment by 1;

create table user (
  role                          varchar(31) not null,
  id_num                        varchar(255) not null,
  fname                         varchar(255),
  lname                         varchar(255),
  phone_number                  varchar(255),
  address                       varchar(255),
  pps_number                    varchar(255),
  date_of_birth                 timestamp,
  start_date                    timestamp,
  email                         varchar(255),
  password_hash                 varchar(255),
  specialization                varchar(255),
  constraint pk_user primary key (id_num)
);

create table ward (
  ward_id                       varchar(255) not null,
  name                          varchar(255),
  max_capacity                  integer,
  current_occupancy             integer,
  status                        boolean,
  constraint pk_ward primary key (ward_id)
);
create sequence ward_seq increment by 1;

alter table appointment add constraint fk_appointment_recordid foreign key (recordid) references patient_record (record_id) on delete restrict on update restrict;
create index ix_appointment_recordid on appointment (recordid);

alter table appointment add constraint fk_appointment_mrn foreign key (mrn) references patient (mrn) on delete restrict on update restrict;
create index ix_appointment_mrn on appointment (mrn);

alter table appointment add constraint fk_appointment_idnum foreign key (idnum) references user (id_num) on delete restrict on update restrict;
create index ix_appointment_idnum on appointment (idnum);

alter table appointment add constraint fk_appointment_equipid foreign key (equipid) references equipment (equip_id) on delete restrict on update restrict;
create index ix_appointment_equipid on appointment (equipid);

alter table chart add constraint fk_chart_mrn foreign key (mrn) references patient (mrn) on delete restrict on update restrict;
create index ix_chart_mrn on chart (mrn);

alter table chart add constraint fk_chart_recordid foreign key (recordid) references patient_record (record_id) on delete restrict on update restrict;
create index ix_chart_recordid on chart (recordid);

alter table patient add constraint fk_patient_idnum foreign key (idnum) references user (id_num) on delete restrict on update restrict;
create index ix_patient_idnum on patient (idnum);

alter table patient add constraint fk_patient_wardid foreign key (wardid) references ward (ward_id) on delete restrict on update restrict;
create index ix_patient_wardid on patient (wardid);

alter table patient add constraint fk_patient_standbyid foreign key (standbyid) references standby_list (standby_id) on delete restrict on update restrict;
create index ix_patient_standbyid on patient (standbyid);

alter table patient add constraint fk_patient_billid foreign key (billid) references bill (bill_id) on delete restrict on update restrict;

alter table patient_record add constraint fk_patient_record_mrn foreign key (mrn) references patient (mrn) on delete restrict on update restrict;

alter table prescription add constraint fk_prescription_medicineid foreign key (medicineid) references medicine (medicine_id) on delete restrict on update restrict;
create index ix_prescription_medicineid on prescription (medicineid);

alter table prescription add constraint fk_prescription_mrn foreign key (mrn) references patient (mrn) on delete restrict on update restrict;
create index ix_prescription_mrn on prescription (mrn);

alter table standby_list add constraint fk_standby_list_wardid foreign key (wardid) references ward (ward_id) on delete restrict on update restrict;


# --- !Downs

alter table appointment drop constraint if exists fk_appointment_recordid;
drop index if exists ix_appointment_recordid;

alter table appointment drop constraint if exists fk_appointment_mrn;
drop index if exists ix_appointment_mrn;

alter table appointment drop constraint if exists fk_appointment_idnum;
drop index if exists ix_appointment_idnum;

alter table appointment drop constraint if exists fk_appointment_equipid;
drop index if exists ix_appointment_equipid;

alter table chart drop constraint if exists fk_chart_mrn;
drop index if exists ix_chart_mrn;

alter table chart drop constraint if exists fk_chart_recordid;
drop index if exists ix_chart_recordid;

alter table patient drop constraint if exists fk_patient_idnum;
drop index if exists ix_patient_idnum;

alter table patient drop constraint if exists fk_patient_wardid;
drop index if exists ix_patient_wardid;

alter table patient drop constraint if exists fk_patient_standbyid;
drop index if exists ix_patient_standbyid;

alter table patient drop constraint if exists fk_patient_billid;

alter table patient_record drop constraint if exists fk_patient_record_mrn;

alter table prescription drop constraint if exists fk_prescription_medicineid;
drop index if exists ix_prescription_medicineid;

alter table prescription drop constraint if exists fk_prescription_mrn;
drop index if exists ix_prescription_mrn;

alter table standby_list drop constraint if exists fk_standby_list_wardid;

drop table if exists appointment;
drop sequence if exists appointment_seq;

drop table if exists bill;
drop sequence if exists bill_seq;

drop table if exists chart;
drop sequence if exists chart_seq;

drop table if exists equipment;
drop sequence if exists equipment_seq;

drop table if exists medicine;
drop sequence if exists medicine_seq;

drop table if exists patient;

drop table if exists patient_record;
drop sequence if exists patient_record_seq;

drop table if exists prescription;
drop sequence if exists prescription_seq;

drop table if exists standby_list;
drop sequence if exists standby_list_seq;

drop table if exists user;

drop table if exists ward;
drop sequence if exists ward_seq;

