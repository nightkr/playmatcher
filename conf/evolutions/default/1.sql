# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "games" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254) NOT NULL,"STEAM_APPID" BIGINT,"icon" VARCHAR(254));
create unique index "NAME_INDEX" on "games" ("NAME");
create unique index "STEAM_APPID_INDEX" on "games" ("STEAM_APPID");
create table "identities" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"USER_ID" BIGINT NOT NULL,"KIND" VARCHAR(254) NOT NULL,"VALUE" VARCHAR(254) NOT NULL);
create unique index "KIND_VALUE_INDEX" on "identities" ("KIND","VALUE");
create unique index "USER_KIND_INDEX" on "identities" ("USER_ID","KIND");
create table "user_games" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"USER_ID" BIGINT NOT NULL,"GAME_ID" BIGINT NOT NULL);
create table "users" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254),"STEAMID" BIGINT,"BANNED" BOOLEAN NOT NULL);
alter table "identities" add constraint "USER_FK" foreign key("USER_ID") references "users"("id") on update NO ACTION on delete NO ACTION;
alter table "user_games" add constraint "GAME_FK" foreign key("GAME_ID") references "games"("id") on update NO ACTION on delete NO ACTION;
alter table "user_games" add constraint "USER_FK" foreign key("USER_ID") references "users"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "user_games" drop constraint "GAME_FK";
alter table "user_games" drop constraint "USER_FK";
alter table "identities" drop constraint "USER_FK";
drop table "users";
drop table "user_games";
drop table "identities";
drop table "games";

