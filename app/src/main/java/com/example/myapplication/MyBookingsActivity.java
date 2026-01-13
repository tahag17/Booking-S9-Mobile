package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyBookingsActivity extends BaseActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView fallbackText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);
        setupHeaderLogin();

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        fallbackText = findViewById(R.id.fallbackText);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        fetchBookings();
    }

    private void fetchBookings() {
        progressBar.setVisibility(View.VISIBLE);
        fallbackText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://booking-backend-295607ecab74.herokuapp.com/api/booking/get-booked-listing")
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        fallbackText.setVisibility(View.VISIBLE);
                        fallbackText.setText("Failed to fetch bookings");
                    });
                    return;
                }

                String jsonStr = response.body().string();
                Log.d("MyBookings", "Bookings JSON: " + jsonStr);
                JSONArray bookingsArray = new JSONArray(jsonStr);

                List<JSONObject> bookingsList = new ArrayList<>();
                for (int i = 0; i < bookingsArray.length(); i++) {
                    bookingsList.add(bookingsArray.getJSONObject(i));
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (bookingsList.isEmpty()) {
                        fallbackText.setVisibility(View.VISIBLE);
                        fallbackText.setText("No bookings found");
                    } else {
                        recyclerView.setAdapter(new BookingAdapter(bookingsList));
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    fallbackText.setVisibility(View.VISIBLE);
                    fallbackText.setText("Failed to parse bookings");
                });
            }
        }).start();
    }

    static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

        List<JSONObject> items;

        BookingAdapter(List<JSONObject> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            JSONObject item = items.get(position);

            holder.locationText.setText(item.optString("location", ""));

            // --- Dates ---
            try {
                JSONObject datesObj = item.getJSONObject("dates");
                String startStr = datesObj.getString("startDate");
                String endStr = datesObj.getString("endDate");

                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy");

                Date startDate = apiFormat.parse(startStr);
                Date endDate = apiFormat.parse(endStr);

                holder.datesText.setText("From: " + displayFormat.format(startDate)
                        + " → To: " + displayFormat.format(endDate));
            } catch (Exception e) {
                holder.datesText.setText("Dates unavailable");
            }

            // --- Price ---
            try {
                JSONObject priceObj = item.getJSONObject("totalPrice");
                holder.priceText.setText("Price: " + priceObj.optInt("value", 0));
            } catch (Exception e) {
                holder.priceText.setText("Price: N/A");
            }

            try {
                JSONObject coverObj = item.optJSONObject("cover");
                String coverFile = coverObj != null ? coverObj.optString("file", "") : "";

                if (!coverFile.isEmpty()) {
                    byte[] decoded = android.util.Base64.decode(coverFile, android.util.Base64.DEFAULT);
                    Glide.with(holder.coverImage.getContext())
                            .asBitmap()
                            .load(decoded)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(holder.coverImage);
                } else {
                    holder.coverImage.setImageResource(R.drawable.ic_launcher_background);
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.coverImage.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView coverImage;
            TextView locationText, datesText, priceText;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                coverImage = itemView.findViewById(R.id.coverImage);
                locationText = itemView.findViewById(R.id.locationText);
                datesText = itemView.findViewById(R.id.datesText);
                priceText = itemView.findViewById(R.id.priceText);
            }
        }
    }
}
