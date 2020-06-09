package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity<val> extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private EditText newPostText;
    private Button newPostBtn;
    private Uri postImageUri = null;
    private ProgressBar newPostProgress;
    private Button categoryBtn;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;
    private Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Write something here");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        categoryBtn = findViewById(R.id.category_btn_post);
        newPostImage = findViewById(R.id.new_post_image);
        newPostText = findViewById(R.id.new_post_text);
        newPostBtn = findViewById(R.id.post_btn);
        newPostProgress = findViewById(R.id.new_post_progress);
        newPostProgress.setVisibility(View.INVISIBLE);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);

            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc = newPostText.getText().toString();
                if(postImageUri==null){
                    Toast.makeText(NewPostActivity.this, "Please add a suitable picture to your post!", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(desc)){
                    Toast.makeText(NewPostActivity.this, "Please add your text before posting!", Toast.LENGTH_LONG).show();
                } else if(!TextUtils.isEmpty(desc) && postImageUri!=null){

                    newPostProgress.setVisibility(View.VISIBLE);
                    final String randomName = UUID.randomUUID().toString();
                    final StorageReference filePath = storageReference.child("post_images").child(randomName);



                    filePath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                File newImageFile = new File(postImageUri.getPath());
                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(3)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                            @Override
                                            public void onSuccess(Uri uri) {
                                                final String downloadUri = uri.toString();
                                                final StorageReference thumbPath = storageReference.child("post_images/thumbs").child(randomName + ".jpg");
                                                thumbPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        final String downThumbUri = uri.toString();
                                                        Map<String, Object> postMap = new HashMap<>();
                                                        if(categoryBtn.getText().equals("Choose text genre")){
                                                            postMap.put("category", "Free creation");
                                                        } else {
                                                            postMap.put("category", categoryBtn.getText());
                                                        }
                                                        postMap.put("currentNoOfLikes",0);
                                                        postMap.put("image_url", downloadUri);
                                                        postMap.put("thumb_url", downThumbUri); //might want to check this!!
                                                        postMap.put("text", desc);
                                                        postMap.put("user_id", current_user_id);
                                                        postMap.put("timestamp", FieldValue.serverTimestamp());
                                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if(task.isSuccessful()){

                                                                    Toast.makeText(NewPostActivity.this, "Post was added!", Toast.LENGTH_LONG).show();
                                                                    Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                                    startActivity(mainIntent);
                                                                    finish();

                                                                } else {
                                                                    String errorMessage = task.getException().getMessage();
                                                                    Toast.makeText(NewPostActivity.this, "Error:" + errorMessage, Toast.LENGTH_LONG).show();
                                                                }
                                                                newPostProgress.setVisibility(View.INVISIBLE);
                                                            }
                                                        });
                                                    }
                                                });

                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String errorMessage = e.getMessage();
                                        Toast.makeText(NewPostActivity.this, "Error:" + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });



                            } else {
                                newPostProgress.setVisibility(View.INVISIBLE);
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error:" + errorMessage, Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_menu_post);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                categoryBtn.setText("Free creation");
                return true;
            case R.id.item2:
                categoryBtn.setText("Poetry");
                return true;
            case R.id.item3:
                categoryBtn.setText("Horror");
                return true;
            case R.id.item4:
                categoryBtn.setText("Sci-Fi");
                return true;
            case R.id.item5:
                categoryBtn.setText("QRea");
                return true;
            default:
                categoryBtn.setText("Free creation");
                return false;
        }
    }

}
