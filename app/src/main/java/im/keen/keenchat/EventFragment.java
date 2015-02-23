package im.keen.keenchat;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by user on 2015/2/10.
 */
public class EventFragment extends Fragment {
    public final String TAG = "KeenEventFragment";

    //UI references
    ListView mEventList;
    ListView mIdeaList;

    //Controller object setup
    ArrayList<DataEvent> event_adapter_data = new ArrayList<DataEvent>();
    ArrayList<DataEvent> event_idea_adapter_data = new ArrayList<DataEvent>();
    EventAdapter mEventAdapter;
    EventAdapter mIdeaAdapter;

    private int user_id;
    private String auth_token;
    private String user_name;

    public static EventFragment newInstance(String auth_token, int user_id, String user_name){
        //we cheat a little by passing in the user name via login, instead of getting it from the database
        //name list. Since it will have been verified by the server by this point anyway.

        Bundle new_frag_args = new Bundle(); //This is the bundle specific to the fragment (not the activity hosting it)
        new_frag_args.putSerializable(LoginActivity.user_id_hash, user_id);
        new_frag_args.putSerializable(LoginActivity.auth_hash,auth_token);
        new_frag_args.putSerializable(LoginActivity.user_name_hash,user_name);
        EventFragment new_chat_frag = new EventFragment();
        new_chat_frag.setArguments(new_frag_args);
        return new_chat_frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        user_id = (int)getArguments().getSerializable(LoginActivity.user_id_hash);
        auth_token = (String)getArguments().getSerializable(LoginActivity.auth_hash);
        user_name = (String)getArguments().getSerializable(LoginActivity.user_name_hash);
        Log.d(TAG,"Logged in as user:" + user_id);
        //Todo: Wire up real event display
        DataEvent dummy_event1 = new DataEvent();
        DataEvent dummy_event2 = new DataEvent();
        DataEvent dummy_event3 = new DataEvent();
        DataEvent dummy_idea1 = new DataEvent();
        DataEvent dummy_idea2 = new DataEvent();
        DataEvent dummy_idea3 = new DataEvent();

        dummy_event1.setId(1); dummy_event1.setDescription("Super golf day with the boys @ morack on tuesday");
        dummy_event1.setTitle("Golf");dummy_event1.setFullEvent(true);

        dummy_event2.setId(2); dummy_event2.setDescription("Dinner in Bill's Canteen");
        dummy_event2.setTitle("Dinner?"); dummy_event2.setFullEvent(true);
/*
        dummy_event3.setId(3); dummy_event3.setDescription("At the lan party house");
        dummy_event3.setTitle("LAN"); dummy_event3.setFullEvent(true);
*/
        dummy_idea1.setId(4); dummy_idea1.setDescription("Party's on in Bill's dungeon");
        dummy_idea1.setTitle("Drinks"); dummy_idea1.setFullEvent(false);

        dummy_idea2.setId(5); dummy_idea2.setDescription("Tonight. Be there or be square.");
        dummy_idea2.setTitle("Alumbra"); dummy_idea2.setFullEvent(false);

        dummy_idea3.setId(6); dummy_idea3.setDescription("Because summer. And sharks.");
        dummy_idea3.setTitle("Beach time"); dummy_idea3.setFullEvent(false);

        event_adapter_data.add(dummy_event1);
        event_adapter_data.add(dummy_event2);
        //event_adapter_data.add(dummy_event3);
        event_idea_adapter_data.add(dummy_idea1);
        event_idea_adapter_data.add(dummy_idea2);
        event_idea_adapter_data.add(dummy_idea3);

        mEventAdapter = new EventAdapter(event_adapter_data, true); //Bind adapter to data source.
        mIdeaAdapter = new EventAdapter(event_idea_adapter_data, false);
        //the boolean argument tells the adapter whether to use the event list template or the broadcast
        //ideas list template
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event,container,false);
        getActivity().setTitle("Feed");
        //mEventScroll = (ScrollView)v.findViewById(R.id.event_scrollView);
        //mIdeaScroll = (ScrollView)v.findViewById(R.id.event_idea_scrollView);
        mEventList = (ListView)v.findViewById(R.id.event_listView);
        mIdeaList = (ListView)v.findViewById(R.id.event_idea_listView);
        //Todo: Get it to display user's name
        updateAdapterView();
        mEventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int goto_event_id = mEventAdapter.getItem(position).getId();

                Log.d(TAG, "Event accessed: " + goto_event_id);
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra(LoginActivity.user_id_hash,user_id);
                i.putExtra(LoginActivity.auth_hash,auth_token);
                i.putExtra(LoginActivity.event_id_hash,goto_event_id);
                startActivity(i);
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_event,menu);
        getActivity().getActionBar().setTitle(user_name + "'s Events");
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateAdapterView() {
        //TODO: re-route message list through DataCore
        //This method should update the view with the latest messages in the adapter.
        if (getActivity() == null || mEventList == null) {
            Log.e(TAG, "Warning: Event Adapter components missing. Cannot setup");
        } else {
            if (event_adapter_data != null) {
                mEventList.setAdapter(mEventAdapter);
                ListDisplayController.setListViewHeightBasedOnChildren(mEventList);
                Log.i(TAG, "Event Adapter updated and set");
            } else {
                mEventList.setAdapter(null);
                Log.e(TAG, "Warning: No events found");
            }
        }
        if (getActivity() == null || mIdeaList == null) {
            Log.e(TAG, "Warning: Ideas Adapter components missing. Unable to initiate");
        } else {
            if (event_idea_adapter_data != null) {
                mIdeaList.setAdapter(mIdeaAdapter);
                ListDisplayController.setListViewHeightBasedOnChildren(mIdeaList);
                Log.i(TAG, "Ideas Adapter updated and set");
            } else {
                mIdeaList.setAdapter(null);
                Log.e(TAG, "Warning: No events found");
            }
        }
    }

    private class EventAdapter extends ArrayAdapter<DataEvent> {
        private boolean isEventList;
        public EventAdapter(ArrayList<DataEvent> events, boolean isEvents) {
            super(getActivity(), 0, events);
            isEventList = isEvents;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (isEventList) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.feed_event_layout, null);
                } else {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.feed_broadcast_layout, null);
                }
            }
            //Todo: Fix up actual fields for event. incl. time etc.
            DataEvent feed_event = getItem(position); //This is getting the message from the adapter's data source
            //And then we we display this message in the view using commands below:

            TextView eventTitle = (TextView) convertView.findViewById(R.id.event_title);
            TextView eventDescr = (TextView) convertView.findViewById(R.id.event_descr_short);

            //SimpleDateFormat timestamp_format = new SimpleDateFormat("E, dd MMM, HH:mm");
            //String user_name = DataCore.getDataCore(getActivity()).getUserNamefromId(feed_event.getId());

            eventTitle.setText(feed_event.getTitle());
            eventDescr.setText(feed_event.getDescription());
            return convertView;
        }
            //Todo: Set onclick listeners here for buttons
    }


}
