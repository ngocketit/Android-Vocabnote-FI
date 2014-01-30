package com.ngocketit.vocabnote2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.content.Intent;
import android.widget.Toast;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import android.text.TextUtils;
import android.graphics.Color;

import com.ngocketit.vocabnote2.R;
import com.ngocketit.vocabnote2.VocabNote.VocabNoteColumns;

public class ViewWordActivity extends Activity implements View.OnClickListener {
    private long wordId = 0;
    private static final String WORD_ID = "wordId";
    private static final int VIEW_WORD_REQ_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.view_word);

        ((ImageButton)findViewById(R.id.view_word_btn_edit)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.action_bar_btn_cancel)).setOnClickListener(this);

        wordId = getIntent().getExtras().getLong("wordId", 0);
        loadWord();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.view_word_btn_edit:
            Intent i = new Intent(ViewWordActivity.this, AddWordActivity.class);
            i.putExtra(WORD_ID, wordId);
            startActivityForResult(i, VIEW_WORD_REQ_CODE);
            break;

        case R.id.action_bar_btn_cancel:
            finish();
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIEW_WORD_REQ_CODE && resultCode == RESULT_OK) {
            loadWord();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean loadWord() {
        Uri uri = Uri.withAppendedPath(VocabNoteColumns.CONTENT_URI, String.valueOf(wordId));
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        
        if (cursor == null && cursor.getCount() < 1) {
            Toast.makeText(this, "Failed to load word with ID " + wordId, Toast.LENGTH_SHORT).show();
            return false;
        }

        cursor.moveToFirst();

        TextView tvWord = (TextView)findViewById(R.id.view_word_txt_word);
        TextView tvNote = (TextView)findViewById(R.id.view_word_txt_note);
        TextView tvDate = (TextView)findViewById(R.id.view_word_txt_date);

        int wordColIndex  = cursor.getColumnIndex(VocabNoteColumns.NAME);
        int noteColIndex  = cursor.getColumnIndex(VocabNoteColumns.NOTE);
        int dateColIndex  = cursor.getColumnIndex(VocabNoteColumns.MODIFIED_DATE);
        int stateColIndex = cursor.getColumnIndex(VocabNoteColumns.MARK_REVIEW); 

        String word = cursor.getString(wordColIndex);
        String note = cursor.getString(noteColIndex);
        int state   = cursor.getInt(stateColIndex);

        String[] parts = cursor.getString(dateColIndex).split("-");
        String date = String.format("%s-%s-%s", parts[2], parts[1], parts[0]);

        tvWord.setText(word);
        if (TextUtils.isEmpty(note)) {
            tvNote.setText(getResources().getString(R.string.word_no_note));
            tvNote.setTextColor(Color.parseColor("#ff616161"));
        } else {
            tvNote.setText(note);
            tvNote.setTextColor(Color.parseColor("#ff000000"));
        }

        tvDate.setText(date);
        createStatesPanel(state);

        return true;
    }

    private void createStatesPanel(int state) {
        LinearLayout panel = (LinearLayout)findViewById(R.id.view_word_panl_states);
        panel.removeAllViews();

        if ((state & WordState.STATE_LEARNT) > 0) {
            panel.addView(createStateView(R.drawable.ic_learnt_dark));

        } else if (state == 0 || state == WordState.STATE_UNLEARNT) {
            panel.addView(createStateView(R.drawable.ic_unlearnt_dark));

        } else {
            if ((state & WordState.STATE_REVIEW_GRAMMA) > 0)
                    panel.addView(createStateView(R.drawable.ic_gramma_dark));
            
            if ((state & WordState.STATE_REVIEW_COLLOC) > 0)
                    panel.addView(createStateView(R.drawable.ic_colloc_dark));

            if ((state & WordState.STATE_REVIEW_PRONUN) > 0)
                    panel.addView(createStateView(R.drawable.ic_pronun_dark));

            if ((state & WordState.STATE_REVIEW_PHRASA) > 0)
                    panel.addView(createStateView(R.drawable.ic_phrasa_dark));
        }
    }

    private ImageView createStateView(int srcResId) {
        ImageView view = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(32, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL;

        view.setLayoutParams(layoutParams);
        view.setImageResource(srcResId);
        view.setScaleType(ImageView.ScaleType.CENTER);

        return view;
    }
}
