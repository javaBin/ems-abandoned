create table event_attachement (
  eventId         varchar(255) not null,
  attachementId   varchar(255) not null,
  position        integer not null,
  primary key(eventId, attachementId, position),
  foreign key (eventId) references event(id)
)
