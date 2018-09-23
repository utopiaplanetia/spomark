package com.utopiaplanetia.spomark;

public class Track {

    private String artist;

    private String trackID;
    private String album;
    private String songtitle;
    private int length;

    public Track(String trackID, String artist, String album, String songtitle, int length) {

        this.trackID = trackID;
        this.artist = artist;
        this.album = album;
        this.songtitle = songtitle;
        this.length = length;

    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTrackID() {
        return trackID;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getSongtitle() {
        return songtitle;
    }

    public void setSongtitle(String songtitle) {
        this.songtitle = songtitle;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Track) {
            Track temp = (Track) obj;
            return temp.getTrackID().equals(trackID);
        }
        return false;
    }
}
