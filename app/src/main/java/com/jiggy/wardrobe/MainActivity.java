package com.jiggy.wardrobe;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jiggy.wardrobe.adapters.MyAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int KEY_PERMISSION = 1, KEY_CAM_PER_FOR_ADD_SHIRT = 2, KEY_CAM_PER_FOR_ADD_PANT = 3;
    private static final String TAG = "MainActivity";
    private static final int CAPTURE_IMAGE_ACTIVITY = 123;
    FloatingActionButton fabAddShirt, fabLike, fabRandom, fabAddPaint;
    ArrayList<String> shirtList = new ArrayList<>();
    ArrayList<String> pantList = new ArrayList<>();
    String mCurrentPhotoPath;
    ViewPager2 viewPagerShirt, viewPagerPant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fabAddShirt = findViewById(R.id.fab_add_shirts);
        fabLike = findViewById(R.id.fab_like);
        fabRandom = findViewById(R.id.fab_change);
        fabAddPaint = findViewById(R.id.fab_add_pants);
        viewPagerShirt = findViewById(R.id.view_pager_shirt);
        viewPagerPant = findViewById(R.id.view_pager_pant);

        fabAddShirt.setOnClickListener(this);
        fabLike.setOnClickListener(this);
        fabRandom.setOnClickListener(this);
        fabAddPaint.setOnClickListener(this);
        shirtList.add("0");
        pantList.add("0");
        setViewPagerAdapter();
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            askForPermission(KEY_PERMISSION);
        }


    }

    private void askForPermission(int code) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, code);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, code);
            }
        } else {
            Toast.makeText(this, "Permission is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);

        switch (requestCode) {
            case KEY_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "onRequestPermissionsResult: request");

                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                }
                break;
            case KEY_CAM_PER_FOR_ADD_SHIRT:
            case KEY_CAM_PER_FOR_ADD_PANT:
                callCam(requestCode);
                break;

        }
    }

    public void callCam(int type) {
       /* Intent camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uriSavedImage = null;
        try {
            uriSavedImage = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
                    createImageFile(type));

            camera.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
            camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(camera, CAPTURE_IMAGE_ACTIVITY);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        startActivityForResult(getPickImageChooserIntent(type), type);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fab_add_shirts:
                openCam(KEY_CAM_PER_FOR_ADD_SHIRT);
                break;
            case R.id.fab_like:
                break;
            case R.id.fab_change:
                doShuffle();
                break;
            case R.id.fab_add_pants:
                openCam(KEY_CAM_PER_FOR_ADD_PANT);
                break;
        }
    }
    Uri outputFileUri = null;
    public Intent getPickImageChooserIntent(int type) {

        // Determine Uri of camera image to save.

        try {
            outputFileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",
                createImageFile(type));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent mainIntent =   allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }



    public void doShuffle() {
        Random rand = new Random();
        int imgRandomShirt = rand.nextInt((shirtList.size()-1) + 1);
        int imgRandomPant = rand.nextInt((pantList.size()-1) + 1);
        Log.i(TAG, "doShuffle: shirt:- " + imgRandomShirt + " Pant:- " + imgRandomPant);
        viewPagerShirt.setCurrentItem(imgRandomShirt,true);
        viewPagerPant.setCurrentItem(imgRandomPant,true);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        System.gc();
        if (requestCode == CAPTURE_IMAGE_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                   /* Uri imageUri = Uri.parse(mCurrentPhotoPath);
                    File file = new File(imageUri.getPath());
                    try {
                        InputStream ims = new FileInputStream(file);
//                        ivPreview.setImageBitmap(BitmapFactory.decodeStream(ims));
                    } catch (FileNotFoundException e) {
                        return;
                    }*/
                    setViewPagerAdapter();
                } catch (Exception e) {

                }
            }

        } else if (requestCode == KEY_CAM_PER_FOR_ADD_SHIRT||requestCode == KEY_CAM_PER_FOR_ADD_PANT) {
            Bitmap bitmap;
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri picUri = getPickImageResultUri(data);
                    File file = new File(picUri.getPath());
                    if (requestCode == KEY_CAM_PER_FOR_ADD_PANT) {
                        pantList.remove(pantList.size() - 1);
                        pantList.add("file:" + file.getAbsolutePath());
                    } else {
                        shirtList.remove(shirtList.size() - 1);
                        shirtList.add(""+picUri);
                    }
                }
                setViewPagerAdapter();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }


        return isCamera ? outputFileUri : data.getData();
    }
    public void openCam(int type) {

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            askForPermission(type);
        } else {
            callCam(type);
        }
    }

    private File createImageFile(int type) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();

        switch (type) {
            case KEY_CAM_PER_FOR_ADD_SHIRT:
                shirtList.add(mCurrentPhotoPath);
                break;
            case KEY_CAM_PER_FOR_ADD_PANT:
                pantList.add(mCurrentPhotoPath);
                break;

        }


        return image;
    }

    public void setViewPagerAdapter() {

        viewPagerPant.setAdapter(new MyAdapter(this, pantList, "p"));
        viewPagerShirt.setAdapter(new MyAdapter(this, shirtList, "s"));

        viewPagerShirt.setCurrentItem(shirtList.size() - 1);
        viewPagerPant.setCurrentItem(pantList.size() - 1);

    }


}