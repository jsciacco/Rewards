package com.example.rewards;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class MyViewHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView date;
    TextView note;
    TextView points;

    MyViewHolder(View view) {
        super(view);
        name = view.findViewById(R.id.full_name_profile);
        date = view.findViewById(R.id.date_profile);
        note = view.findViewById(R.id.comment_profile);
        points = view.findViewById(R.id.points_profile);
    }

}