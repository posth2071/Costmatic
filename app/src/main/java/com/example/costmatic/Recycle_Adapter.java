package com.example.costmatic;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URLEncoder;
import java.util.List;

public class Recycle_Adapter extends RecyclerView.Adapter<Recycle_Adapter.ViewHolder> implements View.OnClickListener {
    List<CosmaticItem> items;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView items_image;
        private TextView items_brand, items_name, items_category;

        public ViewHolder(@NonNull View view) {
            super(view);
            // Recycler Item 클릭이벤트 - 네이버 쇼핑 검색
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String strUrl = String.format("http://search.shopping.naver.com/search/all.nhn?query=%s", items_name.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(strUrl));
                    view.getContext().startActivity(intent);
                }
            });
            items_image = view.findViewById(R.id.items_image);
            items_brand = view.findViewById(R.id.items_brand);
            items_name = view.findViewById(R.id.items_name);
            items_category = view.findViewById(R.id.items_category);
        }
    }

    public Recycle_Adapter(List<CosmaticItem> items) {
        this.items = items;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflate = LayoutInflater.from(parent.getContext());
        View view = inflate.inflate(R.layout.items, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.items_image.setImageBitmap(items.get(position).getBmImg());
        holder.items_brand.setText(items.get(position).getBrand());
        holder.items_name.setText(items.get(position).getName());
        holder.items_category.setText(items.get(position).getSub_categories());
    }

    @Override
    public void onClick(View view) {

    }
}
