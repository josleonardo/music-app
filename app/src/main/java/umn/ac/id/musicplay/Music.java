package umn.ac.id.musicplay;

import android.net.Uri;

public class Music {
    private final String title;
    private final String artist;
    private final String duration;
    private final Uri albumArtUri;

    public Music(String title, String artist, String duration, Uri albumArtUri) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.albumArtUri = albumArtUri;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    public Uri getAlbumArtUri() {
        return albumArtUri;
    }
}
