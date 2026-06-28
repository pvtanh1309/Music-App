-- Bước 1: Tạo user
CREATE USER app_user WITH PASSWORD 'app_user_1412';

-- Bước 2: Cấp quyền kết nối vào DB (thiếu cái này là không vào được)
GRANT CONNECT ON DATABASE "SoundCloudDB" TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;

-- Bước 3: Tạo tất cả bảng (giữ nguyên toàn bộ phần CREATE TABLE + ALTER TABLE của bạn)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE "users" (
  "id" uuid PRIMARY KEY,
  "username" varchar NOT NULL,
  "email" varchar UNIQUE NOT NULL,
  "password" varchar NOT NULL,
  "avatar_url" text,
  "role" varchar DEFAULT 'USER',
  "created_at" timestamp
);

CREATE TABLE "artists" (
  "id" uuid PRIMARY KEY,
  "name" varchar NOT NULL,
  "bio" text,
  "avatar_url" text,
  "created_at" timestamp
);

CREATE TABLE "albums" (
  "id" uuid PRIMARY KEY,
  "name" varchar NOT NULL,
  "artist_id" uuid,
  "release_date" date,
  "cover_url" text
);

CREATE TABLE "songs" (
  "id" uuid PRIMARY KEY,
  "title" varchar NOT NULL,
  "artist_id" uuid,
  "album_id" uuid,
  "duration" int,
  "file_url" text,
  "cover_url" text,
  "created_at" timestamp
);

CREATE TABLE "playlists" (
  "id" uuid PRIMARY KEY,
  "user_id" uuid,
  "name" varchar NOT NULL,
  "is_public" boolean DEFAULT false,
  "created_at" timestamp
);

CREATE TABLE "playlist_songs" (
  "playlist_id" uuid,
  "song_id" uuid,
  "added_at" timestamp,
  PRIMARY KEY ("playlist_id", "song_id")
);

CREATE TABLE "favorites" (
  "user_id" uuid,
  "song_id" uuid,
  "created_at" timestamp,
  PRIMARY KEY ("user_id", "song_id")
);

CREATE TABLE "listening_histories" (
  "id" uuid PRIMARY KEY,
  "user_id" uuid,
  "song_id" uuid,
  "listened_at" timestamp
);

CREATE TABLE "follow_artists" (
  "user_id" uuid,
  "artist_id" uuid,
  PRIMARY KEY ("user_id", "artist_id")
);

CREATE TABLE "uploads" (
  "id" uuid PRIMARY KEY,
  "user_id" uuid,
  "song_id" uuid,
  "status" varchar,
  "created_at" timestamp
);

ALTER TABLE "songs" ADD FOREIGN KEY ("artist_id") REFERENCES "artists" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "songs" ADD FOREIGN KEY ("album_id") REFERENCES "albums" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "albums" ADD FOREIGN KEY ("artist_id") REFERENCES "artists" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "playlists" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "playlist_songs" ADD FOREIGN KEY ("playlist_id") REFERENCES "playlists" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "playlist_songs" ADD FOREIGN KEY ("song_id") REFERENCES "songs" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "favorites" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "favorites" ADD FOREIGN KEY ("song_id") REFERENCES "songs" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "listening_histories" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "listening_histories" ADD FOREIGN KEY ("song_id") REFERENCES "songs" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "follow_artists" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "follow_artists" ADD FOREIGN KEY ("artist_id") REFERENCES "artists" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "uploads" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "uploads" ADD FOREIGN KEY ("song_id") REFERENCES "songs" ("id") DEFERRABLE INITIALLY IMMEDIATE;


-- Bước 4: GRANT sau khi bảng đã tồn tại
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Bước 5: Quyền tự động cho bảng Hibernate tạo sau này
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO app_user;
