package com.elyesasimsek.kelimebilmece;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.elyesasimsek.kelimebilmece.databinding.ShopItemBinding;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopHolder>{
    private Context mContext;
    private ArrayList<Shop> shopArrayList;
    private OnItemClickListener listener;

    public ShopAdapter(Context mContext, ArrayList<Shop> shopArrayList) {
        this.mContext = mContext;
        this.shopArrayList = shopArrayList;
    }

    @NonNull
    @Override
    public ShopHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        ShopItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.shop_item, parent, false);
        return new ShopHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopHolder holder, int position) {
        Shop shop = shopArrayList.get(position);
        ShopItemBinding binding = holder.binding;
        binding.textViewShopItemItemTitle.setText(shop.getItemTitle());
        binding.imageViewShopItemItemImg.setBackgroundResource(shop.getItemImg());
        binding.buttonShopItemItemPrice.setText(shop.getItemPrice() + " TL");
        binding.cardViewShopItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && position != RecyclerView.NO_POSITION){
                    listener.onItemClick(shop, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (shopArrayList == null){
            return 0;
        }else {
            return shopArrayList.size();
        }
    }

    public class ShopHolder extends RecyclerView.ViewHolder{
        ShopItemBinding binding;

        public ShopHolder(ShopItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(Shop mShop, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
