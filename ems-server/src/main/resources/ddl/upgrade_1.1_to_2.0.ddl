alter table event add lastModified timestamp default CURRENT_TIMESTAMP;
alter table event add modifiedBy varchar(255) not null default 'unknown';
alter table session add lastModified timestamp default CURRENT_TIMESTAMP;
alter table session add modifiedBy varchar(255) not null default 'unknown';
alter table person add lastModified timestamp default CURRENT_TIMESTAMP;
alter table person add modifiedBy varchar(255) not null default 'unknown';
alter table room add lastModified timestamp default CURRENT_TIMESTAMP;
alter table room add modifiedBy varchar(255) not null default 'unknown';