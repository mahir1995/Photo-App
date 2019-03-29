package com.mahir.photoapp;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final int PICK_GALLERY_IMAGE = 0;

    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Give read and write permission
        if (Build.VERSION.SDK_INT > 22)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // Create new folder MyImages if it doesn't exist
        file = new File(Environment.getExternalStorageDirectory() + "/" + "MyImages");
        if (!file.exists())
        {
            boolean success = file.mkdir();
            if (success)
            {
                Log.d("MainActivity", "Created My Images Folder");
            }
            else
            {
                Log.d("MainActivity", "Failed to create My Images Folder");
            }
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                uploadImage();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        ArrayList<Bitmap> bitmaps = decrypt();
        GridView gridView = findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapterGridView(this, R.layout.grid_layout, bitmaps));
    }

    private void uploadImage()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_GALLERY_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (requestCode == PICK_GALLERY_IMAGE)
            {
                upload(data.getData());
            }
        }
    }

    private void upload(Uri photoUri)
    {
        String path = getPath(photoUri);                                    // Get path of image
        File sourceFile = new File(path);
        String filename = path.substring(path.lastIndexOf("/") + 1);    // Get image filename

        Bitmap bitmap = compressImage(sourceFile);
        Encryption.saveFile(bitmap, filename);
    }

    private String getPath(Uri uri)
    {
        String res = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor.moveToFirst())
        {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private Bitmap compressImage(File f)
    {
        Bitmap b = null;

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis;
        try
        {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE)
        {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try
        {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return b;
    }

    private ArrayList<Bitmap> decrypt()
    {
        String path = Environment.getExternalStorageDirectory() + "/MyImages";
        File dir = new File(path);
        File[] files = dir.listFiles();

        Bitmap bitmap;
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        for (int i = 0; i < files.length; i++)
        {
            bitmap = Encryption.decodeFile(files[i].getName());
            bitmaps.add(bitmap);
        }
        return bitmaps;
    }
}
