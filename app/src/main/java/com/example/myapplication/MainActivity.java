package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends BaseActivity {

    RecyclerView recyclerView;
    Spinner categorySpinner;
    ProgressBar progressBar;
    TextView fallbackText;
    JSONArray listingsArray = new JSONArray();

    HashMap<String, Integer> categoryIcons = new HashMap<>();
    String[] categories = {
            "ALL","AMAZING_VIEWS","OMG","TREEHOUSES","BEACH","FARMS",
            "TINY_HOMES","LAKE","CONTAINERS","CAMPERS","CASTLE","ARTIC",
            "BOAT","BED_AND_BREAKFASTS","ROOMS","EARTH_HOMES","TOWER",
            "CAVES","LUXES","CHEFS_KITCHEN","SKIING"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupHeaderLogin();

        // --- Bind Views ---
        recyclerView = findViewById(R.id.recyclerView);
        categorySpinner = findViewById(R.id.categorySpinner);
        progressBar = findViewById(R.id.progressBar);
        fallbackText = findViewById(R.id.fallbackText);

        // Adaptive span count for tablets vs phones
        int spanCount = getResources().getConfiguration().screenWidthDp > 600 ? 2 : 1;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        // Map categories to drawable icons
        categoryIcons.put("ALL", R.drawable.all);
        categoryIcons.put("AMAZING_VIEWS", R.drawable.eye);
        categoryIcons.put("OMG", R.drawable.exclamation);
        categoryIcons.put("TREEHOUSES", R.drawable.tree);
        categoryIcons.put("BEACH", R.drawable.beach);
        categoryIcons.put("FARMS", R.drawable.farm);
        categoryIcons.put("TINY_HOMES", R.drawable.tiny_home);
        categoryIcons.put("LAKE", R.drawable.lake);
        categoryIcons.put("CONTAINERS", R.drawable.container);
        categoryIcons.put("CAMPERS", R.drawable.campers);
        categoryIcons.put("CASTLE", R.drawable.castle);
        categoryIcons.put("ARTIC", R.drawable.arctic);
        categoryIcons.put("BOAT", R.drawable.boat);
        categoryIcons.put("BED_AND_BREAKFASTS", R.drawable.bednbreakfast);
        categoryIcons.put("ROOMS", R.drawable.rooms);
        categoryIcons.put("EARTH_HOMES", R.drawable.earth_homes);
        categoryIcons.put("TOWER", R.drawable.tower);
        categoryIcons.put("CAVES", R.drawable.caves);
        categoryIcons.put("LUXES", R.drawable.luxes);
        categoryIcons.put("CHEFS_KITCHEN", R.drawable.chefs_kitchen);
        categoryIcons.put("SKIING", R.drawable.skiing);

        setupCategoryDropdown();
        fetchData("ALL");
    }

    // --- Category Spinner ---
    private void setupCategoryDropdown() {
        CategoryAdapter adapter = new CategoryAdapter();
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchData(categories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    class CategoryAdapter extends android.widget.BaseAdapter {

        @Override
        public int getCount() { return categories.length; }

        @Override
        public Object getItem(int position) { return categories[position]; }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createItemView(position, convertView, parent, true);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createItemView(position, convertView, parent, false);
        }

        private View createItemView(int position, View convertView, ViewGroup parent, boolean isSelectedView) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.spinner_item_with_icon, parent, false);
            }
            ImageView icon = convertView.findViewById(R.id.iconImage);
            TextView text = convertView.findViewById(R.id.textLabel);

            String category = categories[position];
            text.setText(category.replace("_", " "));
            if (categoryIcons.containsKey(category))
                icon.setImageResource(categoryIcons.get(category));

            // Highlight selected
            if (isSelectedView) {
                convertView.setBackgroundColor(getResources().getColor(R.color.primaryLight));
                text.setTextColor(getResources().getColor(R.color.primaryDarker));
            }

            return convertView;
        }
    }

    // --- Fetch Listings ---
    private void fetchData(String category) {
        progressBar.setVisibility(View.VISIBLE);
        fallbackText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://booking-backend-295607ecab74.herokuapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getAllByCategory(0, 20, category)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                JSONObject obj = new JSONObject(response.body().string());
                                listingsArray = obj.getJSONArray("content");

                                if (listingsArray.length() == 0) {
                                    fallbackText.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    fallbackText.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    recyclerView.setAdapter(new ListingAdapter(listingsArray));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            fallbackText.setVisibility(View.VISIBLE);
                            fallbackText.setText("Failed to parse listings");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        fallbackText.setText("Failed to load listings");
                        fallbackText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        t.printStackTrace();
                    }
                });
    }

    interface ApiService {
        @GET("tenant-listing/get-all-by-category")
        Call<ResponseBody> getAllByCategory(@Query("page") int page,
                                            @Query("size") int size,
                                            @Query("category") String category);
    }

    // --- RecyclerView Adapter ---
    static class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {
        JSONArray items;

        ListingAdapter(JSONArray items) { this.items = items; }

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
            String coverFile = coverObj != null ? coverObj.optString("file", "") : "";

            if (!coverFile.isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(coverFile, Base64.DEFAULT);
                    Glide.with(holder.coverImage.getContext())
                            .asBitmap()
                            .load(decoded)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(holder.coverImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.coverImage.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                holder.coverImage.setImageResource(R.drawable.ic_launcher_background);
            }

            holder.itemView.setOnClickListener(v -> {
                String publicId = item.optString("publicId", null);
                if (publicId != null) {
                    Intent intent = new Intent(v.getContext(), ListingDetailsActivity.class);
                    intent.putExtra("listingId", publicId);
                    v.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() { return items.length(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
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
