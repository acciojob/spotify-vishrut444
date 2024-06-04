package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository() {
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User newUser = new User(name, mobile);
        users.add(newUser);
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name);
        artists.add(newArtist);
        artistAlbumMap.put(newArtist, new ArrayList<>());
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = null;
        for (Artist a : artists) {
            if (a.getName().equals(artistName)) {
                artist = a;
                break;
            }
        }
        if (artist == null) {
            artist = createArtist(artistName);
        }
        Album album = new Album(title);
        albums.add(album);
        albumSongMap.put(album, new ArrayList<>());
        artistAlbumMap.get(artist).add(album);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = null;
        for (Album a : albums) {
            if (a.getTitle().equals(albumName)) {
                album = a;
                break;
            }
        }
        if (album == null) {
            throw new Exception("Album does not exist");
        }
        Song song = new Song(title, length);
        songs.add(song);
        albumSongMap.get(album).add(song);
        songLikeMap.put(song, new ArrayList<>());
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        creatorPlaylistMap.put(user, playlist);
        userPlaylistMap.putIfAbsent(user, new ArrayList<>());
        userPlaylistMap.get(user).add(playlist);
        playlistListenerMap.put(playlist, new ArrayList<>(Collections.singletonList(user)));

        for (Song s : songs) {
            if (s.getLength() == length) {
                playlistSongMap.putIfAbsent(playlist, new ArrayList<>());
                playlistSongMap.get(playlist).add(s);
            }
        }

        return playlist;

    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User does not exist");
        }

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        creatorPlaylistMap.put(user, playlist);
        userPlaylistMap.putIfAbsent(user, new ArrayList<>());
        userPlaylistMap.get(user).add(playlist);
        playlistListenerMap.put(playlist, new ArrayList<>(Collections.singletonList(user)));

        for (String s : songTitles) {
            for (Song song : songs) {
                if (song.getTitle().equals(s)) {
                    playlistSongMap.putIfAbsent(playlist, new ArrayList<>());
                    playlistSongMap.get(playlist).add(song);
                }
            }
        }

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = null;
        for (Playlist p : playlists) {
            if (p.getTitle().equals(playlistTitle)) {
                playlist = p;
                break;
            }
        }
        if (playlist == null) {
            throw new Exception("Playlist does not exist");
        }
        List<User> listeners = playlistListenerMap.get(playlist);
        if (!listeners.contains(user)) {
            listeners.add(user);
            playlistListenerMap.put(playlist, listeners);
            userPlaylistMap.get(user).add(playlist);
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Song song = null;
        for (Song s : songs) {
            if (s.getTitle().equals(songTitle)) {
                song = s;
                break;
            }
        }
        if (song == null) {
            throw new Exception("Song does not exist");
        }
        List<User> likes = songLikeMap.get(song);
        if (!likes.contains(user)) {
            likes.add(user);
            song.setLikes(song.getLikes() + 1);
            for (Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()) {
                for (Album album : entry.getValue()) {
                    if (albumSongMap.get(album).contains(song)) {
                        Artist artist = entry.getKey();
                        artist.setLikes(artist.getLikes() + 1);
                        break;
                    }
                }
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        Artist popularArtist = null;
        int maxLikes = 0;
        for (Artist artist : artists) {
            if (artist.getLikes() > maxLikes) {
                popularArtist = artist;
                maxLikes = artist.getLikes();
            }
        }
        return popularArtist != null ? popularArtist.getName() : null;
    }

    public String mostPopularSong() {
        Song popularSong = null;
        int maxLikes = 0;
        for (Song song : songs) {
            if (song.getLikes() > maxLikes) {
                popularSong = song;
                maxLikes = song.getLikes();
            }
        }
        return popularSong != null ? popularSong.getTitle() : null;
    }
}
