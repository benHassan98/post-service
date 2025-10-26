create database odinbook;

create table if not exists accounts(
id integer primary key GENERATED ALWAYS AS IDENTITY,
picture_id varchar(280),
fullname varchar(50) not null,
username varchar(50) unique not null,
email varchar(50) unique not null,
password varchar(280) not null,
is_verified boolean,
about_me text,
created_date timestamp
);
-- index on id includes(picture_id, fullname, username)


create table if not exists posts(
id integer primary key GENERATED ALWAYS AS IDENTITY,
account_id integer not null,
content text,
shared_from_posts integer[],
content_history text[],
update_time_history timestamp[],
is_deleted boolean,
created_date timestamp,
foreign key(account_id) references accounts(id) on delete cascade
);

create table if not exists subscriptions(
account_id integer not null,
subscriber_id integer not null,
foreign key(account_id) references accounts(id) on delete cascade,
foreign key(subscriber_id) references accounts(id) on delete cascade
);

create table if not exists followers(
follower_id integer not null,
followee_id integer not null,
foreign key(follower_id) references accounts(id) on delete cascade,
foreign key(followee_id) references accounts(id) on delete cascade
);


create table if not exists friends(
id1 integer not null,
id2 integer not null,
foreign key(id1) references accounts(id) on delete cascade,
foreign key(id2) references accounts(id) on delete cascade
);


create table if not exists likes(
account_id integer not null,
post_id integer not null,
foreign key(account_id) references accounts(id) on delete cascade,
foreign key(post_id) references posts(id) on delete cascade
);


create table if not exists tokens(
id integer primary key GENERATED ALWAYS AS IDENTITY,
type varchar(60) not null,
email varchar(60) not null,
code varchar(300) not null,
created_date timestamp
);


create table if not exists comments(
id integer primary key GENERATED ALWAYS AS IDENTITY,
account_id integer not null,
post_id integer not null,
content text not null,
is_edited boolean,
created_date timestamp,
foreign key(account_id) references accounts(id) on delete cascade,
foreign key(post_id) references posts(id) on delete cascade
);

create table if not exists notifications(
id integer primary key GENERATED ALWAYS AS IDENTITY,
type varchar(50) not null,
account_id integer,
adding_id integer,
added_id integer,
receiver_id integer,
post_id integer,
comment_id integer,
is_request boolean,
is_accepted boolean,
is_created boolean,
is_viewed boolean,
created_date timestamp,
foreign key(account_id) references accounts(id) on delete cascade,
foreign key(adding_id) references accounts(id) on delete cascade,
foreign key(added_id) references accounts(id) on delete cascade,
foreign key(receiver_id) references accounts(id) on delete cascade,
foreign key(post_id) references posts(id) on delete cascade,
foreign key(comment_id) references comments(id) on delete cascade

);

create table if not exists messages(
id integer primary key GENERATED ALWAYS AS IDENTITY,
sender_id integer not null,
receiver_id integer not null,
content text,
is_viewed boolean,
is_deleted boolean,
created_date timestamp,
foreign key(sender_id) references accounts(id) on delete cascade,
foreign key(receiver_id) references accounts(id) on delete cascade
);

CREATE FUNCTION clean_post()
RETURNS TRIGGER AS '
BEGIN
    DELETE FROM comments WHERE OLD.id = post_id;
    DELETE FROM likes WHERE OLD.id = post_id;
    DELETE FROM notifications WHERE OLD.id = post_id;
    RETURN NEW;
END;
' LANGUAGE plpgsql;



CREATE TRIGGER clean_post_trigger
BEFORE UPDATE ON posts
FOR EACH ROW
WHEN (NEW.is_deleted = true AND OLD.is_deleted = false)
EXECUTE FUNCTION clean_post();

commit;
