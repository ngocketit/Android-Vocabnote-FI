package com.ngocketit.vocabnote2;

import com.ngocketit.vocabnote2.R;

import android.widget.LinearLayout;
import android.content.Context;
import android.view.LayoutInflater;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;
import android.view.View.OnTouchListener;
import android.view.View;
import android.widget.TextView;

public class ListItemView extends LinearLayout 
{
    private static final String TAG = "ListItemView";
    private View headerView = null;
    
    public ListItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.list_word_item, this);
        headerView = findViewById(R.id.list_item_txt_date);
    }

    public boolean onTouchEvent(MotionEvent evt)
    {
        switch (evt.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float ex = evt.getX();
                float ey = evt.getY();
                
                int x = headerView.getLeft();
                int y = headerView.getTop();
                int w = headerView.getWidth();
                int h = headerView.getHeight();

                if (ex >= x && ex <= x + w && ey >= y && ey <= y + h)
                    return true;
            default:
                return super.onTouchEvent(evt);
        }
    }
}
