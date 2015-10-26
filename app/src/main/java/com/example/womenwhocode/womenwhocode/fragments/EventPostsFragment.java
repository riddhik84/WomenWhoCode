package com.example.womenwhocode.womenwhocode.fragments;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.womenwhocode.womenwhocode.models.Event;
import com.example.womenwhocode.womenwhocode.models.Post;
import com.example.womenwhocode.womenwhocode.utils.LocalDataStore;
import com.example.womenwhocode.womenwhocode.utils.NetworkConnectivityReceiver;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by zassmin on 10/26/15.
 */
public class EventPostsFragment extends PostsListFragment {
    public static String EVENT_ID = "event_id";
    public static String eventId;
    protected ParseQuery<Event> eventParseQuery;
    protected ParseQuery<Post> postParseQuery;

    public static EventPostsFragment newInstance(String eventObjectId) {
        EventPostsFragment eventPostsFragment = new EventPostsFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventObjectId);
        eventPostsFragment.setArguments(args);
        return eventPostsFragment;
    }

    @Override
    protected void populatePosts() {
        // we need to get the Event object
        eventId = getArguments().getString(EVENT_ID, "");

        eventParseQuery = ParseQuery.getQuery(Event.class);
        postParseQuery = ParseQuery.getQuery(Post.class);
        if (!NetworkConnectivityReceiver.isNetworkAvailable(getContext())) {
            postParseQuery.fromPin(eventId);
        }

        eventParseQuery.getInBackground(eventId, new GetCallback<Event>() {
            @Override
            public void done(Event event, ParseException e) {
                postParseQuery.whereEqualTo(Post.EVENT_KEY, event);
                postParseQuery.findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> list, ParseException e) {
                        if (e == null && list.size() > 0) {
                            // clear adapter
                            clear();
                            // add to adapter
                            add(list);
                            // TODO: make progress bar invisible and data visible
                            clearSpinners();
                            // pin locally
                            LocalDataStore.unpinAndRepin(list, eventId);
                        } else if (e != null) {
                            Log.d("PARSE_EVENTS_POST_FAIL", "Error: " + e.getMessage());
                        } else {
                            clearSpinners();
                            noPostsView();
                        }
                    }
                });
            }
        });
    }
}