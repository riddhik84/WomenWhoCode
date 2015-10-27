package com.example.womenwhocode.womenwhocode.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.womenwhocode.womenwhocode.R;
import com.example.womenwhocode.womenwhocode.models.Network;
import com.example.womenwhocode.womenwhocode.models.PersonalizationDetail;
import com.example.womenwhocode.womenwhocode.models.Profile;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pnroy on 10/19/15.
 */
public class UserProfileActivity extends AppCompatActivity {
    EditText txtName;
    EditText txtEmail;
    EditText txtjobTitle;
    Spinner spnNetwork;
    String name="";
    String email="";
    String password="";
    Profile userProfile= new Profile();;
    String filepath;
    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    ArrayAdapter<String> adapterForSpinner;
    private static final int SELECTED_PICTURE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        Bundle extras = getIntent().getExtras();
        final ArrayList<String> networks=new ArrayList<String>();
        // TODO: needs a take photo intent for when where is no camera

        if (extras != null) {

            email = extras.getString("Email");

        }
        txtName=(EditText)findViewById(R.id.txtName);
        txtEmail=(EditText)findViewById(R.id.txtEmail);
        txtjobTitle=(EditText)findViewById(R.id.etJob);
        spnNetwork=(Spinner)findViewById(R.id.spnNetwork);
       //get the Network data
        Network ntwkAll=new Network();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Network");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> networkList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < networkList.size(); i++) {
                        networks.add(networkList.get(i).getString("title"));
                    }

                    //  Toast.makeText(getApplicationContext(),networkList.get(0).getString("title"),Toast.LENGTH_LONG).show();

                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });

       // txtName.setText(name);
        txtEmail.setText(email);
        networks.add("select");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,  android.R.layout.simple_spinner_item, networks);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spnNetwork.setAdapter(spinnerArrayAdapter);

        spnNetwork.setSelection(0);
//        txtPwd.setText(password);
    }

    public void OnFinalize(View view) {
        try {
            // Save user with the updated input in Profile model

            userProfile.setFullName(txtName.getText().toString());
            if(spnNetwork.getSelectedItem().toString().equals("select")){
                userProfile.setNetwork("");
            }
            else{
                userProfile.setNetwork(spnNetwork.getSelectedItem().toString());
            }

            userProfile.setJobTitle(txtjobTitle.getText().toString());
            userProfile.setUser(ParseUser.getCurrentUser());
            userProfile.save();
            Intent i = new Intent(UserProfileActivity.this, TimelineActivity.class);
            startActivity(i);
        }catch(ParseException p){

        }
    }

    public void onSelectImage(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, SELECTED_PICTURE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap finalImg=null;
        try {
            // When an Image is picked
            if (requestCode == SELECTED_PICTURE && resultCode == RESULT_OK
                    && null != data) {

                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);

                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filepath = cursor.getString(columnIndex);
                cursor.close();
                Bitmap bmap=BitmapFactory
                        .decodeFile(filepath);
               finalImg = Bitmap.createScaledBitmap(bmap, 200, 200, true);
                ImageView imgView = (ImageView) findViewById(R.id.ivphoto);
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(finalImg);
            }

            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    Uri takenPhotoUri = getPhotoFileUri(photoFileName);

                    // by this point we have the camera photo on disk
                    //Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                    Bitmap rotateImg = rotateBitmapOrientation(takenPhotoUri.getPath());

                    finalImg = Bitmap.createScaledBitmap(rotateImg, 150, 150, true);
                    // Load the taken image into a preview
                    ImageView ivPreview = (ImageView) findViewById(R.id.ivphoto);
                    ivPreview.setImageBitmap(finalImg );
                } else { // Result was a failure
                    Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                }
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if(finalImg !=null) {
                finalImg.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                // get byte array here
                byte[] bytearray = stream.toByteArray();
                ParseFile imgFile = new ParseFile("profileImg.png", bytearray);
                imgFile.saveInBackground();
                userProfile.setPhotoFile(imgFile);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }


    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName)); // set the image file name
        // Start the image capture intent to take photo
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }



    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }
}
