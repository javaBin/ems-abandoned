create table room (
  id              varchar(255) not null,
  revision        integer not null,
  name            varchar(255) not null,
  description     varchar(255),
  primary key(id)
)
