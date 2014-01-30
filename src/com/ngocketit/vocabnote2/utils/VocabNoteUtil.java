package com.ngocketit.vocabnote2.utils;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class VocabNoteUtil {
    
    private VocabNoteUtil() {

    }

    public static void openCambridgeDict(Context context, String word) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri
                        .parse("mobisystems.com.cgi-bin2.pdb.file=CALD_no_sound.pdb?article&open&txt="
                                        + Uri.encode(word)));
            i.setComponent(new ComponentName(
                        "com.mobisystems.msdict.embedded.wireless.cambridge.cald",
                        "com.mobisystems.msdict.viewer.ArticleActivity"));
            context.startActivity(i);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, "Cambridge dictionary not installed",
                        Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    public static void openLongmanDict(Context context, String word) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("word=" + Uri.encode(word)));
            i.putExtra("word", word);
            i.setComponent(new ComponentName("com.mobifusion.android.ldoce5",
                        "com.mobifusion.android.ldoce5.WordActivity"));
            context.startActivity(i);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, "Longman dictionary not installed",
                        Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        } catch (Exception ex) {
            Toast.makeText(context, "Can not open Longman dictionary",
                        Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    public static void callGoogleTranslateApps(Context context, String word, String toLang) {
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
            context.startActivity(i);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, "Google translate app is not installed",
                        Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    public static void searchWeb(Context context, String word) {
        Intent i = new Intent(Intent.ACTION_WEB_SEARCH);
        i.putExtra(SearchManager.QUERY, word);
        context.startActivity(i);
    }
}
