package com.ngocketit.vocabnote2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class TranslatorAsyncTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "TranslatorAsyncTask";
    private Context context = null;
    
    private static final String ENCODING = "UTF-8";
    private static final String INPUT_TEXT = "&q=";
    private static final String FROM_LANG = "en";
    private static final String API_KEY = "ABQIAAAAbFsERds0hxXxK1n0uCHFJhRoToOy41ldpGLhmWKrdGE2Q76t5hSgXKLAW_jVnyhfm8x1Rzo0oo0gOw";
    private static final String REFERER = "http://ngocketit.wordpress.com";
    private static final String BASE_URL = "http://ajax.googleapis.com/ajax/services/language/translate?v=2.0&langpair=";
    
    public TranslatorAsyncTask(Context ctx) {
        super();
        this.context = ctx;
    }
    
    @Override
    protected String doInBackground(String... args) {
        if (isCancelled()) return null;
        
        if (args.length < 2) {
            Log.d(TAG, "Missing parameters");
            return null;
        }
        
        return translate(args[0], args[1]);
    }
    
    private String translate(String word, String toLang) {
        String translatedText = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(BASE_URL).append(FROM_LANG)
            .append("%7C").append(toLang)
            .append(INPUT_TEXT)
            .append(URLEncoder.encode(word, ENCODING));
            
            HttpURLConnection conn = (HttpURLConnection)new URL(builder.toString()).openConnection();
            conn.setRequestProperty("REFERER", REFERER);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            
            try {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, ENCODING));
                String line = null;
                StringBuilder data = new StringBuilder();
                
                while (null != (line = reader.readLine())) {
                    data.append(line).append('\n');
                }
                
                Log.d(TAG, "Raw data: " + data.toString());
                
                JSONObject jObject = new JSONObject(data.toString());
                translatedText = StringEscapeUtils.unescapeXml(jObject.getJSONObject("responseData").getString("translatedText"));
            } finally {
                conn.getInputStream().close();
                if (conn.getErrorStream() != null)
                    conn.getErrorStream().close();
            }
                
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
        
        return translatedText;
    }
    
    @Override
    protected void onPreExecute() {
        if (context instanceof TranslatorActivity) {
            ((TranslatorActivity)context).showProgress();
        }
        super.onPreExecute();
    }
    
    @Override
    protected void onPostExecute(String result) {
        if (context instanceof TranslatorActivity) {
            ((TranslatorActivity)context).processResult(result);
        }
    }
}
