package com.example.myapplication.navbar.category;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryService {

    private List<Category> categories;

    public CategoryService() {
        categories = new ArrayList<>();
        // Example icons: you need to add drawable resources for these
        categories.add(new Category("All", "ALL", R.drawable.all, false));
        categories.add(new Category("Amazing views", "AMAZING_VIEWS", R.drawable.eye, false));
        categories.add(new Category("OMG!", "OMG", R.drawable.exclamation, false));
        categories.add(new Category("Treehouses", "TREEHOUSES", R.drawable.tree, false));
        categories.add(new Category("Beach", "BEACH", R.drawable.beach, false));
        categories.add(new Category("Farms", "FARMS", R.drawable.farm, false));
        categories.add(new Category("Tiny homes", "TINY_HOMES", R.drawable.tiny_home, false));
        categories.add(new Category("Lake", "LAKE", R.drawable.lake, false));
        categories.add(new Category("Containers", "CONTAINERS", R.drawable.container, false));
        categories.add(new Category("Camping", "CAMPING", R.drawable.tent, false));
        // Remaining 12 categories
        categories.add(new Category("Castle", "CASTLE", R.drawable.castle, false));
        categories.add(new Category("Skiing", "SKIING", R.drawable.skiing, false));
        categories.add(new Category("Campers", "CAMPERS", R.drawable.campers, false));
        categories.add(new Category("Artic", "ARTIC", R.drawable.arctic, false));
        categories.add(new Category("Boat", "BOAT", R.drawable.boat, false));
        categories.add(new Category("Bed & breakfasts", "BED_AND_BREAKFASTS", R.drawable.bednbreakfast, false));
        categories.add(new Category("Rooms", "ROOMS", R.drawable.rooms, false));
        categories.add(new Category("Earth homes", "EARTH_HOMES", R.drawable.earth_homes, false));
        categories.add(new Category("Tower", "TOWER", R.drawable.tower, false));
        categories.add(new Category("Caves", "CAVES", R.drawable.caves, false));
        categories.add(new Category("Luxes", "LUXES", R.drawable.luxes, false));
        categories.add(new Category("Chef's kitchen", "CHEFS_KITCHEN", R.drawable.chefs_kitchen, false));

    }

    public List<Category> getCategories() {
        return categories;
    }

    public Category getDefaultCategory() {
        return categories.get(0);
    }

    public Category getCategoryByTechnicalName(String technicalName) {
        for (Category c : categories) {
            if (c.getTechnicalName().equals(technicalName)) {
                return c;
            }
        }
        return null;
    }


}
