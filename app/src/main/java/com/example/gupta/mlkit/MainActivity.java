package com.example.gupta.mlkit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_PERMISSION_CODE=100;
    private static final int CAMERA_REQUEST=1888;

    ImageView imgv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgv=findViewById(R.id.imgv);
    }

    public void Capture(View view){
//        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
//            requestPermissions(new String[]{Manifest.permission.CAMERA},MY_CAMERA_PERMISSION_CODE);
//        }
//        else {
            Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent,CAMERA_REQUEST);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==MY_CAMERA_PERMISSION_CODE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,CAMERA_REQUEST);
            }
            else Toast.makeText(this,"permission denied, cant start camera",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CAMERA_REQUEST && resultCode==Activity.RESULT_OK){
            Bitmap pic=(Bitmap) data.getExtras().get("data");
            imgv.setImageBitmap(pic);
        }
    }
}
