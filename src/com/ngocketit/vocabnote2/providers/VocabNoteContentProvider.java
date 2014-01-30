package com.ngocketit.vocabnote2.providers;

import java.util.HashMap;
import com.ngocketit.vocabnote2.VocabNote;
import com.ngocketit.vocabnote2.VocabNote.VocabNoteColumns;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import java.util.Date;
import android.text.TextUtils;
import java.text.SimpleDateFormat;

public class VocabNoteContentProvider extends ContentProvider {
    private static final String DATABASE_NAME = "vocabnote.db";
    private static final int DATABASE_VERSION = 3;
    private static final String VOCAB_NOTE_TABLE = "tbl_VocabNotes";
    
    private static UriMatcher sUriMatcher;
    private static HashMap<String, String> sNotesProjectionMap;
    
    private DatabaseHelper mDatabaseHelper;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            
            db.execSQL("CREATE TABLE " + VOCAB_NOTE_TABLE + " ("
                    + VocabNoteColumns._ID + " INTEGER PRIMARY KEY,"
                    + VocabNoteColumns.NAME + " TEXT,"
                    + VocabNoteColumns.NOTE + " TEXT,"
                    + VocabNoteColumns.ADDED_DATE + " TEXT,"
                    + VocabNoteColumns.MODIFIED_DATE + " TEXT,"
                    + VocabNoteColumns.MARK_REVIEW + " INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + VOCAB_NOTE_TABLE);
            onCreate(db);
        }
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int count = 0;
        
        switch (sUriMatcher.match(uri)) {
        case QueryUriTypes.VOCAB_NOTES:
            count = db.delete(VOCAB_NOTE_TABLE, where, whereArgs);
            break;
                
        case QueryUriTypes.VOCAB_NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            String whereClause = VocabNoteColumns._ID + "=" + noteId + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : "");
            count = db.delete(VOCAB_NOTE_TABLE, whereClause, whereArgs);
            break;
                
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(VocabNoteColumns.CONTENT_URI, null);
        
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        
        case QueryUriTypes.VOCAB_NOTES:
        case QueryUriTypes.VOCAB_NOTE_DATES:
            return VocabNoteColumns.CONTENT_TYPE;
                
        case QueryUriTypes.VOCAB_NOTE_ID:
            return VocabNoteColumns.CONTENT_ITEM_TYPE;
                
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues params) {
        if (sUriMatcher.match(uri) != QueryUriTypes.VOCAB_NOTES) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        
        ContentValues values = (params != null ? params : new ContentValues());
        
        SimpleDateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd");
        String today = dfmt.format(new Date());

        if (!values.containsKey(VocabNoteColumns.NAME)) {
            throw new IllegalArgumentException("Word is not specified");
        }
        
        if (!values.containsKey(VocabNoteColumns.MARK_REVIEW)) {
            values.put(VocabNoteColumns.MARK_REVIEW, 0);
        }
        
        if (!values.containsKey(VocabNoteColumns.ADDED_DATE)) {
            values.put(VocabNoteColumns.ADDED_DATE, today);
        }
        
        if (!values.containsKey(VocabNoteColumns.MODIFIED_DATE)) {
            values.put(VocabNoteColumns.MODIFIED_DATE, today);
        }
        
        if (!values.containsKey(VocabNoteColumns.NOTE)) {
            values.put(VocabNoteColumns.NOTE, "");
        }
        
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        long id = db.insert(VOCAB_NOTE_TABLE, VocabNoteColumns.NOTE, values);

        if (id > 0) {
            Uri itemUri = Uri.withAppendedPath(VocabNoteColumns.CONTENT_URI, String.valueOf(id));
            getContext().getContentResolver().notifyChange(VocabNoteColumns.CONTENT_URI, null);
            return itemUri;
        }
        
        return null;
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                String sortOrder) {
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(VOCAB_NOTE_TABLE);
        
        boolean noteDates = false;
        
        switch (sUriMatcher.match(uri)) {
        case QueryUriTypes.VOCAB_NOTES:
            qb.setProjectionMap(sNotesProjectionMap);
            break;
                
        case QueryUriTypes.VOCAB_NOTE_ID:
            qb.setProjectionMap(sNotesProjectionMap);
            String itemId = uri.getPathSegments().get(1);
            qb.appendWhere(VocabNoteColumns._ID + "=" + itemId);
            break;
                
        case QueryUriTypes.VOCAB_NOTE_DATES:
            noteDates = true;
            break;
                
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        String sortBy = TextUtils.isEmpty(sortOrder) ? VocabNoteColumns.DEFAULT_SORT_ORDER : sortOrder;
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        
        Cursor c = null;
        if (!noteDates) c = qb.query(db, projection, selection, selectionArgs, null, null, sortBy);
        else c = db.rawQuery(RawQueries.QUERY_NOTE_DATES, null);
        
        c.setNotificationUri(getContext().getContentResolver(), uri);
        
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int count = 0;
        
        switch (sUriMatcher.match(uri)) {
        case QueryUriTypes.VOCAB_NOTES:
            count = db.update(VOCAB_NOTE_TABLE, values, where, whereArgs);
            break;
                
        case QueryUriTypes.VOCAB_NOTE_ID:
            String itemId = uri.getPathSegments().get(1);
            String whereClause = VocabNoteColumns._ID + "=" + itemId + (!TextUtils.isEmpty(where) ? (" AND (" + where + ")") : ""); 
            count = db.update(VOCAB_NOTE_TABLE, values, whereClause, whereArgs);
            break;
                
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        
        // Get items in this table 
        sUriMatcher.addURI(VocabNote.AUTHORITY, "vocabnotes", QueryUriTypes.VOCAB_NOTES);
        
        // Get one item in this table based on item ID
        sUriMatcher.addURI(VocabNote.AUTHORITY, "vocabnotes/#", QueryUriTypes.VOCAB_NOTE_ID);
        
        // Get summary about items for each date, e.g, date & number of items
        sUriMatcher.addURI(VocabNote.AUTHORITY, "vocabnotes/" + VocabNote.DATE_URI_PATH, QueryUriTypes.VOCAB_NOTE_DATES);
        
        sNotesProjectionMap = new HashMap<String, String>();
        sNotesProjectionMap.put(VocabNoteColumns._ID, VocabNoteColumns._ID);
        sNotesProjectionMap.put(VocabNoteColumns.NAME, VocabNoteColumns.NAME);
        sNotesProjectionMap.put(VocabNoteColumns.NOTE, VocabNoteColumns.NOTE);
        sNotesProjectionMap.put(VocabNoteColumns.ADDED_DATE, VocabNoteColumns.ADDED_DATE);
        sNotesProjectionMap.put(VocabNoteColumns.MODIFIED_DATE, VocabNoteColumns.MODIFIED_DATE);
        sNotesProjectionMap.put(VocabNoteColumns.MARK_REVIEW, VocabNoteColumns.MARK_REVIEW);
    }
    
    private interface QueryUriTypes {
        // Query for all items
        static final int VOCAB_NOTES 	  = 1;
        
        // Query for one item
        static final int VOCAB_NOTE_ID	  = 2;
        
        // Custom query for dates & number of items
        static final int VOCAB_NOTE_DATES = 3;
    }
    
    private interface RawQueries {
        String QUERY_NOTE_DATES = "SELECT " 
                + VocabNoteColumns.MODIFIED_DATE + " AS " 
                + VocabNoteColumns._ID + ", COUNT(*) AS " 
                + VocabNoteColumns._COUNT + " FROM " + VOCAB_NOTE_TABLE + " GROUP BY " 
                + VocabNoteColumns._ID + " ORDER BY " 
                + VocabNoteColumns._ID + " DESC ";
    }
}
