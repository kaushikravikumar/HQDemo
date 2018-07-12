package com.pubnub.kaushik.hqdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.pubnub.kaushik.hqdemo.Util.Constants;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignupActivity extends AppCompatActivity {

    EditText username;

    EditText password;

    Button signUp;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signUp = findViewById(R.id.signup);

        // SharedPreferences object used to store UUID locally on device.
        pref = getApplicationContext().getSharedPreferences("pref", 0);

        // If user already has created account and has UUID, then go to MainActivity.
        if (pref.contains("uuid")) {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
        }
    }

    /*
        This method first generates a random UUID for the user's device and then makes a request to our Grant Access PubNub function,
        which will grant the necessary read/write access permissions to the user's unique auth key (their UUID).
     */
    private void grantAccess() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        try {
            JSONObject requestParams = new JSONObject();
            requestParams.put("uuid", pref.getString("uuid", null));

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, Constants.GRANT_ACCESS_FUNCTION_URL, requestParams, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(SignupActivity.this, "You've been signed up!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error", error.toString());
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("User-agent", "user");
                    return headers;
                }
            };

            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

/*
    This method is invoked when a user presses the sign up button. For this demo, we will not do anything with the username and password,
    since the UUID is randomly generated for each device playing the game.
 */
    public void signUp(View v) {
        // SET UUID in SharedPreferences
        SharedPreferences.Editor editor = pref.edit();
        String uuid = UUID.randomUUID().toString();
        editor.putString("uuid", uuid);
        editor.commit();

        grantAccess();
    }
}
