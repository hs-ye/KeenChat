package im.keen.keenchat;

import android.app.Fragment;

/**
 * Created by user on 2015/2/10.
 */
public class EventActivity extends Activity_SingleFragment {
    @Override
    protected Fragment createFragment() {
        String auth_token = (String)getIntent().getSerializableExtra(LoginActivity.auth_hash);
        int user_id = (int)getIntent().getSerializableExtra(LoginActivity.user_id_hash);

        return EventFragment.newInstance(auth_token,user_id);
    }
}
