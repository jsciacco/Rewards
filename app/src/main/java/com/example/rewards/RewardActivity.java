package com.example.rewards;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.TypefaceCompatUtil;

import com.example.rewards.databinding.ActivityRewardBinding;

import org.json.JSONObject;

public class RewardActivity extends AppCompatActivity {

    private static final String TAG = "RewardActivity";

    private static final int MAX_LEN_AMOUNT = 11;
    private static final int MAX_LEN_NOTE = 80;

    private String apiKey;

    private Profile giverProfile;

    private Profile receiverProfile;

    private ActivityRewardBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private TextView position;
    private TextView yourStory;
    private ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleResult);

        Intent intent = getIntent();
        if (intent.hasExtra("API")) {
            apiKey = (String) intent.getSerializableExtra("API");
        }
        if (intent.hasExtra("ChosenProfile")) {
            receiverProfile = (Profile) intent.getSerializableExtra("ChosenProfile");
        }
        if (intent.hasExtra("Profile")) {
            giverProfile = (Profile) intent.getSerializableExtra("Profile");
        }

        binding = ActivityRewardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        position = findViewById(R.id.position_content_reward);
        yourStory = findViewById(R.id.tell_us_reward);
        image = findViewById(R.id.image_reward);
        String chosenFullName = String.format("%s, %s", receiverProfile.getLastName(), receiverProfile.getFirstName());
        binding.lastNameFirstNameReward.setText(chosenFullName);
        binding.departmentNameReward.setText(receiverProfile.getDepartment());
        position.setText(receiverProfile.getPosition());
        yourStory.setText(receiverProfile.getStory());

        String imageBase64 = receiverProfile.getImage();
        byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        image.setImageBitmap(bitmap);

        int totalAmount = receiverProfile.getPointsReceived();
        binding.pointsAwardedReward.setText(String.valueOf(totalAmount));

        customizeActionBar();

        setupEditText();

        Log.d(TAG, "onCreate: ");
    }

    private void setupEditText() {

        binding.rewardPointsSentAmount.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_AMOUNT) // Specifies a max text length
        });
        binding.rewardComment.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_NOTE) // Specifies a max text length
        });
        binding.rewardComment.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        // This one executes upon completion of typing a character
                        int len = s.toString().length();
                        String countText = "Comment: (" + len + " of " + MAX_LEN_NOTE + ")";
                        binding.Comment.setText(countText);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                });
    }

    public void handleResult(ActivityResult result) {
        Toast.makeText(this, "Back to RewardProfileActivity!", Toast.LENGTH_SHORT).show();
    }

    private void customizeActionBar() {

        // This function sets the font of the title in the app bar

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        String t = receiverProfile.getFirstName() + " " + receiverProfile.getLastName();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuA) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Rewards Points?");
            builder.setMessage("Add rewards for "+receiverProfile.getFirstName() + " "
                    + receiverProfile.getLastName() + "?");
            builder.setIcon(R.drawable.logo);
            builder.setPositiveButton("OK", (dialog, id) -> {
                Toast.makeText(this, "You want to Save", Toast.LENGTH_SHORT).show();
                String receiverUser = receiverProfile.getUsername();
                String giverUser = giverProfile.getUsername();
                String giverName = giverProfile.getFirstName() + " " + giverProfile.getLastName();
                String amount = binding.rewardPointsSentAmount.getText().toString();
                String note = binding.rewardComment.getText().toString();
                RewardsAPIRunnable.rewardsAPI(
                        this, apiKey, receiverUser, giverUser,
                        giverName, amount, note);
            });
            builder.setNegativeButton("CANCEL", (dialog, id) -> {
                Toast.makeText(RewardActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void handleAddRewardSucceeded(String op, JSONObject profileJson) {

        String logUser = giverProfile.getUsername();
        String logPass = giverProfile.getPassword();
        String login = "Login";
        Intent intent = new Intent(RewardActivity.this, ProfileActivity.class);
        intent.putExtra("LogUser", logUser);
        intent.putExtra("LogPass", logPass);
        intent.putExtra("API", apiKey);
        intent.putExtra("Login", login);

        activityResultLauncher.launch(intent);
        }

    public void handleError(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(s);
        builder.setIcon(R.drawable.logo);
        builder.setPositiveButton("OK", (dialog, id) -> {
            Toast.makeText(RewardActivity.this, "Attempt to give award Failed!", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
