package com.example.costmatic;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CosmaticItem {
    private String brand;
    private String name;
    private String sub_categories;
    private String sub_category_ids;
    private String image;
    private Bitmap bmImg;

    public CosmaticItem(){

    }
    public CosmaticItem(JSONObject object) throws JSONException {
        this.brand = object.getString("brand");
        this.name = object.getString("name");
        this.sub_categories = object.getJSONArray("sub_categories").get(0).toString();
        this.sub_category_ids = object.getString("sub_category_ids");
        this.image = object.getString("image");
        toString();
    }

    @Override
    public String toString() {
        return "CosmaticItem {" +
                "\n\t brand='" + brand + '\'' +
                ",\n\t name='" + name + '\'' +
                ",\n\t sub_categories='" + sub_categories + '\'' +
                ",\n\t sub_category_ids='" + sub_category_ids + '\'' +
                ",\n\t image='" + image + '\'' +
                "\n}";
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSub_categories() {
        return sub_categories;
    }

    public void setSub_categories(String sub_categories) {
        this.sub_categories = sub_categories;
    }

    public String getSub_category_ids() {
        return sub_category_ids;
    }

    public void setSub_category_ids(String sub_category_ids) {
        this.sub_category_ids = sub_category_ids;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Bitmap getBmImg() {
        return bmImg;
    }

    public void setBmImg(Bitmap bmImg) {
        this.bmImg = bmImg;
    }
}
