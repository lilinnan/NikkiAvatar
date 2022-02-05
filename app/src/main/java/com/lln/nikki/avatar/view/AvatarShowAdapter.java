package com.lln.nikki.avatar.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.lln.nikki.avatar.R;
import com.lln.nikki.avatar.model.Avatar;
import com.lln.nikki.avatar.util.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 头像显示适配器
 */
public class AvatarShowAdapter extends RecyclerView.Adapter<AvatarShowAdapter.AvatarShowViewHolder> {

    private final List<Avatar> mAvatarList;
    private final Context mContext;

    public AvatarShowAdapter(Context context) {
        this.mContext = context;
        this.mAvatarList = new ArrayList<>();
    }

    @NonNull
    @Override
    public AvatarShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AvatarShowViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.avatar_show, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarShowViewHolder holder, int position) {
        Avatar avatar = mAvatarList.get(position);
        holder.mAvatarShowImageView.setImageBitmap(avatar.getAvatar());
    }

    @Override
    public int getItemCount() {
        return mAvatarList.size();
    }

    public void clear() {
        int size = mAvatarList.size();
        mAvatarList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addData(Avatar avatar) {
        notifyItemInserted(insertAndGetPosition(avatar));
    }

    private int insertAndGetPosition(Avatar avatar) {
        if (mAvatarList.size() == 0) {
            mAvatarList.add(avatar);
            return 0;
        }
        for (int i = 0; i < mAvatarList.size(); i++) {
            if (mAvatarList.get(i).getNumber() <= avatar.getNumber()) {
                continue;
            }
            mAvatarList.add(i, avatar);
            return i;
        }
        mAvatarList.add(avatar);
        return mAvatarList.size();
    }

    class AvatarShowViewHolder extends RecyclerView.ViewHolder {

        ImageView mAvatarShowImageView;

        public AvatarShowViewHolder(@NonNull View itemView) {
            super(itemView);
            init();
        }

        private void init() {
            mAvatarShowImageView = findViewById(R.id.avatar_show);
            mAvatarShowImageView.setOnClickListener(this::avatarShowClick);
            mAvatarShowImageView.setOnLongClickListener(this::avatarShowLongClick);
        }

        private void avatarShowClick(View view) {
            BigImageShowHelper.showImage(mContext, mAvatarList.get(getAdapterPosition()).getAvatar());
        }


        private boolean avatarShowLongClick(View v) {
            new AlertDialog.Builder(mContext)
                    .setTitle("提示")
                    .setMessage("是否要保存此头像到相册？")
                    .setPositiveButton("确定", (dialog, which) -> AvatarUtils.saveToGallery(mContext,
                            mAvatarList.get(getAdapterPosition()).getAvatar()))
                    .setNegativeButton("取消", null).show();
            return false;
        }

        private <T extends View> T findViewById(@IdRes int id) {
            return itemView.findViewById(id);
        }
    }
}
