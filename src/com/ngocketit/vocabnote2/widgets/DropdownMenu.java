package com.ngocketit.vocabnote2.widgets;

import android.widget.PopupWindow;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.graphics.drawable.BitmapDrawable;
import android.content.Context;
import java.util.List;
import java.util.ArrayList;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.Display;
import android.view.WindowManager.LayoutParams;
import android.util.Log;
import android.content.res.Resources;
import android.widget.AdapterView.OnItemClickListener;

import com.ngocketit.vocabnote2.R;
import com.ngocketit.vocabnote2.adapters.DropdownMenuAdapter;
import com.ngocketit.vocabnote2.adapters.DropdownMenuAdapter.DropdownMenuItem;

public class DropdownMenu 
{
    private static final String TAG = "DropdownMenu";
    private ListView mItemList = null;
    private PopupWindow mPopupWindow = null;
    private List<DropdownMenuItem> mActionItems = null;

    public DropdownMenu(Context context, List<DropdownMenuItem> items) {
        mActionItems = items;
        LayoutInflater inflater = LayoutInflater.from(context);
        View contentView = inflater.inflate(R.layout.dropdown_menu, null, false);

        Resources res = context.getResources();
        int popupWidth  = res.getInteger(R.integer.filter_menu_width);
        int popupHeight = res.getInteger(R.integer.filter_menu_height);

        mPopupWindow = new PopupWindow(contentView, popupWidth, popupHeight);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setAnimationStyle(R.style.Animations_PopDownMenu_Right);

        mItemList = (ListView)contentView.findViewById(R.id.menu_lst_items);
        mItemList.setAdapter(new DropdownMenuAdapter(context, items));

        mPopupWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mPopupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });
    }
    
    public DropdownMenuItem getItemAt(int position) {
        return mActionItems.get(position);
    }
    
    public DropdownMenuItem getItemById(int itemId) {
        for (DropdownMenuItem item : mActionItems) {
            if (item.itemId == itemId) 
                return item;
        }
        
        return null;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemList.setOnItemClickListener(listener);
    }
    
    public void showAtLocation(View parent, int gravity, int x, int y) {
        final Context context = parent.getContext();

        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();

        if (mPopupWindow.getHeight() > screenHeight - y) 
            mPopupWindow.setHeight(screenHeight - y);

        if (mPopupWindow.getWidth() > screenWidth) 
            mPopupWindow.setWidth(screenWidth);

        mPopupWindow.showAtLocation(parent, gravity, x, y);
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }
}
