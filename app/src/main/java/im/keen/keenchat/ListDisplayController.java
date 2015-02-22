package im.keen.keenchat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by user on 2015/2/11.
 */
public class ListDisplayController {
    public static void setListViewHeightBasedOnChildren(ListView lv){
        ListAdapter listAdapter = lv.getAdapter();
        if (listAdapter == null){
            return;
        }
        //Todo: Bug if list isn't filled, 2 separate scrolls visable
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(lv.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i<listAdapter.getCount(); i++){
            View listItem = listAdapter.getView(i,null,lv);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = lv.getLayoutParams();
        params.height = totalHeight + (lv.getDividerHeight()*(listAdapter.getCount() - 1));
        lv.setLayoutParams(params);
        lv.requestLayout();
    }
}
