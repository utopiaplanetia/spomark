package com.utopiaplanetia.spomark;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
//import com.spotify.sdk.android.authentication.AuthenticationClient;
//import com.spotify.sdk.android.authentication.AuthenticationResponse;
//import com.spotify.sdk.android.player.Config;
//import com.spotify.sdk.android.player.ConnectionStateCallback;
//import com.spotify.sdk.android.player.Error;
//import com.spotify.sdk.android.player.Player;
//import com.spotify.sdk.android.player.PlayerEvent;
//import com.spotify.sdk.android.player.Spotify;
//import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "310af167a7e74edd986e2bc6529350b9";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "https://github.com/utopiaplanetia/spomark";
    // Request code that will be used to verify if the result comes from correct activity
// Can be any integer
    private static final int REQUEST_CODE = 1337;
    private static String MAIN_STORE_KEY = "com.utopiaplanetia.spomark.MainActivity";
    private static String SETTINGS_KEY = "settings";
    private MetaDataReceiverService mService;
    private boolean mBound = false;
    private Track currentTrack;
    private String lastTrack = "";

    private ArrayAdapter<String> playlistsAdapter;

    private List<Track> subscribedPlaylists;
    private List<String> subscribedPlaylistsView;
    //    private Player mPlayer;
    private SharedPreferences pref;

    private Track selectedTrack;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d("ServiceConnection", "onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MetaDataReceiverService.ServiceBinder binder = (MetaDataReceiverService.ServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("ServiceConnection", "onServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



/*
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        */

        Button playbutton = findViewById(R.id.playbutton);
        playbutton.setOnClickListener(this);

        //Button pausButton = (Button) findViewById(R.id.pausebutton);
        //pausButton.setOnClickListener(this);


        // exchange data between service and its media broadcast receivers and the main activity
        MainActivityReceiver mar = new MainActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaBroadcastReceiver.SPO_METADATA_CHANGED);
        registerReceiver(mar, filter);


        subscribedPlaylists = new ArrayList<Track>();
        subscribedPlaylistsView = new ArrayList<String>();


        //subscribedPlaylists.forEach((Track t) -> subscribedPlaylistsView.add(t.getArtist() + "," + t.getAlbum() + "," + t.getSongtitle()));


        Spinner s = findViewById(R.id.subscribelist);

        playlistsAdapter = new ArrayAdapter<String>(this,
                R.layout.textview, subscribedPlaylistsView);

        playlistsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(playlistsAdapter);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                if (position < subscribedPlaylists.size()) {
                    selectedTrack = subscribedPlaylists.get(position);
                }
                Toast.makeText(getBaseContext(), subscribedPlaylistsView.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                selectedTrack = null;
            }
        });

        if (!mBound) {
            bindService(new Intent(this, MetaDataReceiverService.class), mConnection, Context.BIND_AUTO_CREATE);
        }

        // load old subscriptions
        deserializeSettings();

    }

    @Override
    protected void onStop() {
        super.onStop();
        serializeSettings();
    }

    public void subscribeToPlaylist(View view) {
        Log.d("MainActivity", "subscribe");
        if (currentTrack != null && !subscribedPlaylists.contains(currentTrack)) {
            subscribedPlaylists.add(currentTrack);
            subscribedPlaylistsView = new ArrayList<String>();
            subscribedPlaylists.forEach((Track t) -> subscribedPlaylistsView.add(t.getArtist() + "," + t.getAlbum() + "," + t.getSongtitle()));
            playlistsAdapter.clear();
            playlistsAdapter.addAll(subscribedPlaylistsView);
            Toast.makeText(this, "Track added successfully to subscription list.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No track selected or already subscribed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void unsubscribeToPlaylist(View view) {
        Log.d("MainActivity", "unsubscribe");

        if (selectedTrack != null) {
            int index = subscribedPlaylists.indexOf(selectedTrack);
            subscribedPlaylistsView.remove(index);
            subscribedPlaylists.remove(index);
            playlistsAdapter.clear();
            playlistsAdapter.addAll(subscribedPlaylistsView);
            selectedTrack = null;
            Toast.makeText(this, "Unsubscribed from playlist successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No track selected!", Toast.LENGTH_SHORT).show();
        }


    }

    public void startService(View view) {
        //startService(new Intent(getBaseContext(), MetaDataReceiverService.class));
        //bindService(new Intent(getBaseContext(), MetaDataReceiverService.class), mConnection, Context.BIND_AUTO_CREATE);
        if (!mBound) {
            bindService(new Intent(this, MetaDataReceiverService.class), mConnection, Context.BIND_AUTO_CREATE);
            Toast.makeText(this, "Service started!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service already running!", Toast.LENGTH_SHORT).show();
        }


    }

    public void stopService(View view) {
        //stopService(new Intent(getBaseContext(), MetaDataReceiverService.class));
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        Toast.makeText(this, "Service stopped!", Toast.LENGTH_SHORT).show();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//
//
//        // Check if result comes from the correct activity
//        if (requestCode == REQUEST_CODE) {
//            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
//            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
//                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
//                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
//                    @Override
//                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
//                        mPlayer = spotifyPlayer;
//                        mPlayer.addConnectionStateCallback(MainActivity.this);
//                        mPlayer.addNotificationCallback(MainActivity.this);
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
//                    }
//                });
//            }
//        }
//    }

    private void deserializeSettings() {
        pref = getSharedPreferences(MAIN_STORE_KEY, Context.MODE_PRIVATE);


        if (pref != null) {

            String json = pref.getString(SETTINGS_KEY, "");
            Log.d("MainActivity", "deserializeSettings");
            Log.d("MainActivity", json);


            if (json != null && !json.equals("")) {
                Gson gson = new Gson();
                Track[] tracks = gson.fromJson(json, Track[].class);
                subscribedPlaylists = new ArrayList<Track>();
                subscribedPlaylistsView = new ArrayList<String>();

                for (Track t : tracks) {
                    subscribedPlaylists.add(t);
                    subscribedPlaylistsView.add(t.getArtist() + "," + t.getAlbum() + "," + t.getSongtitle());
                }

                playlistsAdapter.clear();
                playlistsAdapter.addAll(subscribedPlaylistsView);
            }

        }
    }

    private void serializeSettings() {
        pref = getSharedPreferences(MAIN_STORE_KEY, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = gson.toJson(subscribedPlaylists);
        if (pref != null) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(SETTINGS_KEY, json);
            editor.commit();
            Log.d("MainActivity", "serializeSettings");
            Log.d("MainActivity", json);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "onDestroy");
//        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

//    @Override
//    public void onPlaybackEvent(PlayerEvent playerEvent) {
//        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
//        switch (playerEvent) {
//            // Handle event type as necessary
//            default:
//                break;
//        }
//    }

//    @Override
//    public void onPlaybackError(Error error) {
//        Log.d("MainActivity", "Playback error received: " + error.name());
//        switch (error) {
//            // Handle error type as necessary
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public void onLoggedIn() {
//        Log.d("MainActivity", "User logged in");
//
//        //mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
//    }
//
//    @Override
//    public void onLoggedOut() {
//        Log.d("MainActivity", "User logged out");
//    }
//
//    @Override
//    public void onLoginFailed(Error error) {
//        Log.d("MainActivity", "Login failed");
//    }
//
//    @Override
//    public void onTemporaryError() {
//        Log.d("MainActivity", "Temporary error occurred");
//    }
//
//    @Override
//    public void onConnectionMessage(String message) {
//        Log.d("MainActivity", "Received connection message: " + message);
//    }

    @Override
    public void onClick(View view) {


        switch (view.getId()) {

//            case R.id.pausebutton: {
//                mPlayer.pause(null);
//                Log.d("MainActivity", "Player Paused");
//                break;
//            }

            case R.id.playbutton: {

                // right click on a track in Spotify to get the URI, or use the Web API.
//                String uri = "spotify:track:73y1LV0U4ybMNDyO8S4fwW#40";

                if (selectedTrack != null) {
                    Intent launcher = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedTrack.getTrackID()));
                    startActivity(launcher);
                } else {
                    Toast.makeText(this, "No track selected!", Toast.LENGTH_SHORT).show();
                }
                //Metadata md = mPlayer.getMetadata();
                //Log.d("MainActivity", "-->" + md.toString());
                //mPlayer.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
                break;

            }


        }

    }

    private class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub


            Log.d("MainActivityReceiver", "onReceive:");
            String action = intent.getAction();
            if (action.equals(MediaBroadcastReceiver.SPO_METADATA_CHANGED)) {

                String trackId = intent.getStringExtra("id");
                String artistName = intent.getStringExtra("artist");
                String albumName = intent.getStringExtra("album");
                String trackName = intent.getStringExtra("track");
                int trackLengthInSec = intent.getIntExtra("length", 0);

                if (trackId != null && artistName != null && albumName != null && trackName != null) {
                    currentTrack = new Track(trackId, artistName, albumName, trackName, trackLengthInSec);

                    for (Track t : subscribedPlaylists) {
                        // Detect whether we have subscribed to the same artist an album already and update position information for tracked track
                        if (currentTrack.getAlbum().equals(t.getAlbum()) && currentTrack.getArtist().equals(t.getArtist())) {
                            t.setTrackID(currentTrack.getTrackID());
                            t.setLength(currentTrack.getLength());
                            t.setSongtitle(currentTrack.getSongtitle());

                            // update entry in subscribed list view
                            subscribedPlaylistsView = new ArrayList<String>();
                            subscribedPlaylists.forEach((Track t1) -> subscribedPlaylistsView.add(t1.getArtist() + "," + t1.getAlbum() + "," + t1.getSongtitle()));
                            playlistsAdapter.clear();
                            playlistsAdapter.addAll(subscribedPlaylistsView);

                        }
                    }

                    TextView metaDataView = findViewById(R.id.textMetaData);
                    metaDataView.setText(currentTrack.getArtist() + ", " + currentTrack.getAlbum() + ", " + currentTrack.getSongtitle());

                }
            }


        }
    }
}
