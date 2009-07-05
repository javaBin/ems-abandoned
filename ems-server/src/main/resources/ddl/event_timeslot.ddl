create table event_timeslot (
  eventId         varchar(255) not null,
  position        integer not null,
  start           timestamp not null,
  durationMinutes integer not null,
  constraint event_timeslot_pk primary key(eventId, position),
  constraint event_timeslot_pk foreign key (eventId) references event(id)
)
