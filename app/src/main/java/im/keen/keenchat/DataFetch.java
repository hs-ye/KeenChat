package im.keen.keenchat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import retrofit.http.POST;

/**
 * Created by user on 2015/2/6.
 */
public class DataFetch {
    public final static String TAG = "DataFetch";

    Gson mGson = new GsonBuilder().create();

    byte[] getBytesFromUrl(String url_destination, String request_method, String request_payload) throws IOException {
        URL data_url = new URL(url_destination);
        HttpURLConnection data_connection = (HttpURLConnection)data_url.openConnection();
        data_connection.setConnectTimeout(10000);
        data_connection.setReadTimeout(15000);
        data_connection.setRequestMethod(request_method);
        try {
            ByteArrayOutputStream byte_stream_output = new ByteArrayOutputStream();
            if(request_method == "POST") {
                data_connection.setRequestProperty("Content-Type","application/json");
                data_connection.setRequestProperty("Content-Length",Integer.toString(request_payload.length()));
                //You need to tell the server what to expect before giving it the payload or it'll
                //not know what to do with it. Therefore setRequestProperty properly
                DataOutputStream writer_stream = new DataOutputStream(data_connection.getOutputStream());
                writer_stream.writeBytes(request_payload);
                writer_stream.flush();
                writer_stream.close(); //Reset/close the writer after done.
                Log.d(TAG, "Connection opened and payload delivered to " + url_destination);
            } else{
                Log.d(TAG, "Attempting connection to " + url_destination);
            }
            InputStream data_stream_input = data_connection.getInputStream();
            if (data_connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Could not get response. Error code" + data_connection.getResponseCode());
                return null;
            }
            Log.d(TAG, "Server response:" + data_connection.getResponseCode());
            int bytesRead = 0;
            byte[] dataBuffer = new byte[1024];

            while ((bytesRead = data_stream_input.read(dataBuffer)) != -1) {
                byte_stream_output.write(dataBuffer, 0, bytesRead);
            }
            byte_stream_output.close();
            Log.d(TAG, "Data download success");
            return byte_stream_output.toByteArray();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
        } finally {
            if (data_connection != null) {
                data_connection.disconnect();
                Log.d(TAG, "Closing connection");
            }
        }

    }

    String getStringFromUrl (String url_destination, String request_method, String request_payload) throws IOException {
        return new String(getBytesFromUrl(url_destination,request_method,request_payload));
    }

    public void FetchMsgs (Context c, String endpoint){
        FetchMsgsTask mFetchMsgsTask = new FetchMsgsTask(c);
        mFetchMsgsTask.execute(endpoint,"GET");
    }


    private class FetchMsgsTask extends AsyncTask<String, Void, String> {

        private Context mContext;
        public FetchMsgsTask(Context context){
            mContext = context;
        }
        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.d(TAG, "Starting background data fetch:"+ urls[1] + urls[0]);
                return new DataFetch().getStringFromUrl(urls[0],urls[1],null);
            } catch (IOException IOE) {
                Log.e(TAG, "Error - could not get data");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v(TAG, "Data fetch success. Result: " + s);
            DataMsg[] msg_list = mGson.fromJson(s, DataMsg[].class);
            DataCore.getDataCore(mContext).setDataMsgs(new ArrayList<DataMsg>(Arrays.asList(msg_list)));
            Log.v(TAG, "Number of messages fetched and loaded: " + msg_list.length);
        }
    }

    private class FetchUsersTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.d(TAG, "Starting background data fetch:"+ urls[1] + urls[0]);
                return new DataFetch().getStringFromUrl(urls[0],urls[1],null);
            } catch (IOException IOE) {
                Log.e(TAG, "Keen data fetch error - could not get data");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v(TAG, "User list fetch success. Result: " + s);
            DataUser[] user_list = mGson.fromJson(s, DataUser[].class);
            //this.setUsers(new ArrayList<DataUser>(Arrays.asList(user_list)));
            //This line makes the array of users from Gson into a list, which is then converted into an
            //array list by the ArrayList(List) constructor, an then sets it into the datacore singleton
            //instance. Yeah.
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
              }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v(TAG, "Channel list fetch success. Result: " + s);
            DataEvent[] channel_list = mGson.fromJson(s, DataEvent[].class);
            //DataCore.getDataCore(getActivity()).setEvents(new ArrayList<DataEvent>(Arrays.asList(channel_list)));
        //todo: Fix these async tasks. Make sure refresh logic is in the calling method (e.g. text updates)
        }
    }

}
