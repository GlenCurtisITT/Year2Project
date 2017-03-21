# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table appointment (
  id                            varchar(255) not null,
  app_date                      timestamp,
  mrn                           varchar(255),
  idnum                         varchar(255),
  equipid                       varchar(255),
  constraint pk_appointment primary key (id)
);
create sequence appointment_seq increment by 1;

create table chart (
  chart_id                      integer not null,
  current_ward                  varchar(255),
  date_of_admittance            timestamp,
  discharge_date                timestamp,
  meal_plan                     varchar(255),
  mrn                           varchar(255),
  constraint uq_chart_mrn unique (mrn),
  constraint pk_chart primary key (chart_id)
);
create sequence chart_seq increment by 1;

create table equipment (
  equip_id                      varchar(255) not null,
  type                          varchar(255),
  status                        boolean,
  constraint pk_equipment primary key (equip_id)
);

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
  prev_illnesses                varchar(255),
  idnum                         varchar(255),
  wardid                        varchar(255),
  constraint pk_patient primary key (mrn)
);

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
  current_capacity              integer,
  constraint pk_ward primary key (ward_id)
);

alter table appointment add constraint fk_appointment_mrn foreign key (mrn) references patient (mrn) on delete restrict on update restrict;
create index ix_appointment_mrn on appointment (mrn);

alter table appointment add constraint fk_appointment_idnum foreign key (idnum) references user (id_num) on delete restrict on update restrict;
create index ix_appointment_idnum on appointment (idnum);

alter table appointment add constraint fk_appointment_equipid foreign key (equipid) references equipment (equip_id) on delete restrict on update restrict;
create index ix_appointment_equipid on appointment (equipid);

alter table chart add constraint fk_chart_mrn foreign key (mrn) references patient (mrn) on delete restrict on update restrict;

alter table patient add constraint fk_patient_idnum foreign key (idnum) references user (id_num) on delete restrict on update restrict;
create index ix_patient_idnum on patient (idnum);

alter table patient add constraint fk_patient_wardid foreign key (wardid) references ward (ward_id) on delete restrict on update restrict;
create index ix_patient_wardid on patient (wardid);


# --- !Downs

alter table appointment drop constraint if exists fk_appointment_mrn;
drop index if exists ix_appointment_mrn;

alter table appointment drop constraint if exists fk_appointment_idnum;
drop index if exists ix_appointment_idnum;

alter table appointment drop constraint if exists fk_appointment_equipid;
drop index if exists ix_appointment_equipid;

alter table chart drop constraint if exists fk_chart_mrn;

alter table patient drop constraint if exists fk_patient_idnum;
drop index if exists ix_patient_idnum;

alter table patient drop constraint if exists fk_patient_wardid;
drop index if exists ix_patient_wardid;

drop table if exists appointment;
drop sequence if exists appointment_seq;

drop table if exists chart;
drop sequence if exists chart_seq;

drop table if exists equipment;

drop table if exists patient;

drop table if exists user;

drop table if exists ward;

