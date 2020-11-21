package com.pla_bear.board.challenge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pla_bear.R;
import com.pla_bear.board.base.ImageUploadWriteActivity;

import java.io.File;
import java.util.Objects;

public class ChallengeSubmitActivity extends ImageUploadWriteActivity {
    private static final int MAX_IMAGE_COUNT = 1;
    private final int MODIFY_CONTENT = 1000;
    protected ImageButton imageButton;
    protected TextView contentView;
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_submit);

        TextView textView = findViewById(R.id.write_name_textView);
        textView.setText(firebaseUser.getDisplayName() + " 님");

        contentView = findViewById(R.id.write_content_textView);
        imageButton = findViewById(R.id.write_image_imageView);
        imageButton.setOnClickListener(view -> {
            if(localImageUri.size() < MAX_IMAGE_COUNT) {
                localSave();
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.warning)
                        .setMessage(R.string.review_max_exceed)
                        .setPositiveButton(R.string.ok, null)
                        .setIcon(R.drawable.warning_icon)
                        .create();
                alertDialog.show();
            }
        });

        Button button = findViewById(R.id.store_content); //저장버튼
        button.setOnClickListener(view -> onSubmit());

        button = findViewById(R.id.cancel_content);
        button.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("취소")
                    .setMessage("글 쓰기를 취소하시겠습니까?")
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        finish();
                    })
                    .create();
            alertDialog.show();
        });
    }

    // submit 버튼 클릭시 호출
    @Override
    public void onSubmit() {
        if(localImageUri.size() > 0) {
            Uri uri = localImageUri.get(0);
            uploadOnServer(getString(R.string.challenge_submit_database), uri.toString());
        } else {
            submit();
        }
    }

    private void submit() {
        String uid = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();

        String content = contentView.getText().toString();
        String imageUri;

        try {
            imageUri = serverImageUri.get(0).toString();
        } catch(IndexOutOfBoundsException e) {
            imageUri = null;
        }

        ChallengeBoardDTO challengeBoardDTO = new ChallengeBoardDTO(uid, name, content, imageUri);

        onSuccess(challengeBoardDTO);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.success)
                .setMessage(R.string.register_success_message)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    finish();
                })
                .create();
        alertDialog.show();
    }

    public void onSuccess(ChallengeBoardDTO challengeShareBoardDTO) {
        writeToDatabase(getString(R.string.challenge_submit_database), challengeShareBoardDTO);
    }

    // Firebase 상에 업로드 성공시 호출되도록 설계
    @Override
    public void onUploadComplete(Uri uri) {
        super.onUploadComplete(uri);
        submit();
    }

    // 카메라, 갤러리에서 이미지를 가져온 뒤 호출
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case EXTERNAL_CONTENT:
                if (resultCode == RESULT_OK && intent != null) {
                    int index = localImageUri.size() - 1;
                    File file = new File(Objects.requireNonNull(localImageUri.get(index).getPath()));
                    Glide.with(this)
                            .load(file)
                            .into(imageButton);
                }
                break;
        }
    }
}