package com.ngocketit.vocabnote2.adapters;

import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

import com.ngocketit.vocabnote2.R;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

public class DropdownMenuAdapter extends BaseAdapter
{
    private List<DropdownMenuItem> mItems = null;
    private Context mContext = null;

    public DropdownMenuAdapter(Context context, List<DropdownMenuItem> items)
    {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dropdown_menu_item, null);
            ImageView iconView = (ImageView)convertView.findViewById(R.id.menu_item_img_icon);
            TextView textView  = (TextView)convertView.findViewById(R.id.menu_item_txt_name);

            viewHolder = new ViewHolder(iconView, textView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        DropdownMenuItem item = mItems.get(position);
        viewHolder.iconView.setImageResource(item.iconId);
        viewHolder.textView.setText(item.textId);

        return convertView;
    }

    private class ViewHolder
    {
        public ImageView iconView = null;
        public TextView textView = null;

        public ViewHolder(ImageView iconV, TextView tView)
        {
            iconView = iconV;
            textView = tView;
        }
    }

    public static class DropdownMenuItem
    {
        public int iconId = 0;
        public int textId = 0;
        public int itemId = 0;

        public DropdownMenuItem(int iconId, int textId, int itemId)
        {
            this.iconId = iconId;
            this.textId = textId;
            this.itemId = itemId;
        }
    }
}
