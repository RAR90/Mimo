package br.com.mimoapp.mimo.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.com.mimoapp.mimo.R;
import br.com.mimoapp.mimo.helper.PermissionsHelper;
import br.com.mimoapp.mimo.model.User;
import br.com.mimoapp.mimo.persistence.UserPersist;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class UserImageUploadActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private Boolean isWorker;

    private ProgressBar progressBar;

    private static final String KEY_ISWORKER = "is_worker";

    private String[] appPermissions = new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //Variables
    private Button btnChoose;
    private FloatingActionButton btnUpload;
    private ImageView userImage;

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 10;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        // PermissionsHelper
        PermissionsHelper.validatePermissions(1, this, appPermissions);

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_image_upload);

        // Get progress bar then hide
        progressBar = findViewById( R.id.pb_user_image );
        progressBar.setVisibility( View.GONE );

        // Get bundle of recipient information
        Bundle extra = getIntent().getExtras();

        // Get isWorker Flag to handle the UI
        isWorker = false;
        if ( extra != null ) {
            isWorker = (Boolean) extra.get(KEY_ISWORKER);
        }

        if ( isWorker ) {
            progressBar.setVisibility( View.VISIBLE );
        }

        // Set default toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.page_name_my_image);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar( toolbar );

        // Get created toolbar
        ActionBar actionBar = getSupportActionBar();

        // Get firebase storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Initialize Views
        btnChoose = findViewById(R.id.bt_choose_image);
        userImage = findViewById(R.id.iv_user_image_upload);
        btnUpload = findViewById(R.id.fab_save);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        UserPersist userPersist = new UserPersist( getApplicationContext() );
        User user = userPersist.getUser();

        try {
            final File tmpFile = File.createTempFile("img", "png");
            StorageReference reference = FirebaseStorage.getInstance().getReference("images");

            //  "id" is name of the image file....

            reference.child(user.getId()).getFile(tmpFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap image = BitmapFactory.decodeFile(tmpFile.getAbsolutePath());
                    progressBar.setVisibility( View.GONE );
                    userImage.setImageBitmap(image);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
        .setGuidelines(CropImageView.Guidelines.ON)
        .setScaleType( CropImageView.ScaleType.CENTER )
        .setAspectRatio(1,1)
        .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                filePath = imageUri;
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    userImage.setScaleType( ImageView.ScaleType.FIT_CENTER );
                    userImage.setImageBitmap( bitmap );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadImage() {

        UserPersist userPersist = new UserPersist( getApplicationContext() );
        User user = userPersist.getUser();

        if(filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog( this );
            progressDialog.setTitle( "Uploading..." );
            progressDialog.show();

            StorageReference ref = storageReference.child( "images/"+ user.getId() );
            ref.putFile( filePath )
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UserImageUploadActivity.this, "Upload concluído", Toast.LENGTH_SHORT).show();
                            Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intentMain);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserImageUploadActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Progresso "+(int)progress+"%");
                        }
                    });
        } else {
            Toast.makeText(this, R.string.choose_image, Toast.LENGTH_LONG).show();
        }

    }

    public Uri getResizedFileUri(Context c, Uri uri, final int requiredSize) throws FileNotFoundException {

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream( c.getContentResolver().openInputStream(uri), null, o );

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while( true ) {
            if( width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize ){
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        Bitmap bmp = BitmapFactory.decodeStream( c.getContentResolver().openInputStream(uri), null, o2 );

        return getImageUri( c, bmp );
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    // [ start PermissionsHelper treatment ]

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                permissionsAlert();
            }
        }
    }

    private void permissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle(R.string.no_permission);
        builder.setMessage(R.string.no_permission_text);
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // [ end PermissionsHelper treatment ]
}
