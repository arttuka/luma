create table album_tag (
    title text not null,
    artist text not null,
    tag text not null,
    primary key (title, artist, tag)
);

create table artist_tag (
    artist text not null,
    tag text not null,
    primary key (artist, tag)
);
