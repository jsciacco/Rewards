package com.example.rewards;

import android.util.JsonWriter;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

public class Reward implements Comparable<Reward>, Serializable {

    private final String giverName;
    private final String receiverName;
    private final int amount;
    private final String note;
    private final String awardDate;

    Reward(String giverName, String receiverName, int amount, String note,
            String awardDate) {
        this.giverName = giverName;
        this.receiverName = receiverName;
        this.amount = amount;
        this.note = note;
        this.awardDate = awardDate;
    }

    String getGiverName() { return giverName;}

    String getReceiverName() { return receiverName;}

    int getAmount() { return amount;}

    String getNote() { return note;}

    String getAwardDate() { return awardDate;}

    @NonNull
    @Override
    public String toString() {

        try {
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(sw);
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject();
            jsonWriter.name("giverName").value(getGiverName());
            jsonWriter.name("receiverName").value(getReceiverName());
            jsonWriter.name("amount").value(getAmount());
            jsonWriter.name("note").value(getNote());
            jsonWriter.name("awardDate").value(getAwardDate());
            jsonWriter.endObject();
            jsonWriter.close();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
    @Override
    public int compareTo(Reward reward) {
        return awardDate.compareTo(reward.awardDate);
    }
}
