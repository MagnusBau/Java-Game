drop table chrctr;
drop table monster;
drop table creature_weapon;
drop table creature;
drop table weapon;
drop table player;
drop table initiative;
drop table chat_message;
drop table password;
drop table usr;
drop table game_lobby;


-- create tables

create table creature(
    creature_id integer not null,
    HP smallint,
    AC smallint,
    attack_bonus smallint,
    movement smallint,
    attack_turn smallint,
    damage_bonus smallint,
    pos_x integer default 0,
    pos_y integer default 0,
    creature_lv integer default 1,
    constraint creature_pk primary key(creature_id));

create table chrctr(
    character_id integer not null references creature(creature_id),
    character_name varchar(20),
    back_story text,
    constraint chrctr_pk primary key(character_id));

create table monster(
    monster_id integer not null references creature(creature_id),
    monster_name varchar(20),
    monster_lv smallint default 0,
    constraint monster_pk primary key(monster_id));

create table weapon(
    weapon_id integer not null,
    weapon_name varchar(20),
    damage_dice smallint default 0,
    dice_amount smallint default 0,
    description varchar(20),
    constraint weapon_pk primary key(weapon_id));

create table creature_weapon(
    weapon_id integer not null,
    creature_id integer not null default 0,
    constraint creature_weapon_pk primary key(weapon_id, creature_id));

create table usr(
    user_id integer not null auto_increment,
    username varchar(30),
    rank integer default 0,
    lobby_key integer,
    constraint user_pk primary key(user_id));

create table password(
    user_id integer not null references usr(user_id),
    salt_pass char(64) not null,
    hash_pass text not null,
    constraint password_pk primary key(user_id));

create table player(
    player_id integer not null,
    player_lv integer,
    constraint player_pk primary key(player_id));

create table initiative(
    lobby_key integer not null references game_lobby(lobby_key),
    user_id integer not null references usr(user_id),
    initiative_turn smallint,
    constraint initiative_pk primary key(lobby_key, user_id));

create table game_lobby(
    lobby_key integer not null auto_increment,
    player_turn smallint,
    constraint game_lobby_pk primary key(lobby_key));

create table chat_message(
    lobby_key integer not null references game_lobby(lobby_key),
    message_id integer not null,
    user_id integer not null references usr(user_id),
    message text NOT NULL,
    time_stamp time NOT NULL,
    constraint chat_message_pk primary key(lobby_key, message_id));


    -- Foreign keys

    ALTER TABLE chat_message ADD CONSTRAINT chat_message_fk1 FOREIGN KEY(lobby_key) REFERENCES game_lobby(lobby_key);
    ALTER TABLE chat_message ADD CONSTRAINT chat_message_fk2 FOREIGN KEY(user_id) REFERENCES usr(user_id);

    ALTER TABLE usr ADD CONSTRAINT usr_fk FOREIGN KEY(lobby_key) REFERENCES game_lobby(lobby_key);