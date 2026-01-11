package com.example.myapplication;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

public class ListingDetailsActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ViewPager2 imagePager;
    TextView titleText, descriptionText, locationText, categoryText;
    TextView guestsText, bedroomsText, bedsText, bathsText;
    TextView priceText, landlordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_details);

        // --- Bind Views ---
        progressBar = findViewById(R.id.progressBar);
        imagePager = findViewById(R.id.imagePager);
        titleText = findViewById(R.id.titleText);
        descriptionText = findViewById(R.id.descriptionText);
        locationText = findViewById(R.id.locationText);
        categoryText = findViewById(R.id.categoryText);
        guestsText = findViewById(R.id.guestsText);
        bedroomsText = findViewById(R.id.bedroomsText);
        bedsText = findViewById(R.id.bedsText);
        bathsText = findViewById(R.id.bathsText);
        priceText = findViewById(R.id.priceText);
        landlordText = findViewById(R.id.landlordText);

        // Optimize ViewPager
        imagePager.setOffscreenPageLimit(3);
        imagePager.setClipToPadding(false);

        // Get listing ID from intent
        String listingId = getIntent().getStringExtra("listingId");
        if (listingId != null) {
            fetchListingDetails(listingId);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    // --- Fetch Listing Details ---
    private void fetchListingDetails(String listingId) {
        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://booking-backend-295607ecab74.herokuapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getOneListing(listingId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject obj = new JSONObject(response.body().string());

                        // --- Description ---
                        JSONObject description = obj.optJSONObject("description");
                        if (description != null) {
                            String title = description.optJSONObject("title") != null ?
                                    description.optJSONObject("title").optString("value", "") : "";
                            String desc = description.optJSONObject("description") != null ?
                                    description.optJSONObject("description").optString("value", "") : "";

                            titleText.setText(title);
                            descriptionText.setText(desc);
                        }

                        // --- Location & Category ---
                        locationText.setText(obj.optString("location", ""));
                        categoryText.setText(obj.optString("category", ""));

                        // --- Infos ---
                        JSONObject infos = obj.optJSONObject("infos");
                        if (infos != null) {
                            guestsText.setText("Guests: " + getValueFromJson(infos, "guests"));
                            bedroomsText.setText("Bedrooms: " + getValueFromJson(infos, "bedrooms"));
                            bedsText.setText("Beds: " + getValueFromJson(infos, "beds"));
                            bathsText.setText("Baths: " + getValueFromJson(infos, "baths"));
                        }

                        // --- Price ---
                        JSONObject price = obj.optJSONObject("price");
                        priceText.setText("Price: " + (price != null ? price.optInt("value", 0) : 0));

                        // --- Landlord ---
                        JSONObject landlord = obj.optJSONObject("landlord");
                        landlordText.setText("Landlord: " + (landlord != null ? landlord.optString("firstname", "") : ""));

                        // --- Pictures ---
                        JSONArray pictures = obj.optJSONArray("pictures");
                        if (pictures != null && pictures.length() > 0) {
                            imagePager.setAdapter(new ImagePagerAdapter(pictures));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });
    }

    // Helper for getting values from JSON objects
    private int getValueFromJson(JSONObject parent, String key) {
        JSONObject obj = parent.optJSONObject(key);
        return obj != null ? obj.optInt("value", 0) : 0;
    }

    // --- Retrofit API ---
    interface ApiService {
        @GET("tenant-listing/get-one")
        Call<ResponseBody> getOneListing(@Query("publicId") String publicId);
    }

    // --- Image Pager Adapter ---
    static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

        JSONArray pictures;

        ImagePagerAdapter(JSONArray pictures) {
            this.pictures = pictures;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            try {
                JSONObject picture = pictures.getJSONObject(position);
                String file = picture.optString("file", "");
                if (!file.isEmpty()) {
                    byte[] decoded = Base64.decode(file, Base64.DEFAULT);
                    Glide.with(holder.imageView.getContext())
                            .load(decoded)
                            .centerCrop()
                            .into(holder.imageView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return pictures.length();
        }

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}
