package com.ngocketit.vocabnote2.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ngocketit.vocabnote2.ListItemView;
import com.ngocketit.vocabnote2.R;
import com.ngocketit.vocabnote2.WordFilterOptions;
import com.ngocketit.vocabnote2.WordState;
import com.ngocketit.vocabnote2.VocabNote.VocabNoteColumns;

public class WordListAdapter extends SimpleCursorAdapter {
    private static final String TAG = "ListItemView";
    
    private Context mContext = null;
    
    private String[] mProjection = { 
        VocabNoteColumns._ID, 
        VocabNoteColumns.NAME,
        VocabNoteColumns.MARK_REVIEW, 
        VocabNoteColumns.MODIFIED_DATE 
    };

    public WordListAdapter(Context context) {
        // NOTE: from can't not be null
        super(context, R.layout.list_word_item, null, new String[] {VocabNoteColumns.NAME}, null);
        this.mContext = context;
        setFilterQueryProvider(filterQueryProvider);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int ONE_DAY_MS = 86400000;

        // Get date for this item
        Cursor c = getCursor();
        c.moveToPosition(position);

        String wordName = c.getString(c.getColumnIndex(VocabNoteColumns.NAME));
        int wordState = c.getInt(c.getColumnIndex(VocabNoteColumns.MARK_REVIEW));
        String wordModifDate = c.getString(c.getColumnIndex(VocabNoteColumns.MODIFIED_DATE));
                
        String prevModifDate = "";
        String nextModifDate = "";

        if (c.moveToPrevious()) prevModifDate = c.getString(c.getColumnIndex(VocabNoteColumns.MODIFIED_DATE));
        if (c.moveToNext()) 	nextModifDate = c.getString(c.getColumnIndex(VocabNoteColumns.MODIFIED_DATE));

        boolean modifDateChanged = (wordModifDate.compareTo(prevModifDate) != 0 || 
                                wordModifDate.compareTo(nextModifDate) != 0); 

        ViewHolder viewHolder = null;

        // We don't have recycled view, just create a fresh one
        if (convertView == null) {
            convertView = new ListItemView(mContext, null);

            TextView nameView = (TextView)convertView.findViewById(R.id.list_item_txt_word);
            TextView dateView = (TextView)convertView.findViewById(R.id.list_item_txt_date);
            ImageView reviewView = (ImageView)convertView.findViewById(R.id.noteMark);

            viewHolder = new ViewHolder(nameView, dateView, reviewView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.wordNameView.setText(wordName);
        viewHolder.wordNameView.setTag((Integer)wordState);

        // Only show icons for learnt words, other words have no icons
        if ((wordState & WordState.STATE_LEARNT) > 0) { 
            viewHolder.wordStateView.setVisibility(View.VISIBLE);
            viewHolder.wordStateView.setBackgroundResource(R.drawable.ic_learnt_dark);
        } else {
            viewHolder.wordStateView.setVisibility(View.GONE);
        }

        // If this word needs a new header, e.g, new date
        if (modifDateChanged) {
            viewHolder.wordDateView.setVisibility(View.VISIBLE);
            String[] parts = wordModifDate.split("-");
            viewHolder.wordDateView.setText(String.format("%s-%s-%s", parts[2], parts[1], parts[0]));
        } else {
            viewHolder.wordDateView.setVisibility(View.GONE);
        }

        return convertView;
    }

    private class ViewHolder {
        public TextView wordNameView;
        public TextView wordDateView;
        public ImageView wordStateView;

        public ViewHolder(TextView nameView, TextView dateView, ImageView stateView) {
            wordNameView = nameView;
            wordDateView = dateView;
            wordStateView = stateView;
        }
    }
    
    private FilterQueryProvider filterQueryProvider = new FilterQueryProvider() {
        public Cursor runQuery(CharSequence constraint) {

            Log.d(TAG, "Query(" + constraint.toString() + ")");
            
            Map<String, String> filterParams = parseQueryString((String)constraint);
            ContentResolver resolver = mContext.getContentResolver();
            Cursor oldCursor = getCursor();
            
            StringBuilder whereBuilder = new StringBuilder();
            List<String> whereArgs = new ArrayList<String>();

            // If search pattern is set
            if (filterParams.containsKey(WordFilterOptions.FILTER_WORD)) {
                String wordSearch = filterParams.get(WordFilterOptions.FILTER_WORD);
                if (!TextUtils.isEmpty(wordSearch)) {
                    whereBuilder.append(VocabNoteColumns.NAME + " LIKE ?");
                    whereArgs.add("%" + wordSearch + "%");
                }
            }
            
            // Filter by word state
            if (filterParams.containsKey(WordFilterOptions.FILTER_STATE)) {
                String wordState = filterParams.get(WordFilterOptions.FILTER_STATE);
                
                if (wordState.compareTo(String.valueOf(WordState.STATE_NONE)) != 0) {
                    if (whereBuilder.length() > 0) whereBuilder.append(" AND ");

                    whereBuilder.append("(");
                    whereBuilder.append(VocabNoteColumns.MARK_REVIEW + " & " + wordState + " > 0 ");
                    
                    int unlearnt = WordState.STATE_REVIEW_PRONUN 
                             | WordState.STATE_REVIEW_GRAMMA
                             | WordState.STATE_REVIEW_PHRASA
                             | WordState.STATE_REVIEW_COLLOC;

                    // Hmm, this is un exceptional yet odd case
                    if (wordState.compareTo(String.valueOf(unlearnt)) == 0) {
                        whereBuilder.append(" OR " + VocabNoteColumns.MARK_REVIEW + "='" + WordState.STATE_UNLEARNT + "'");
                    }

                    whereBuilder.append(")");
                }
            }

            // Filter by word note, e.g, has or has not note
            if (filterParams.containsKey(WordFilterOptions.FILTER_NOTE)) {
                int wordNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;

                try {
                    wordNote = Integer.parseInt(filterParams.get(WordFilterOptions.FILTER_NOTE));
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
                
                // If note filter option is WITH or WITHOUT
                if (wordNote != WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE) {
                    boolean andAppended = false;
            
                    if (whereBuilder.length() > 0) {
                        whereBuilder.append(" AND (");
                        andAppended = true;
                    }
                    whereBuilder.append(VocabNoteColumns.NOTE); 

                    if (wordNote == WordFilterOptions.NoteFilterOptions.NOTE_FILTER_WITH) {
                        whereBuilder.append(" IS NOT NULL and ");
                        whereBuilder.append(VocabNoteColumns.NOTE);
                        whereBuilder.append(" != '' ");
                    } else {
                        whereBuilder.append(" IS NULL or ");
                        whereBuilder.append(VocabNoteColumns.NOTE); 
                        whereBuilder.append(" = '' ");
                    }
                    
                    if (andAppended) whereBuilder.append(" ) ");
                }
            }
            
            if (filterParams.containsKey(WordFilterOptions.FILTER_DATE)) {
                String filterDate = filterParams.get(WordFilterOptions.FILTER_DATE);

                // Zero date means no filter on date
                if (!TextUtils.isEmpty(filterDate)) {
                    if (whereBuilder.length() > 0) whereBuilder.append(" AND ");

                    whereBuilder.append(VocabNoteColumns.MODIFIED_DATE);
                    whereBuilder.append("='");
                    whereBuilder.append(filterDate);
                    whereBuilder.append("'");
                }
            }

            Log.d(TAG, "WHERE CLAUSE(" + whereBuilder + ")");
            
            Cursor newCursor = resolver.query(VocabNoteColumns.CONTENT_URI, 
                        mProjection, whereBuilder.toString(), whereArgs.toArray(new String[] {}) , null);

            Activity activity = (Activity)mContext;
            if (oldCursor != null) activity.stopManagingCursor(oldCursor);
            if (newCursor != null) activity.startManagingCursor(newCursor);

            return newCursor;
        }

        private Map<String, String> parseQueryString(String query) {
            Map<String, String> parameters = new HashMap<String, String>();
            String[] parts = query.split("&");

            for (int i  = 0; i < parts.length; ++i) {
                String[] nameKeyPair = parts[i].split("=");
                if (nameKeyPair.length == 2) {
                    parameters.put(nameKeyPair[0], nameKeyPair[1]);
                }
            }

            return parameters;
        }
    };
}
