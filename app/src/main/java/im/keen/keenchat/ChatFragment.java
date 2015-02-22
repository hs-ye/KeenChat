package im.keen.keenchat;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by user on 2015/2/5.
 */
public class ChatFragment extends Fragment {
    //Static variable/string tag declarations
    public final String TAG = "KeenChatFragment";
    public final static String KEENUser = "http://chat.server.keen.im/user";
    public final static String KEENMsg = "http://chat.server.keen.im/message";
    public final static String KEENChannel = "http://chat.server.keen.im/channel";

    private String msgSendEndpoint;
    private String current_user_name; //may replace with a user object sometime later
    private int event_id;
    private int user_id;
    //Todo: event id is hard coded atm.
    //Todo: Add user name display to title bar

    //Data object declarations
    ArrayList<DataMsg> msgs_adapter_data = new ArrayList<DataMsg>();

    //controller object declaration and instantiation where appropriate
    Gson mGson = new GsonBuilder().create();
    MsgAdapter mMsgAdapter;
    private SwipeRefreshLayout mSwipe_updater;

    boolean[] swipeIsRefreshing = {false,false,false}; //This array keeps track on which update tasks are refreshing
    final boolean[] refreshCondition = {false,false,false};//This is the condition when no refresh tasks are still running
        //we need to manually keep track of which refresh task is which in this array, though.

    //View object declaration and instantiation
    EditText mChatBox;
    ImageButton mSendMsgButt;
    ListView mMsgListView;

    //This method is called by the hosting fragment to get a copy of this fragment, but with arguments
    //already inserted into that instance.
    public static ChatFragment newInstance(String auth_token, int event_id, int user_id){
        Bundle new_frag_args = new Bundle(); //This is the bundle specific to the fragment (not the activity hosting it)
        new_frag_args.putSerializable(LoginActivity.user_id_hash, user_id);
        new_frag_args.putSerializable(LoginActivity.event_id_hash, event_id);
        new_frag_args.putSerializable(LoginActivity.auth_hash,auth_token);
        ChatFragment new_chat_frag = new ChatFragment();
        new_chat_frag.setArguments(new_frag_args);
        return new_chat_frag;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        loadAllData();
        String auth_token = (String)getArguments().getSerializable(LoginActivity.auth_hash);
        user_id = (int)getArguments().getSerializable(LoginActivity.user_id_hash);
        event_id = (int)getArguments().getSerializable(LoginActivity.event_id_hash);

        msgSendEndpoint = Uri.parse(KEENMsg).buildUpon().appendQueryParameter("auth",auth_token)
                .build().toString();
        Log.i(TAG,"Msg endpoint setup: " + msgSendEndpoint);
        Log.i(TAG,"Current Auth token: "+auth_token);
        Log.i(TAG,"Current user id " + user_id + " on channel " + event_id);
        mMsgAdapter = new MsgAdapter(msgs_adapter_data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_chat,menu);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatBox = (EditText) v.findViewById(R.id.chat_text_entry);
        mSendMsgButt = (ImageButton) v.findViewById(R.id.chat_send_button);
        mMsgListView = (ListView) v.findViewById(R.id.chat_msg_list);
        mSwipe_updater = (SwipeRefreshLayout) v.findViewById(R.id.chat_swipe_refresh);

        mSendMsgButt.setEnabled(false);

        mSwipe_updater.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            //Only allow refresh if no tasks are currently running
                if (Arrays.equals(swipeIsRefreshing, refreshCondition)) {
                    mSwipe_updater.setRefreshing(true);
                    for (boolean itemrefreshing:swipeIsRefreshing){
                        itemrefreshing = true;
                    }
                    loadAllData();
                }
            }
        });
        mMsgListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //Blank
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    mSwipe_updater.setEnabled(true);
                } else {
                    mSwipe_updater.setEnabled(false);
                }
            }
        });

        updateAdapterView(); //Custom view adapter binding. Refreshes lists.

        mSendMsgButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    HashMap<String, Object> sent_msg = new HashMap<String, Object>();
                    sent_msg.put("message_text", mChatBox.getText().toString());
                    sent_msg.put("channel_id", event_id);
                    String send_json = mGson.toJson(sent_msg);
                    new sendMessageTask().execute(msgSendEndpoint, "POST", send_json);
                    mChatBox.setText("");

            }
        });

        mChatBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0){
                    mSendMsgButt.setEnabled(false);
                } else {
                    mSendMsgButt.setEnabled(true);
                }
            }
        });
        return v;
    }

    public void loadAllData() {
        //TODO: This should be loaded previously, and data should be fetched from the data-core.
        //Todo: need to work out timing of these events, allow for real time updating and logic for refresh is allowed.
         new DataFetch().FetchMsgs(getActivity(),KEENMsg);
        new FetchUsersTask().execute(KEENUser,"GET");
        new FetchChannelsTask().execute(KEENChannel,"GET");
        //MSGS are now being updated from the datacore. But NOT events/users.
        if(msgs_adapter_data != null){ msgs_adapter_data.clear();}
        HashMap<Integer,DataMsg> msg_map = DataCore.getDataCore(getActivity()).getDataMsgs();
        for(int j = 1; j<=msg_map.size();j++){
            msgs_adapter_data.add(msg_map.get(j));
        }
        Log.v(TAG, "Number of messages fetched and loaded: " + msgs_adapter_data.size());
        updateAdapterView();
    }

    public void stopRefresh(){
        if(Arrays.equals(swipeIsRefreshing,refreshCondition)){
            mSwipe_updater.setRefreshing(false);
        }
    }

    private void updateAdapterView() {
        //TODO: re-route message list through DataCore
        //This method should update the view with the latest messages in the adapter.
        if (getActivity() == null || mMsgListView == null) {
            Log.e(TAG, "Warning: Adapter components missing. Cannot setup");
            return;
        }

        if (msgs_adapter_data != null) {
            mMsgListView.setAdapter(mMsgAdapter);
            Log.i(TAG, "Adapter updated and set");
        } else {
            mMsgListView.setAdapter(null);
            Log.e(TAG, "Warning: No messages found");
        }
    }

    private class MsgAdapter extends ArrayAdapter<DataMsg> {
        public MsgAdapter(ArrayList<DataMsg> msgs) {
            super(getActivity(), 0, msgs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.chat_msg_layout, null);
            }
            DataMsg message = getItem(position); //This is getting the message from the adapter's data source
            //And then we we display this message in the view using commands below:

            TextView msgTextBody = (TextView) convertView.findViewById(R.id.msgbox_text);
            TextView msgTextName = (TextView) convertView.findViewById(R.id.msgbox_name);
            TextView msgTextTime = (TextView) convertView.findViewById(R.id.msgbox_time);

            SimpleDateFormat timestamp_format = new SimpleDateFormat("E, dd MMM, HH:mm");
            String user_name = DataCore.getDataCore(getActivity()).getUserNamefromId(message.getUser_id());

            msgTextBody.setText(message.getMessage_text());
            msgTextName.setText(user_name);
            msgTextTime.setText(timestamp_format.format(message.getTime()));
            return convertView;
        }

        @Override
        public DataMsg getItem(int position) {
            return super.getItem(getCount() - position - 1);
        }
    }

/*
    private class FetchMsgsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.d(TAG, "Starting background data fetch:"+ urls[1] + urls[0]);
                return new DataFetch().getStringFromUrl(urls[0],urls[1],null);
            } catch (IOException IOE) {
                Log.e(TAG, "Error - could not get data");
                swipeIsRefreshing[0] = false;
                stopRefresh();
            }
            return null;
        }
        //Todo: remove redundant code?
       @Override
        protected void onPostExecute(String s) {
            Log.v(TAG, "Data fetch success. Result: " + s);
            DataMsg[] msg_list = mGson.fromJson(s, DataMsg[].class);
            DataCore.getDataCore(getActivity()).setDataMsgs(new ArrayList<DataMsg>(Arrays.asList(msg_list)));
            msgs_adapter_data.clear();
            HashMap<Integer,DataMsg> msg_map = DataCore.getDataCore(getActivity()).getDataMsgs();
            for(int j = 1; j<=msg_map.size();j++){
                msgs_adapter_data.add(msg_map.get(j));
            }
            Log.v(TAG, "Number of messages fetched and loaded: " + msgs_adapter_data.size());
            updateAdapterView();
            swipeIsRefreshing[0] = false;
            stopRefresh();
        }

    }
        */
    private class FetchUsersTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.d(TAG, "Starting background data fetch:"+ urls[1] + urls[0]);
                return new DataFetch().getStringFromUrl(urls[0],urls[1],null);
            } catch (IOException IOE) {
                Log.e(TAG, "Keen data fetch error - could not get data");
                swipeIsRefreshing[1] = false;
                stopRefresh();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v(TAG, "User list fetch success. Result: " + s);
            DataUser[] user_list = mGson.fromJson(s, DataUser[].class);
            DataCore.getDataCore(getActivity()).setUsers(new ArrayList<DataUser>(Arrays.asList(user_list)));
            //This line makes the array of users from Gson into a list, which is then converted into an
            //array list by the ArrayList(List) constructor, an then sets it into the datacore singleton
            //instance. Yeah.
            swipeIsRefreshing[1] = false;
            stopRefresh();
        }
    }

    private class FetchChannelsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.d(TAG, "Starting background data fetch:"+ urls[1] + urls[0]);
                return new DataFetch().getStringFromUrl(urls[0],urls[1],null);
            } catch (IOException IOE) {
                Log.e(TAG, "Keen data fetch error - could not get channel data");
                swipeIsRefreshing[2] = false;
                stopRefresh();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v(TAG, "Channel list fetch success. Result: " + s);
            DataEvent[] channel_list = mGson.fromJson(s, DataEvent[].class);
            DataCore.getDataCore(getActivity()).setEvents(new ArrayList<DataEvent>(Arrays.asList(channel_list)));
            swipeIsRefreshing[2] = false;
            stopRefresh();
        }
    }

    private class sendMessageTask extends AsyncTask<String, Void, String> {
        //requires 3 params: destination, request type and the contents.
        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d(TAG, "Sending data:"+ params[2] + " to " + params[1] + params[0]);
                return new DataFetch().getStringFromUrl(params[0],params[1],params[2]);
            } catch (IOException IOE) {
                Log.e(TAG, "Keen data fetch error - could not send msg");

            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v(TAG, "Message sent. Result: " + s);
            loadAllData();
            //TODO: change data update logic for sending messages.
        }
    }
}
