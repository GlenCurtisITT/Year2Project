# --- Sample dataset

# --- !Ups

insert into patient (mrn,f_name,l_name,gender,pps_number, dob, address, email, home_phone, mobile_phone, nok_fname, nok_lname, nok_address, nok_number, medical_card, illness, idnum, wardId, standbyid)
values ( '11111211','John','Jacobs', true,'454545','2001-07-08 00:00:00', '21 Cairnhill Grove', 'john@jacobs.ie', '0149494949', '0854545455', 'Cary', 'Laurie', '21 Cairnhill Grove', '045456116',true, 'Abdominal Pains', null, null, null);

insert into chart (chart_id, current_ward, date_of_admittance, discharge_date, meal_plan, mrn, billid)
values ('3000', null, null, null, null, '11111211', null);

insert into patient (mrn,f_name,l_name,gender,pps_number, dob, address, email, home_phone, mobile_phone, nok_fname, nok_lname, nok_address, nok_number, medical_card, illness, idnum, wardId, standbyid)
values ( '22222221','Jean','Janson', false,'4545','1990-09-01 00:00:00', '21 Keephill Grove', 'jean@janson.ie', '01494963', '0854545499', 'Cary', 'Laurie', '21 Keephill Grove', '045456116',false, 'Pregnancy', null, null, null);

insert into chart (chart_id, current_ward, date_of_admittance, discharge_date, meal_plan, mrn, billid)
values ('3001', null, null, null, null, '22222221', null);