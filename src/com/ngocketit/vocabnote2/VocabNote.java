package com.ngocketit.vocabnote2;

import android.net.Uri;
import android.provider.BaseColumns;

public class VocabNote {
    
    public static final String AUTHORITY = "com.ngocketit.vocabnote2.provider.VocabNoteBook";
    
    public static final String DATE_URI_PATH = "dates";
    
    private VocabNote() {
            
    }
    
    public static final class VocabNoteColumns implements BaseColumns {
            
        private VocabNoteColumns() {
                
        }
            
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/vocabnotes");
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.vocabnote";
        
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.vocabnote";
        
        public static final String DEFAULT_SORT_ORDER = "modified_date DESC, name ASC";
        
        public static final String NAME = "name";
        
        public static final String NOTE = "category";
        
        public static final String ADDED_DATE = "added_date";
        
        public static final String MODIFIED_DATE = "modified_date";
        
        public static final String MARK_REVIEW = "mark_for_review";
    }
}
