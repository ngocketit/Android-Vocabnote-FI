package com.ngocketit.vocabnote2;

import android.os.Bundle;

public class WordFilterOptions
{
    public static final String FILTER_WORD	= "wordSearch";
    public static final String FILTER_STATE	= "wordState";
    public static final String FILTER_NOTE	= "wordNote";
    public static final String FILTER_DATE	= "wordDate";
    
    public String filterWord = "";
    public int filterState	 = WordState.STATE_NONE;
    public int filterNote = NoteFilterOptions.NOTE_FILTER_IGNORE;
    public String filterDate = "";
    
    public WordFilterOptions() {

    }
    
    public WordFilterOptions(String filterWord, int filterState, 
                int filterNote, String filterDate) {
        this.filterWord  = filterWord;
        this.filterState = filterState;
        this.filterNote  = filterNote;
        this.filterDate  = filterDate;
    }
    
    public void saveToBundle(Bundle outState) {
        outState.putString(FILTER_WORD, filterWord);
        outState.putInt(FILTER_STATE, filterState);
        outState.putInt(FILTER_NOTE, filterNote);
        outState.putString(FILTER_DATE, filterDate);
    }
    
    public void restoreFromBundle(Bundle inState) {
        filterWord = inState.getString(FILTER_WORD);
        filterState = inState.getInt(FILTER_STATE);
        filterNote = inState.getInt(FILTER_NOTE);
        filterDate = inState.getString(FILTER_DATE);
    }
    
    public String queryString() {
        return new StringBuilder()
        .append(FILTER_WORD).append("=")
        .append(filterWord) 
        .append("&")
        
        .append(FILTER_NOTE).append("=")
        .append(filterNote) 
        .append("&")
        
        .append(FILTER_DATE).append("=")
        .append(filterDate) 
        .append("&")
        
        .append(FILTER_STATE).append("=")
        .append(filterState) 
        .toString();
    }

    public static class NoteFilterOptions {
        // No filter on note
        public static int NOTE_FILTER_IGNORE  = 0x0;

        // Only ones with notes
        public static int NOTE_FILTER_WITH    = 0x1;

        // Only ones without notes
        public static int NOTE_FILTER_WITHOUT = 0x2;
    }
}
