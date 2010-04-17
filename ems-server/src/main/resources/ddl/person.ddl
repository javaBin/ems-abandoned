create table person (
  id              varchar(255) not null,
  revision        integer not null,
  tags            long varchar,
  name            varchar (255),
  description     long varchar,
  notes           long varchar,
  gender          varchar(6),
  birthdate       date,
  language        varchar(8),
  nationality     varchar(2),
  addresses       long varchar,
  photo           long varchar, -- URI of the photo
  lastModified    timestamp default CURRENT_TIMESTAMP,
  modifiedBy varchar(255) default 'unknown',
  primary key(id)
)
