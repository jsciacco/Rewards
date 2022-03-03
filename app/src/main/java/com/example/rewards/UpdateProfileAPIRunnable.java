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

public class UpdateProfileAPIRunnable {

    private static final String TAG = "UpdateProfile";
    private static String sampleApiKey;
    private static final String endPoint = "Profile/UpdateProfile";

    public static void updateProfile(
            ProfileActivity activity, String apiKey, Profile profile) {

        sampleApiKey = apiKey;

        RequestQueue queue = Volley.newRequestQueue(activity);

        String urlToUse = makeUrl(activity, profile);
        String imageBase64 = profile.getImage();

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                // Here I would parse the JSON and make a profile object
                // The send it back to the activity
                activity.runOnUiThread(() ->
                        activity.handleCreateOrLoginSucceeded("UpdateProfile", response, imageBase64));
            }
        };

        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());
                String errorMsg = error.networkResponse == null ? error.getClass().getName() : new String(error.networkResponse.data);
                activity.runOnUiThread(() ->
                        activity.handleEditError(errorMsg));
            }
        };

        // Request a string response from the provided URL.
        JsonRequest<JSONObject> jsonRequest = new JsonRequest<JSONObject>(
                Request.Method.PUT, urlToUse, imageBase64, listener, error) {

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
                                  Profile profile) {

        String urlString = activity.getResources().getString(R.string.base_url) + endPoint;
        Uri.Builder buildURL = Uri.parse(urlString).buildUpon();
        buildURL.appendQueryParameter("firstName", profile.getFirstName());
        buildURL.appendQueryParameter("lastName", profile.getLastName());
        buildURL.appendQueryParameter("userName", profile.getUsername());
        buildURL.appendQueryParameter("department", profile.getDepartment());
        buildURL.appendQueryParameter("story", profile.getStory());
        buildURL.appendQueryParameter("position", profile.getPosition());
        buildURL.appendQueryParameter("password", profile.getPassword());
        buildURL.appendQueryParameter("remainingPointsToAward", String.valueOf(profile.getRemainingPointsToAward()));
        buildURL.appendQueryParameter("location", profile.getLocation());

        return buildURL.build().toString();
    }
}
