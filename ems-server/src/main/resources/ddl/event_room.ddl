create table event_room (
  eventId         varchar(255) not null,
  roomId          varchar(255) not null,
  position        integer not null,
  primary key(eventId, roomId),
  foreign key (eventId) references event(id),
  foreign key (roomId) references room(id)
)
