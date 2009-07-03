create table event_attachement (
  eventId         varchar(255) not null,
  attachementId   varchar(255) not null,
  position        integer not null,
  constraint event_attachment_pk primary key(eventId, attachementId, position),
  constraint event_attachment_fk_event foreign key(eventId) references event(id)
)
