create table person_attachement (
  personId        varchar(255) not null,
  attachementId   varchar(255) not null,
position          integer not null,
  primary key(personId, attachementId, position),
  foreign key (personId) references person(id)
)
