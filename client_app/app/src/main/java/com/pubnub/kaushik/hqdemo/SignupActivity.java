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
import com.pubnub.kaushik.hqdemo.Util.MyJSONObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pubnub.kaushik.hqdemo.Util.Constants;

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

        pref = getApplicationContext().getSharedPreferences("pref", 0); // 0 - for private mode

        // If user doesn't have uuid create a random one! Then grant access to read/write for PubNub channels.
        if (pref.contains("uuid")) {
            Log.d("ACCOUNT ", "ALREADY MADE");
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
        }
    }

    private void grantAccess() {
        // SET UUID in SharedPreferences
        SharedPreferences.Editor editor = pref.edit();
        String uuid = UUID.randomUUID().toString();
        editor.putString("uuid", uuid);
        editor.commit();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.GRANT_ACCESS_FUNCTION_URL;
        try {
            JSONObject requestParams = new JSONObject();
            requestParams.put("uuid", pref.getString("uuid", null));

            MyJSONObjectRequest jsonObjectRequest = new MyJSONObjectRequest
                    (Request.Method.POST, url, requestParams, new Response.Listener<JSONObject>() {
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
                    })
            {
                @Override
                public Map<String, String> getHeaders()
                {
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


    public void signUp(View v)
    {
        // THIS DEMO WON'T DO ANYTHING WITH USERNAME AND PASSWORD
        grantAccess();
    }
}
