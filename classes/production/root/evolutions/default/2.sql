# --- Sample dataset

# --- !Ups

insert into bill (bill_id, amount, is_Paid) values ('1002', 0, false);

insert into bill (bill_id, amount, is_Paid) values ('1003', 0, false);

insert into patient (mrn,f_name,l_name,gender,pps_number, dob, address, email, home_phone, mobile_phone, nok_fname, nok_lname, nok_address, nok_number, medical_card, illness, idnum, wardId, standbyid, billid)
values ( '11111215','John','Jacobs', true,'45454','2001-07-08 00:00:00', '21 Cairnhill Grove', 'john@jacobs.ie', '0149494949', '0854545455', 'Cary', 'Laurie', '21 Cairnhill Grove', '045456116',true, 'Abdominal Pains', null, null, null, '1002');

insert into chart (chart_id, current_ward, date_of_admittance, discharge_date, meal_plan, mrn)
values ('3004', null, null, null, null, '11111215');


insert into patient (mrn,f_name,l_name,gender,pps_number, dob, address, email, home_phone, mobile_phone, nok_fname, nok_lname, nok_address, nok_number, medical_card, illness, idnum, wardId, standbyid, billid)
values ( '22222222','Jean','Janson', false,'4545','1990-09-01 00:00:00', '21 Keephill Grove', 'jean@janson.ie', '01494963', '0854545499', 'Cary', 'Laurie', '21 Keephill Grove', '045456116',false, 'Pregnancy', null, null, null, '1003');

insert into chart (chart_id, current_ward, date_of_admittance, discharge_date, meal_plan, mrn)
values ('3003', null, null, null, null, '22222222');