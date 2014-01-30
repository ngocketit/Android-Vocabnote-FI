package com.ngocketit.vocabnote2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.ngocketit.vocabnote2.VocabNote.VocabNoteColumns;
import com.ngocketit.vocabnote2.adapters.WordFilterDateAdapter;

public class WordDateFilterActivity extends Activity {
    private static final String TAG = "WordDateFilterActivity";
    private ListView mItemsList = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
            
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.filter_date);
        
        ((ImageButton)findViewById(R.id.action_bar_btn_cancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        
        mItemsList = (ListView)findViewById(R.id.filter_date_lst_items);
        mItemsList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String date = (String)(view.findViewById(R.id.filter_date_item_txt_date).getTag());
                Intent intent = new Intent();
                intent.putExtra(VocabNoteListActivity.FILTER_DATE, date);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        
        bindAdapter();
    }
    
    private void bindAdapter() {
        String[] projection = {VocabNoteColumns.MODIFIED_DATE, VocabNoteColumns._COUNT};
        Uri dateUri = Uri.withAppendedPath(VocabNoteColumns.CONTENT_URI, VocabNote.DATE_URI_PATH);
        
        Cursor cursor = getContentResolver().query(dateUri, projection, null, null, null);
        startManagingCursor(cursor);
        
        int count = cursor.getCount();
        findViewById(R.id.word_list_empty_view).setVisibility(count > 0 ? View.GONE : View.VISIBLE);
        mItemsList.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        
        WordFilterDateAdapter adapter = new WordFilterDateAdapter(this, cursor);
        mItemsList.setAdapter(adapter);
    }
}
