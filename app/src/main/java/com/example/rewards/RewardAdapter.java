package com.example.rewards;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private static final String TAG = "OfficialAdapter";
    private final List<Reward> rewardList;
    private final ProfileActivity profileAct;

    RewardAdapter(List<Reward> rewardList, ProfileActivity pa) {
        this.rewardList = rewardList;
        profileAct = pa;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_layout_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Reward reward = rewardList.get(position);
        holder.name.setText(reward.getGiverName());
        holder.date.setText(reward.getAwardDate());
        holder.note.setText(reward.getNote());
        holder.points.setText(String.valueOf(reward.getAmount()));
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

}
