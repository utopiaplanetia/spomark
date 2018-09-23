package com.utopiaplanetia.spomark;

/**
 * Created by Christian on 11.12.2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MediaBroadcastReceiver extends BroadcastReceiver {


    public static String SPO_METADATA_CHANGED = "SPO_METADATA_CHANGED";
    public static String SPO_PLAYBACK_STATE_CHANGED = "SPO_PLAYBACK_STATE_CHANGED";


    private MetaDataReceiverService metaDataReceiverService;

    public MediaBroadcastReceiver(MetaDataReceiverService metaDataReceiverService) {
        this.metaDataReceiverService = metaDataReceiverService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This is sent with all broadcasts, regardless of type. The value is taken from
        // System.currentTimeMillis(), which you can compare to in order to determine how
        // old the event is.

        Log.d("MediaBroadcastReceiver", "onReceive");

        long timeSentInMs = intent.getLongExtra("timeSent", 0L);

        String action = intent.getAction();

        if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
            String trackId = intent.getStringExtra("id");
            String artistName = intent.getStringExtra("artist");
            String albumName = intent.getStringExtra("album");
            String trackName = intent.getStringExtra("track");
            int trackLengthInSec = intent.getIntExtra("length", 0);

            //Intent i = new Intent(SPO_METADATA_CHANGED);
            //i.putExtra("metadata", trackId + ", " + artistName + ", " + albumName  + ", " + trackName);
            Log.d("MetaDataReceiverService", "sending broadcast to main activity");
            //context.sendBroadcast(i);
            intent.setAction(SPO_METADATA_CHANGED);
            context.sendBroadcast(intent);


            Log.d("MediaBroadcastReceiver", "Playing song: " + trackId + ", " + artistName + ", " + albumName + ", " + trackName);
            // Do something with extracted information...
        } else if (action.equals(BroadcastTypes.PLAYBACK_STATE_CHANGED)) {
            boolean playing = intent.getBooleanExtra("playing", false);
            int positionInMs = intent.getIntExtra("playbackPosition", 0);
            intent.setAction(SPO_PLAYBACK_STATE_CHANGED);
            context.sendBroadcast(intent);
            Log.d("MediaBroadcastReceiver", "Playback position [ms]" + positionInMs);

            // Do something with extracted information
        } else if (action.equals(BroadcastTypes.QUEUE_CHANGED)) {
            // Sent only as a notification, your app may want to respond accordingly.
        }
    }

    static final class BroadcastTypes {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }
}