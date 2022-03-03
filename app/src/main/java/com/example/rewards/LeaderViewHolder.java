package com.example.rewards;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class LeaderViewHolder extends RecyclerView.ViewHolder {

    TextView lastNameFirstName;
    TextView positionDepartment;
    TextView points;
    ImageView image;

    LeaderViewHolder(View view) {
        super(view);
        lastNameFirstName = view.findViewById(R.id.last_name_first_name_lead);
        positionDepartment = view.findViewById(R.id.position_department_lead);
        points = view.findViewById(R.id.points_lead);
        image = view.findViewById((R.id.profile_pic_leader));
    }
}
