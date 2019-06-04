package com.gelakinetic.mtgfam.helpers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.gelakinetic.mtgfam.R;

import java.util.LinkedHashSet;

public class ExpansionImageHelper {

    public enum ExpansionImageSize {
        SMALL,
        LARGE
    }

    public static class ExpansionImageData {
        private String mExpansionName;
        private String mExpansionCode;
        private char mRarity;
        private long mMultiverseId;

        public ExpansionImageData(String name, String code, char rarity, long multiverseID) {
            mExpansionName = name;
            mExpansionCode = code;
            mRarity = rarity;
            mMultiverseId = multiverseID;
        }

        public String getSetCode() {
            return mExpansionCode;
        }

        public long getMultiverseId() {
            return mMultiverseId;
        }
    }

    private class ChangeSetListViewHolder extends RecyclerView.ViewHolder {

        private TextView setName;
        private ImageView setImage;
        private ExpansionImageData data;

        ChangeSetListViewHolder(@NonNull ViewGroup view, ChangeSetListAdapter changeSetListAdapter) {
            // Inflates to itemView
            super(LayoutInflater.from(view.getContext()).inflate(R.layout.trader_change_set, null, false));
            setName = itemView.findViewById(R.id.changeSetName);
            setImage = itemView.findViewById(R.id.changeSetImage);
            itemView.findViewById(R.id.changeSetCombo).setOnClickListener(v -> {
                if (null != data) {
                    changeSetListAdapter.onClickDismiss(data);
                }
            });
        }

        ImageView getImageView() {
            return setImage;
        }

        public void setData(ExpansionImageData d) {
            data = d;
        }
    }

    public abstract class ChangeSetListAdapter extends RecyclerView.Adapter<ChangeSetListViewHolder> {

        private final Context mContext;
        private final ExpansionImageData[] mExpansions;
        private Dialog dialog;
        private ExpansionImageSize mImageSize;

        protected ChangeSetListAdapter(Context context, LinkedHashSet<ExpansionImageData> expansions, ExpansionImageSize size) {
            mContext = context;
            mExpansions = expansions.toArray(new ExpansionImageData[0]);
            mImageSize = size;
        }

        @NonNull
        @Override
        public ChangeSetListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ChangeSetListViewHolder(viewGroup, this);
        }

        @Override
        public void onBindViewHolder(@NonNull ChangeSetListViewHolder changeSetListViewHolder, int i) {
            changeSetListViewHolder.setData(mExpansions[i]);
            changeSetListViewHolder.setName.setText(mExpansions[i].mExpansionName);
            ExpansionImageHelper.loadExpansionImage(mContext, mExpansions[i].mExpansionCode, mExpansions[i].mRarity, changeSetListViewHolder.getImageView(), mImageSize);
        }

        @Override
        public int getItemCount() {
            return mExpansions.length;
        }

        public void setDialogReference(@NonNull Dialog d) {
            dialog = d;
        }

        void onClickDismiss(ExpansionImageData data) {
            onClick(data);
            if (null != dialog) {
                dialog.dismiss();
            }
        }

        protected abstract void onClick(ExpansionImageData data);
    }

    public static void loadExpansionImage(Context context, String set, char rarity, ImageView imageView, ExpansionImageSize size) {
        if (context != null) {

            Log.v("EIH", "Loading " + set + "_" + rarity);
            imageView.setVisibility(View.GONE);

            int width, height;
            switch (size) {
                case SMALL:
                    width = context.getResources().getDimensionPixelSize(R.dimen.ExpansionImageWidthSmall);
                    height = context.getResources().getDimensionPixelSize(R.dimen.ExpansionImageHeightSmall);
                    break;
                default:
                case LARGE:
                    width = context.getResources().getDimensionPixelSize(R.dimen.ExpansionImageWidthLarge);
                    height = context.getResources().getDimensionPixelSize(R.dimen.ExpansionImageHeightLarge);
                    break;
            }

            // Then load the image
            Glide.with(context)
                    .load("https://raw.githubusercontent.com/AEFeinstein/GathererScraper/no-foreign-mid/symbols/" + set + "_" + rarity + ".png")
                    .dontAnimate()
                    .fitCenter()
                    .override(width, height)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.v("EIH", "Failed " + set + "_" + rarity);
                            if (e != null) {
                                Log.e("EIH", e.getMessage());
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.v("EIH", "Succeeded " + set + "_" + rarity);
                            imageView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}
