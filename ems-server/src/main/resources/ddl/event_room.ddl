create table event_room (
  eventId         varchar(255) not null,
  roomId          varchar(255) not null,
  position        integer not null,
  constraint event_room_pk primary key(eventId, roomId),
  constraint event_room_fk_event foreign key (eventId) references event(id),
  constraint event_room_fk_room foreign key (roomId) references room(id)
)
