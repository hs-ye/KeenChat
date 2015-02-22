package im.keen.keenchat;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * Created by user on 2015/2/5.
 */
public abstract class Activity_SingleFragment extends Activity {
    FragmentManager mFragManager;
    Fragment mFragmentContainer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        mFragManager = getFragmentManager();
        mFragmentContainer = mFragManager.findFragmentById(R.id.fragment_container);

        if (mFragmentContainer == null) {
            //Checks if the container is not yet set up
            mFragmentContainer = createFragment();
            mFragManager.beginTransaction().add(R.id.fragment_container,mFragmentContainer).commit();
        }

    }

    protected abstract Fragment createFragment();
}
