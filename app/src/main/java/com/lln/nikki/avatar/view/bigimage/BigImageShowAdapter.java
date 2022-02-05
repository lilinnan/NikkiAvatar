package com.lln.nikki.avatar.view.bigimage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.lln.nikki.avatar.R;
import com.lln.nikki.avatar.model.Avatar;

import java.util.List;

public class BigImageShowAdapter extends RecyclerView.Adapter<BigImageShowAdapter.BigImageShowViewHolder> {


    private final Context mContext;
    private final List<Avatar> mAvatarList;
    private final Runnable mOnCloseRunnable;

    public BigImageShowAdapter(Context context, List<Avatar> avatarList, Runnable onCloseRunnable) {
        this.mContext = context;
        this.mAvatarList = avatarList;
        this.mOnCloseRunnable = onCloseRunnable;
    }

    @NonNull
    @Override
    public BigImageShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BigImageShowViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.big_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BigImageShowViewHolder holder, int position) {
        holder.mPhotoView.setImageBitmap(mAvatarList.get(position).getAvatar());
    }

    @Override
    public int getItemCount() {
        return mAvatarList.size();
    }

    class BigImageShowViewHolder extends RecyclerView.ViewHolder {

        private PhotoView mPhotoView;

        public BigImageShowViewHolder(@NonNull View itemView) {
            super(itemView);
            init();
        }

        private void init() {
            mPhotoView = findViewById(R.id.big_image_show);
            mPhotoView.setOnOutsidePhotoTapListener(imageView -> mOnCloseRunnable.run());
        }

        private <T extends View> T findViewById(@IdRes int id) {
            return itemView.findViewById(id);
        }
    }
}
