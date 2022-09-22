package com.krishna.jaya.imageeditor;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final int PIC_CROP =2 ;
    private ImageView imageView;
    private Button selectImage, saveImage, flipy_btn, flipx_btn, crop_btn;
    ActivityResultLauncher<Intent> imageResultLauncher;
    Uri imageUri;
    int count = 0;
    Bitmap selectedBitmap;
    FileDescriptor fileDescriptor;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        selectImage = findViewById(R.id.selectImage);
        saveImage = findViewById(R.id.saveImage);
        flipy_btn = findViewById(R.id.flipY);
        flipx_btn = findViewById(R.id.flipX);
        crop_btn = findViewById(R.id.crop);



        registerImageResultLauncher();
        selectImage.setOnClickListener(view -> new AlertDialog.Builder(MainActivity.this)
                .setMessage("Do you want to open the image")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    checkStoragePermissionAndGetImage();
                })
                .setNegativeButton("No", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })).create().show());
        saveImage.setOnClickListener(view -> new AlertDialog.Builder(MainActivity.this)
                .setMessage("Do you want to Save the image")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    saveImageToGallery();
                })
                .setNegativeButton("No", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })).create().show());
        flipy_btn.setOnClickListener(view -> flipOnY_axis());
        flipx_btn.setOnClickListener(view -> flipOnX_axis());
        imageView.setOnClickListener(view -> showExif(imageUri));
        crop_btn.setOnClickListener(view->performCrop(imageUri));

    }



    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_CROP) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                selectedBitmap = extras.getParcelable("data");

                imageView.setImageBitmap(selectedBitmap);
            }
        }
    }

    private void flipOnY_axis() {
        if (count == 0) {
            imageView.setScaleY(-1);
            count = 1;
        } else if (count == 1) {
            imageView.setScaleY(1);
            count = 0;
        }
    }

    private void flipOnX_axis() {
        if (count == 0) {
            imageView.setScaleX(-1);
            count = 1;
        } else if (count == 1) {
            imageView.setScaleX(1);
            count = 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showExif(Uri imageUri) {
        if (imageUri != null) {

            ParcelFileDescriptor parcelFileDescriptor;

            /*
            How to convert the Uri to FileDescriptor, refer to the example in the document:
            https://developer.android.com/guide/topics/providers/document-provider.html
             */
            try {
                parcelFileDescriptor = getContentResolver().openFileDescriptor(imageUri, "r");
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                /*
                ExifInterface (FileDescriptor fileDescriptor) added in API level 24
                 */
                ExifInterface exifInterface = new ExifInterface(fileDescriptor);
                String exif = "Exif: " + fileDescriptor.toString();
                exif += "\nIMAGE_LENGTH: " +
                        exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                exif += "\nIMAGE_WIDTH: " +
                        exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                exif += "\n DATETIME: " +
                        exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                exif += "\n TAG_MAKE: " +
                        exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                exif += "\n TAG_MODEL: " +
                        exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                exif += "\n TAG_ORIENTATION: " +
                        exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
                exif += "\n TAG_WHITE_BALANCE: " +
                        exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
                exif += "\n TAG_FOCAL_LENGTH: " +
                        exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                exif += "\n TAG_FLASH: " +
                        exifInterface.getAttribute(ExifInterface.TAG_FLASH);
                exif += "\nGPS related:";
                exif += "\n TAG_GPS_DATESTAMP: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
                exif += "\n TAG_GPS_TIMESTAMP: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
                exif += "\n TAG_GPS_LATITUDE: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                exif += "\n TAG_GPS_LATITUDE_REF: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                exif += "\n TAG_GPS_LONGITUDE: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                exif += "\n TAG_GPS_LONGITUDE_REF: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                exif += "\n TAG_GPS_PROCESSING_METHOD: " +
                        exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

                parcelFileDescriptor.close();

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(exif)
                        .setCancelable(true)
                        .show();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            }

            String strPhotoPath = imageUri.getPath();

        } else {
            Toast.makeText(getApplicationContext(),
                    "photoUri == null",
                    Toast.LENGTH_LONG).show();
        }
    }


    private void saveImageToGallery() {
//
//        BitmapDrawable bitmapDrawable = (BitmapDrawable)imageView.getDrawable ( ) ;
//        Bitmap bitmap = bitmapDrawable.getBitmap ( ) ;

//        Bitmap bitmap= null;
//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try {
            selectedBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileOutputStream outputStream = null;

        File dir = new File(Environment.getDataDirectory(), "SavedImages");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File outFile = new File(dir, System.currentTimeMillis() + ".jpg");

        try {
            outputStream = new FileOutputStream(outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (selectedBitmap != null) {
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        }

        Toast.makeText(this, "Image saved successfully in " + outFile, Toast.LENGTH_LONG).show();
        try {
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void registerImageResultLauncher() {
        imageResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            imageUri = result.getData().getData();
                            imageView.setImageURI(imageUri);

                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                });
    }

    private void checkStoragePermissionAndGetImage() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

        } else {
            pickImageFromGallery();
        }

    }

    private void pickImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        imageResultLauncher.launch(galleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}