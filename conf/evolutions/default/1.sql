# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table user (
  email                         varchar(255) not null,
  role                          varchar(255),
  name                          varchar(255),
  password_hash                 varchar(255),
  constraint pk_user primary key (email)
);


# --- !Downs

drop table if exists user;

