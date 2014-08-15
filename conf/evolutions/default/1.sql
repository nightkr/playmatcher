# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "GAMES" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254) NOT NULL,"STEAM_APPID" BIGINT,"icon" VARCHAR(254));
create unique index "NAME_INDEX" on "GAMES" ("NAME");
create unique index "STEAM_APPID_INDEX" on "GAMES" ("STEAM_APPID");
create table "IDENTITIES" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"USER_ID" BIGINT NOT NULL,"KIND" VARCHAR(254) NOT NULL,"VALUE" VARCHAR(254) NOT NULL);
create unique index "KIND_VALUE_INDEX" on "IDENTITIES" ("KIND","VALUE");
create unique index "USER_KIND_INDEX" on "IDENTITIES" ("USER_ID","KIND");
create table "USERS" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"BANNED" BOOLEAN NOT NULL);
create table "USER_GAMES" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"USER_ID" BIGINT NOT NULL,"GAME_ID" BIGINT NOT NULL);
alter table "IDENTITIES" add constraint "USER_FK" foreign key("USER_ID") references "USERS"("id") on update NO ACTION on delete NO ACTION;
alter table "USER_GAMES" add constraint "GAME_FK" foreign key("GAME_ID") references "GAMES"("id") on update NO ACTION on delete NO ACTION;
alter table "USER_GAMES" add constraint "USER_FK" foreign key("USER_ID") references "USERS"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "USER_GAMES" drop constraint "GAME_FK";
alter table "USER_GAMES" drop constraint "USER_FK";
alter table "IDENTITIES" drop constraint "USER_FK";
drop table "USER_GAMES";
drop table "USERS";
drop table "IDENTITIES";
drop table "GAMES";

