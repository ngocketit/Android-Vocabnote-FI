package com.ngocketit.vocabnote2.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.util.Date;

import com.ngocketit.vocabnote2.R;
import com.ngocketit.vocabnote2.VocabNote.VocabNoteColumns;

public class WordFilterDateAdapter extends SimpleCursorAdapter {
    private Context mContext = null;
    
    public WordFilterDateAdapter(Context context, Cursor cursor) {
        super(context, R.layout.filter_date_item, cursor, new String[] {VocabNoteColumns._ID}, null);
        mContext = context;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.filter_date_item, null);
            
            TextView dateView  = (TextView)convertView.findViewById(R.id.filter_date_item_txt_date);
            TextView countView = (TextView)convertView.findViewById(R.id.filter_date_item_txt_count);
            
            viewHolder = new ViewHolder(dateView, countView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        
        int count  = cursor.getInt(1);
        String modifDate = cursor.getString(0);
        String[] parts = modifDate.split("-");
        String date = String.format("%s-%s-%s", parts[2], parts[1], parts[0]);
        
        viewHolder.dateView.setTag(modifDate);
        viewHolder.dateView.setText(date);
        viewHolder.countView.setText(String.valueOf(count));
        
        return convertView;
    }
    
    private class ViewHolder {
        public TextView dateView;
        private TextView countView;
        
        public ViewHolder(TextView dView, TextView cView) {
            dateView = dView;
            countView = cView;
        }
    }
}
