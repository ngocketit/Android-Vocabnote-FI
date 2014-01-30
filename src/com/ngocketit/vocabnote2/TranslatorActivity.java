package com.ngocketit.vocabnote2;

import com.ngocketit.vocabnote2.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TranslatorActivity extends Activity {
    private static final String TAG = "TranslatorActivity";
    
    private Spinner spinnerLanguage = null;
    private TextView translatedTextView = null;
    private ProgressDialog translatingDlg = null;
    private String word = null;
    private TranslatorAsyncTask translator = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translator);
        
        translatedTextView = (TextView)findViewById(R.id.txtTranslatedText);
        Button btnTranslate = (Button)findViewById(R.id.btnTranslate);
        btnTranslate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    doTranslation();
            }
        });
        
        spinnerLanguage = (Spinner)findViewById(R.id.spinnerToLanguage);
        
        ArrayAdapter<?> toLanguagesAdapter = ArrayAdapter.createFromResource(this, R.array.language_names, android.R.layout.simple_spinner_item);
        toLanguagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(toLanguagesAdapter);
        spinnerLanguage.setSelection(0);
        
        word = getIntent().getExtras().getString("word");
        setTitle(word);
        
        doTranslation();
    }
    
    private void doTranslation() {
        String[] langValues = getResources().getStringArray(R.array.language_values);
        String toLang = langValues[spinnerLanguage.getSelectedItemPosition()];
        
        Log.d(TAG, "Translating " + word + " to language " + toLang);
        translator = new TranslatorAsyncTask(this);
        translator.execute(word, toLang);
    }
    
    public void showProgress() {
        translatingDlg = ProgressDialog.show(this, null, getResources().getString(R.string.translating));
    }
    
    public void processResult(String translatedText) {
        if (null == translatedText) {
            Toast.makeText(this, getResources().getString(R.string.translateFail), Toast.LENGTH_SHORT).show();
        } else {
            translatedTextView.setText(translatedText);
        }
        if (translatingDlg != null)
            translatingDlg.cancel();
    }
    
    @Override
    public void onBackPressed() {
        if (translatingDlg != null) {
            translatingDlg.cancel();
        }
        
        if (translator != null) 
            translator.cancel(true);
        
        super.onBackPressed();
    }
}
