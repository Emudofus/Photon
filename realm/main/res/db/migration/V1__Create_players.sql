create table players(
	id serial primary key,
	owner_id integer not null,
	name varchar(255) not null,
	breed integer not null,
	gender boolean not null,
	level smallint not null,
	skin smallint not null,
	color1 integer not null,
	color2 integer not null,
	color3 integer not null,
	constraint players_name_unique unique(name)
);