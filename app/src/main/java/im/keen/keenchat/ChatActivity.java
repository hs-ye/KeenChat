package im.keen.keenchat;

import android.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;


public class ChatActivity extends Activity_SingleFragment {

/*
@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    */



    protected Fragment createFragment() {
        //You have to put the activity's intent's that are meant for the fragment where things happen
        //here, so the fragment can retrieve it once it's running.
        String auth_token = (String)getIntent().getSerializableExtra(LoginActivity.auth_hash);
        int user_id = (int)getIntent().getSerializableExtra(LoginActivity.user_id_hash);
        //int event_id = (int) getIntent().getSerializableExtra(LoginActivity.event_id_hash);
        //Todo: Undo testing default event id
        int event_id = 1;
        return ChatFragment.newInstance(auth_token,event_id,user_id);
    }
}
