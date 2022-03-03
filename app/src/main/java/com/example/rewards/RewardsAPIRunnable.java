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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RewardsAPIRunnable {

    private static final String TAG = "RewardActivity";
    private static final String endPoint = "Rewards/AddRewardRecord";
    private static String sampleApiKey;

    public static void rewardsAPI(
            RewardActivity activity, String apiKey, String receiverUser, String giverUser,
            String giverName, String amount, String note) {

        sampleApiKey = apiKey;

        RequestQueue queue = Volley.newRequestQueue(activity);

        String urlToUse = makeUrl(activity, receiverUser, giverUser, giverName, amount, note);

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                activity.runOnUiThread(() ->
                        activity.handleAddRewardSucceeded("UserLogin", response));
            }
        };

        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
                String errorMsg = error.networkResponse == null ? error.getClass().getName() : new String(error.networkResponse.data);
                activity.runOnUiThread(() ->
                        activity.handleError(errorMsg));
            }
        };

        // Request a string response from the provided URL.
        JsonRequest<JSONObject> jsonRequest = new JsonRequest<JSONObject>(
                Request.Method.POST, urlToUse, null, listener, error) {

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

    private static String makeUrl(RewardActivity activity,
                                  String receiverUser, String giverUser, String giverName, String amount, String note) {

        String urlString = activity.getResources().getString(R.string.base_url) + endPoint;
        Uri.Builder buildURL = Uri.parse(urlString).buildUpon();
        buildURL.appendQueryParameter("receiverUser", receiverUser);
        buildURL.appendQueryParameter("giverUser", giverUser);
        buildURL.appendQueryParameter("giverName", giverName);
        buildURL.appendQueryParameter("amount", String.valueOf(amount));
        buildURL.appendQueryParameter("note", note);

        return buildURL.build().toString();
    }
}
