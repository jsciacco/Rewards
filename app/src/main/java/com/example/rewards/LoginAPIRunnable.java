package com.example.rewards;

import android.net.Uri;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginAPIRunnable {
    private static final String TAG = "CreateProfileVolley";
    private static final String endPoint = "Profile/Login";
    private static String sampleApiKey;

    public static void userLogin(
            ProfileActivity activity, String apiKey, String userName, String password) {

        sampleApiKey = apiKey;

        RequestQueue queue = Volley.newRequestQueue(activity);

        String urlToUse = makeUrl(activity, userName, password);

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    String image = response.getString("imageBytes");
                    response.remove("imageBytes");
                    activity.runOnUiThread(() ->
                            activity.handleCreateOrLoginSucceeded("UserLogin", response, image));
                } catch (JSONException e) {
                    e.printStackTrace();
                    activity.runOnUiThread(() ->
                            activity.handleLoginError(e.getMessage()));
                }

            }
        };

        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
                String errorMsg = error.networkResponse == null ? error.getClass().getName() : new String(error.networkResponse.data);
                activity.runOnUiThread(() ->
                        activity.handleLoginError(errorMsg));
            }
        };

        // Request a string response from the provided URL.
        JsonRequest<JSONObject> jsonRequest = new JsonRequest<JSONObject>(
                Request.Method.GET, urlToUse, null, listener, error) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // This method is always the same!
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Accept", "application/json");
                headers.put("ApiKey", sampleApiKey);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

    private static String makeUrl(ProfileActivity activity,
                                  String userName, String password) {

        String urlString = activity.getResources().getString(R.string.base_url) + endPoint;
        Uri.Builder buildURL = Uri.parse(urlString).buildUpon();
        buildURL.appendQueryParameter("userName", userName);
        buildURL.appendQueryParameter("password", password);

        return buildURL.build().toString();
    }

}
