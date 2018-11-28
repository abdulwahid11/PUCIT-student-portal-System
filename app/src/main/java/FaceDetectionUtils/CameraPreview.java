package FaceDetectionUtils;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.isityou.sdk.IsItYouConstants;
import com.isityou.sdk.IsItYouSdk;
import com.isityou.sdk.interfaces.CameraListener;

import java.io.IOException;
import java.util.List;


/**
 * CameraPreview.java
 *
 * @author Yair Ohayon
 *
 */

public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private FrameLayout preview;
    private Activity mActivity;
    private static Camera mCamera = null;
    private CameraListener mListener;
    //private CameraListener waitForNextFrame;
    public byte[] myData;
    public byte[] myPostMatchData;
    boolean startRolling =false;
    Size maxSize;
    int bufferSize;
    int bufferSizeForMorre;
    //boolean m_stopped = false;
    //int mRotation=0;
    int counter = 0;
    public final int _REGULAR_PREVIEW_SIZE = 0 ;
    public final int _MAX_PREVIEW_SIZE = 1 ;


    public static Camera getCameraInstance()
    {
        return mCamera;
    }

    @Override
    protected void onDetachedFromWindow() {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();
        mHolder = null;
        preview = null;
        mActivity = null;
        //mCamera = null;
        mListener = null;
        myData = null;
        //IsItYouLogger.getInstance().logMessageDebugByTag("DESTROY", "CAMERAPREVIEW: onDetachedFromWindow");
    }

    private void setupCamera(int previewSize) {

        int cameraId;
        cameraId = 1; //rear is 0 in my phone, 1 is front
        if (cameraId != -1) {
            try {
                mCamera = Camera.open(cameraId); //cameraId
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ALON", ""+e.getMessage());
                Toast.makeText(
                        mActivity,
                        e.toString(),
                        Toast.LENGTH_LONG).show();
                if(mCamera!=null)
                    mCamera.release();
                mCamera = null;
            }


        } else {
            Toast.makeText(mActivity,
                    "Camera feature is not available on this device",
                    Toast.LENGTH_SHORT).show();
        }

        if (mCamera != null) {
            synchronized (mCamera) {
                Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(640, 480); //yay kaboom bomm
                mCamera.setParameters(parameters); //musty setty otherwise it won't save
                //List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                //Log.d("ALON", "Sizes: "+sizes.get(0).height);
                //parameters.setPreviewSize(1080, 1920);
                //mCamera.setParameters(parameters);
                //mCamera.setDisplayOrientation(180);
                bufferSize = 480
                        * 640
                        * ImageFormat.getBitsPerPixel(mCamera.getParameters()
                        .getPreviewFormat());
                myData = new byte[bufferSize];

                initMaxSizeBuffer();
            }
        }

    }

    public void initStartMatchPreviewSize()
    {
        try {
            stopPreview(); //restart preview
            Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(480, 640); //i don't think this is necessary but if it works don't fix it
            mCamera.setParameters(parameters);
            startPreview();
        }
        catch(Exception e)
        {

        }

    }

    public synchronized void getOnePictureWithHighResolution(final CameraListener listen) //unused
    {
        /** Set parameters for high resolution **/
        try {
            mCamera.addCallbackBuffer(null);
            Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(maxSize.width, maxSize.height);
            if(mHolder!=null)
                mHolder.removeCallback(this);
            mCamera.setParameters(parameters);
            myPostMatchData = new byte[bufferSizeForMorre];
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {
                @Override
                public synchronized void onPreviewFrame(byte[] data, Camera camera) {
                    if (myPostMatchData != null && myPostMatchData.length == data.length) {
                        myPostMatchData = null;
                        mCamera.addCallbackBuffer(null);
                        mCamera.stopPreview();
                        listen.onImageTaken(data);
                        Log.d("CAMERA","PostMatch Finished");
                    } else {
                        if (myPostMatchData != null) {
                            mCamera.addCallbackBuffer(data);
                        }
                    }
                }
            });
            mCamera.addCallbackBuffer(myPostMatchData);
            startPreviewBackground();

        }
        catch(Exception e)
        {
            listen.onImageTaken(null);
        }

    }


    public static Size getMaxPreviewSize()
    {
        List<Size> list = mCamera.getParameters().getSupportedPreviewSizes();
        //Size size = list.get(0);
        int maxHeightPerRatio=0;
        int choosenSizepreRation = 0;
        int maxHeight=0;
        int choosenSize = 0;
        for(int i = 0; i<list.size(); i++){
            if((list.get(i).height + list.get(i).width)>maxHeightPerRatio){
                float ratio = (float)((float)list.get(i).width/(float)list.get(i).height);
                if(ratio>1.1 && ratio <1.5) {
                    maxHeightPerRatio = list.get(i).height + list.get(i).width;
                    choosenSizepreRation = i;
                }

            }

            if((list.get(i).height + list.get(i).width)>maxHeight){
                maxHeight = list.get(i).height + list.get(i).width;
                choosenSize = i;
            }
        }
        //Log.d("CAMERA", "Camera max size is " + maxHeightPerRatio);
        if(maxHeightPerRatio>=(1280 + 1024))
            return list.get(choosenSizepreRation);
        else
            return list.get(choosenSize);

    }

    public CameraPreview(Activity activity, FrameLayout contentView,
                         CameraListener listener, boolean startRolling) {
        super(activity);

        /**
         * First configure basic things for IIY camera preview
         * We will set the Camera front face id to the specified id by the device
         * We will set the orientation display for the right device
         */
        if(getCameraInstance()!=null)
            getCameraInstance().release();

        mActivity = activity;
        mListener = listener;
        mHolder = getHolder();
        mHolder.addCallback(this);
        this.startRolling = startRolling;
        preview = contentView;

    }

    public void initMaxSizeBuffer()
    {
        maxSize = getMaxPreviewSize();
        int bufferSize = maxSize.width
                * maxSize.height
                * ImageFormat.getBitsPerPixel(mCamera.getParameters()
                .getPreviewFormat());
        bufferSizeForMorre = bufferSize;
        //myPostMatchData = new byte[bufferSize];
    }


    public byte [] getCurrentFrame()
    {
        Log.d("RESULT2", myData+"");
//		double d = Math.random();
//		Helper.savePicture((int)(d*10),myData,getMaxPreviewSize().width,getMaxPreviewSize().height);
        return myData;
    }

    public void snag() {


        new AsyncTask<String, String, String>() {
            int enrollResult = IsItYouConstants.NO_FACE_DETECT;
            @Override
            protected String doInBackground(String... params) {
                // TODO Auto-generated method stub
                IsItYouSdk alone = IsItYouSdk.getInstance(getContext());
                enrollResult = alone.saveEnrollment(myData,0);
                //IsItYouLogger.getInstance().logMessageDebugByTag("BAR", "DONE: "+enrollResult);
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                // TODO Auto-generated method stub

                super.onPostExecute(result);
                if(enrollResult == IsItYouConstants.ENROLL_SUCCESS)
                {
                    //IsItYouLogger.getInstance().logMessageDebugByTag("ENROLL", ""+enrollResult);
                    if(mListener!=null)
                        mListener.onImageTaken(myData);
                    //if(mCamera!=null)mCamera.addCallbackBuffer(data);
                }
                else
                {
                    switch (enrollResult) {
                        case IsItYouConstants.ENROLL_EXCEPTION:
                            Toast.makeText(getContext(), "ENROLL_EXCEPTION", Toast.LENGTH_SHORT).show();
                            break;
                        case IsItYouConstants.NO_FACE_DETECT:
                            Toast.makeText(getContext(), "ENROLL_FACE_NOT_DETECTED", Toast.LENGTH_SHORT).show();
                            break;
                        case IsItYouConstants.ENROLL_MOVE_AWAY:
                            Toast.makeText(getContext(), "ENROLL_MOVE_AWAY", Toast.LENGTH_SHORT).show();
                            break;
                        case IsItYouConstants.ENROLL_LOCKED:
                            Toast.makeText(getContext(), "ENROLL_LOCKED", Toast.LENGTH_SHORT).show();
                            break;
                        case IsItYouConstants.DETECT_MULTIPLE_FACES:
                            Toast.makeText(getContext(), "DETECT MULTIPLE FACES", Toast.LENGTH_SHORT).show();
                            break;


                        default:
                            break;
                    }
                    if(mListener!=null)
                        mListener.onImageTaken(null);
                    //IsItYouLogger.getInstance().logMessageDebugByTag("ENROLL", ""+enrollResult);
                }

            }
        }.execute();
    }


//	@Override
//	public void draw(Canvas canvas) {
//	   super.draw(canvas);
//	   if (m_stopped) // Set it to true when you stop the preview.
//	   {
//	       canvas.drawColor(Color.WHITE);
//	   }
//	}

    public void stopPreview()
    {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }
        catch(Exception e)
        {

        }

    }

    public void startPreview()
    {
        try {
            if (mCamera != null) {
                mCamera.startPreview();
            }
        }
        catch(Exception e)
        {
            Log.d("ERROR","START PREVIEW FAILED");
        }

    }

    public void stopPreviewBackground(final IsOpFinished callback)
    {
        if (mCamera != null) {
            new AsyncTask<String, Void, String>() {

                @Override
                protected String doInBackground(String... params) {
                    try {
                        mCamera.stopPreview();
                    }
                    catch(Exception e)
                    {
                        Log.d("ERROR","UN_IDENTIFY_ERROR 3");
                        return null;
                    }
                    return "OK";
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if(s == null)
                        callback.isFinished(false);
                    else
                        callback.isFinished(true);
                }
            }.execute();
        }
        else
        {
            Log.d("ERROR","UN_IDENTIFY_ERROR 4");
            callback.isFinished(false);
        }



    }



    public void startPreviewBackground()
    {
        try {
            if (mCamera != null) {
                new AsyncTask<String, Void, String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        try {
                            mCamera.startPreview();
                        }
                        catch(Exception e)
                        {

                        }
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }
                }.execute();
            }
        }
        catch(Exception e)
        {

        }

    }

    public void startPreviewCallbackBuffer() throws Exception {
        /**
         * Start preview called after open camera called because we already have instance of camera
         * So we just need to start preview.
         * initStartMatchPreviewSize - change the preview mode to 640 X 480 because this is the match process
         * and this process can change in a middle of a match. (MORRE)
         */
        if(mHolder==null)
            throw new Exception("mHolder surfaceView is null");
        if (mCamera != null)
        {
            startRolling = true;
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {

                @Override
                public void onPreviewFrame(final byte[] data,
                                           final Camera camera) {
                    synchronized (camera) {


                        if(mCamera!=null)
                            mCamera.addCallbackBuffer(data);
                        if(mListener!=null)
                            mListener.startCapture(data);
//							if(waitForNextFrame!=null)
//								waitForNextFrame.onImageTaken(data);
                    }
                }
            });

            //initStartMatchPreviewSize();
            startPreview();
            if(startRolling) {
                mCamera.addCallbackBuffer(myData);
            }
        }
        else
        {
            throw new Exception("Camera instance is null");
        }
    }


//	public void takePictureNow(final CameraListener listener)
//	{
//		Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback(){
//
//			public void onShutter() {
//
//				// TODO Auto-generated method stub
//			}};
//
//		Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback(){
//
//			public void onPictureTaken(byte[] arg0, Camera arg1) {
//				// TODO Auto-generated method stub
//				int a = 0 ;
//				a++;
//
//			}};
//
//		Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback(){
//
//			public void onPictureTaken(byte[] arg0, Camera arg1) {
//				// TODO Auto-generated method stub
//
//				if(listener!=null)
//				{
//					MatOfByte raw=new MatOfByte(arg0);
//					Mat mat = Imgcodecs.imdecode(raw, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
//					int length = (int) (mat.total() * mat.elemSize());
//					byte buffer[] = new byte[length];
//					mat.get(0,0,buffer);
//					listener.onImageTaken(arg0);
//				}
//			}};
//
//		mCamera.stopPreview();
//		mCamera.startPreview();
//		mCamera.takePicture(myShutterCallback,myPictureCallback_RAW,myPictureCallback_JPG);
//	}

    public void surfaceCreated(SurfaceHolder holder) {

        setupCamera(_REGULAR_PREVIEW_SIZE);
        if (mCamera != null) {

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {

                    @Override
                    public void onPreviewFrame(final byte[] data,
                                               final Camera camera) {
                        synchronized (camera) {
                            //Log.d("CAMERA", ""+data.length);
                            myData = data;
                            if(mCamera!=null)
                                mCamera.addCallbackBuffer(data);
                            if(mListener!=null)
                                mListener.startCapture(data);
//							if(waitForNextFrame!=null)
//								waitForNextFrame.onImageTaken(data);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            Size previewSize = mCamera.getParameters().getPreviewSize();
            float aspect = (float) previewSize.width / previewSize.height;
            int previewSurfaceHeight = preview.getHeight();
            LayoutParams lp = preview.getLayoutParams();
            mCamera.setDisplayOrientation(90);
            if (mActivity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {

                lp.height = previewSurfaceHeight;
                lp.width = (int) (previewSurfaceHeight / aspect);
            } else {
                // landscape
                lp.width = (int) (previewSurfaceHeight * aspect);
                lp.height = previewSurfaceHeight;
            }

            preview.setLayoutParams(lp);
            startPreview();

            if(startRolling) {
                mCamera.addCallbackBuffer(myData);

            }
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mListener!=null)
            mListener.onSurfcaeDestroy();
        if (mCamera != null) {
            synchronized (mCamera) {
                if (mCamera != null) {
                    try {
                        mCamera.stopPreview();
                        mCamera.setPreviewCallback(null);
                        myData = null;
                        myPostMatchData = null;
                    }
                    catch(Exception e)
                    {

                    }
//				mCamera.release();
//				mCamera = null;

                }
            }
        }

        //Log.d("CAMERAPREVIEW", "surfaceDestroyed");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }



}
