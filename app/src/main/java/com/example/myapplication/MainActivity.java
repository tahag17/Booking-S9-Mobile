package com.example.myapplication;

import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    JSONArray listingsArray = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        fetchData();
    }

    private void fetchData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://booking-backend-295607ecab74.herokuapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<ResponseBody> call = apiService.getAllByCategory(0, 20, "ALL");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        listingsArray = obj.getJSONArray("content");

                        // set adapter
                        recyclerView.setAdapter(new ListingAdapter(listingsArray));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // ------------------- API Interface -------------------
    interface ApiService {
        @GET("tenant-listing/get-all-by-category")
        Call<ResponseBody> getAllByCategory(
                @Query("page") int page,
                @Query("size") int size,
                @Query("category") String category
        );
    }

    // ------------------- RecyclerView Adapter -------------------
    class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

        JSONArray items;

        ListingAdapter(JSONArray items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_listing_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            JSONObject item = items.optJSONObject(position);
            if (item == null) return;

            holder.locationText.setText(item.optString("location", ""));
            holder.categoryText.setText(item.optString("bookingCategory", ""));
            JSONObject priceObj = item.optJSONObject("price");
            holder.priceText.setText("Price: " + (priceObj != null ? priceObj.optInt("value", 0) : "0"));

            JSONObject coverObj = item.optJSONObject("cover");
            if (coverObj != null) {
                String coverFile = coverObj.optString("file", "");
                if (coverFile.startsWith("/9j/")) { // likely a base64 image
                    try {
                        byte[] decodedBytes = Base64.decode(coverFile, Base64.DEFAULT);

                        // <-- Glide used for smooth scrolling -->
                        Glide.with(holder.coverImage.getContext())
                                .asBitmap()
                                .load(decodedBytes)
                                .centerCrop()
                                .into(holder.coverImage);

                    } catch (Exception e) {
                        e.printStackTrace();
                        holder.coverImage.setImageResource(R.drawable.ic_launcher_background);
                    }
                } else {
                    holder.coverImage.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                holder.coverImage.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        @Override
        public int getItemCount() {
            return items.length();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView coverImage;
            TextView locationText, categoryText, priceText;

            ViewHolder(View itemView) {
                super(itemView);
                coverImage = itemView.findViewById(R.id.coverImage);
                locationText = itemView.findViewById(R.id.locationText);
                categoryText = itemView.findViewById(R.id.categoryText);
                priceText = itemView.findViewById(R.id.priceText);
            }
        }
    }
}
