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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


import com.example.rewards.databinding.ActivityEditBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int MAX_LEN_NAME = 20;
    private static final int MAX_LEN_DEPARTMENT = 30;
    private static final int MAX_LEN_STORY = 360;
    private static final int MAX_LEN_PASSWORD = 40;

    private String apiKey;

    private ActivityEditBinding binding;

    private ActivityResultLauncher<Intent> thumbActivityResultLauncher;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_REQUEST = 111;

    private Profile profile;
    private String firstName;
    private String lastName;
    private String username;
    private String department;
    private String story;
    private String position;
    private String password;
    private int remainingPointsToAward = 1000;
    private int pointsReceived = 0;
    private static String locationString = "unspecified location";
    private String imageString64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        customizeActionBar();

        mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
        determineLocation();

        Intent intent = getIntent();
        if (intent.hasExtra("API")) {
            apiKey = (String) intent.getSerializableExtra("API");
        }
        if (intent.hasExtra("Profile")) {
            profile = (Profile) intent.getSerializableExtra("Profile");
        }

        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupEditText();

        binding.passwordEdit.setText(profile.getPassword());
        binding.usernameEdit.setText(profile.getUsername());
        binding.firstNameEdit.setText(profile.getFirstName());
        binding.lastNameEdit.setText(profile.getLastName());
        binding.positionEdit.setText(profile.getPosition());
        binding.departmentEdit.setText(profile.getDepartment());
        binding.tellUsEdit.setText(profile.getStory());

        imageString64 = profile.getImage();

        byte[] imageBytes = Base64.decode(imageString64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        binding.imageProfileEdit.setImageBitmap(bitmap);

        Log.d(TAG, "onCreate: ");

        thumbActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleThumbResult);

        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleGalleryResult);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleResult);
    }

    private void determineLocation() {
        if (checkPermission()) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Got last known location. In some situations this can be null.
                        if (location != null) {
                            locationString = getPlace(location);
                        }
                    })
                    .addOnFailureListener(this, e -> Toast.makeText(EditProfileActivity.this,
                            e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_REQUEST);
            return false;
        }
        return true;
    }

    private String getPlace(Location loc) {

        StringBuilder sb = new StringBuilder();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            sb.append(String.format(
                    Locale.getDefault(),
                    "%s, %s%n%n",
                    city, state, loc.getProvider(), loc.getLatitude(), loc.getLongitude()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    determineLocation();
                } else {
                    locationString = "unspecified location";
                }
            }
        }
    }

    public void handleResult(ActivityResult result) {
        Toast.makeText(this, "Back to CreateProfileActivity!", Toast.LENGTH_SHORT).show();
    }

    public void galleryOrCamera(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Take picture from:");
        builder.setTitle("Profile Picture");
        builder.setIcon(R.drawable.logo);

        builder.setNegativeButton("GALLERY", (dialog, id) -> {
            doGallery(null);
        });
        builder.setPositiveButton("CAMERA", (dialog, id) -> {
            doThumb(null);
        });
        builder.setNeutralButton("CANCEL", (dialog, id) -> {
            Toast.makeText(EditProfileActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void doThumb(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        thumbActivityResultLauncher.launch(takePictureIntent);
    }

    public void doGallery(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        galleryActivityResultLauncher.launch(photoPickerIntent);
    }

    public void handleThumbResult(ActivityResult result) {
        if (result == null || result.getData() == null) {
            Log.d(TAG, "handleResult: NULL ActivityResult received");
            return;
        }

        if (result.getResultCode() == RESULT_OK) {
            try {
                Intent data = result.getData();
                processCameraThumb(data.getExtras());
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void handleGalleryResult(ActivityResult result) {
        if (result == null || result.getData() == null) {
            Log.d(TAG, "handleResult: NULL ActivityResult received");
            return;
        }

        if (result.getResultCode() == RESULT_OK) {
            try {
                Intent data = result.getData();
                processGallery(data);
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void processGallery(Intent data) {
        Uri galleryImageUri = data.getData();
        if (galleryImageUri == null)
            return;

        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(galleryImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        binding.imageProfileEdit.setImageBitmap(selectedImage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Resize to match imageview size
        int bmW = selectedImage.getWidth();
        int bmH = selectedImage.getHeight();
        double ratio = (double) bmW / (double) bmH;

        int h = binding.imageProfileEdit.getHeight();
        int w = (int) (h * ratio);
        selectedImage = Bitmap.createScaledBitmap(selectedImage, w, h, false);

        selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] byteArray = baos.toByteArray();
        imageString64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

    }

    private void processCameraThumb(Bundle extras) {

        Bitmap imageBitmap = (Bitmap) extras.get("data");
        binding.imageProfileEdit.setImageBitmap(imageBitmap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Resize to match imageview size
        int bmW = imageBitmap.getWidth();
        int bmH = imageBitmap.getHeight();
        double ratio = (double) bmW / (double) bmH;

        int h = binding.imageProfileEdit.getHeight();
        int w = (int) (h * ratio);
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, w, h, false);

        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] byteArray = baos.toByteArray();
        imageString64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void setupEditText() {

        binding.usernameEdit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_NAME) // Specifies a max text length
        });
        binding.firstNameEdit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_NAME) // Specifies a max text length
        });
        binding.lastNameEdit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_NAME) // Specifies a max text length
        });
        binding.departmentEdit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_DEPARTMENT) // Specifies a max text length
        });
        binding.tellUsEdit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_STORY) // Specifies a max text length
        });
        binding.tellUsEdit.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        // This one executes upon completion of typing a character
                        int len = s.toString().length();
                        String countText = "Your Story: (" + len + " of " + MAX_LEN_STORY + ")";
                        binding.yourStoryEdit.setText(countText);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                });
        binding.positionEdit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_NAME) // Specifies a max text length
        });
        binding.passwordEdit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LEN_PASSWORD) // Specifies a max text length
        });
    }

    private void customizeActionBar() {

        // This function sets the font of the title in the app bar

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        String t = "Edit Profile";
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
            builder.setTitle("Save Changes?");
            builder.setIcon(R.drawable.logo);
            builder.setPositiveButton("OK", (dialog, id) -> {
                Toast.makeText(this, "You want to Save", Toast.LENGTH_SHORT).show();
                firstName = binding.firstNameEdit.getText().toString();
                lastName = binding.lastNameEdit.getText().toString();
                username = binding.usernameEdit.getText().toString();
                department = binding.departmentEdit.getText().toString();
                story = binding.tellUsEdit.getText().toString();
                position = binding.positionEdit.getText().toString();
                password = binding.passwordEdit.getText().toString();
                remainingPointsToAward = profile.getRemainingPointsToAward();
                pointsReceived = profile.getPointsReceived();

                profile = new Profile(firstName, lastName, username, department, story, position, password,
                        remainingPointsToAward, locationString, imageString64, pointsReceived);

                String updateProfile = "UpdateProfile";
                Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                intent.putExtra("Profile", profile);
                intent.putExtra("API", apiKey);
                intent.putExtra("UpdateProfile", updateProfile);

                activityResultLauncher.launch(intent);
            });
            builder.setNegativeButton("CANCEL", (dialog, id) -> {
                Toast.makeText(EditProfileActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}