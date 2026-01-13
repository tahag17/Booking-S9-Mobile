package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyBookingsActivity extends BaseActivity {

    private TextView textViewBookings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        textViewBookings = findViewById(R.id.textViewBookings);

        fetchBookings();
    }

    private void fetchBookings() {
        String token = getAccessToken();
        if (token == null) {
            textViewBookings.setText("You need to login first!");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://booking-backend-295607ecab74.herokuapp.com/api/booking/get-booked-listing")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MyBookings", "Network error", e);
                runOnUiThread(() -> textViewBookings.setText("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> textViewBookings.setText("Error: " + response.code()));
                    return;
                }

                // Parse JSON and remove "cover" field
                try {
                    JSONArray bookings = new JSONArray(body);
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < bookings.length(); i++) {
                        JSONObject booking = bookings.getJSONObject(i);
                        JSONObject filteredBooking = new JSONObject();

                        // Use keys from booking.names()
                        JSONArray keys = booking.names();
                        if (keys != null) {
                            for (int j = 0; j < keys.length(); j++) {
                                String key = keys.getString(j);
                                if (!key.equals("cover")) {
                                    filteredBooking.put(key, booking.get(key));
                                }
                            }
                        }

                        sb.append(filteredBooking.toString(2)); // pretty print
                        sb.append("\n\n");
                    }

                    runOnUiThread(() -> textViewBookings.setText(sb.toString()));

                } catch (JSONException e) {
                    Log.e("MyBookings", "JSON parse error", e);
                    runOnUiThread(() -> textViewBookings.setText("JSON parse error: " + e.getMessage()));
                }

            }
        });
    }
}
