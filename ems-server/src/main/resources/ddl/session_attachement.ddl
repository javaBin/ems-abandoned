create table session_attachement (
  sessionId       varchar(255) not null,
  attachementId   varchar(255) not null,
  position        integer not null,
  primary key(sessionId, attachementId, position),
  foreign key (sessionId) references session(id)
)
