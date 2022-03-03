package com.example.rewards;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.rewards.databinding.ActivityMainBinding;

// J.C. Sciaccotta

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MAX_LEN_USERNAME = 20;
    private static final int MAX_LEN_PASSWORD = 40;
    private RewardsSharedPreference myPrefs;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private String apiKey = "";
    private String f = "";
    private String l = "";
    private String e = "";
    private String i = "";
    private String savedUsername = "";
    private String savedPassword = "";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customizeActionBar();

        Intent intent = getIntent();
        if (intent.hasExtra("Exit")) {
            finishAffinity();
            System.exit(0);
        }

        myPrefs = new RewardsSharedPreference(this);

        apiKey = myPrefs.getValue("API_Key");
        f = myPrefs.getValue("First_Key");
        l = myPrefs.getValue("Last_Key");
        e = myPrefs.getValue("Email_Key");
        i = myPrefs.getValue("ID_Key");
        savedUsername = myPrefs.getValue("Username_Key");
        savedPassword = myPrefs.getValue("Password_Key");

        if (apiKey.equals("")){
            apiCall(null);
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupEditText();
        if (!savedUsername.equals("")){
            binding.editTextTextPersonName.setText(savedUsername);
        }
        if (!savedPassword.equals("")){
            binding.editTextTextPassword.setText(savedPassword);
        }
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleResult);
    }

    public void handleResult(ActivityResult result) {
        Toast.makeText(this, "Back to MainActivity!", Toast.LENGTH_SHORT).show();
    }

    public void apiCall (View v){
        // Dialog with a layout
        // Inflate the dialog's layout
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        final View view = inflater.inflate(R.layout.dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need to request an API Key:");
        builder.setTitle("API Key Needed");
        builder.setIcon(R.drawable.logo);

        // Set the inflated view to be the builder's view
        builder.setView(view);

        builder.setPositiveButton("OK", (dialog, id) -> {

            EditText et1 = view.findViewById(R.id.textF);
            EditText et2 = view.findViewById(R.id.textL);
            EditText et3 = view.findViewById(R.id.textE);
            EditText et4 = view.findViewById(R.id.textI);
            f = et1.getText().toString();
            l = et2.getText().toString();
            e = et3.getText().toString();
            i = et4.getText().toString();
            GetStudentApiKeyRunnable.getApiKey(this, f, l, i, e);
        });
        builder.setNegativeButton("CANCEL", (dialog, id) -> {
            Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void handleError(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setText(s);
        builder.setView(tv);
        builder.setPositiveButton("OK", (dialog, id) -> {
            apiCall(null);
        });
    }

    public void handleApiKeySucceeded(String s) {
        apiKey = s;
        myPrefs.save("API_Key", apiKey);
        myPrefs.save("First_Key", f);
        myPrefs.save("Last_Key", l);
        myPrefs.save("Email_Key", e);
        myPrefs.save("ID_Key", i);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.logo);

        builder.setPositiveButton("OK", (dialog, id) -> {});

        builder.setMessage("Name: " + f + " " + l + "\n"
        + "Student ID: " + i + "\n" + "Email: " + e + "\n" + "API Key: " + apiKey);
        builder.setTitle("API Key Received and Stored");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void clearAll(View v) {
        Log.d(TAG, "clearAll: ");

        myPrefs.removeValue("API_Key");
        myPrefs.removeValue("First_Key");
        myPrefs.removeValue("Last_Key");
        myPrefs.removeValue("Email_Key");
        myPrefs.removeValue("ID_Key");
        apiKey = "";
        f = "";
        l = "";
        e = "";
        i = "";

        apiCall(null);
    }

    private void customizeActionBar() {

        // This function sets the font of the title in the app bar

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        String t = "Rewards";
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

    public void doCreateCall(View v) {
        Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
        intent.putExtra("API", apiKey);
        activityResultLauncher.launch(intent);
    }

    public void doLoginCall(View v) {
        String userName = binding.editTextTextPersonName.getText().toString();
        String passWord = binding.editTextTextPassword.getText().toString();
        String login = "Login";

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("API", apiKey);
        intent.putExtra("Login", login);
        intent.putExtra("LogUser", userName);
        intent.putExtra("LogPass", passWord);
        activityResultLauncher.launch(intent);
    }

    private void setupEditText(){

        binding.editTextTextPersonName.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_USERNAME) // Specifies a max text length
        });

        binding.editTextTextPassword.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_PASSWORD) // Specifies a max text length
        });
    }

    public void credentialsChecked(View v){
        boolean checked = ((CheckBox) v).isChecked();

        if (checked) {
            String username = binding.editTextTextPersonName.getText().toString();
            String password = binding.editTextTextPassword.getText().toString();
            myPrefs.save("Username_Key", username);
            myPrefs.save("Password_Key", password);
        }
        else {
            myPrefs.removeValue("Username_Key");
            myPrefs.removeValue("Password_Key");
            savedUsername = "";
            savedPassword = "";
        }
    }
}