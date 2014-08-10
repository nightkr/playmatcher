# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "GAMES" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254) NOT NULL);
create unique index "NAME_INDEX" on "GAMES" ("NAME");
create table "IDENTITIES" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"USER_ID" BIGINT NOT NULL,"KIND" VARCHAR(254) NOT NULL,"VALUE" VARCHAR(254) NOT NULL);
create unique index "KIND_VALUE_INDEX" on "IDENTITIES" ("KIND","VALUE");
create unique index "USER_KIND_INDEX" on "IDENTITIES" ("USER_ID","KIND");
create table "USERS" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"BANNED" BOOLEAN NOT NULL);
alter table "IDENTITIES" add constraint "USER" foreign key("USER_ID") references "USERS"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "IDENTITIES" drop constraint "USER";
drop table "USERS";
drop table "IDENTITIES";
drop table "GAMES";

