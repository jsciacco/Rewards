package com.example.rewards;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderViewHolder> {

    private static final String TAG = "LeaderboardAdapter";
    private final List<Profile> profileList;
    private final LeaderboardActivity leaderAct;

    LeaderboardAdapter(List<Profile> profileList, LeaderboardActivity la) {
        this.profileList = profileList;
        leaderAct = la;
    }

    @NonNull
    @Override
    public LeaderViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_layout_row, parent, false);

        return new LeaderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        String lastNameFirstName = profile.getLastName() + ", "+profile.getFirstName();
        holder.lastNameFirstName.setText(lastNameFirstName);
        String positionDepartment = profile.getPosition() + ", "+profile.getDepartment();
        holder.positionDepartment.setText(positionDepartment);
        String imageBase64 = profile.getImage();
        byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        holder.image.setImageBitmap(bitmap);
        int totalAmount = profile.getPointsReceived();
        holder.points.setText(String.valueOf(totalAmount));
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }
}
