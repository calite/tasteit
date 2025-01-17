package com.example.tasteit_java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tasteit_java.ApiRequest.UserDeleteRequest;
import com.example.tasteit_java.ApiService.ApiClient;
import com.example.tasteit_java.ApiGetters.UserLoader;
import com.example.tasteit_java.clases.SharedPreferencesSaved;
import com.example.tasteit_java.clases.User;
import com.example.tasteit_java.clases.Utils;
import com.example.tasteit_java.ApiRequest.UserEditRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityEditProfile extends AppCompatActivity {
    private ImageButton ibPickPhoto;
    private ImageView ivProfilePhoto;
    private TextView tvUsername, tvConfirmPassword, optChangePass;
    private EditText etUsername, etEmail, etNewPassword, etConfirmPassword, etBiography, etOldPassword;
    private Button btnSave, btnDeleteAcc;
    private String accessToken;
    private String uid;
    private User user;
    private Uri newFilePath;
    private Uri lastFileUrl;
    private ShimmerFrameLayout shimmer;
    private ConstraintLayout constraintLayout, clPassword;

    private ConstraintSet constraintSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        shimmer = findViewById(R.id.shimmer);
        shimmer.startShimmer();

        accessToken = new SharedPreferencesSaved(this).getSharedPreferences().getString("accessToken", "null");
        uid = new SharedPreferencesSaved(this).getSharedPreferences().getString("uid", "null");

        initializeViews();
        bringUser();

        //Menu superior
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_profile);
    }

    //Metodo para instanciar los elementos de la UI
    private void initializeViews() {
        ibPickPhoto = findViewById(R.id.ibPickPhoto);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);

        tvUsername = findViewById(R.id.tvUsername);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etOldPassword = findViewById(R.id.etOldPassword);
        etBiography = findViewById(R.id.etBiography);
        tvConfirmPassword = findViewById(R.id.tvConfirmPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);
        btnDeleteAcc = findViewById(R.id.btnDeleteAcc);
        optChangePass = findViewById(R.id.optChangePass);
        clPassword = findViewById(R.id.clPassword);

        optChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clPassword.getVisibility() != View.VISIBLE) {
                    clPassword.setVisibility(View.VISIBLE);
                } else {
                    clPassword.setVisibility(View.GONE);
                }
            }
        });

        //Cambiar foto de perfil
        PopupMenu.OnMenuItemClickListener popupListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.iCamera: {
                        Utils.takePicture(ActivityEditProfile.this);
                        break;
                    }
                    case R.id.iGallery: {
                        Utils.selectImageFromMedia(ActivityEditProfile.this);
                        break;
                    }
                }
                return false;
            }
        };

        ibPickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(ActivityEditProfile.this, view);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.change_image_from, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(popupListener);
                popupMenu.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = etOldPassword.getText().toString();
                String newPassword = etNewPassword.getText().toString();
                String ConfPass = etConfirmPassword.getText().toString();

                if (newPassword.length() >= 6 && newPassword.equals(ConfPass) && oldPassword.length() > 0 && newFilePath != null) {
                    changePassword(oldPassword, newPassword);
                    uploadImage(newFilePath);
                } else if (newPassword.length() >= 6 && newPassword.equals(ConfPass) && oldPassword.length() > 0 && newFilePath == null) {
                    changePassword(oldPassword, newPassword);
                    saveDataUser(null);
                } else if (oldPassword.length() == 0 && newFilePath != null) {
                    uploadImage(newFilePath);
                } else if (oldPassword.length() == 0 && newFilePath == null) {
                    saveDataUser(null);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.pass_match_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDeleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserDeleteRequest editer = new UserDeleteRequest(uid);
                String urlImage = new SharedPreferencesSaved(getApplicationContext()).getSharedPreferences().getString("urlImgProfile", "null");

                ApiClient apiClient = ApiClient.getInstance(accessToken);
                Call<Void> call = apiClient.getService().deleteUser(editer);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().getStorage().getReferenceFromUrl(urlImage);
                                        storageReference.delete();
                                    }
                                }
                            });
                            Toast.makeText(ActivityEditProfile.this, R.string.info_deleted, Toast.LENGTH_SHORT).show();
                        } else {
                            // Handle the error
                            Log.e("API_ERROR", "Response error: " + response.code() + " " + response.message());
                            Toast.makeText(ActivityEditProfile.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Handle the error
                        Toast.makeText(ActivityEditProfile.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                    }
                });
                new SharedPreferencesSaved(getApplicationContext()).getEditer().clear().commit();
                startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                finish();
            }
        });
    }

    private void changePassword(String oldPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(ActivityEditProfile.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ActivityEditProfile.this, R.string.data_saved_succ, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(ActivityEditProfile.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Metodo para traer los datos del perfil
    private void retrieveData() {
        tvUsername.setText(user.getUsername());
        etUsername.setText(user.getUsername());

        etBiography.setText(user.getBiography());
        etEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        try {
            Picasso.with(this)
                    .load(user.getImgProfile())
                    .into(ivProfilePhoto);
        } catch (IllegalArgumentException e) {
            Log.e("Image Error", "Error loading profile image");
        }

        lastFileUrl = Uri.parse(user.getImgProfile());
        newFilePath = null;

        shimmer.stopShimmer();
        shimmer.hideShimmer();
    }
    //END Recogida de datos

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == -1) {
            if (data.getData() != null) {
                newFilePath = data.getData();
                Utils.onActivityResult(this, requestCode, resultCode, data, ivProfilePhoto);
            }
        } else if(requestCode == 202 && resultCode == -1) {
            if(data.getExtras().get("data") != null ){
                Bitmap photo = (Bitmap) data.getExtras().get("data");

                File f = new File(getCacheDir(), UUID.randomUUID().toString());
                try {
                    f.createNewFile();
                }catch(Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                //Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                try{
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                }catch(Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(f.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                switch(orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        photo = Utils.rotateImage(photo, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        photo = Utils.rotateImage(photo, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        photo = Utils.rotateImage(photo, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:

                    default:
                        break;
                }

                newFilePath = Uri.fromFile(f);
                ivProfilePhoto.setImageBitmap(photo);
            }
        }
    }
    //END PHOTO PICKER

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.iCloseSesion:
                signOut();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //END MENU superior

    //LOGOUT
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, ActivityLogin.class));
    }

    private Uri uploadImage(Uri filePath) {
        if (filePath != null) {
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
            UploadTask uploadTask = ref.putFile(filePath);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        saveDataUser(downloadUri);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
        return null;
    }

    //Carga de usuario asyncrona
    private void bringUser() {
        UserLoader userCountersLoader = new UserLoader(ApiClient.getInstance(accessToken).getService(), this, uid);
        userCountersLoader.getAllUser().observe(this, this::onUserLoaded);
        userCountersLoader.loadAllUser();
    }

    private void onUserLoaded(HashMap<String, Object> counters) {
        this.user = (User) counters.get("user");
        retrieveData();
    }

    public void saveDataUser(Uri imgUrl) {
        ApiClient apiClient = ApiClient.getInstance(accessToken);

        String urlImage = (imgUrl != null ? imgUrl.toString() : lastFileUrl.toString());
        String username = etUsername.getText().toString();
        String bio = etBiography.getText().toString();

        UserEditRequest editer = new UserEditRequest(Utils.getUserToken(), username, urlImage, bio);
        SharedPreferencesSaved sharedPreferences = new SharedPreferencesSaved(this);

        Call<Void> call = apiClient.getService().editUser(editer);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (newFilePath != null && lastFileUrl != null) {
                        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().getStorage().getReferenceFromUrl(user.getImgProfile());
                        storageReference.delete();

                        if(!sharedPreferences.getSharedPreferences().contains("urlImgProfile") || !sharedPreferences.getSharedPreferences().getString("urlImgProfile", "null").equals(urlImage)) {
                            SharedPreferences.Editor editor = sharedPreferences.getEditer();
                            editor.putString("urlImgProfile", urlImage);
                            editor.commit();
                        }
                    }
                    finish();
                } else {
                    // Handle the error
                    Log.e("API_ERROR", "Response error: " + response.code() + " " + response.message());
                    Toast.makeText(ActivityEditProfile.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle the error
                Toast.makeText(ActivityEditProfile.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
            }
        });

    }

}