package com.ngocketit.vocabnote2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.ngocketit.vocabnote2.VocabNote.VocabNoteColumns;
import com.ngocketit.vocabnote2.widgets.WordStatePanelView;

public class AddWordActivity extends Activity implements View.OnClickListener {
    private static final String NEW_WORD = "newWord";
    private static final String WORD_ID = "wordId";

    private EditText newWord = null; 
    private EditText wordNote = null;
    private WordStatePanelView mStatePanelView = null;
    
    private long wordId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.add_word);
        
        OnEditorActionListener editorListener = new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveWord();
                    return true;
                }
                return false;
            }
        };

        mStatePanelView = (WordStatePanelView)findViewById(R.id.view_word_panl_states);

        newWord = (EditText)findViewById(R.id.add_word_etxt_word);
        newWord.setOnEditorActionListener(editorListener);

        wordNote = (EditText)findViewById(R.id.add_word_etxt_note);
        wordNote.setOnEditorActionListener(editorListener);
        
        ((ImageButton)findViewById(R.id.action_bar_btn_cancel)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.action_bar_btn_done)).setOnClickListener(this);
        
        // Get word which is passed from external intents
        Intent i = getIntent();
        String word = i.getStringExtra(NEW_WORD);
        newWord.setText(word);

        // If we have wordId then it's in edit mode
        wordId = i.getLongExtra(WORD_ID, 0);
        if (wordId > 0) loadWord();

        // Otherwise, unlearnt sate is default for new word
        else mStatePanelView.setState(0);

        TextView txtTitle = (TextView)findViewById(R.id.action_bar_txt_title);
        txtTitle.setText(wordId > 0 ? R.string.edit_word : R.string.add_word);
    }

    private boolean loadWord() {
        Uri uri = Uri.withAppendedPath(VocabNoteColumns.CONTENT_URI, String.valueOf(wordId));
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        
        if (cursor == null && cursor.getCount() < 1) {
            Toast.makeText(this, "Failed to load word with ID " + wordId, Toast.LENGTH_SHORT).show();
            return false;
        }

        cursor.moveToFirst();

        int wordColIndex = cursor.getColumnIndex(VocabNoteColumns.NAME);
        int noteColIndex = cursor.getColumnIndex(VocabNoteColumns.NOTE);
        int stateColIndex = cursor.getColumnIndex(VocabNoteColumns.MARK_REVIEW);

        String word = cursor.getString(wordColIndex);
        String note = cursor.getString(noteColIndex);
        int state   = cursor.getInt(stateColIndex);
        
        newWord.setText(word);
        wordNote.setText(note);
        mStatePanelView.setState(state);

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.action_bar_btn_cancel:
            hideKeyboard();
            finish();
            break;
                
        case R.id.action_bar_btn_done:
            saveWord();
            break;
        }
    }
    
    private void saveWord() {
        String word = newWord.getText().toString();
        if (TextUtils.isEmpty(word)) {
            Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String note = wordNote.getText().toString();

        ContentValues values = new ContentValues();
        values.put(VocabNoteColumns.NAME, word);
        values.put(VocabNoteColumns.NOTE, note);
        values.put(VocabNoteColumns.MARK_REVIEW, mStatePanelView.getState());
        
        if (wordId > 0) { 
            SimpleDateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd");
            String today = dfmt.format(new Date());
            values.put(VocabNoteColumns.MODIFIED_DATE, today);
        }
        
        try {
            Uri itemUri = null;
            ContentResolver resolver = getContentResolver();

            if (wordId <= 0) {
                itemUri = resolver.insert(VocabNoteColumns.CONTENT_URI, values);
            } else {
                Uri updateUri = Uri.withAppendedPath(VocabNoteColumns.CONTENT_URI, String.valueOf(wordId));
                int updatedId = resolver.update(updateUri, values, null, null);
            }

            hideKeyboard();

            if (wordId > 0) setResult(RESULT_OK);

            finish();

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }
    
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(newWord.getWindowToken(), 0);
    }
}
