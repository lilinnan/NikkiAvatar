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
            toast("id?????????");
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
                    toast("??????????????????????????????~");
                    enableSearchButton(true);
                });
                return;
            }
            if (!usableMessage.isUsable()) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("??????")
                        .setMessage(usableMessage.getReason())
                        .setPositiveButton("??????", (dialog, which) -> System.exit(0))
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
            String time = "????????? " + (System.currentTimeMillis() - mStartTime) + "ms";
            enableSearchButton(true);
            if (mAvatarShowAdapter.getItemCount() == 0) {
                toast("????????????????????????" + time);
                return;
            }
            toast("????????????" + time);
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
                .setTitle("????????????")
                .setMessage("???????????????????????????????????????????????????????????????????????????????????????????????????" +
                        "???????????????????????????????????????????????????????????????QQ??????906730189?????????????????????")
                .setPositiveButton("??????", null)
                .setNegativeButton("??????Q???", (dialog, which) -> CommonUtils.joinQQGroup(this))
                .show();
    }

    private void checkUpdate(boolean showToast) {
        showUpdateToast(showToast, "???????????????...");
        mAppService.checkUpdate(updateMessage -> runOnUiThread(() -> {
            if (updateMessage == null) {
                showUpdateToast(showToast, "????????????");
                return;
            }
            if (updateMessage.getVersionCode() <= ApplicationManager.getVersionCode()) {
                showUpdateToast(showToast, "????????????");
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("??????????????????")
                    .setPositiveButton("??????", (dialog, which) -> System.exit(0))
                    .setCancelable(false)
                    .show();
            new AlertDialog.Builder(this)
                    .setTitle("?????????(" + updateMessage.getVersionName() + ")")
                    .setMessage(updateMessage.getMessage())
                    .setPositiveButton("????????????", (dialog, which) -> {
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