    package com.example.myapplication;

    import android.Manifest;
    import android.content.Context;
    import android.content.pm.PackageManager;
    import android.hardware.camera2.CameraAccessException;
    import android.hardware.camera2.CameraCaptureSession;
    import android.hardware.camera2.CameraDevice;
    import android.hardware.camera2.CameraManager;
    import android.hardware.camera2.CaptureRequest;
    import android.os.Bundle;
    import android.util.SparseArray;
    import android.view.SurfaceHolder;
    import android.view.SurfaceView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;

    import com.google.android.gms.vision.Detector;
    import com.google.android.gms.vision.MultiProcessor;
    import com.google.android.gms.vision.Tracker;
    import com.google.android.gms.vision.barcode.Barcode;
    import com.google.android.gms.vision.barcode.BarcodeDetector;

    import java.util.Arrays;


    public class QRCode_Scanner extends AppCompatActivity implements SurfaceHolder.Callback, Detector.Processor<Barcode> {

        private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

        private CameraManager cameraManager;
        private CameraDevice cameraDevice;
        private CaptureRequest.Builder captureRequestBuilder;
        private SurfaceView surfaceView;
        private BarcodeDetector barcodeDetector;

        private boolean cameraPermissionGranted = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_qrcode_scanner);

            surfaceView = findViewById(R.id.surfaceView);
            barcodeDetector = new BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.QR_CODE).build();

            barcodeDetector.setProcessor(new MultiProcessor.Builder<>(new QRCodeTrackerFactory()).build());
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(this);


        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
                // Check if the permission is granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraPermissionGranted = true;
                    // Permission is granted, setup the camera
                    setupCamera();
                } else {
                    // Permission is denied, show a message or take action accordingly
                    Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (ActivityCompat.checkSelfPermission(QRCode_Scanner.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, setup the camera
                setupCamera();

            }
        }

        private void setupCamera() {
            // Add your camera setup code here
            cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cameraManager.getCameraIdList()[0];

                if (ActivityCompat.checkSelfPermission(QRCode_Scanner.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(QRCode_Scanner.this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST_CODE);


                } else {
                    // Permission is granted, setup the camera
                    cameraPermissionGranted = true;

                }

                cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        cameraDevice = camera;
                        try {
                            captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            captureRequestBuilder.addTarget(surfaceView.getHolder().getSurface());
                            camera.createCaptureSession(Arrays.asList(surfaceView.getHolder().getSurface()), new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(@NonNull CameraCaptureSession session) {
                                    try {
                                        session.setRepeatingRequest(captureRequestBuilder.build(), null, null);

                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }

                                }

                                @Override
                                public void onConfigureFailed(@NonNull CameraCaptureSession session) {


                                }
                            }, null);
                        } catch (CameraAccessException e) {
                            throw new RuntimeException(e);
                        }

                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {

                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {

                    }
                }, null);

            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }

        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // Add your code here
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Add your cleanup code here
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }

        @Override
        public void release() {


        }

        @Override
        public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
            SparseArray<Barcode>barcodes=detections.getDetectedItems();
            if (barcodes.size() > 0) {
                onBarcodeScanned(barcodes.valueAt(0));
            }

        }

        private void onBarcodeScanned(Barcode valueAt) {
        }

        private class QRCodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
            @Override
            public Tracker<Barcode> create(Barcode barcode) {
                return new QRCodeTracker();
            }
        }

        private class QRCodeTracker extends Tracker<Barcode> {
            @Override
            public void onUpdate(Detector.Detections<Barcode> detections, Barcode barcode) {
                onBarcodeDetected(barcode);
            }
            public void onBarcodeDetected(Barcode barcode) {
                // Add your code here to handle the detected barcode
            }
        }

        }

