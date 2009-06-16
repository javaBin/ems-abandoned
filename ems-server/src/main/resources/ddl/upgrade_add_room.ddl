connect 'jdbc:derby:/Users/yngvars/.ems/database/ems';

create table room (
  id              varchar(255) not null,
  revision        integer not null,
  name            varchar(255) not null,
  description     varchar(255),
  primary key(id)
);

create table event_room (
  eventId         varchar(255) not null,
  roomId          varchar(255) not null,
  position        integer not null,
  primary key(eventId, roomId),
  foreign key (eventId) references event(id),
  foreign key (roomId) references room(id)
);

create table event_timeslot (
  eventId         varchar(255) not null,
  position        integer not null,
  start           timestamp not null,
  durationMinutes integer not null,
  primary key(eventId, position),
  foreign key (eventId) references event(id)
);

alter table session add roomId varchar(255);
alter table session add foreign key(roomId) references room(id);

alter table session add start timestamp;
alter table session add durationMinutes integer;
alter table session add published char(1);
alter table session drop sessiondate;

insert into room values ('jz06-1', 0, 'Scandinavia scene', null);
insert into room values ('jz06-2', 0, 'Stockholm', null);
insert into room values ('jz06-3', 0, 'K?benhavn', null);
insert into room values ('jz06-4', 0, 'Oslo', null);
insert into room values ('jz06-5', 0, 'Telemar', null);
insert into room values ('jz06-6', 0, 'Lofoten', null);
insert into room values ('jz06-7', 0, 'Brasseriet', null);
insert into room values ('jz06-8', 0, 'Helsingfors', null);
insert into room values ('jz06-9', 0, 'Helsingfors/K?benhavn', null);

insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-1', 0);
insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-2', 1);
insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-3', 2);
insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-4', 3);
insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-5', 4);
insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-6', 5);
insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-7', 6);
insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-8', 7);
insert into event_room values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 'jz06-9', 8);

update session set state = 'Approved' where eventId='1ca89aef-c7ce-45b5-95a0-630f55d7efa6';

insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 0, '2006-09-13 09:00:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 1, '2006-09-13 10:15:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 2, '2006-09-13 11:45:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 3, '2006-09-13 13:00:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 4, '2006-09-13 14:15:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 5, '2006-09-13 15:45:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 6, '2006-09-13 17:00:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 7, '2006-09-13 18:15:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 8, '2006-09-13 19:30:00', 60);

insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6', 9, '2006-09-14 09:00:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6',10, '2006-09-14 10:15:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6',11, '2006-09-14 11:45:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6',12, '2006-09-14 13:00:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6',13, '2006-09-14 14:15:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6',14, '2006-09-14 15:45:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6',15, '2006-09-14 17:00:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6',16, '2006-09-14 18:15:00', 60);
insert into event_timeslot values ('1ca89aef-c7ce-45b5-95a0-630f55d7efa6',17, '2006-09-14 19:30:00', 60);

-- SELECT 'update session set roomid=''' || ROOMID || ''', start=''' || START || ''', DURATIONMINUTES=0 where id=''' || id || ''';' FROM session where roomid is not null order by start

update session set roomid='jz06-1', start='2006-09-13 09:00:00.0', DURATIONMINUTES=0 where id='8cb26329-ee60-4f66-bd2a-ce1d2f9f65e4';
update session set roomid='jz06-2', start='2006-09-13 09:00:00.0', DURATIONMINUTES=0 where id='dfe24873-dc0d-44e2-bf4b-59598cedf851';
update session set roomid='jz06-2', start='2006-09-13 10:15:00.0', DURATIONMINUTES=0 where id='9ca1a054-7282-4cc3-9949-20708919cb24';
update session set roomid='jz06-1', start='2006-09-13 10:15:00.0', DURATIONMINUTES=0 where id='cbe45a57-c5a8-4668-8267-89b2c153fb1b';
update session set roomid='jz06-1', start='2006-09-13 11:45:00.0', DURATIONMINUTES=0 where id='f85d99df-2cbf-4c48-96bc-1e542220da0f';
update session set roomid='jz06-2', start='2006-09-13 11:45:00.0', DURATIONMINUTES=0 where id='1b0230dc-c3e6-4830-a5c8-060e1fb0af60';
update session set roomid='jz06-4', start='2006-09-13 11:45:00.0', DURATIONMINUTES=0 where id='af2f3022-5324-408a-941e-4021b4eaea14';
update session set roomid='jz06-2', start='2006-09-13 13:00:00.0', DURATIONMINUTES=0 where id='742f12b1-0fba-4fef-8d80-0e4718d7242f';
update session set roomid='jz06-1', start='2006-09-13 13:00:00.0', DURATIONMINUTES=0 where id='763828a2-ce92-4acd-94ea-b33860311693';
update session set roomid='jz06-2', start='2006-09-13 14:15:00.0', DURATIONMINUTES=0 where id='04b43e47-46ab-47f3-bc13-5f41c98d2ff5';
update session set roomid='jz06-1', start='2006-09-13 15:45:00.0', DURATIONMINUTES=0 where id='a68d52a7-9eeb-4daf-b085-f99d38011637';
update session set roomid='jz06-2', start='2006-09-13 15:45:00.0', DURATIONMINUTES=0 where id='e2a5cfbc-c722-4852-b873-3f9f59dd7f46';
update session set roomid='jz06-2', start='2006-09-13 17:00:00.0', DURATIONMINUTES=0 where id='ca897007-b91b-4d58-af53-9172a2da6e9e';
update session set roomid='jz06-1', start='2006-09-13 17:00:00.0', DURATIONMINUTES=0 where id='5c94ed52-3aa2-4bfb-a504-7025c48981cd';
update session set roomid='jz06-1', start='2006-09-13 18:15:00.0', DURATIONMINUTES=0 where id='846ac0b8-19e3-43c1-8512-7b2706b3118c';
update session set roomid='jz06-2', start='2006-09-13 18:15:00.0', DURATIONMINUTES=0 where id='595a210b-9474-44d5-9f32-16a1afaa153b';
update session set roomid='jz06-2', start='2006-09-13 19:30:00.0', DURATIONMINUTES=0 where id='e15dc80b-2e68-465a-af30-a65b04076117';
update session set roomid='jz06-1', start='2006-09-14 09:00:00.0', DURATIONMINUTES=0 where id='ec9af933-ecf0-41f0-b47a-dbf5308924c2';
update session set roomid='jz06-1', start='2006-09-14 10:15:00.0', DURATIONMINUTES=0 where id='6ed6453d-48fa-40fa-8fdf-3b7449d81db8';
update session set roomid='jz06-1', start='2006-09-14 11:45:00.0', DURATIONMINUTES=0 where id='a8cd3d55-4b34-4c97-8d08-7f3785f84eed';
update session set roomid='jz06-1', start='2006-09-14 13:00:00.0', DURATIONMINUTES=0 where id='8cd846a4-2e1b-4618-b50b-2c8dac7fb3e5';
update session set roomid='jz06-1', start='2006-09-14 14:15:00.0', DURATIONMINUTES=0 where id='ddde6847-4a89-48da-b287-38f4c62968b2';
update session set roomid='jz06-9', start='2006-09-14 17:00:00.0', DURATIONMINUTES=0 where id='1e6b95ed-49d7-499e-b921-dcab189a0909';
update session set roomid='jz06-1', start='2006-09-14 17:00:00.0', DURATIONMINUTES=0 where id='19b146ac-0d50-4c04-ac2f-a71dc8ff9422';

insert into room values('jz08-1', 0, 'Lab 1', '');
insert into room values('jz08-2', 0, 'Lab 2', '');
insert into room values('jz08-3', 0, 'Lab 3', '');
insert into room values('jz08-4', 0, 'Lab 4', '');
insert into room values('jz08-5', 0, 'Lab 5', '');
insert into room values('jz08-6', 0, 'Lab 6', '');

insert into event_room values ('5e130372-285d-49cd-aedb-7f306d97b04d', 'jz08-1', 0);
insert into event_room values ('5e130372-285d-49cd-aedb-7f306d97b04d', 'jz08-2', 1);
insert into event_room values ('5e130372-285d-49cd-aedb-7f306d97b04d', 'jz08-3', 2);
insert into event_room values ('5e130372-285d-49cd-aedb-7f306d97b04d', 'jz08-4', 3);
insert into event_room values ('5e130372-285d-49cd-aedb-7f306d97b04d', 'jz08-5', 4);
insert into event_room values ('5e130372-285d-49cd-aedb-7f306d97b04d', 'jz08-6', 5);

insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 0, '2008-09-17 09:00:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 1, '2008-09-17 10:15:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 2, '2008-09-17 11:45:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 3, '2008-09-17 13:00:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 4, '2008-09-17 14:15:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 5, '2008-09-17 15:45:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 6, '2008-09-17 17:00:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 7, '2008-09-17 18:15:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 8, '2008-09-17 19:30:00', 60);

insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d', 9, '2008-09-18 09:00:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d',10, '2008-09-18 10:15:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d',11, '2008-09-18 11:45:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d',12, '2008-09-18 13:00:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d',13, '2008-09-18 14:15:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d',14, '2008-09-18 15:45:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d',15, '2008-09-18 17:00:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d',16, '2008-09-18 18:15:00', 60);
insert into event_timeslot values ('5e130372-285d-49cd-aedb-7f306d97b04d',17, '2008-09-18 19:30:00', 60);

-- Randomly assign room ids to sessions:
-- update session set roomId = 'jz08-' || cast(mod(locate('a', id), 6) + 1 as char(2)) where eventId='5e130372-285d-49cd-aedb-7f306d97b04d';

update session set start='2008-09-17 00:00:00.0', DURATIONMINUTES=60 where eventId='5e130372-285d-49cd-aedb-7f306d97b04d' and start is null;
