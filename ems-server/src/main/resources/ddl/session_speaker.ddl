create table session_speaker (
  sessionId       varchar(255) not null,
  personId        varchar(255) not null,
  position        integer not null,
  description     long varchar,
  tags            long varchar,
  photo           varchar(255),
  primary key(sessionId, personId, position),
  foreign key (sessionId) references session(id),
  foreign key (personId) references person(id)
)
