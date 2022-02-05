package com.lln.nikki.avatar.network.impl;

import static com.lln.nikki.avatar.ApplicationManager.getOkHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.lln.nikki.avatar.model.Avatar;
import com.lln.nikki.avatar.model.AvatarType;
import com.lln.nikki.avatar.network.AvatarService;
import com.lln.nikki.avatar.util.AvatarUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 头像爬取类
 */
public class AvatarServiceImpl implements AvatarService {
    private static final String TAG = AvatarServiceImpl.class.getSimpleName();
    private final Executor mExecutorService;
    private final List<Integer> mFailList;
    private final Stack<AvatarSearchRunnable> mSearchRunnablePool;
    private final List<AvatarSearchRunnable> mAllSearchRunnable;
    private AvatarResourcePool mCurrentResourcePool;
    private int mNowRunningRunnableNum;
    private OnAvatarAddListener mOnAvatarAddListener;
    private Runnable mOnSearchFinished;

    public AvatarServiceImpl() {
        this.mExecutorService = Executors.newFixedThreadPool(64);
        this.mFailList = new ArrayList<>();
        this.mSearchRunnablePool = new Stack<>();
        this.mAllSearchRunnable = new ArrayList<>();
    }

    public void getUserAvatar(String userId, AvatarType type, int threadNumber,
                              OnAvatarAddListener onAvatarAddListener, Runnable onSearchFinished) {
        mCurrentResourcePool = new AvatarResourcePool(userId, type);
        mFailList.clear();
        mAllSearchRunnable.clear();
        mOnAvatarAddListener = onAvatarAddListener;
        mOnSearchFinished = onSearchFinished;
        for (int i = 0; i < threadNumber; i++) {
            if (mOnSearchFinished == null) {
                //可能太快了
                return;
            }
            AvatarSearchRunnable searchRunnable = getSearchRunnable();
            searchRunnable.init(mCurrentResourcePool);
            mAllSearchRunnable.add(searchRunnable);
            executeAsyncTask(searchRunnable);
        }

    }

    private synchronized void searchFail(int mFailNumber) {
        int index = mFailList.size();
        //先按照顺序排好
        for (int i = 0; i < mFailList.size(); i++) {
            if (mFailList.get(i) > mFailNumber) {
                index = i;
                break;
            }
        }
        //先放进去
        mFailList.add(index, mFailNumber);
        int result = checkHasThreeSuccessiveNumber();
        if (result == -1) {
            return;
        }
        mCurrentResourcePool.setNextNumber(100);
        mAllSearchRunnable.stream()
                .filter(avatarSearchRunnable -> avatarSearchRunnable.getCurrentNumber() > result
                        && !avatarSearchRunnable.hasFinish())
                .forEach(AvatarSearchRunnable::finish);
    }

    private void searchSuccess(Avatar avatar) {
        this.mOnAvatarAddListener.onAvatarAdded(avatar);
    }

    private int checkHasThreeSuccessiveNumber() {
        //接下来寻找是否有三个连着的数字
        if (mFailList.size() < 3) {
            return -1;
        }
        for (int i = 1; i < mFailList.size() - 1; i++) {
            int current = mFailList.get(i);
            int pre = mFailList.get(i - 1);
            int next = mFailList.get(i + 1);
            if (current + 1 == next && current - 1 == pre) {
                return pre;
            }
        }
        return -1;
    }

    private void searchFinish() {
        mNowRunningRunnableNum--;
        if (mNowRunningRunnableNum != 0) {
            return;
        }
        if (mOnSearchFinished == null) {
            return;
        }
        mOnSearchFinished.run();
        mOnSearchFinished = null;
    }

    private synchronized void searchStart() {
        mNowRunningRunnableNum++;
    }

    public void runFinish(AvatarSearchRunnable avatarSearchRunnable) {
        mSearchRunnablePool.push(avatarSearchRunnable);
    }

    private AvatarSearchRunnable getSearchRunnable() {
        if (mSearchRunnablePool.empty()) {
            mSearchRunnablePool.push(new AvatarSearchRunnable());
        }
        return mSearchRunnablePool.pop();
    }


    private Avatar checkAnAvatar(String userId, AvatarType type, int num) {
        for (int i = 0; i < 3; i++) {
            Request request = new Request
                    .Builder()
                    .url(AvatarUtils.getAvatarUrl(type, userId, num))
                    .build();
            try {
                Response response = getOkHttpClient().newCall(request).execute();
                int code = response.code();
                if (code != 200) {
                    response.close();
                    return null;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    continue;
                }
                InputStream inputStream = body.byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return Avatar.builder()
                        .avatar(bitmap)
                        .userId(userId)
                        .number(num)
                        .type(type)
                        .build();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
        return null;
    }

    private void executeAsyncTask(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

    private static class AvatarResourcePool {
        private final String mUserId;
        private final AvatarType mAvatarType;
        private int mNextNumber;

        public AvatarResourcePool(String userId, AvatarType avatarType) {
            this.mUserId = userId;
            this.mNextNumber = 1;
            this.mAvatarType = avatarType;
        }

        public synchronized int getNextNumber() {
            if (mNextNumber > 99) {
                return -1;
            }
            return mNextNumber++;
        }

        public synchronized void setNextNumber(int nextNumber) {
            this.mNextNumber = nextNumber;
        }
    }

    private class AvatarSearchRunnable implements Runnable {

        private boolean mHasFinish;
        private int mCurrentNumber;
        private AvatarResourcePool mAvatarResourcePool;

        private void init(AvatarResourcePool avatarResourcePool) {
            mHasFinish = false;
            mAvatarResourcePool = avatarResourcePool;
        }

        public synchronized int getCurrentNumber() {
            return mCurrentNumber;
        }

        public synchronized void finish() {
            if (mHasFinish) {
                return;
            }
            mHasFinish = true;
            searchFinish();
        }

        public synchronized boolean hasFinish() {
            return mHasFinish;
        }

        @Override
        public void run() {
            searchStart();
            while ((mCurrentNumber = mAvatarResourcePool.getNextNumber()) != -1) {
                Avatar avatar = checkAnAvatar(mAvatarResourcePool.mUserId,
                        mAvatarResourcePool.mAvatarType, mCurrentNumber);
                if (hasFinish()) {
                    break;
                }
                if (avatar == null) {
                    searchFail(mCurrentNumber);
                    continue;
                }
                searchSuccess(avatar);
            }
            finish();
            runFinish(this);
        }
    }
}
