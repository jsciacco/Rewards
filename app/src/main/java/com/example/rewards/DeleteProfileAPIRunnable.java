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

import java.util.HashMap;
import java.util.Map;

public class DeleteProfileAPIRunnable {

    private static final String TAG = "DeleteProfile";
    private static final String endPoint = "Profile/DeleteProfile";
    private static String sampleApiKey;

    public static void deleteProfile(
            ProfileActivity activity, String apiKey, String userName) {

        sampleApiKey = apiKey;

        RequestQueue queue = Volley.newRequestQueue(activity);

        String urlToUse = makeUrl(activity, userName);

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                activity.runOnUiThread(() ->
                        activity.handleDeleteSucceeded("DeleteProfile"));
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
        JsonRequest<String> jsonRequest = new JsonRequest<String>(
                Request.Method.DELETE, urlToUse, null, listener, error) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // This method is always the same!
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new String(jsonString),
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
                                  String userName) {

        String urlString = activity.getResources().getString(R.string.base_url) + endPoint;
        Uri.Builder buildURL = Uri.parse(urlString).buildUpon();
        buildURL.appendQueryParameter("username", userName);

        return buildURL.build().toString();
    }
}
