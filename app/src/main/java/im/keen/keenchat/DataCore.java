package im.keen.keenchat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by user on 2015/2/5.
 */
public final class DataCore {
    public final static String KEENUser = "http://chat.server.keen.im/user";
    public final static String KEENMsg = "http://chat.server.keen.im/message";
    public final static String KEENChannel = "http://chat.server.keen.im/channel";

    private Context mAppContext;
    private static DataCore sDataCore; //A single instance of the datacore is allowed here, as the reference is fixed using static
    private SparseArray<DataUser> mUsers = new SparseArray<DataUser>();
    private HashMap<Integer,DataMsg> mDataMsgs = new HashMap<Integer,DataMsg>();
    private HashMap<Integer,DataEvent> mDataEvents = new HashMap<Integer,DataEvent>();
    private static final String TAG = "DataCore";
    private static final String FILENAME = "Keen_save.json";
    //Todo: Implement serialisation saving/loading


    public static DataCore getDataCore(Context context) {
        if (sDataCore == null){
            sDataCore = new DataCore(context.getApplicationContext());
        }
        return sDataCore;
    }
    //All the methods below must be called on the singleton instance of the datacore, using the
    //getDataCore static method above, which allow particular singleton instance to be referenced
    //throughout the program.

    //Getters setter for individual objects, hashmap lookups etc.
    public String getUserNamefromId(int user_id){
        return (String)mUsers.get(user_id).getUsername();
    }

    //Datacore setters and getters (of entire objects)
    public SparseArray<DataUser> getUsers() {
        return mUsers;
    }

    public void setUsers(ArrayList<DataUser> userlist) {
        if(mUsers != null){
            mUsers.clear();
        }
        for(DataUser user: userlist){
            mUsers.put(user.getId(),user);
        }
        Log.i(TAG,"Datacore users updated");
    }

    public void setEvents(ArrayList<DataEvent> eventList) {
        if(mDataEvents != null){
            mDataEvents.clear();
        }
        for(DataEvent event: eventList){
            mDataEvents.put(event.getId(), event);
        }
        Log.i(TAG, "Datacore events updated. Total events:" + mDataEvents.size());
    }

    public HashMap<Integer,DataMsg> getDataMsgs() {
        return mDataMsgs;
    }

    public HashMap getDataEvents() {

        return mDataEvents;
    }

    public void setDataMsgs(ArrayList<DataMsg> message_list) {
        if(mDataMsgs != null){mDataMsgs.clear();}
        for(DataMsg one_message: message_list){
            mDataMsgs.put(one_message.getId(),one_message);
        }
        Log.i(TAG,"Datacore msgs updated. Total messages (all events):" + mDataMsgs.size());
    }

    private DataCore(Context c) {
        mAppContext = c;
    }

}
