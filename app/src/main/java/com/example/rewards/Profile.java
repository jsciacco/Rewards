package com.example.rewards;

import android.util.JsonWriter;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

public class Profile implements Comparable<Profile>, Serializable {

    private final String firstName;
    private final String lastName;
    private final String username;
    private final String department;
    private final String story;
    private final String position;
    private final String password;
    private int remainingPointsToAward;
    private final String location;
    private final String image;
    private int pointsReceived;

    Profile(String firstName, String lastName, String username,
            String department, String story, String position, String password,
            int remainingPointsToAward, String location, String image, int pointsReceived) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.department = department;
        this.story = story;
        this.position = position;
        this.password = password;
        this.remainingPointsToAward = remainingPointsToAward;
        this.location = location;
        this.image = image;
        this.pointsReceived = pointsReceived;
       // this.rewardRecordViews = rewardRecordViews;
    }

    String getFirstName() {
        return firstName;
    }

    String getLastName() {
        return lastName;
    }

    String getUsername() {
        return username;
    }

    String getDepartment() {
        return department;
    }

    String getStory() {
        return story;
    }

    String getPosition() {
        return position;
    }

    String getPassword() {
        return password;
    }

    int getRemainingPointsToAward() {
        return remainingPointsToAward;
    }

    String getLocation() {
        return location;
    }

    String getImage() { return image;}

    int getPointsReceived() {
        return pointsReceived;
    }

    public void setRemainingPointsToAward(int newPoints){
        this.remainingPointsToAward = newPoints;
    }

    @NonNull
    @Override
    public String toString() {

        try {
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(sw);
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            jsonWriter.name("firstName").value(getFirstName());
            jsonWriter.name("lastName").value(getLastName());
            jsonWriter.name("username").value(getUsername());
            jsonWriter.name("department").value(getDepartment());
            jsonWriter.name("story").value(getStory());
            jsonWriter.name("position").value(getPosition());
            jsonWriter.name("password").value(getPassword());
            jsonWriter.name("remainingPointsToAward").value(getRemainingPointsToAward());
            jsonWriter.name("location").value(getLocation());
            jsonWriter.name("image").value(getImage());
            jsonWriter.endObject();
            jsonWriter.close();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
    @Override
    public int compareTo(Profile profile) {
        return lastName.compareTo(profile.lastName);
    }
}
