package com.pubnub.kaushik.hqdemo;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.kaushik.hqdemo.Util.Constants;
import com.pubnub.kaushik.hqdemo.Util.DecimalRemover;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.SharedPreferences.Editor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView question, numPlayers, timeLeft, answer, loadingText;

    private Button aButton, bButton, cButton, dButton;

    private ProgressBar loadingSpinner;

    private PubNub pubNub;

    private String questionText, optionAText, optionBText, optionCText, optionDText, optionChosen;

    private HorizontalBarChart answerResultsChart;

    private ImageView questionImage;


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

        SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", 0); // 0 - for private mode
        String uuid;
        if (!pref.contains("uuid")) {
            Editor editor = pref.edit();
            uuid = UUID.randomUUID().toString();
            editor.putString("uuid", uuid);
            editor.commit();
        } else {
            uuid = pref.getString("uuid", null);
        }
        pnConfiguration.setUuid(uuid);
        pnConfiguration.setSubscribeKey(Constants.PUBNUB_SUBSCRIBE_KEY);
        pnConfiguration.setPublishKey(Constants.PUBNUB_PUBLISH_KEY);
//        pnConfiguration.setSecretKey(Constants.PUBNUB_SECRET_KEY);
        pnConfiguration.setAuthKey(Constants.PUBNUB_USER_AUTH_KEY);
        pnConfiguration.setSecure(true);
        pubNub = new PubNub(pnConfiguration);

//        // Sets up authentication key and establishes read write permissions.
//        pubNub.grant()
//                .channels(Arrays.asList(Constants.POST_QUESTION_CHANNEL, Constants.POST_ANSWER_CHANNEL)) //channels to allow grant on
//                .authKeys(Arrays.asList(Constants.PUBNUB_USER_AUTH_KEY)) // the keys we are provisioning
//                .write(false) // allow those keys to write (false by default)
//                .manage(false) // allow those keys to manage channel groups (false by default)
//                .read(true) // allow keys to read the subscribe feed (false by default)
//                .ttl(0) // how long those keys will remain valid (0 for eternity)
//                .async(new PNCallback<PNAccessManagerGrantResult>() {
//                    @Override
//                    public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
//                        Log.d("CAN READ", "QUESTION/ANSWER RESULTS");
//                        updateUI();
//                    }
//                });
//
//        pubNub.grant()
//                .channels(Arrays.asList(Constants.SUBMIT_ANSWER_CHANNEL))
//                .authKeys(Arrays.asList(Constants.PUBNUB_USER_AUTH_KEY))
//                .write(true)
//                .manage(false)
//                .read(false)
//                .ttl(10)
//                .async(new PNCallback<PNAccessManagerGrantResult>() {
//                    @Override
//                    public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
//                        Log.d("CAN FIRE", "NOW");
//                    }
//                });
        updateUI();
    }

    private void updateUI() {
        Log.d("UPDATE", "UI");
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
                        if (message.getChannel().equals("question_post")) {
                            questionText = message.getMessage().getAsJsonObject().get("question").getAsString();
                            optionAText = message.getMessage().getAsJsonObject().get("optionA").getAsString();
                            optionBText = message.getMessage().getAsJsonObject().get("optionB").getAsString();
                            optionCText = message.getMessage().getAsJsonObject().get("optionC").getAsString();
                            optionDText = message.getMessage().getAsJsonObject().get("optionD").getAsString();

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
                .channels(Arrays.asList(Constants.POST_QUESTION_CHANNEL, Constants.POST_ANSWER_CHANNEL)) // subscribe to channels
                .withPresence()
                .execute();

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
                                numPlayers.setText(String.valueOf(result.getTotalOccupancy()));
                            }
                        });
                    }
                });
    }

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

        if (correct.equals("optionA")) {
            correctAnswerMessage += optionAText;
        } else if (correct.equals("optionB")) {
            correctAnswerMessage += optionBText;
        } else if (correct.equals("optionC")) {
            correctAnswerMessage += optionCText;
        } else if (correct.equals("optionD")) {
            correctAnswerMessage += optionDText;
        }
        correctAnswerMessage += " was the answer.";
        answer.setVisibility(View.VISIBLE);
        answer.setText(correctAnswerMessage);
    }

    private void showAnswerResults(PNMessageResult message) {
        int countA = message.getMessage().getAsJsonObject().get("optionA").getAsInt();
        int countB = message.getMessage().getAsJsonObject().get("optionB").getAsInt();
        int countC = message.getMessage().getAsJsonObject().get("optionC").getAsInt();
        int countD = message.getMessage().getAsJsonObject().get("optionD").getAsInt();

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, countA));
        entries.add(new BarEntry(1, countB));
        entries.add(new BarEntry(2, countC));
        entries.add(new BarEntry(3, countD));
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


    public void pressedA(View v) {
        aButton.setBackgroundColor(Color.rgb(63, 81, 181));
        bButton.setBackgroundColor(0x00000000);
        cButton.setBackgroundColor(0x00000000);
        dButton.setBackgroundColor(0x00000000);
        optionChosen = "optionA";
    }

    public void pressedB(View v) {
        aButton.setBackgroundColor(0x00000000);
        bButton.setBackgroundColor(Color.rgb(63, 81, 181));
        cButton.setBackgroundColor(0x00000000);
        dButton.setBackgroundColor(0x00000000);
        optionChosen = "optionB";
    }

    public void pressedC(View v) {
        aButton.setBackgroundColor(0x00000000);
        bButton.setBackgroundColor(0x00000000);
        cButton.setBackgroundColor(Color.rgb(63, 81, 181));
        dButton.setBackgroundColor(0x00000000);
        optionChosen = "optionC";
    }

    public void pressedD(View v) {
        aButton.setBackgroundColor(0x00000000);
        bButton.setBackgroundColor(0x00000000);
        cButton.setBackgroundColor(0x00000000);
        dButton.setBackgroundColor(Color.rgb(63, 81, 181));
        optionChosen = "optionD";
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateUI();
    }
}
