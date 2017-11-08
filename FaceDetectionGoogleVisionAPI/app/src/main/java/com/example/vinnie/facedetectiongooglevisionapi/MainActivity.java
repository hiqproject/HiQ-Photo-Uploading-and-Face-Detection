package com.example.vinnie.facedetectiongooglevisionapi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.util.Timer;

public class MainActivity extends Activity implements View.OnClickListener {

    /* Flag indicating that a request to use the camera has occured */
    private static final int CAM_REQUEST = 0;
    /* Flag indicating that a request has been made to load an image on the imageView */
    private static final int RESULT_LOAD_IMAGE = 1;
    /* String name of the server address where images are stored once uploaded */
    private static final String SERVER_ADDRESS = "https://hiqproject.000webhostapp.com/";
    final Context context = this;
    /* Flag indicating whether the imageView is occupied or not */
    private boolean imageViewOccupied = false;

    /* Declare ImageViews and Buttons */
    ImageView imageToUpload;
    LinearLayout bSubmitImage, bCamera, bUploadImage, bProcessImage;

    /* Declare global variables */
    private String imageFileLocation, selectedImageLocation;
    private ProgressDialog submitting, processing;
    public Bitmap tempBitmap;

    /* Declare Timer Seconds for Performance Analysis */
    int secondsPassed = 0;


    /**
     * Initialise the Activity and each imageView, button and their associated event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialised after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);

        bCamera = (LinearLayout) findViewById(R.id.bCamera);
        bUploadImage = (LinearLayout) findViewById(R.id.bUploadImage);
        bSubmitImage = (LinearLayout) findViewById(R.id.bSubmitImage);
        bProcessImage = (LinearLayout) findViewById(R.id.bProcessImage);

        bCamera.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        bSubmitImage.setOnClickListener(this);
        bProcessImage.setOnClickListener(this);

        BottomNavigationView btmNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        btmNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_home:
                        Intent home = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(home);
                        break;
                }
                return false;
            }
        });
    }

    /**
     * Function which gets called when a button has been clicked. Each button is given an ID
     * which differentiates which buttons have been clicked.
     * <p>
     * bCamera - Create a camera intent and makes a call to capture an image using default Camera App.
     * Saves image Uri into a file and stores it in device storage.
     * bUploadImage - Create a gallery intent and stores the Uri from the selected image to be displayed
     * On an ImageView.
     * bSubmitImage - Grabs the bitmap of the image currently in the ImageView, gives it a file name based
     * on the current date and time and uploads the image to a web-server.
     * bProcessImage - Runs the face detection algorithms to detect a face in an image on the ImageView.
     *
     * @param v The view that was clicked
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bCamera:
                Intent cameraIntent = new Intent();
                cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = getFile();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(cameraIntent, CAM_REQUEST);
                break;
            case R.id.bUploadImage:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
                break;
            case R.id.bSubmitImage:
                /* Check to see if the imageView contains an image to be uploaded */
                if (!imageViewOccupied) {
                    createDialog("No Image to Submit!", "Please upload an image of your frontal face to be submitted.");
                    return;
                }
                /* Launch a submitting message until the image is uploaded */
                uploadStatus();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp;

                /* Uploads image to the web server */
                Bitmap image = ((BitmapDrawable) imageToUpload.getDrawable()).getBitmap();
                new UploadImage(image, imageFileName).execute();

                /* Uploads a scaled image to the web server */
                Bitmap imageScaled = ((BitmapDrawable) imageToUpload.getDrawable()).getBitmap();
                imageScaled = Bitmap.createScaledBitmap(imageScaled, 128, 171, false);
                new UploadImage(imageScaled, imageFileName + "_scaled").execute();
                break;
            case R.id.bProcessImage:

                /* Declare confidence level variable */
                float confidence = 0;

                /* Timer */
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        secondsPassed++;
                        System.out.println(secondsPassed);
                    }
                };
                timer.scheduleAtFixedRate(timerTask, 1, 1); // Added timer

                /* Check to see if the imageView contains an image to be processes */
                if (!imageViewOccupied) {
                    createDialog("No Image to Process!", "Please upload an image of your frontal face to be processed.");
                    return;
                }
                /* Launch a processing message until the image is processed */
                processingStatus();
                /* Begin the Face Detection */
                Bitmap bitmapToProcess = ((BitmapDrawable) imageToUpload.getDrawable()).getBitmap();

                final Paint rectPaint = new Paint();
                rectPaint.setStrokeWidth(5);
                rectPaint.setColor(Color.RED);
                rectPaint.setStyle(Paint.Style.STROKE);

                final Bitmap tempBitmap = Bitmap.createBitmap(bitmapToProcess.getWidth(), bitmapToProcess.getHeight(), Bitmap.Config.RGB_565);
                final Canvas canvas = new Canvas(tempBitmap);
                canvas.drawBitmap(bitmapToProcess, 0, 0, null);

                /* Run Face Detection */
                performFaceDetection(timer, confidence);

                timer.cancel(); // Timer cancel
                secondsPassed = 0; // Reset seconds
                Toast.makeText(MainActivity.this, "Face Detected!", Toast.LENGTH_SHORT).show();
                processing.dismiss();
                break;
        }
        imageToUpload.setImageBitmap(tempBitmap); //Uncomment this to see the box drawn around the face
    }

    /**
     * Function which performs the majority of the face detection using the Google Vision API.
     * Stores each detected face into an array and conducts facial landmark detection.
     *
     * @param timer timer to measure execution time of face detection
     * @param confidence confidence level for a detected face
     */
    private void performFaceDetection(Timer timer, float confidence) {

        Bitmap bitmapToProcess = ((BitmapDrawable) imageToUpload.getDrawable()).getBitmap();

        final Paint rectPaint = new Paint();
        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);

        tempBitmap = Bitmap.createBitmap(bitmapToProcess.getWidth(), bitmapToProcess.getHeight(), Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(tempBitmap);
        canvas.drawBitmap(bitmapToProcess, 0, 0, null);

        /* Run Face Detection */
        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        /* Check if the library has been downloaded onto the device and the face detector is operational.*/
        if (!faceDetector.isOperational()) {
            Toast.makeText(MainActivity.this, "Face detector could not be set up on your device", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Place each detected face into an array */
        Frame frame = new Frame.Builder().setBitmap(bitmapToProcess).build();
        SparseArray<Face> sparseArray = faceDetector.detect(frame);
        /* Check if array contains faces, if not then send failure message */
        if (sparseArray.size() == 0) {
            Toast.makeText(MainActivity.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
            createDialog("Submission Failed!", "Try the following:\n\n  - Consider taking a photo standing in front of a plain background \n\n  - Minimise any shadowing or brightness around the face\n\n  - Make sure that the photo is of an acceptable quality with no bluriness\n\n - Make sure hair, headwear or glasses are not obstructing the face");
            processing.dismiss();
            timer.cancel(); // Timer cancel
            secondsPassed = 0; // Reset seconds
        } else {
            /* Draw a box around each detected face and prompt that a face has been detected */
            for (int i = 0; i < sparseArray.size(); i++) {
                Face face = sparseArray.valueAt(i);
                float x1 = face.getPosition().x;
                float y1 = face.getPosition().y;
                float x2 = x1 + face.getWidth();
                float y2 = y1 + face.getHeight();
                RectF rectF = new RectF(x1, y1, x2, y2);
                canvas.drawRoundRect(rectF, 2, 2, rectPaint);

                /* Array of strings to hold landmark types */
                List<String> landmarkTypes = new ArrayList<String>();

                /* Detect the landmarks on the face */
                for (Landmark landmark : face.getLandmarks()) {
                    int x = (int) (landmark.getPosition().x);
                    int y = (int) (landmark.getPosition().y);
                    String type = String.valueOf(landmark.getType());
                    landmarkTypes.add(type);
                    rectPaint.setTextSize(50);
                    canvas.drawText(type, x, y, rectPaint);
                    confidence += 0.125;
                }

                /* Check if each landmark is found, otherwise send error message */
                checkFacialLandmarks(landmarkTypes, confidence);

                /* Store the facial data obtained from the face and output further messages */
                storeFacialData(face, confidence);
            }
        }

    }

    /**
     * Function which adjusts confidence level based on facial landmarks detected and outputs error
     * messages based on what landmarks were detected or not detected.
     *
     * @param landmarkTypes array of strings holding the ID's for each detected facial landmark
     * @param confidence confidence level for a detected face
     */
    private void checkFacialLandmarks(List<String> landmarkTypes, float confidence) {

        /* Adjust confidence level reading to determine if frontal face */
        if (landmarkTypes.contains("2") || landmarkTypes.contains("3") || landmarkTypes.contains("8") || landmarkTypes.contains("9")) {
            confidence -= 0.125;
        }

        /* Check if each landmark is found, if not then send an error message */
        if (!landmarkTypes.contains("10") || !landmarkTypes.contains("4")) {
            Toast.makeText(MainActivity.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
            createDialog("Submission Failed!", "Try the following:\n\n - Make sure headwear, hair or glasses are not obstructing the eyes\n\n - Minimise any shadowing or brightness around the eyes");
        } else if (!landmarkTypes.contains("6")) {
            Toast.makeText(MainActivity.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
            createDialog("Submission Failed!", "Minimise any shadowing or brightness around the nose");
        } else if (!landmarkTypes.contains("1") || !landmarkTypes.contains("7")) {
            Toast.makeText(MainActivity.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
            createDialog("Submission Failed!", "Try the following:\n\n - Make sure headwear or hair is not obstructing the left and right cheek\n\n - Minimise any shadowing or brightness around the left and right cheeks");
        } else if (!landmarkTypes.contains("0") || !landmarkTypes.contains("5") || !landmarkTypes.contains("11")) {
            Toast.makeText(MainActivity.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
            createDialog("Submission Failed!", "- Minimise any shadowing or brightness around the mouth");
        }
    }

    /**
     * Function which captures facial data and uses it to further adjust confidence levels, output error messages
     * and stores it for further analysis.
     *
     * @param face face object that was detected
     * @param confidence confidence level for a detected face
     */
    private void storeFacialData(Face face, float confidence) {

        /* Returns the rotation of the face about the vertical axis of the image.
           Positive euler y is when the face turns toward the right side of the of the
           image that is being processed. */
        float eulerY = face.getEulerY();

        /* Returns the rotation of the face about the axis pointing out of the image.
           Positive euler z is a counter-clockwise rotation within the image plane. */
        float eulerZ = face.getEulerZ();

        /* Returns a value between 0.0 and 1.0 giving a probability that the face's left eye is open. */
        float leftEyeProb = face.getIsLeftEyeOpenProbability();
        /* Returns a value between 0.0 and 1.0 giving a probability that the face's right eye is open. */
        float rightEyeProb = face.getIsRightEyeOpenProbability();
        /* Returns a value between 0.0 and 1.0 giving a probability that the face is smiling. */
        float smileProb = face.getIsSmilingProbability();

        /* Returns the height of a face in pixels. */
        float faceHeight = face.getHeight();
        /* Returns the width of a face in pixels. */
        float faceWidth = face.getWidth();

        System.out.println("Height:" + imageToUpload.getDrawable().getIntrinsicHeight());
        System.out.println("Width:" + imageToUpload.getDrawable().getIntrinsicWidth());

        /* Error messages based on the returned data */
        if (leftEyeProb < 0.6 || rightEyeProb < 0.6) {
            confidence -= 0.25;
            Toast.makeText(MainActivity.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
            createDialog("Submission Failed!", "- Make sure both left and right eyes are open and visible in the image");
        } else if (eulerY > 9 || eulerZ > 9 || eulerY < -9 || eulerZ < -9 ) {
            confidence -= 0.25;
            Toast.makeText(MainActivity.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
            createDialog("Submission Failed!", "- Make sure that your face is completely frontal and not tilted in any direction");
        }

        /* Error messages based on face size */
        if (faceHeight < 600 || faceHeight > 1000) {
            Toast.makeText(MainActivity.this, "Submission Failed!", Toast.LENGTH_SHORT).show();
            createDialog("Submission Failed!", "Make sure your face in the center of the frame with the camera positioned 25-35cm away from your face");
        } else if (faceWidth < 500 || faceHeight > 800) {

        }

        /* Print the data obtained from the face */
        printFacialData(faceHeight, faceWidth, eulerY, eulerZ, leftEyeProb, rightEyeProb, smileProb, confidence);
    }

    /**
     * Function to print all of the captured data to the console.
     *
     * @param faceHeight float value for the height of the face in the image
     * @param faceWidth float value for the width of the face in the image
     * @param eulerY float value for the facial orientation on the Y axis
     * @param eulerZ float value for the facial orientation
     * @param leftEyeProb probability value that the left eye is open
     * @param rightEyeProb probability value that the right eye is open
     * @param smileProb probability value that the face is smiling
     * @param confidence confidence level for a detected face
     */
    private void printFacialData(float faceHeight, float faceWidth, float eulerY, float eulerZ, float leftEyeProb, float rightEyeProb, float smileProb, float confidence) {

        System.out.println("Face Height: " + faceHeight);
        System.out.println("Face Width: " + faceWidth);
        System.out.println("EulerY: " + eulerY);
        System.out.println("EulerZ " + eulerZ);
        System.out.println("Left Eye Probability: " + leftEyeProb);
        System.out.println("Right Eye Probability: " + rightEyeProb);
        System.out.println("Smiling Probability: " + smileProb);
        System.out.println("Confidence Level: " + confidence);
    }

    /**
     * Creates a dialog prompt.
     *
     * @param title   The text which appears at the top of the dialog box.
     * @param message The text which appears in the body of the dialog box.
     */
    public void createDialog(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Creates a loading message/dialog until the image is uploaded to the web-server.
     */
    public void uploadStatus() {
        submitting = new ProgressDialog(this);
        submitting.setTitle("Please Wait...");
        submitting.setMessage("Submitting");
        submitting.setCancelable(false);
        submitting.show();
    }

    /**
     * Creates a loading message/dialog while the face detection executes on an image.
     */
    public void processingStatus() {
        processing = new ProgressDialog(this);
        processing.setTitle("Please Wait...");
        processing.setMessage("Processing");
        processing.setCancelable(false);
        processing.show();
    }

    /**
     * Handles saving an image taken using the Camera App using the current date and time as the file name.
     *
     * @return File path of the saved image.
     */
    private File getFile() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";

        File folder = new File("sdcard/camera_app");

        if (!folder.exists()) {
            folder.mkdir();
        }

        File imageFile = new File(folder, imageFileName);
        imageFileLocation = imageFile.getAbsolutePath();

        return imageFile;
    }

    /**
     * Reduces the size of an image displayed on an ImageView in order to save on RAM and CPU usage.
     *
     * @return Bitmap of the image with a reduced size.
     */
    private Bitmap setReducedImageSize() {
        int targetImageViewWidth = imageToUpload.getWidth();
        int targetImageViewHeight = imageToUpload.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFileLocation, bmOptions);

        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth, cameraImageHeight/targetImageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;

        Bitmap photoReducedSizeBitmap = BitmapFactory.decodeFile(imageFileLocation, bmOptions);
        return photoReducedSizeBitmap;
    }

    /**
     * Rotates the image captured using the Camera App so that it sits in the correct orientation on the ImageView.
     *
     * @param bitmap The image that is currently in the process of being displayed on the ImageView.
     */
    private void rotateImage(Bitmap bitmap) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(imageFileLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageToUpload.setImageBitmap(rotatedBitmap);
    }

    /**
     * Rotates the image selected from the image gallery so that it sits in the correct orientation on the ImageView.
     *
     * @param bitmap The image that is currently in the process of being displayed on the ImageView.
     */
    private void rotateSelectedImage(Bitmap bitmap) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(selectedImageLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageToUpload.setImageBitmap(rotatedBitmap);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with, the resultCode it returned,
     * and any additional data from it. The resultCode will be RESULT_CANCELED if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     *
     * Function to display the image on an ImageView in the app.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify where this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            /* Read the selected image data via its URI */
            Uri selectedImage = data.getData();
            /* Grab the file location of the image to use for the rotation */
            selectedImageLocation = getRealPathFromURI(selectedImage);
            /* Grab the Bitmap of the selected image */
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            rotateSelectedImage(bitmap);
            imageViewOccupied = true;
        }

        if (requestCode == CAM_REQUEST && resultCode == RESULT_OK) {
            rotateImage(setReducedImageSize());
            imageViewOccupied = true;
        }
    }

    /**
     * Grabs the file location path from an image in the image gallery.
     *
     * @param uri The Uri of the image that we want the location of.
     * @return
     */
    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    /* Classes and functions to handle the Server connection*/
    public class UploadImage extends AsyncTask<Void, Void, Void> {

        Bitmap image;
        String name;

        public UploadImage(Bitmap image, String name) {
            this.image = image;
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            Map<String, String> dataToSend = new HashMap<>();
            dataToSend.put("image", encodedImage);
            dataToSend.put("name", name);

            String encodedStr = getEncodedData(dataToSend);

            BufferedReader reader = null;

            try {
                URL url = new URL(SERVER_ADDRESS + "SavePicture.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("POST");
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                line = sb.toString();

                // Check the values recieved at Logcat
                Log.i("custom_check", "The values recieved at the store part are as follows:");
                Log.i("custom_check", line);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        /* Encoded String method used for Server Communication */
        private String getEncodedData(Map<String, String> data) {
            StringBuilder sb = new StringBuilder();
            for (String key : data.keySet()) {
                String value = null;
                try {
                    value = URLEncoder.encode(data.get(key), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(key + "=" + value);
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Image Submitted", Toast.LENGTH_SHORT).show();
            submitting.dismiss();
        }
    }
}