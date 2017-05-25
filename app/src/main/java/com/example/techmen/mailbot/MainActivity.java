package com.example.techmen.mailbot;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.alicebot.ab.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    TextView speechText;
    private TextToSpeech tts;
    private boolean canSpeak= false;
    private static Chat chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speechText = (TextView) findViewById(R.id.speechView);
        checkTTS();
        new MyAsyncTask().execute();
        }

    public void useMic(View view){
        if (view == findViewById(R.id.micButton)){
            /*promptSpeechInput();*/
            EditText inputstr = (EditText) findViewById(R.id.inputText);
            String s=chat.multisentenceRespond(inputstr.getText().toString());
            speechText.setText(s);
            botSpeak(s,true);
        }
    }
    public void botSpeak(String s, boolean wannaFlush){
        if (canSpeak){
            if (wannaFlush){
                tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(s, TextToSpeech.QUEUE_ADD, null, null);
            }
        } else {
            Toast.makeText(MainActivity.this, "Mic not initialised yet", Toast.LENGTH_LONG).show();
        }
    }

    //Intent TextToSpeech 10
    public void checkTTS(){
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent,10);
    }

    //Intent voice detection 100
    public void promptSpeechInput(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL , RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE , Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT , "MailBot is waiting for your speech ");

        try{
            startActivityForResult(i , 100);
        }
        catch(ActivityNotFoundException e){
            Toast.makeText(MainActivity.this , "You should change your phone !" , Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult (int request_code, int result_code, Intent i){
        super.onActivityResult(request_code, result_code, i);
        switch(request_code){
            case 100: if (result_code == RESULT_OK){
                ArrayList<String> result = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String speech = result.get(0);
                speechText.setText(speech);
                botSpeak(chat.multisentenceRespond(speech), true);
                break;
            }
            case 10: if (result_code == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                tts = new TextToSpeech(MainActivity.this, this);
                break;
            } else if (result_code != 0){
                Toast.makeText(MainActivity.this , "TextToSpeech Not Supported "+result_code , Toast.LENGTH_LONG).show();
                break;
            }

        }
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            tts.setLanguage(Locale.getDefault());
            tts.setPitch((float)1.3);
            tts.setSpeechRate((float)0.9);
            canSpeak = true ;
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "Please wait..", "MailBot is loading ..", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                File fileExt = new File(getExternalFilesDir(null).getAbsolutePath()+"/bots");
                ZipFileExtraction extract = new ZipFileExtraction();
                if(!fileExt.exists())
                    {

                        try
                        {
                            extract.unZipIt(getAssets().open("deploy.zip"), getExternalFilesDir(null).getAbsolutePath()+"/");
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                MagicStrings.root_path = getExternalFilesDir(null).getAbsolutePath();
                Bot bot = new Bot("alice2", MagicStrings.root_path);
                chat = new Chat(bot);
                }
                catch (NullPointerException e){
                    Toast.makeText(MainActivity.this, "Storage Problem ", Toast.LENGTH_LONG).show();
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }

    }
}
