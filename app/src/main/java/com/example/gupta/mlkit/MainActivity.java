package com.example.gupta.mlkit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_PERMISSION_CODE=100;
    private static final int CAMERA_REQUEST=1888;

    Bitmap pic;

    ImageView imgv;
    TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgv=findViewById(R.id.imgv);
        result=findViewById(R.id.result);
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
            pic=(Bitmap) data.getExtras().get("data");
            imgv.setImageBitmap(pic);
        }
    }

    public void detectText(View view){
        FirebaseVisionImage image=FirebaseVisionImage.fromBitmap(pic);
        FirebaseVisionTextRecognizer rec=FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        rec.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processTxt(firebaseVisionText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void processTxt(FirebaseVisionText txt){
        List<FirebaseVisionText.TextBlock> block=txt.getTextBlocks();
        if(block.size()==0) {
            result.setText("no text");
            return;
        }
        for(FirebaseVisionText.TextBlock blk:block){
            String text=blk.getText();
            result.setText(text+"\n");
        }
    }

    public void detectFace(View view){
        FirebaseVisionFaceDetectorOptions options=new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();

        FirebaseVisionImage image=FirebaseVisionImage.fromBitmap(pic);
        FirebaseVisionFaceDetector det=FirebaseVision.getInstance().getVisionFaceDetector(options);
        Task<List<FirebaseVisionFace>> res=
                det.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        processFace(firebaseVisionFaces);
                    }
                });
    }

    private void processFace(List<FirebaseVisionFace> faces){
        if(faces.size()==0){
            result.setText("no faces");
            return;
        }
        for(FirebaseVisionFace face:faces){
            Rect bounds=face.getBoundingBox();
            float rotY=face.getHeadEulerAngleY();
            float rotz=face.getHeadEulerAngleZ();

            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float smileProb = face.getSmilingProbability();
                result.setText("Smiling : "+smileProb);
            }
            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                result.append("Roight eye open : "+rightEyeOpenProb);
            }
        }
    }

    public void detectLabel(View view){
        FirebaseVisionLabelDetectorOptions options=new FirebaseVisionLabelDetectorOptions.Builder()
                .setConfidenceThreshold(0.8f)
                .build();

        FirebaseVisionImage image=FirebaseVisionImage.fromBitmap(pic);
        FirebaseVisionLabelDetector det=FirebaseVision.getInstance().getVisionLabelDetector(options);
        Task<List<FirebaseVisionLabel>> labels=
                det.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionLabel> firebaseVisionLabels) {
                        processLabel(firebaseVisionLabels);
                    }
                });
    }

    private void processLabel(List<FirebaseVisionLabel> lbls){
        if(lbls.size()==0){
            result.setText("no labels");
            return;
        }
        for(FirebaseVisionLabel lbl:lbls){
            String text=lbl.getLabel();
            String id=lbl.getEntityId();
            float conf=lbl.getConfidence();
            result.append("Label : "+text+" ID : "+id+" Confidence : "+conf+"\n");
        }
    }
}
