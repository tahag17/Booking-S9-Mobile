package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

public class ListingDetailsActivity extends AppCompatActivity {

    ProgressBar progressBar;
    androidx.viewpager2.widget.ViewPager2 imagePager;
    TextView titleText, descriptionText, locationText, categoryText;
    TextView guestsText, bedroomsText, bedsText, bathsText;
    TextView priceText, landlordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_details);

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

        // Smooth carousel optimizations
        imagePager.setOffscreenPageLimit(3);
        imagePager.setClipToPadding(false);

        String listingId = getIntent().getStringExtra("listingId");
        fetchListingDetails(listingId);
    }


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

                        // Description
                        JSONObject description = obj.optJSONObject("description");
                        if (description != null) {
                            JSONObject titleObj = description.optJSONObject("title");
                            JSONObject descObj = description.optJSONObject("description");

                            String title = titleObj != null ? titleObj.optString("value", "") : "";
                            String desc = descObj != null ? descObj.optString("value", "") : "";

                            titleText.setText(title);
                            descriptionText.setText(desc);
                        }


                        // Location & Category
                        locationText.setText(obj.optString("location", ""));
                        categoryText.setText(obj.optString("category", ""));

                        // Infos
                        JSONObject infos = obj.optJSONObject("infos");
                        if (infos != null) {
                            JSONObject guestsObj = infos.optJSONObject("guests");
                            JSONObject bedroomsObj = infos.optJSONObject("bedrooms");
                            JSONObject bedsObj = infos.optJSONObject("beds");
                            JSONObject bathsObj = infos.optJSONObject("baths");

                            int guests = guestsObj != null ? guestsObj.optInt("value", 0) : 0;
                            int bedrooms = bedroomsObj != null ? bedroomsObj.optInt("value", 0) : 0;
                            int beds = bedsObj != null ? bedsObj.optInt("value", 0) : 0;
                            int baths = bathsObj != null ? bathsObj.optInt("value", 0) : 0;

                            guestsText.setText("Guests: " + guests);
                            bedroomsText.setText("Bedrooms: " + bedrooms);
                            bedsText.setText("Beds: " + beds);
                            bathsText.setText("Baths: " + baths);
                        }


                        // Price
                        JSONObject price = obj.optJSONObject("price");
                        if (price != null) {
                            priceText.setText("Price: " + price.optInt("value", 0));
                        }

                        // Landlord
                        JSONObject landlord = obj.optJSONObject("landlord");
                        if (landlord != null) {
                            landlordText.setText(
                                    "Landlord: " + landlord.optString("firstname", "")
                            );
                        }

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

    interface ApiService {
        @GET("tenant-listing/get-one")
        Call<ResponseBody> getOneListing(@Query("publicId") String publicId);
    }
}



class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

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

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}

