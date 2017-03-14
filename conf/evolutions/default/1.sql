# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table appointment (
  id                            varchar(255) not null,
  app_date                      timestamp,
  mrn                           varchar(255),
  idnum                         varchar(255),
  constraint pk_appointment primary key (id)
);
create sequence appointment_seq increment by 1;

create table patient (
  mrn                           varchar(255) not null,
  f_name                        varchar(255),
  l_name                        varchar(255),
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

alter table appointment add constraint fk_appointment_mrn foreign key (mrn) references patient (mrn) on delete restrict on update restrict;
create index ix_appointment_mrn on appointment (mrn);

alter table appointment add constraint fk_appointment_idnum foreign key (idnum) references user (id_num) on delete restrict on update restrict;
create index ix_appointment_idnum on appointment (idnum);

alter table patient add constraint fk_patient_idnum foreign key (idnum) references user (id_num) on delete restrict on update restrict;
create index ix_patient_idnum on patient (idnum);


# --- !Downs

alter table appointment drop constraint if exists fk_appointment_mrn;
drop index if exists ix_appointment_mrn;

alter table appointment drop constraint if exists fk_appointment_idnum;
drop index if exists ix_appointment_idnum;

alter table patient drop constraint if exists fk_patient_idnum;
drop index if exists ix_patient_idnum;

drop table if exists appointment;
drop sequence if exists appointment_seq;

drop table if exists patient;

drop table if exists user;

