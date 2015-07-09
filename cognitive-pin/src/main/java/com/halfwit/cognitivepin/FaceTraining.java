package com.halfwit.cognitivepin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class FaceTraining extends Activity implements CvCameraViewListener2 {
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;
    public static final int TRAINING = 0;
    public static final int SEARCHING = 1;
    public static final int IDLE = 2;
    static final long MAXIMG = 10;
    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final int frontCam = 1;
    private static final int backCam = 2;
    private static final int PIN_LEN = 3;
    //    private int countTrain=0;
    String mPath = "";
    EditText etName;
    TextView textresult;
    Bitmap mBitmap;
    Handler mHandler;
    PersonRecognizer fr;
    ToggleButton toggleButtonGrabar, toggleButtonTrain, buttonSearch;
    //   private DetectionBasedTracker  mNativeDetector;
    Button buttonCatalog;
    ImageView ivGreen, ivYellow, ivRed;
    ImageButton imCamera;
    TextView textState;
    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;
    ArrayList<Mat> alimgs = new ArrayList<Mat>();
    int[] labels = new int[(int) MAXIMG];
    int countImages = 0;
    labels labelsFile;
    private int faceState = IDLE;
    //    private MenuItem               mItemFace50;
//    private MenuItem               mItemFace40;
//    private MenuItem               mItemFace30;
//    private MenuItem               mItemFace20;
//    private MenuItem               mItemType;
//
    private Button btnCapture;
    private Button btnSave;

    private MenuItem nBackCam;
    private MenuItem mFrontCam;
    private MenuItem mEigen;
    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;
    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private int mLikely = 999;
    private RecognitionView mOpenCvCameraView;
    private int mChooseCamera = frontCam;
    private ImageView ivPerson;
    private boolean capturedFlag = false;

    //static { if (!OpenCVLoader.initDebug()) {  }}


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //   System.loadLibrary("detection_based_tracker");


                    fr = new PersonRecognizer(mPath);
                    String s = getResources().getString(R.string.Straining);
                    //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public FaceTraining() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_face_training);

        mOpenCvCameraView = (RecognitionView) findViewById(R.id.face_training_java_surface_view);

        mOpenCvCameraView.setCvCameraViewListener(this);
        //mOpenCvCameraView.setCamFront();
        //mOpenCvCameraView.setCamBack();


        //mPath = getFilesDir() + "/userList/";
        mPath = extras.getString("PATH");

        labelsFile = new labels(mPath);

        ivPerson = (ImageView) findViewById(R.id.face_training_iv_person);


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj == "IMG") {
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(mBitmap);
                    ivPerson.setImageBitmap(mBitmap);
                    capturedFlag = true;
                }
                /*else {
                    textresult.setText(msg.obj.toString());
                    //ivGreen.setVisibility(View.INVISIBLE);
                    //ivYellow.setVisibility(View.INVISIBLE);
                    //ivRed.setVisibility(View.INVISIBLE);

                    *//*if (mLikely < 0) ;
                    else if (mLikely < 50)
                        ivGreen.setVisibility(View.VISIBLE);
                    else if (mLikely < 80)
                        ivYellow.setVisibility(View.VISIBLE);
                    else
                        ivRed.setVisibility(View.VISIBLE);*//*
                }*/
            }
        };

        etName = (EditText) findViewById(R.id.face_training_et_name);
        btnCapture = (Button) findViewById(R.id.face_training_btn_capture);
        btnSave = (Button) findViewById(R.id.face_training_btn_save);

        //btnCapture.setVisibility(View.INVISIBLE);
        //btnSave.setVisibility(View.INVISIBLE);
        btnCapture.setEnabled(false);



        //buttonCatalog = (Button) findViewById(R.id.buttonCat);
        //toggleButtonGrabar = (ToggleButton) findViewById(R.id.toggleButtonGrabar);
        //buttonSearch = (ToggleButton) findViewById(R.id.buttonBuscar);
        //toggleButtonTrain = (ToggleButton) findViewById(R.id.toggleButton1);
        //textState = (TextView) findViewById(R.id.textViewState);
        //ivGreen = (ImageView) findViewById(R.id.imageView3);
        //ivYellow = (ImageView) findViewById(R.id.imageView4);
        //ivRed = (ImageView) findViewById(R.id.imageView2);
        //imCamera = (ImageButton) findViewById(R.id.imageButton1);

        /*ivGreen.setVisibility(View.INVISIBLE);
        ivYellow.setVisibility(View.INVISIBLE);
        ivRed.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
        textresult.setVisibility(View.INVISIBLE);*/


        //toggleButtonGrabar.setVisibility(View.INVISIBLE);

        /*buttonCatalog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(org.opencv.javacv.facerecognition.FdActivity.this,
                        org.opencv.javacv.facerecognition.ImageGallery.class);
                i.putExtra("path", mPath);
                startActivity(i);
            }

            ;
        });*/


        etName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (etName.getText().toString().length() > 0)
                    btnCapture.setEnabled(true);
                else {
                    btnCapture.setEnabled(false);
                }

                return false;
            }
        });

        btnCapture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (faceState == TRAINING) {
                    countImages = 0;
                    faceState = IDLE;
                }
                faceState = TRAINING;
            }
        });


        /*toggleButtonTrain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (toggleButtonTrain.isChecked()) {
                    textState.setText(getResources().getString(R.string.SEnter));
                    buttonSearch.setVisibility(View.INVISIBLE);
                    textresult.setVisibility(View.VISIBLE);
                    etName.setVisibility(View.VISIBLE);
                    textresult.setText(getResources().getString(R.string.SFaceName));
                    if (etName.getText().toString().length() > 0)
                        toggleButtonGrabar.setVisibility(View.VISIBLE);

                } else {
                    textState.setText(R.string.Straininig);
                    textresult.setText("");
                    etName.setVisibility(View.INVISIBLE);

                    buttonSearch.setVisibility(View.VISIBLE);
                    ;
                    textresult.setText("");
                    {
                        toggleButtonGrabar.setVisibility(View.INVISIBLE);
                        etName.setVisibility(View.INVISIBLE);
                    }
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Straininig), Toast.LENGTH_LONG).show();
                    fr.train();
                    textState.setText(getResources().getString(R.string.SIdle));

                }
            }

        });*/


        /*toggleButtonGrabar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                grabarOnclick();
            }
        });*/

        /*imCamera.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (mChooseCamera == frontCam) {
                    mChooseCamera = backCam;
                    mOpenCvCameraView.setCamBack();
                } else {
                    mChooseCamera = frontCam;
                    mOpenCvCameraView.setCamFront();

                }
            }
        });*/

        /*buttonSearch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (buttonSearch.isChecked()) {
                    if (!fr.canPredict()) {
                        buttonSearch.setChecked(false);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
                        return;
                    }
                    textState.setText(getResources().getString(R.string.SSearching));
                    toggleButtonGrabar.setVisibility(View.INVISIBLE);
                    toggleButtonTrain.setVisibility(View.INVISIBLE);
                    etName.setVisibility(View.INVISIBLE);
                    faceState = SEARCHING;
                    textresult.setVisibility(View.VISIBLE);
                } else {
                    faceState = IDLE;
                    textState.setText(getResources().getString(R.string.SIdle));
                    toggleButtonGrabar.setVisibility(View.INVISIBLE);
                    toggleButtonTrain.setVisibility(View.VISIBLE);
                    etName.setVisibility(View.INVISIBLE);
                    textresult.setVisibility(View.INVISIBLE);

                }
            }
        });*/

        boolean success = (new File(mPath)).mkdirs();
        if (!success) {
            Log.e("Error", "Error creating directory");
        }
    }

    void grabarOnclick() {
        if (toggleButtonGrabar.isChecked())
            faceState = TRAINING;
        else {
            if (faceState == TRAINING) ;
            // train();
            //fr.train();
            countImages = 0;
            faceState = IDLE;
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        /*
        Mat mRgbaT = mRgba.t();
        Core.flip(mRgba.t(), mRgbaT, -1);
        Imgproc.resize(mRgbaT, mRgbaT, new Size(640, 480), 1920, 1080, Imgproc.INTER_AREA);
        //Log.e("size", mRgba.size().toString());
        mRgba = mRgbaT;
        */


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }


        /*
        Mat temp = mGray.clone();

        Core.transpose(mGray, temp);
        Core.flip(temp, temp, -1);
        */

        MatOfRect faces = new MatOfRect();


        //Core.flip(mRgbaT.t(), mRgbaT, -1);
        //Core.flip(mGray.t(), mGray, -1);


        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        } else if (mDetectorType == NATIVE_DETECTOR) {
//            if (mNativeDetector != null)
//                mNativeDetector.detect(mGray, faces);
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length == 1) && (faceState == TRAINING) && (countImages < MAXIMG) && (!etName.getText().toString().isEmpty())) {


            Mat m = new Mat();
            Rect r = facesArray[0];


            m = mRgba.submat(r);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            // SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");

            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);
            if (countImages < MAXIMG) {
                fr.add(m, etName.getText().toString());
                countImages++;
            }

        }
        /*else if ((facesArray.length > 0) && (faceState == SEARCHING)) {
            Mat m = new Mat();
            m = mGray.submat(facesArray[0]);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

            textTochange = fr.predict(m);
            mLikely = fr.getProb();
            msg = new Message();
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

        }*/

        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Log.i(TAG, "called onCreateOptionsMenu");
        if (mOpenCvCameraView.numberCameras() > 1) {
            nBackCam = menu.add(getResources().getString(R.string.SFrontCamera));
            mFrontCam = menu.add(getResources().getString(R.string.SBackCamera));
//        mEigen = menu.add("EigenFaces");
//        mLBPH.setChecked(true);
        } else {
            imCamera.setVisibility(View.INVISIBLE);

        }
        //mOpenCvCameraView.setAutofocus();*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*// Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
//        if (item == mItemFace50)
//            setMinFaceSize(0.5f);
//        else if (item == mItemFace40)
//            setMinFaceSize(0.4f);
//        else if (item == mItemFace30)
//            setMinFaceSize(0.3f);
//        else if (item == mItemFace20)
//            setMinFaceSize(0.2f);
//        else if (item == mItemType) {
//            mDetectorType = (mDetectorType + 1) % mDetectorName.length;
//            item.setTitle(mDetectorName[mDetectorType]);
//            setDetectorType(mDetectorType);
//
//        }
        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);
        //  mEigen.setChecked(false);
        if (item == nBackCam) {
            mOpenCvCameraView.setCamFront();
            mChooseCamera = frontCam;
        }
        //fr.changeRecognizer(0);
        else if (item == mFrontCam) {
            mChooseCamera = backCam;
            mOpenCvCameraView.setCamBack();

        }

        item.setChecked(true);*/
        return true;
    }


    public void loadSetPIN(View view){
        fr.train();
        Intent intent = new Intent(this, SetPIN.class);
        startActivity(intent);
    }

    public void savePIN(View view){
        Intent intent = new Intent(this, HomeScreen.class);
        EditText pinText = (EditText) findViewById(R.id.face_training_et_pin);
        EditText userName = (EditText) findViewById(R.id.face_training_et_name);
        Bundle bundle = new Bundle();
        RadioButton radioLeft = (RadioButton) findViewById(R.id.face_training_rd_left);
        RadioButton radioRight = (RadioButton) findViewById(R.id.face_training_rd_right);
        if ((userName.length() > 0) && (pinText.length() >= PIN_LEN) && (radioLeft.isChecked() || radioRight.isChecked())) {
            bundle.putString("EXTRA_PIN", pinText.getText().toString());

            if (radioLeft.isChecked())
                bundle.putString("EXTRA_SELECTION", "left");
            else
                bundle.putString("EXTRA_SELECTION", "right");

            intent.putExtras(bundle);

            startActivity(intent);
        }
        else if (userName.length() == 0){
            Toast.makeText(getApplicationContext(), "Please, enter the user name!", Toast.LENGTH_SHORT).show();
        }
        else if (!capturedFlag){
            Toast.makeText(getApplicationContext(), "Please, capture your photo!", Toast.LENGTH_SHORT).show();
        }
        else if (pinText.length() < PIN_LEN) {
            Toast.makeText(getApplicationContext(), "PIN has to be minimum 3 digits!", Toast.LENGTH_SHORT).show();
        }
        else if (!radioLeft.isChecked() || !radioRight.isChecked()) {
            Toast.makeText(getApplicationContext(), "Select Left or Right!", Toast.LENGTH_SHORT).show();
        }


        //startActivity (new Intent(this, HomeScreen.class));
    }

}
