create table artist (
  id text not null primary key
);

create table album (
  id text not null primary key
);

create table album_artist (
  album text not null references album(id),
  artist text not null references artist(id),
  primary key (album, artist)
)

create table tag (
  tag text not null primary key
);

create table album_tag (
  album text not null references album(id),
  tag text not null references tag(tag),
  primary key (album, tag)
);

create table artist_tag (
  artist text not null references artist(id),
  tag text not null references tag(tag),
  primary key (artist, tag)
);
