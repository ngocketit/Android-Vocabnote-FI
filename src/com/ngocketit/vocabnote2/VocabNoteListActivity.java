package com.ngocketit.vocabnote2;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.Filter.FilterListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

import com.ngocketit.vocabnote2.VocabNote.VocabNoteColumns;
import com.ngocketit.vocabnote2.adapters.DropdownMenuAdapter.DropdownMenuItem;
import com.ngocketit.vocabnote2.adapters.WordListAdapter;
import com.ngocketit.vocabnote2.widgets.ActionItem;
import com.ngocketit.vocabnote2.widgets.DropdownMenu;
import com.ngocketit.vocabnote2.widgets.QuickAction;
import com.ngocketit.vocabnote2.widgets.QuickAction.OnActionItemClickListener;
import com.ngocketit.vocabnote2.widgets.WordStatePanelView;

public class VocabNoteListActivity extends Activity
{
    private static final String TAG = "VocabNoteListActivity";
    private static final int DIALOG_ID_ABOUT 	= 0x1;
    private static final int DIALOG_ID_WORD_STATE	= 0x2;
    private static final int DIALOG_ID_TTS_CONFIRM	= 0x3;

    private static final String WORD_LIST_STATE	= "wordsListState";
    public static final String FILTER_DATE		= "wordFilterDate";
    private static final String FILTER_ITEM_INDEX	= "filterItemIndex";
    
    private static final int CODE_PICK_WORD_BY_DATE = 0x1;
    private static final int CODE_CHECK_TTS_DATA	= 0x2;

    private ListView mWordList = null;
    private EditText mWordSearch = null;
    private ImageButton mBtnWordFilter = null;
    private QuickAction mQuickAction = null;
    private DropdownMenu mFilterMenu = null;

    private Parcelable mWordListState = null;

    private String mSelectedWord = null;
    private long mSelectedWordId = 0;
    private int mSelectedWordState = 0;
    private boolean mWaitingForTTS = false;
    private int mFilterItemIndex = 0;

    private WordFilterOptions mFilterOptions = null;
    private SimpleCursorAdapter mWordListAdapter = null;
    private TextToSpeech mTTS = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        mWordList = (ListView)findViewById(R.id.main_lst_words);
        mWordSearch = (EditText)findViewById(R.id.search_bar_etxt_word);
        mWordList.setEmptyView(findViewById(R.id.word_list_empty_view));
        
        (mBtnWordFilter = (ImageButton)findViewById(R.id.action_bar_btn_filter)).setOnClickListener(mOnClickListener);
        ((ImageButton)findViewById(R.id.action_bar_btn_add)).setOnClickListener(mOnClickListener);

        buildQuickActionBar();
        buildFilterMenu();

        ((ImageButton)findViewById(R.id.search_bar_btn_clear)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mWordSearch.setText("");
            }
        });
        
        mWordSearch.addTextChangedListener(mSearchTextWatcher);
        
        mWordListAdapter = new WordListAdapter(this);
        mWordList.setAdapter(mWordListAdapter);
        
        mWordList.setLongClickable(true);
        mWordList.setOnItemLongClickListener(mListItemLongClickListener);
        mWordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                    String word = ((TextView) view.findViewById(R.id.list_item_txt_word)).getText().toString();
                    copyWord(word);
            }
        });

        mFilterOptions = new WordFilterOptions();
    }

    @Override
    public void onResume() {
        super.onResume();
        filterWord();

        if (mWordListState != null) {
            mWordList.onRestoreInstanceState(mWordListState);
            mWordListState = null;
        }

        DropdownMenuItem item = mFilterMenu.getItemAt(mFilterItemIndex);
        mBtnWordFilter.setImageResource(item.iconId);
    }
    
    private void buildQuickActionBar() {
        Resources res = getResources();
        
        mQuickAction = new QuickAction(this);

        ActionItem itemToggle = new ActionItem(QuickActionIDs.QA_ID_MARK_STATE,
                    res.getString(R.string.qa_mark),
                    res.getDrawable(R.drawable.ic_mark));
        
        ActionItem itemDelete = new ActionItem(QuickActionIDs.QA_ID_DELETE,
                    res.getString(R.string.qa_delete),
                    res.getDrawable(R.drawable.ic_delete));
        
        ActionItem itemLongman = new ActionItem(QuickActionIDs.QA_ID_OPEN_LONGMAN,
                    res.getString(R.string.qa_longman),
                    res.getDrawable(R.drawable.ic_longman));
        
        ActionItem itemCambridge = new ActionItem(
                    QuickActionIDs.QA_ID_OPEN_CAMBRIDGE,
                    res.getString(R.string.qa_cambridge),
                    res.getDrawable(R.drawable.ic_cambridge));
        
        ActionItem itemTranslate = new ActionItem(QuickActionIDs.QA_ID_TRANSLATE,
                    res.getString(R.string.qa_translate),
                    res.getDrawable(R.drawable.ic_translate));
        
        ActionItem itemSpeak = new ActionItem(QuickActionIDs.QA_ID_SPEAK,
                    res.getString(R.string.qa_speak),
                    res.getDrawable(R.drawable.ic_speak));
        
        ActionItem itemSearch = new ActionItem(QuickActionIDs.QA_ID_SEARCH_WEB,
                    res.getString(R.string.qa_search),
                    res.getDrawable(R.drawable.ic_search));
        
        ActionItem itemNote = new ActionItem(QuickActionIDs.QA_ID_TAKE_NOTE,
                    res.getString(R.string.qa_note),
                    res.getDrawable(R.drawable.ic_note));

        ActionItem itemCopy = new ActionItem(QuickActionIDs.QA_ID_COPY,
                    res.getString(R.string.qa_copy),
                    res.getDrawable(R.drawable.ic_copy));

        mQuickAction.addActionItem(itemCopy);

        //mQuickAction.addActionItem(itemCambridge);
        //mQuickAction.addActionItem(itemLongman);
        
        //mQuickAction.addActionItem(itemTranslate);
        //mQuickAction.addActionItem(itemSpeak);

        mQuickAction.addActionItem(itemNote);
        mQuickAction.addActionItem(itemSearch);

        mQuickAction.addActionItem(itemToggle);
        mQuickAction.addActionItem(itemDelete);
        
        mQuickAction.setOnActionItemClickListener(mQuickActionItemClickListener);
    }
    
    private void buildFilterMenu() {
        final List<DropdownMenuItem> items = new ArrayList<DropdownMenuItem>();

        items.add(new DropdownMenuItem(R.drawable.ic_show_all,	
                R.string.dmi_show_all,
                WordFilterOptionIDs.WORD_FILTER_NONE));

        items.add(new DropdownMenuItem(R.drawable.ic_learnt,
                R.string.dmi_learnt_words,
                WordFilterOptionIDs.WORD_FILTER_LEARNT));

        items.add(new DropdownMenuItem(R.drawable.ic_unlearnt,	
                R.string.dmi_unlearnt_words,
                WordFilterOptionIDs.WORD_FILTER_UNLEARNT));

        items.add(new DropdownMenuItem(R.drawable.ic_by_date,
                R.string.dmi_by_date,
                WordFilterOptionIDs.WORD_FILTER_DATE));

        items.add(new DropdownMenuItem(R.drawable.ic_gramma,
                R.string.dmi_gramma_review,
                WordFilterOptionIDs.WORD_FILTER_GRAMMA));

        items.add(new DropdownMenuItem(R.drawable.ic_colloc,
                R.string.dmi_colloc_review,
                WordFilterOptionIDs.WORD_FILTER_COLLOC));

        items.add(new DropdownMenuItem(R.drawable.ic_pronun,
                R.string.dmi_pronun_review,
                WordFilterOptionIDs.WORD_FILTER_PRONUN));

        items.add(new DropdownMenuItem(R.drawable.ic_phrasa,
                R.string.dmi_phrasa_review,
                WordFilterOptionIDs.WORD_FILTER_PHRASA));

        items.add(new DropdownMenuItem(R.drawable.ic_with_note,
                R.string.dmi_with_note,
                WordFilterOptionIDs.WORD_FITLER_WITH_NOTE));

        mFilterMenu = new DropdownMenu(this, items);
        mFilterMenu.setOnItemClickListener(mFilterMenuClickListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFilterMenu.dismiss();
        if (mTTS != null) mTTS.shutdown();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mWordListState = mWordList.onSaveInstanceState();
        outState.putParcelable(WORD_LIST_STATE, mWordListState);
        mFilterOptions.saveToBundle(outState);
        outState.putInt(FILTER_ITEM_INDEX, mFilterItemIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mWordListState = savedInstanceState.getParcelable(WORD_LIST_STATE);
            mFilterOptions.restoreFromBundle(savedInstanceState);
            mFilterItemIndex = savedInstanceState.getInt(FILTER_ITEM_INDEX);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CODE_PICK_WORD_BY_DATE && resultCode == RESULT_OK) {

            // Only filter by date and word (in search box)
            // ignoring other filter criteria
            mFilterOptions.filterState = WordState.STATE_NONE;
            mFilterOptions.filterNote  = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;
            mFilterOptions.filterDate  = data.getStringExtra(FILTER_DATE);

            DropdownMenuItem menuItem = mFilterMenu.getItemById(WordFilterOptionIDs.WORD_FILTER_DATE);
            mBtnWordFilter.setImageResource(menuItem.iconId);
            
            filterWord();

        } else if (requestCode == CODE_CHECK_TTS_DATA) {
            // Text to speech data is already available on the device
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            mTTS.setLanguage(Locale.ENGLISH);
                            ttsSpeakWord(mSelectedWord);
                            mWaitingForTTS = false;
                        }
                    }
                });
            } 
            // Not availabe, inform user to download & install it
            else {
                showDialog(DIALOG_ID_TTS_CONFIRM);
            }
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = true;

        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = (info != null);
        } catch (PackageManager.NameNotFoundException ex) {
            installed = false;
            ex.printStackTrace();
        }
        return installed;
    }

    private void openCambridgeDict(String word) {
        final String caldPackage = "com.mobisystems.msdict.embedded.wireless.cambridge.cald";
        final String caldAudioPackage = caldPackage + ".audio";
        final String articleActivity = "com.mobisystems.msdict.viewer.ArticleActivity";
        final String caldDbFile = "CALD_no_sound.pdb";
        final String caldAudioDbFile = "CALD-sound.pdb";

        boolean audioVersion = true;

        // First, check if CALD audio is installed (we prefer audio version)
        if (!isAppInstalled(caldAudioPackage)) {
            audioVersion = false;

            if (!isAppInstalled(caldPackage)) {
                Toast.makeText(this, getResources().getString(R.string.cambridge_not_installed),Toast.LENGTH_SHORT)
                .show();
                return;
            }
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setComponent(new ComponentName(audioVersion ? caldAudioPackage : caldPackage, articleActivity));
        i.setData(Uri.parse("mobisystems.com.cgi-bin2.pdb.file=" 
                            + (audioVersion ? caldAudioDbFile : caldDbFile) 
                            + "?article&open&txt="
                            + Uri.encode(word)));
        startActivity(i);
    }

    private void openLongmanDict(String word) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            
            i.setData(Uri.parse("word=" + Uri.encode(word)));
            i.putExtra("word", word);
            
            i.setComponent(new ComponentName("com.mobifusion.android.ldoce5",
                        "com.mobifusion.android.ldoce5.WordActivity"));
            
            startActivity(i);
                
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, getResources().getString(R.string.longman_not_installed), Toast.LENGTH_SHORT)
            .show();
                
        } catch (Exception ex) {
            Toast.makeText(this, getResources().getString(R.string.failed_open_longman), Toast.LENGTH_SHORT)
            .show();
        }
    }

    private void openGoogleTranslateApps(String word, String toLang) {
        try {
            Intent i = new Intent();

            i.setAction(Intent.ACTION_VIEW);

            i.putExtra("key_text_input", word);
            i.putExtra("key_text_output", "");
            i.putExtra("key_language_from", "en");
            i.putExtra("key_language_to", "vi");
            i.putExtra("key_suggest_translation", "");
            i.putExtra("key_from_floating_window", false);

            i.setComponent(new ComponentName(
                        "com.google.android.apps.translate",
                        "com.google.android.apps.translate.TranslateActivity"));
            
            startActivity(i);
                
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, getResources().getString(R.string.google_translate_not_installed), Toast.LENGTH_SHORT)
            .show();
            
            ex.printStackTrace();
        }
    }

    public void searchWordOnWeb(String word) {
        Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
        i.putExtra(SearchManager.QUERY, word);
        startActivity(i);
    }


    private void checkTTS() {
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CODE_CHECK_TTS_DATA);
    }

    private void speakWord(String word) {

        // If TTS instance is already created
        if (mTTS != null) ttsSpeakWord(word);

        // Otherwise, check for TTS availability
        else { 
            mWaitingForTTS = true;
            checkTTS();
        }
    }

    private void ttsSpeakWord(String word) {
        if (mTTS == null) return;

        int code = mTTS.isLanguageAvailable(Locale.ENGLISH);

        if (code == TextToSpeech.LANG_MISSING_DATA || code == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this, R.string.tts_language_not_available, Toast.LENGTH_SHORT)
            .show(); 
        } else {
            mTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    
    private void filterWord() {
        String filterQuery = mFilterOptions.queryString();
        // We don't need a filter listener any more
        mWordListAdapter.getFilter().filter(filterQuery, null);
    }

    private void deleteItem(final long itemId) {
        final Resources res = getResources();

        new AlertDialog.Builder(this)
        .setTitle(res.getString(R.string.delete_item))
        .setMessage(res.getString(R.string.delete_item_message))
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int count = getContentResolver().delete(VocabNoteColumns.CONTENT_URI, VocabNoteColumns._ID + "="
                                            + String.valueOf(itemId), null);
                if (count == 1) {
                    Toast.makeText(VocabNoteListActivity.this, 
                    res.getString(R.string.delete_success), 
                    Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(VocabNoteListActivity.this, 
                    res.getString(R.string.delete_failure),
                    Toast.LENGTH_LONG).show();
                }
            }
        })
        .setNegativeButton(android.R.string.no, null)
        .show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_ID_ABOUT:
            TextView v = new TextView(this);
            v.setText(Html.fromHtml(getResources().getString(
                            R.string.about_message)));
            v.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
            v.setPadding(5, 5, 5, 5);

            return (new AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setPositiveButton(android.R.string.ok, null).setView(v)
            .create());

        case DIALOG_ID_WORD_STATE:
            final WordStatePanelView contentView = new WordStatePanelView(this);
            contentView.setState(mSelectedWordState);

            return (new AlertDialog.Builder(this)
            .setView(contentView)
            .setTitle(R.string.change_word_state)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            updateWordState(mSelectedWordId, contentView.getState());
                    }
            }))
            .create();

        case DIALOG_ID_TTS_CONFIRM:
            return (new AlertDialog.Builder(this)
            .setTitle(R.string.tts_download_title)
            .setMessage(R.string.tts_download_confirm)
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                            startActivity(intent);
                    }
            }))
            . create();
        }

        return super.onCreateDialog(id);
    }

    private void updateWordState(long wordId, int state) {
        final Resources res = getResources();
        Uri itemUri = Uri.withAppendedPath(VocabNoteColumns.CONTENT_URI, String.valueOf(wordId));
        ContentValues values = new ContentValues();
        values.put(VocabNoteColumns.MARK_REVIEW, state);
                                
        int count = getContentResolver().update(itemUri, values, null, null);
        if (count == 1) {
            Toast.makeText(VocabNoteListActivity.this, 
            res.getString(R.string.update_success), 
            Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(VocabNoteListActivity.this, 
            res.getString(R.string.update_failure), 
            Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.itemAddWord:
            Intent i = new Intent(this, AddWordActivity.class);
            startActivity(i);
            break;

        case R.id.itemAbout:
            showDialog(DIALOG_ID_ABOUT);
            break;
        }
        return true;
    }
    
    private void showFilterMenu() {
        View titleBar = findViewById(R.id.action_bar);
        int[] locations = new int[2];
        titleBar.getLocationOnScreen(locations);

        mFilterMenu.showAtLocation(mWordList, Gravity.TOP | Gravity.RIGHT, 0, 
                            titleBar.getHeight() + locations[1] - 10);
    }

    
    private OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

            case R.id.action_bar_btn_add:
                Intent iAdd = new Intent(VocabNoteListActivity.this, AddWordActivity.class);
                startActivity(iAdd);
                break;

            case R.id.action_bar_btn_filter:
                showFilterMenu();
                break;
            }
        }
    };
    
    private OnActionItemClickListener mQuickActionItemClickListener = new OnActionItemClickListener() {
        
        @Override
        public void onItemClick(QuickAction source, int pos, int actionId) {
            switch (actionId) {
            
            case QuickActionIDs.QA_ID_MARK_STATE:
                showDialog(DIALOG_ID_WORD_STATE);
                break;

            case QuickActionIDs.QA_ID_DELETE:
                deleteItem(mSelectedWordId);
                break;

            case QuickActionIDs.QA_ID_OPEN_CAMBRIDGE:
                openCambridgeDict(mSelectedWord);
                break;

            case QuickActionIDs.QA_ID_OPEN_LONGMAN:
                openLongmanDict(mSelectedWord);
                break;

            case QuickActionIDs.QA_ID_TRANSLATE:
                openGoogleTranslateApps(mSelectedWord, "vi");
                break;

            case QuickActionIDs.QA_ID_SEARCH_WEB:
                searchWordOnWeb(mSelectedWord);
                break;

            case QuickActionIDs.QA_ID_TAKE_NOTE:
                Intent i = new Intent(VocabNoteListActivity.this, ViewWordActivity.class);
                i.putExtra("wordId", mSelectedWordId);
                startActivity(i);
                break;

            case QuickActionIDs.QA_ID_SPEAK:
                speakWord(mSelectedWord);
                break;

            case QuickActionIDs.QA_ID_COPY:
                copyWord(mSelectedWord);
                break;
            }
        }
    };

    private void copyWord(String word) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            clipboard.setText("text to clip");
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE); 
            android.content.ClipData clip = android.content.ClipData.newPlainText("vocab_word", word);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(VocabNoteListActivity.this, 
        getResources().getString(R.string.copy_success), 
        Toast.LENGTH_LONG).show();
    }
    
    private TextWatcher mSearchTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String word = s.toString();
            mFilterOptions.filterWord = word;
            filterWord();
        }
    };
    
    private OnItemLongClickListener mListItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
            TextView wordNameView = (TextView)view.findViewById(R.id.list_item_txt_word);
            mSelectedWord = wordNameView.getText().toString();
            mSelectedWordState = (Integer)(wordNameView.getTag());
            mSelectedWordId = id;
            mQuickAction.show(view);

            return true;
        }
    };
    
    private OnItemClickListener mFilterMenuClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DropdownMenuItem clickedItem = mFilterMenu.getItemAt(position);
            mFilterItemIndex = position;
                    
            if (clickedItem.itemId == WordFilterOptionIDs.WORD_FILTER_DATE) {
                
                Intent dateIntent = new Intent(VocabNoteListActivity.this, WordDateFilterActivity.class);
                startActivityForResult(dateIntent, CODE_PICK_WORD_BY_DATE);
                    
            } else {
                mBtnWordFilter.setImageResource(clickedItem.iconId);
                
                switch (clickedItem.itemId) {
                case WordFilterOptionIDs.WORD_FILTER_NONE:
                    mFilterOptions.filterDate = "";
                    mFilterOptions.filterState = WordState.STATE_NONE;
                    mFilterOptions.filterNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;
                    break;
                        
                case WordFilterOptionIDs.WORD_FILTER_COLLOC:
                    mFilterOptions.filterDate = "";
                    mFilterOptions.filterNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;
                    mFilterOptions.filterState = WordState.STATE_REVIEW_COLLOC;
                    break;
                        
                case WordFilterOptionIDs.WORD_FILTER_GRAMMA:
                    mFilterOptions.filterDate = "";
                    mFilterOptions.filterNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;
                    mFilterOptions.filterState = WordState.STATE_REVIEW_GRAMMA;
                    break;
                        
                case WordFilterOptionIDs.WORD_FILTER_PHRASA:
                    mFilterOptions.filterDate = "";
                    mFilterOptions.filterNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;
                    mFilterOptions.filterState = WordState.STATE_REVIEW_PHRASA;
                    break;
                        
                case WordFilterOptionIDs.WORD_FILTER_PRONUN:
                    mFilterOptions.filterDate = "";
                    mFilterOptions.filterState = WordState.STATE_REVIEW_PRONUN;
                    mFilterOptions.filterNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;
                    break;
                        
                case WordFilterOptionIDs.WORD_FILTER_LEARNT:
                    mFilterOptions.filterDate = "";
                    mFilterOptions.filterNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;
                    mFilterOptions.filterState = WordState.STATE_LEARNT;
                    break;
                        
                case WordFilterOptionIDs.WORD_FILTER_UNLEARNT:
                    mFilterOptions.filterDate = "";
                    mFilterOptions.filterNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_IGNORE;
                    mFilterOptions.filterState = WordState.STATE_ALL ^ WordState.STATE_LEARNT;
                    break;
                        
                case WordFilterOptionIDs.WORD_FITLER_WITH_NOTE:
                    mFilterOptions.filterDate = "";
                    mFilterOptions.filterNote = WordFilterOptions.NoteFilterOptions.NOTE_FILTER_WITH;
                    mFilterOptions.filterState = WordState.STATE_NONE;
                    break;
                }
                
                filterWord();
            }
            mFilterMenu.dismiss();
        }
    };

    private interface QuickActionIDs {
        static final int QA_ID_MARK_STATE       = 0x1;
        static final int QA_ID_DELETE   	= 0x2;
        static final int QA_ID_OPEN_CAMBRIDGE   = 0x3;
        static final int QA_ID_OPEN_LONGMAN	= 0x4;
        static final int QA_ID_TRANSLATE	= 0x5;
        static final int QA_ID_SEARCH_WEB	= 0x6;
        static final int QA_ID_TAKE_NOTE	= 0x7;
        static final int QA_ID_SPEAK		= 0x8;
        static final int QA_ID_COPY		= 0x9;
    }
    
    private interface WordFilterOptionIDs {
        // No filter, show all words
        static final int WORD_FILTER_NONE = 0x1;
        
        // Words modified in a specified date
        static final int WORD_FILTER_DATE = 0x2;
        
        // Words that have been marked as learnt
        static final int WORD_FILTER_LEARNT = 0x3;
        
        // Words that need any kinds of review
        static final int WORD_FILTER_UNLEARNT = 0x4;
        
        // Words that contain notes
        static final int WORD_FITLER_WITH_NOTE = 0x5;
        
        // Words that need pronunciation review
        static final int WORD_FILTER_PRONUN	= 0x6;
        
        // Words need grammar review
        static final int WORD_FILTER_GRAMMA = 0x7;
        
        // Words need collocation review
        static final int WORD_FILTER_COLLOC = 0x8;
        
        // Words need phrasal verb review
        static final int WORD_FILTER_PHRASA = 0x9;
    }
}
