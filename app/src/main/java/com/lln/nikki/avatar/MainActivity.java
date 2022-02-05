package com.lln.nikki.avatar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lln.nikki.avatar.model.Avatar;
import com.lln.nikki.avatar.model.AvatarType;
import com.lln.nikki.avatar.network.AppService;
import com.lln.nikki.avatar.network.AvatarService;
import com.lln.nikki.avatar.network.impl.AppServiceImpl;
import com.lln.nikki.avatar.network.impl.AvatarServiceImpl;
import com.lln.nikki.avatar.util.CommonUtils;
import com.lln.nikki.avatar.view.AvatarShowAdapter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText mUserIdInput;
    private Spinner mServerSelect;
    private Button mSearchButton;
    private Executor mExecutorService;
    private AvatarService mAvatarService;
    private AppService mAppService;
    private AvatarShowAdapter mAvatarShowAdapter;
    private long mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mExecutorService = Executors.newSingleThreadExecutor();
        mAvatarShowAdapter = new AvatarShowAdapter(this);
        mAvatarService = new AvatarServiceImpl();
        mAppService = new AppServiceImpl();
        mUserIdInput = findViewById(R.id.user_id_input);
        mServerSelect = findViewById(R.id.server_select);
        mSearchButton = findViewById(R.id.search_button);
        RecyclerView mRecyclerView = findViewById(R.id.avatar_search_result_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAvatarShowAdapter);
        mSearchButton.setOnClickListener(this::startSearch);
        checkUpdate(false);
        showFirst();
    }

    private void showFirst() {
        SharedPreferences preferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        if (preferences.getBoolean("first_use", false)) {
            return;
        }
        showAbout();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("first_use", true);
        editor.apply();
    }

    private void startSearch(View view) {
        if (!mSearchButton.isEnabled()) {
            return;
        }
        String userId = mUserIdInput.getText().toString();
        if (!CommonUtils.checkUserIdIsValid(userId)) {
            toast("id不合法");
            return;
        }
        AvatarType selectedType = getSelectedType();
        realStartSearch(userId, selectedType);
    }

    private void realStartSearch(String userId, AvatarType type) {
        enableSearchButton(false);
        mStartTime = System.currentTimeMillis();
        mAvatarShowAdapter.clear();
        mAppService.checkUsable((usableMessage) -> {
            if (usableMessage == null) {
                runOnUiThread(() -> {
                    toast("权限校验失败，请重试~");
                    enableSearchButton(true);
                });
                return;
            }
            if (!usableMessage.isUsable()) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage(usableMessage.getReason())
                        .setPositiveButton("再见", (dialog, which) -> System.exit(0))
                        .setCancelable(false)
                        .show());
                return;
            }
            mExecutorService.execute(() -> mAvatarService.getUserAvatar(userId, type,
                    32, this::onAvatarAdded, this::finishSearch));
        });
    }

    private void finishSearch() {
        runOnUiThread(() -> {
            String time = "，耗时 " + (System.currentTimeMillis() - mStartTime) + "ms";
            enableSearchButton(true);
            if (mAvatarShowAdapter.getItemCount() == 0) {
                toast("未查询到任何内容" + time);
                return;
            }
            toast("查询完成" + time);
        });
    }

    private void onAvatarAdded(Avatar avatar) {
        runOnUiThread(() -> mAvatarShowAdapter.addData(avatar));
    }

    private void enableSearchButton(boolean enable) {
        mSearchButton.setEnabled(enable);
        mSearchButton.setText(enable ? R.string.search : R.string.searching);
    }

    private void toast(Object message) {
        CommonUtils.toast(this, message);
    }

    private AvatarType getSelectedType() {
        switch (mServerSelect.getSelectedItemPosition()) {
            case 0:
                return AvatarType.MAINLAND;
            case 1:
                return AvatarType.TAIWAN;
            case 2:
                return AvatarType.JAPAN;
            default:
                return AvatarType.UNKNOWN;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.about) {
            showAbout();
        } else if (itemId == R.id.update) {
            checkUpdate(true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle("关于软件")
                .setMessage("本软件用于查询闪耀暖暖历史头像，软件仅需要联网，不会需要其他权限，" +
                        "也不会收集任何隐私数据，如有问题，欢迎加入QQ群：906730189进行交流反馈。")
                .setPositiveButton("确定", null)
                .setNegativeButton("加入Q群", (dialog, which) -> CommonUtils.joinQQGroup(this))
                .show();
    }

    private void checkUpdate(boolean showToast) {
        showUpdateToast(showToast, "检查更新中...");
        mAppService.checkUpdate(updateMessage -> runOnUiThread(() -> {
            if (updateMessage == null) {
                showUpdateToast(showToast, "检查失败");
                return;
            }
            if (updateMessage.getVersionCode() <= ApplicationManager.getVersionCode()) {
                showUpdateToast(showToast, "暂无更新");
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("请更新后使用")
                    .setPositiveButton("退出", (dialog, which) -> System.exit(0))
                    .setCancelable(false)
                    .show();
            new AlertDialog.Builder(this)
                    .setTitle("新版本(" + updateMessage.getVersionName() + ")")
                    .setMessage(updateMessage.getMessage())
                    .setPositiveButton("立即更新", (dialog, which) -> {
                        CommonUtils.openLink(this, updateMessage.getUrl());
                    })
                    .setCancelable(false)
                    .show();
        }));
    }

    private void showUpdateToast(boolean showToast, String message) {
        if (!showToast) {
            return;
        }
        toast(message);
    }


}