package com.example.rewards;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

        private Profile profile;
        private Profile newProfile;

        private String apiKey;

        private TextView fullName;
        private TextView userName;
        private TextView location;
        private TextView pointsAwarded;
        private TextView department;
        private TextView position;
        private TextView pointsToAward;
        private TextView yourStory;
        private TextView rewardHistory;
        private ImageView image;
        private RecyclerView recyclerView;

        private String logUser;
        private String logPass;

        private static final String TAG = "ProfileActivity";

        private List<Reward> rewardList = new ArrayList<>();

        private ActivityResultLauncher<Intent> activityResultLauncher;
        private RewardAdapter mAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_profile);

                fullName = findViewById(R.id.last_name_first_name_profile);
                userName = findViewById(R.id.username_profile);
                location = findViewById(R.id.location_profile);
                position = findViewById(R.id.position_content_profile);
                rewardHistory = findViewById(R.id.reward_history_profile);
                pointsAwarded = findViewById(R.id.points_awarded_number_profile);
                department = findViewById(R.id.department_name_content_profile);
                pointsToAward = findViewById(R.id.points_to_award_content_profile);
                yourStory = findViewById(R.id.tell_us_profile);
                image = findViewById(R.id.image_profile);

                mAdapter = new RewardAdapter(rewardList, this);

                recyclerView = findViewById(R.id.recycler);

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
                if (intent.hasExtra("CreateProfile")) {
                        CreateProfileAPIRunnable.createProfile(this, apiKey, profile);
                }
                if (intent.hasExtra("LogUser")) {
                        logUser = (String) intent.getSerializableExtra("LogUser");
                }
                if (intent.hasExtra("LogPass")) {
                        logPass = (String) intent.getSerializableExtra("LogPass");
                }
                if (intent.hasExtra("Login")){
                        LoginAPIRunnable.userLogin(this, apiKey, logUser, logPass);
                }
                if (intent.hasExtra("UpdateProfile")) {
                        UpdateProfileAPIRunnable.updateProfile(this, apiKey, profile);
                }

                mAdapter.notifyDataSetChanged();
        }

        public void handleCreateOrLoginSucceeded(String op, JSONObject profileJson, String imageBase64) {

                try {
                        String profileUsername = profileJson.getString("userName");
                        String profilePassword = profileJson.getString("password");
                        String profileFirstName = profileJson.getString("firstName");
                        String profileLastName = profileJson.getString("lastName");
                        String profileDepartment = profileJson.getString("department");
                        String profilePosition = profileJson.getString("position");
                        String profileLocation = profileJson.getString("location");
                        int profileRemainingPoints = profileJson.getInt("remainingPointsToAward");
                        String profileStory = profileJson.getString("story");
                        JSONArray rewardsArray = profileJson.getJSONArray("rewardRecordViews");
                        String fullProfileName = String.format("%s, %s", profileLastName, profileFirstName);
                        int totalAmount = 0;
                        for (int i = 0; i < rewardsArray.length(); i++) {
                                JSONObject objArray;
                                try {
                                        objArray = rewardsArray.getJSONObject(i);
                                        String giverName = objArray.getString("giverName");
                                        int amount = objArray.getInt("amount");
                                        totalAmount += amount;
                                        String note = objArray.getString("note");
                                        String awardDate = objArray.getString("awardDate");
                                        String year = awardDate.substring(0,4);
                                        String month = awardDate.substring(5,7);
                                        String date = awardDate.substring(8,10);
                                        String formatDate = String.format("%s/%s/%s", year, month, date);
                                        rewardList.add(new Reward(giverName, fullProfileName, amount, note, formatDate));
                                } catch (JSONException e) {
                                        e.printStackTrace();
                                }
                        }
                        newProfile = new Profile(profileFirstName, profileLastName, profileUsername,
                                profileDepartment, profileStory, profilePosition, profilePassword,
                                profileRemainingPoints, profileLocation, imageBase64, totalAmount);

                        fullName.setText(String.format("%s, %s", newProfile.getLastName(), newProfile.getFirstName()));
                        userName.setText("(" + newProfile.getUsername() + ")");
                        location.setText(newProfile.getLocation());
                        position.setText(newProfile.getPosition());
                        department.setText(newProfile.getDepartment());
                        pointsToAward.setText(String.valueOf(newProfile.getRemainingPointsToAward()));
                        yourStory.setText(newProfile.getStory());

                        pointsAwarded.setText(String.valueOf(totalAmount));
                        String historyReward = "Reward History (" + rewardList.size() + ")";
                        rewardHistory.setText(historyReward);

                        byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        image.setImageBitmap(bitmap);

                        mAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public void handleLoginError(String s) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(s);
                builder.setIcon(R.drawable.logo);
                builder.setPositiveButton("OK", (dialog, id) -> {
                        Toast.makeText(ProfileActivity.this, "Pick a different username!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);

                        activityResultLauncher.launch(intent);
                });
                AlertDialog dialog = builder.create();
                dialog.show();
        }

        public void handleCreateError(String s) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(s);
                builder.setIcon(R.drawable.logo);
                builder.setPositiveButton("OK", (dialog, id) -> {
                        Toast.makeText(ProfileActivity.this, "Please fill-in required fields!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this, CreateProfileActivity.class);
                        intent.putExtra("API", apiKey);

                        activityResultLauncher.launch(intent);
                });
                AlertDialog dialog = builder.create();
                dialog.show();
        }

        public void handleEditError(String s) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(s);
                builder.setIcon(R.drawable.logo);
                builder.setPositiveButton("OK", (dialog, id) -> {
                        Toast.makeText(ProfileActivity.this, "Please fill in the required fields!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                        intent.putExtra("Profile", profile);
                        intent.putExtra("API", apiKey);

                        activityResultLauncher.launch(intent);
                });
                AlertDialog dialog = builder.create();
                dialog.show();
        }

        public void handleResult(ActivityResult result) {
                Toast.makeText(this, "Back to ProfileActivity!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.profile_menu, menu);
                return super.onCreateOptionsMenu(menu);
        }

        private void customizeActionBar() {

                // This function sets the font of the title in the app bar

                ActionBar actionBar = getSupportActionBar();
                if (actionBar == null)
                        return;

                String t = "Your Profile";
                TextView tv = new TextView(this);

                tv.setText(t);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(19);

                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                actionBar.setCustomView(tv);
                actionBar.setLogo(R.drawable.icon);
                actionBar.setDisplayUseLogoEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.menuA) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Delete Profile?");
                        builder.setMessage("Delete profile for " + newProfile.getFirstName()
                                + " " + newProfile.getLastName()
                                + "?\n(The Rewards app will be closed upon deletion).");
                        builder.setIcon(R.drawable.logo);
                        builder.setPositiveButton("OK", (dialog, id) -> {
                                DeleteProfileAPIRunnable.deleteProfile(this, apiKey, newProfile.getUsername());
                        });
                        builder.setNegativeButton("CANCEL", (dialog, id) -> {
                                Toast.makeText(ProfileActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                }
                else if (item.getItemId() == R.id.menuB) {

                        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                        intent.putExtra("Profile", newProfile);
                        intent.putExtra("API", apiKey);

                        activityResultLauncher.launch(intent);

                        return true;
                }
                else if (item.getItemId() == R.id.menuC) {

                        Intent intent = new Intent(ProfileActivity.this, LeaderboardActivity.class);
                        intent.putExtra("Profile", newProfile);
                        intent.putExtra("API", apiKey);

                        activityResultLauncher.launch(intent);

                        return true;
                }
                else {
                        return super.onOptionsItemSelected(item);
                }
        }
        public void handleDeleteSucceeded(String op) {

                try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        TextView tv = new TextView(this);
                        tv.setGravity(Gravity.CENTER_HORIZONTAL);
                        tv.setText(op);
                        builder.setView(tv);

                } catch (Exception e) {
                        e.printStackTrace();
                }
                String exit = "Exit";
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("Exit", exit);

                activityResultLauncher.launch(intent);
        }
        public void handleError(String s) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        TextView tv = new TextView(this);
                        tv.setGravity(Gravity.CENTER_HORIZONTAL);
                        tv.setText(s);
                        builder.setView(tv);
        }
}
