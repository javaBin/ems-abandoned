create table event_timeslot (
  eventId         varchar(255) not null,
  position        integer not null,
  start           timestamp not null,
  durationMinutes integer not null,
  primary key(eventId, position),
  foreign key (eventId) references event(id)
)
