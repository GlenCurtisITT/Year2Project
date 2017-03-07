# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

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
  constraint pk_patient primary key (mrn)
);
create sequence patient_seq increment by 1;

create table user (
  email                         varchar(255) not null,
  fname                         varchar(255),
  lname                         varchar(255),
  phone_number                  varchar(255),
  address                       varchar(255),
  pps_number                    varchar(255),
  date_of_birth                 timestamp,
  start_date                    timestamp,
  role                          varchar(255),
  password_hash                 varchar(255),
  constraint pk_user primary key (email)
);


# --- !Downs

drop table if exists patient;
drop sequence if exists patient_seq;

drop table if exists user;

