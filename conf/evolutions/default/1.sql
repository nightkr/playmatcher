# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "games" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254) NOT NULL,"STEAM_APPID" BIGINT,"icon" VARCHAR(254));
create unique index "NAME_INDEX" on "games" ("NAME");
create unique index "STEAM_APPID_INDEX" on "games" ("STEAM_APPID");
create table "matchable_properties" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"LABEL" VARCHAR(254) NOT NULL);
create table "matchable_property_choices" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"LABEL" VARCHAR(254) NOT NULL,"VALUE" INTEGER NOT NULL,"PROPERTY_ID" BIGINT NOT NULL);
create table "user_games" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"USER_ID" BIGINT NOT NULL,"GAME_ID" BIGINT NOT NULL,"ENABLED" BOOLEAN NOT NULL);
create table "users" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254),"STEAMID" BIGINT,"BANNED" BOOLEAN NOT NULL);
alter table "matchable_property_choices" add constraint "PROPERTY_FK" foreign key("PROPERTY_ID") references "matchable_properties"("id") on update NO ACTION on delete NO ACTION;
alter table "user_games" add constraint "GAME_FK" foreign key("GAME_ID") references "games"("id") on update NO ACTION on delete NO ACTION;
alter table "user_games" add constraint "USER_FK" foreign key("USER_ID") references "users"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "user_games" drop constraint "GAME_FK";
alter table "user_games" drop constraint "USER_FK";
alter table "matchable_property_choices" drop constraint "PROPERTY_FK";
drop table "users";
drop table "user_games";
drop table "matchable_property_choices";
drop table "matchable_properties";
drop table "games";

