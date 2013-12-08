create table users(
	id serial primary key,
	name varchar(255) not null,
	password varchar(255) not null,
	nickname varchar(255) not null,
	secret_question varchar(255) not null,
	secret_answer varchar(255) not null,
	community_id integer not null,
	subscription_end timestamp not null,
	constraint users_name_unique unique(name)
);