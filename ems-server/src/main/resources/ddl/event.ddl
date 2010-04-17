create table event (
  id              varchar(255) not null,
  revision        integer not null,
  name            varchar(255),
  startdate       date default CURRENT_DATE,
  enddate         date default CURRENT_DATE,
  tags            long varchar,
  notes           long varchar,
  lastModified    timestamp default CURRENT_TIMESTAMP,
  modifiedBy varchar(255) default 'unknown',
  primary key(id)
)
