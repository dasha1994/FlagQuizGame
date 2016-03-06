package com.example.s4astya.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by S4ASTYA on 06.03.2016.
 */
public class FlagQuizGame extends Activity {
    private List<String> fileNameList;
    private List<String> quizCountriesList;
    private Map<String,Boolean> regionsMap;
    private String correctAnswer;

    private int totalGuesses;
    private int totalCorrectAnswers;
    private int guessRows;

    private Random random;
    private Handler handler;

    private Animation shakeAnimation;

    private TextView answerTextView;
    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private TableLayout buttinTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_quiz_game);

        fileNameList=new ArrayList<String>();
        quizCountriesList = new ArrayList<String>();
        regionsMap = new HashMap<String,Boolean>();

        guessRows = 1;

        random = new Random();
        handler = new Handler();

        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);
        String[] regionsList = getResources().getStringArray(R.array.regionsList);
        for(String s : regionsList)
        {
            regionsMap.put(s,true);
        }
        questionNumberTextView = (TextView)findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) findViewById(R.id.flagImageView);
        buttinTableLayout = (TableLayout)findViewById(R.id.buttonTableLayout);
        answerTextView = (TextView) findViewById(R.id.answerTextView);

        questionNumberTextView.setText(getResources().getString(R.string.question)+
                " 1" + getResources().getString(R.string.of)+" 10");
        resetQuiz();
    }
    private void resetQuiz()
    {
        AssetManager assets= getAssets();
        fileNameList.clear();
        try{
            Set<String> regions = regionsMap.keySet();
            for(String region : regions)
            {
                String[] paths = assets.list(region);
                for(String path : paths)
                {
                    fileNameList.add(path.replace(".png",""));
                }
            }
        }
        catch (IOException e)
        {

        }
        totalCorrectAnswers=0;
        totalGuesses=0;
        quizCountriesList.clear();
        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();
        while(flagCounter<=10)
        {
            int randomIndex = random.nextInt(numberOfFlags);
            String fileName = fileNameList.get(randomIndex);
            if(!quizCountriesList.contains(fileName))
                quizCountriesList.add(fileName);
            ++flagCounter;
        }
        loadNextFlag();
    }
    private void loadNextFlag()
    {
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage;
        answerTextView.setText("");
        questionNumberTextView.setText(getResources().getString(R.string.question)+" "+
                (totalCorrectAnswers+1)+getResources().getString(R.string.of)+" 10");
        String region = nextImage.substring(0, nextImage.indexOf('-'));
        AssetManager assets = getAssets();

        InputStream stream;
        try {
            stream = assets.open(region+"/"+nextImage+".png");
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int row = 0;row < buttinTableLayout.getChildCount();++row)
        {
            ((TableRow)buttinTableLayout.getChildAt(row)).removeAllViews();
        }
        Collections.shuffle(fileNameList);

        int correct = fileNameList.indexOf(correctAnswer);
       // fileNameList.add(fileNameList.remove(correct));

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int row = 0; row<guessRows;row++)
        {
            TableRow currentTableRow = getTableRow(row);
            for(int column = 0;column<3;column++)
            {
                Button newGuessButton = (Button)inflater.inflate(R.layout.guess_button,null);
                String fileName = fileNameList.get((row*3)+column);
                newGuessButton.setText(getCountryName(fileName));
                newGuessButton.setOnClickListener(guessButtonListener);
                currentTableRow.addView(newGuessButton);

            }
        }
        int randomRow = random.nextInt(guessRows);
        int randomColumn = random.nextInt(3);
        TableRow randomTableRow = getTableRow(randomRow);
        String countryName = getCountryName(correctAnswer);
        ((Button)randomTableRow.getChildAt(randomColumn)).setText(countryName);
    }
    private String getCountryName(String fileName)
    {
        return fileName.substring(fileName.indexOf('-')+1).replace('_',' ');
    }
    private TableRow getTableRow(int row)
    {
        return (TableRow)buttinTableLayout.getChildAt(row);
    }
    private void submitGuess(Button guessButton)
    {
        String guess = guessButton.getText().toString();
        String answer = getCountryName(correctAnswer);
        ++totalGuesses;
        if(guess.equals(answer))
        {
            ++totalCorrectAnswers;
            answerTextView.setText(answer + "!");
            answerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
            disableButtons();

            if(10==totalCorrectAnswers)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(FlagQuizGame.this);
                builder.setTitle(R.string.reset_quiz);
                builder.setMessage(String.format("%d %s,%.02f%% %s",
                        totalGuesses, getResources().getString(R.string.guesses),
                        (1000 / (double) totalGuesses),
                        getResources().getString(R.string.correct)));
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                });
                AlertDialog resetDialog = builder.create();
                resetDialog.show();
            }
            else
            {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                },1000);
            }
        }
        else
        {
            flagImageView.startAnimation(shakeAnimation);
            answerTextView.setText("Incorrect!");
            answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
            guessButton.setEnabled(false);
        }
    }
    private void disableButtons()
    {
        for(int row = 0;row < buttinTableLayout.getChildCount();++row)
        {
            TableRow tableRow = (TableRow)buttinTableLayout.getChildAt(row);
            for(int columnn = 0;columnn<tableRow.getChildCount();columnn++)
            {
                tableRow.getChildAt(columnn).setEnabled(false);
            }
        }
    }
    private final int CHOICES_MENU_ID = Menu.FIRST;
    private final int REGIONS_MENU_ID = Menu.FIRST + 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.choices);
        menu.add(Menu.NONE,REGIONS_MENU_ID,Menu.NONE,R.string.regions);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case CHOICES_MENU_ID :
                final String[] possibleChoices = getResources().getStringArray(R.array.guesssesList);
                AlertDialog.Builder choicesBuilder = new AlertDialog.Builder(FlagQuizGame.this);
                choicesBuilder.setTitle(R.string.choices);
                choicesBuilder.setItems(R.array.guesssesList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        guessRows = Integer.parseInt(possibleChoices[which].toString())/3;
                        resetQuiz();
                    }
                });
                AlertDialog choiceDialog = choicesBuilder.create();
                choiceDialog.show();
                return true;
            case REGIONS_MENU_ID :
                final String[] regionsName = regionsMap.keySet().toArray(new String[regionsMap.size()]);

                boolean[] regionsEnabled = new boolean[regionsMap.size()];
                for(int i = 0;i<regionsEnabled.length;i++)
                {
                    regionsEnabled[i] = regionsMap.get(regionsName[i]);
                }
                AlertDialog.Builder regionsBuilder = new AlertDialog.Builder(FlagQuizGame.this);
                regionsBuilder.setTitle(R.string.regions);
                String[] displayNames = new String[regionsName.length];
                for(int i = 0; i < regionsName.length; i++)
                {
                    displayNames[i] = regionsName[i].replace('_',' ');
                }
                regionsBuilder.setMultiChoiceItems(displayNames, regionsEnabled, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        regionsMap.put(regionsName[which].toString(), isChecked);
                    }
                });
                regionsBuilder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                });
                AlertDialog regionsDialog = regionsBuilder.create();
                regionsDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private OnClickListener guessButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            submitGuess((Button)v);
        }
    };

}
