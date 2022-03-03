package com.example.rewards;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity implements View.OnClickListener {

    private String apiKey;

    private Profile profile;

    private Profile chosenProfile;

    private final List<Profile> profileList = new ArrayList<>();

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private LeaderboardAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        mAdapter = new LeaderboardAdapter(profileList, this);

        recyclerView = findViewById(R.id.recycler_leader);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleResult);

        customizeActionBar();

        Intent intent = getIntent();
        if (intent.hasExtra("Profile")) {
            profile = (Profile) intent.getSerializableExtra("Profile");
        }
        if (intent.hasExtra("API")) {
            apiKey = (String) intent.getSerializableExtra("API");
        }

        mAdapter.notifyDataSetChanged();

        GetAllProfilesAPIRunnable.getAllProfiles(this, apiKey);
    }

    private void customizeActionBar() {

        // This function sets the font of the title in the app bar

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        String t = "Leaderboard";
        TextView tv = new TextView(this);

        tv.setText(t);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(19);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(tv);
        actionBar.setLogo(R.drawable.arrow_with_logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(LeaderboardActivity.this, "You clicked!", Toast.LENGTH_SHORT).show();
        int pos = recyclerView.getChildLayoutPosition(view);
        chosenProfile = profileList.get(pos);
        openNewActivityOfficial(null);
    }

    public void openNewActivityOfficial(View v) {
        Intent intent = new Intent(LeaderboardActivity.this, RewardActivity.class);
        intent.putExtra("Profile", profile);
        intent.putExtra("ChosenProfile", chosenProfile);
        intent.putExtra("API", apiKey);

        activityResultLauncher.launch(intent);
    }
    public void handleError(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText(s);
        builder.setView(tv);
    }

    public void handleGetAllProfilesSucceeded(String op, JSONArray response) throws JSONException {

        for (int h = 0; h < response.length(); h++){
            JSONObject jsonObject = response.getJSONObject(h);
            try {
                String profileUsername = jsonObject.getString("userName");
                String profilePassword = "";
                String profileFirstName = jsonObject.getString("firstName");
                String profileLastName = jsonObject.getString("lastName");
                String profileDepartment = jsonObject.getString("department");
                String profilePosition = jsonObject.getString("position");
                int profileRemainingPoints = 0;
                int pointsReceived = 0;
                String profileLocation = "Unspecified Location";
                String profileStory = jsonObject.getString("story");
                String image = jsonObject.getString("imageBytes");
                JSONArray profileRewards = jsonObject.getJSONArray("rewardRecordViews");
                for (int i = 0; i < profileRewards.length(); i++) {
                    JSONObject objArray;
                    try {
                        objArray = profileRewards.getJSONObject(i);
                        String giverName = objArray.getString("giverName");
                        int amount = objArray.getInt("amount");
                        pointsReceived += amount;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                        profileList.add(new Profile(profileFirstName, profileLastName, profileUsername,
                                profileDepartment, profileStory, profilePosition, profilePassword,
                                profileRemainingPoints, profileLocation, image, pointsReceived));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        Collections.sort(profileList, new Comparator<Profile>() {
            @Override
            public int compare(Profile t1, Profile t2) {
                return t2.getPointsReceived()-t1.getPointsReceived();
            }
        });
        mAdapter.notifyDataSetChanged();
        }

    public void handleResult(ActivityResult result) {
        Toast.makeText(this, "Back to ProfileActivity!", Toast.LENGTH_SHORT).show();
    }
}
