create table session (
  id              varchar(255) not null,
  revision        integer not null,
  title           varchar(255),
  start           timestamp,
  durationMinutes integer,
  state           varchar(50),
  roomId          varchar(255),
  level           varchar(50),
  format          varchar(50),
  tags            long varchar,
  keywords        long varchar,
  language        varchar(8),
  eventId         varchar(255),
  lead            long varchar,
  body            long varchar,
  notes           long varchar,
  outline         long varchar,
  expected        long varchar,
  equipment       long varchar,
  feedback        long varchar,
  published       char(1),
  constraint session_pk primary key(id, revision),
  constraint session_fk_event foreign key(eventId) references event(id),
  constraint session_fk_room foreign key(roomId) references room(id)
)
