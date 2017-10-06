package com.shubham.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ImageView celeb;
    Button choice1;
    Button choice2;
    Button choice3;
    Button choice4;
    ArrayList<String> celebURLS = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer;
    String[] answers = new String[4];

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class downloadHTML extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String html = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    html += current;
                    data = reader.read();
                }
                return html;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        celeb = (ImageView) findViewById(R.id.imageView);
        choice1 = (Button) findViewById(R.id.choice1);
        choice2 = (Button) findViewById(R.id.choice2);
        choice3 = (Button) findViewById(R.id.choice3);
        choice4 = (Button) findViewById(R.id.choice4);
        downloadHTML task = new downloadHTML();
        String result = null;
        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
            String[] resultSplit = result.split("<div class=\"sidebarContainer\">");
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(resultSplit[0]);
            while (m.find()) {
                celebURLS.add(m.group(1));
            }
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(resultSplit[0]);
            while (m.find()) {
                celebNames.add(m.group(1));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        nextQuestion();
    }

    public void nextQuestion() {
        Random random = new Random();
        chosenCeleb = random.nextInt(celebURLS.size());
        System.out.println(celebURLS.get(chosenCeleb));
        System.out.println(celebNames.get(chosenCeleb));
        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celebImage;
        try {
            celebImage = imageTask.execute(celebURLS.get(chosenCeleb)).get();
            celeb.setImageBitmap(celebImage);
            locationOfCorrectAnswer = random.nextInt(4);
            int randAns;
            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    randAns = random.nextInt(celebURLS.size());
                    while (randAns == chosenCeleb) {
                        randAns = random.nextInt(celebURLS.size());
                    }
                    answers[i] = celebNames.get(randAns);
                }
            }
            choice1.setText(answers[0]);
            choice2.setText(answers[1]);
            choice3.setText(answers[2]);
            choice4.setText(answers[3]);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void chooseAnswer(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong: " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();
        }
        nextQuestion();
    }
}