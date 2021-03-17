package com.jiggy.wardrobe.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jiggy.wardrobe.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyShirtViewHolder> {
    Context context;
    ArrayList<String> arrayList;
    String type;

    public MyAdapter(Context context, ArrayList<String> arrayList, String type) {
        this.context = context;
        this.arrayList = arrayList;
        this.type = type;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @NonNull
    @Override
    public MyShirtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shirt, parent, false);
        return new MyShirtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyShirtViewHolder holder, int position) {

        String path = arrayList.get(position);

        try {
            if ("0".equals(path)) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.shirt);
                if ("s".equalsIgnoreCase(type)) {
                    drawable = ContextCompat.getDrawable(context, R.drawable.shirt);
                } else {
                    drawable = ContextCompat.getDrawable(context, R.drawable.pant);
                }

                holder.imageView.setImageDrawable(drawable);


            } else {
                Uri imageUri = Uri.parse(path);

                if (path.contains("content:")) {
                    try {
                        // check version of Android on device
                        if (Build.VERSION.SDK_INT > 27) {
                            // on newer versions of Android, use the new decodeBitmap method
                            ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), imageUri);
                            holder.imageView.setImageBitmap(ImageDecoder.decodeBitmap(source));
                        } else {
                            // support older versions of Android by using getBitmap
                            holder.imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    File file = new File(imageUri.getPath());
                    InputStream ims = new FileInputStream(file);
                    ExifInterface ei = null;
                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
                    Bitmap rotatedBitmap = bitmap;

                    rotatedBitmap = rotateImage(bitmap, 90);
                    holder.imageView.setImageBitmap(rotatedBitmap);
                }




            }


        } catch (FileNotFoundException e) {
            return;
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyShirtViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MyShirtViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_view);
        }
    }

}

