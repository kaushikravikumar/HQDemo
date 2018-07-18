package com.pubnub.kaushik.hqdemo;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.kaushik.hqdemo.Util.Constants;
import com.pubnub.kaushik.hqdemo.Util.DecimalRemover;

import java.util.ArrayList;
import java.util.Arrays;


import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView question, numPlayers, timeLeft, answer, loadingText;

    private Button aButton, bButton, cButton, dButton;

    private ProgressBar loadingSpinner;

    private PubNub pubNub;

    private String questionText, optionAText, optionBText, optionCText, optionDText;

    private HorizontalBarChart answerResultsChart;

    private ImageView questionImage;

    private String optionChosen; // Must be optionA, optionB, optionC, or optionD. Use the constants defined below.

    private final String OPTION_A = "optionA";
    private final String OPTION_B = "optionB";
    private final String OPTION_C = "optionC";
    private final String OPTION_D = "optionD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        question = findViewById(R.id.question);
        questionImage = findViewById(R.id.questionImage);
        answer = findViewById(R.id.answer);
        timeLeft = findViewById(R.id.timeLeft);
        numPlayers = findViewById(R.id.numPlayers);
        aButton = findViewById(R.id.optionA);
        bButton = findViewById(R.id.optionB);
        cButton = findViewById(R.id.optionC);
        dButton = findViewById(R.id.optionD);
        loadingSpinner = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loadingTextView);
        answerResultsChart = findViewById(R.id.answerResultsChart);

        initPubNub();
    }

    private void initPubNub() {
        PNConfiguration pnConfiguration = new PNConfiguration();

        // Local Device Storage to store user's UUID (Universal Unique ID)
        SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", 0);

        pnConfiguration.setUuid(pref.getString("uuid", null));
        pnConfiguration.setSubscribeKey(Constants.PUBNUB_SUBSCRIBE_KEY);
        pnConfiguration.setPublishKey(Constants.PUBNUB_PUBLISH_KEY);
        pnConfiguration.setAuthKey(pref.getString("uuid", null));
        pnConfiguration.setSecure(true);
        pubNub = new PubNub(pnConfiguration);

        updateUI();
    }

    private void updateUI() {
        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                // Empty, not needed.
            }

            @Override
            public void message(PubNub pubnub, final PNMessageResult message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // NEW QUESTION HAS JUST BEEN POSTED
                        if (message.getChannel().equals(Constants.POST_QUESTION_CHANNEL)) {
                            showQuestion(message);
                        }
                        // NEW ANSWER RESULT HAS JUST BEEN POSTED
                        else {
                            showCorrectAnswer(message);
                            showAnswerResults(message);
                        }
                    }
                });
            }

            @Override
            public void presence(PubNub pubnub, final PNPresenceEventResult presence) {
                // Empty, not needed.
            }
        });

        pubNub.subscribe()
                .channels(Arrays.asList(Constants.POST_QUESTION_CHANNEL, Constants.POST_ANSWER_CHANNEL)) // subscribes to channels
                .execute();

        // Used to maintain the current occupancy of the channels,
        // or in other words, players playing the game at the moment.
        pubNub.hereNow()
                .channels(Arrays.asList(Constants.POST_QUESTION_CHANNEL))
                .includeUUIDs(true)
                .async(new PNCallback<PNHereNowResult>() {
                    @Override
                    public void onResponse(final PNHereNowResult result, PNStatus status) {
                        if (status.isError()) {
                            // handle error
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                numPlayers.setText(String.valueOf(result.getTotalOccupancy())); // Displays current number of players
                            }
                        });
                    }
                });
    }

    /**
     * This method updates UI elements such as the question TextView and the buttons showing the
     * answer options. Also starts 10 second countdown timer. Once the timer is finished, it will
     * invoke our method makeRequestToPubNubFunction() and prevent the user from further sending a response.
     *
     * @param message PNMessageResult object from subscribe callback that has the question and each option encoded in JSON.
     */
    private void showQuestion(PNMessageResult message) {
        questionText = message.getMessage().getAsJsonObject().get("question").getAsString();
        optionAText = message.getMessage().getAsJsonObject().get(OPTION_A).getAsString();
        optionBText = message.getMessage().getAsJsonObject().get(OPTION_B).getAsString();
        optionCText = message.getMessage().getAsJsonObject().get(OPTION_C).getAsString();
        optionDText = message.getMessage().getAsJsonObject().get(OPTION_D).getAsString();

        question.setText(questionText);
        question.setVisibility(View.VISIBLE);
        questionImage.setVisibility(View.VISIBLE);
        timeLeft.setVisibility(View.VISIBLE);

        aButton.setText(optionAText);
        aButton.setVisibility(View.VISIBLE);

        bButton.setText(optionBText);
        bButton.setVisibility(View.VISIBLE);

        cButton.setText(optionCText);
        cButton.setVisibility(View.VISIBLE);

        dButton.setText(optionDText);
        dButton.setVisibility(View.VISIBLE);

        loadingSpinner.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                timeLeft.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                makeRequestToPubNubFunction(optionChosen);
                aButton.setVisibility(View.GONE);
                bButton.setVisibility(View.GONE);
                cButton.setVisibility(View.GONE);
                dButton.setVisibility(View.GONE);
                questionImage.setVisibility(View.GONE);
            }
        }.start();
    }

    /**
     * This method displays what correct answer was and tells user if they answered correct or not.
     *
     * @param message PNMessageResult object from subscribe callback. Includes correct answer in JSON.
     */
    private void showCorrectAnswer(PNMessageResult message) {
        String correct = message.getMessage().getAsJsonObject().get("correct").getAsString();

        String correctAnswerMessage = "";

        if (optionChosen == null) {
            correctAnswerMessage += "You ran out of time. ";
        } else if (optionChosen.equals(correct)) {
            correctAnswerMessage += "Good Job! ";
        } else {
            correctAnswerMessage += "Sorry, you are wrong. ";
        }

        if (correct.equals(OPTION_A)) {
            correctAnswerMessage += optionAText;
        } else if (correct.equals(OPTION_B)) {
            correctAnswerMessage += optionBText;
        } else if (correct.equals(OPTION_C)) {
            correctAnswerMessage += optionCText;
        } else if (correct.equals(OPTION_D)) {
            correctAnswerMessage += optionDText;
        }
        correctAnswerMessage += " was the correct answer.";
        answer.setVisibility(View.VISIBLE);
        answer.setText(correctAnswerMessage);
    }

    /**
     * Displays stats of how many users answered each option on Horizontal Bar Graph.
     *
     * @param message PNMessageResult object from subscribe callback. Contains data of how many users answered each option.
     */
    private void showAnswerResults(PNMessageResult message) {
        int countA = message.getMessage().getAsJsonObject().get(OPTION_A).getAsInt();
        int countB = message.getMessage().getAsJsonObject().get(OPTION_B).getAsInt();
        int countC = message.getMessage().getAsJsonObject().get(OPTION_C).getAsInt();
        int countD = message.getMessage().getAsJsonObject().get(OPTION_D).getAsInt();

        // Enter them backwards since BarEntry ArrayLists work like a stack.
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, countD));
        entries.add(new BarEntry(1, countC));
        entries.add(new BarEntry(2, countB));
        entries.add(new BarEntry(3, countA));
        BarDataSet dataSet = new BarDataSet(entries, "Results");
        dataSet.setDrawValues(true);
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueFormatter(new DecimalRemover());

        BarData data = new BarData(dataSet);
        data.setValueTextSize(13f);
        data.setBarWidth(1f);

        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add(optionDText);
        xAxis.add(optionCText);
        xAxis.add(optionBText);
        xAxis.add(optionAText);

        // Hide grid lines
        answerResultsChart.getAxisLeft().setEnabled(false);
        answerResultsChart.getAxisRight().setEnabled(false);
        answerResultsChart.getXAxis().setDrawGridLines(false);

        // Hide graph description
        answerResultsChart.getDescription().setEnabled(false);
        // Hide graph legend
        answerResultsChart.getLegend().setEnabled(false);

        answerResultsChart.setData(data);
        answerResultsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxis));
        answerResultsChart.getXAxis().setLabelCount(xAxis.size());
        answerResultsChart.animateXY(1000, 1000);
        answerResultsChart.invalidate();

        answerResultsChart.setVisibility(View.VISIBLE);
    }

    /**
     * Uses PubNub fire method to efficiently send user's answer over to submitAnswer channel.
     *
     * @param optionChosen this is the option that the user has chosen.
     */
    private void makeRequestToPubNubFunction(final String optionChosen) {
        try {
            JSONObject answerObj = new JSONObject();
            answerObj.put("answer", optionChosen);
            pubNub.fire()
                    .message(answerObj)
                    .channel(Constants.SUBMIT_ANSWER_CHANNEL)
                    .usePOST(false)
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            if (status.isError()) {
                                // something bad happened.
                                System.out.println("error happened while publishing: " + status.toString());
                            } else {
                                System.out.println("publish worked! timetoken: " + result.getTimetoken());
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
        Called when user presses A button.
     */
    public void pressedA(View v) {
        aButton.setBackgroundColor(Color.rgb(63, 81, 181));
        bButton.setBackgroundColor(0x00000000);
        cButton.setBackgroundColor(0x00000000);
        dButton.setBackgroundColor(0x00000000);
        optionChosen = OPTION_A;
    }

    /*
        Called when user presses B button.
     */
    public void pressedB(View v) {
        aButton.setBackgroundColor(0x00000000);
        bButton.setBackgroundColor(Color.rgb(63, 81, 181));
        cButton.setBackgroundColor(0x00000000);
        dButton.setBackgroundColor(0x00000000);
        optionChosen = OPTION_B;
    }

    /*
        Called when user presses C button.
     */
    public void pressedC(View v) {
        aButton.setBackgroundColor(0x00000000);
        bButton.setBackgroundColor(0x00000000);
        cButton.setBackgroundColor(Color.rgb(63, 81, 181));
        dButton.setBackgroundColor(0x00000000);
        optionChosen = OPTION_C;
    }

    /*
        Called when user presses D button.
     */
    public void pressedD(View v) {
        aButton.setBackgroundColor(0x00000000);
        bButton.setBackgroundColor(0x00000000);
        cButton.setBackgroundColor(0x00000000);
        dButton.setBackgroundColor(Color.rgb(63, 81, 181));
        optionChosen = OPTION_D;
    }
}
