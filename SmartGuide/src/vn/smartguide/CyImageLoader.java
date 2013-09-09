package vn.smartguide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

/**
 * 
 * @author CYCRIXLAPTOP-PC
 * 	Disk cache
 *  Memory cache
 *  Resample image 
 */
public class CyImageLoader {
	
	public static final int FROM_MEMORY 	= 0;
	public static final int FROM_DISK 		= 1;
	public static final int FROM_NETWORK 	= 2;
	
	public static final int CONNECTION_NUM	= 8;
	
	// <Sample+Path, Bitmap>
	private WeakHashMap<String, Bitmap> mMemCacheMap = new WeakHashMap<String, Bitmap>();
	
	// <Path, hash>
	private HashMap<String, BitmapInfo> mDiskCacheMap = new HashMap<String, BitmapInfo>();
	
	// ThreadPoolExecutor
	
	private Context ct;
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	
	public CyImageLoader(Context ct) {
		
		this.ct = ct;
		clearCache(ct);
	}
	
	public void release() {
		
		ct = null;
	}
	
	public void showImage(String path, ImageView imgView) {

		loadImage(path, new Listener() {
			
			private ImageView imgView;
			
			public Listener init(ImageView img) {
				imgView = img;
				return this;
			}
			
			@Override
			public void loadFinish(int from, Bitmap image) {
				imgView.setImageBitmap(image);
			}
		}.init(imgView), new Point(imgView.getWidth(), imgView.getHeight()), imgView.getContext());
	}
	
	public void loadImage(String path, Listener listener, Point expectedSize, Context ct) {
		
		Bitmap bm = checkMemCache(path, expectedSize);
		
		if (bm != null) {
			listener.startLoad(FROM_MEMORY);
			listener.loadFinish(FROM_MEMORY, bm);
			return;
		}
		
		BitmapInfo bmInf = checkDiskCache(path);
		
		if (bmInf != null) {
			listener.startLoad(FROM_DISK);
			int sample = calcSample(bmInf, expectedSize);
			try {
				bm = loadOptimize(bmInf, sample, ct);
			} catch (Exception e) {
				// If this line is reached, something went wrong!
				listener.loadFail(e);
				return;
			}
			cacheMem(bm, path, sample);
			listener.loadFinish(FROM_DISK, bm);
			return;
		}
		
		download(path, expectedSize, ct, listener);
	}
	
	public void clearCache(Context ct) {
		File cacheDir = ct.getCacheDir();
		for (File f : cacheDir.listFiles())
			f.delete();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////	
	
	private Bitmap checkMemCache(String path, Point expectedSize) {
		
		BitmapInfo bmInf = mDiskCacheMap.get(path);
		if (bmInf == null) {
//			debugLog("checkMemCache miss");
			return null;
		}
			
		int expectedSample = calcSample(bmInf, expectedSize);
		Bitmap bm = mMemCacheMap.get(String.format("%03d", expectedSample) + path);
		if (bm == null) {
//			debugLog("checkMemCache miss");
		} else {
//			debugLog("checkMemCache hit");
		}
		return bm;
	}
	
	private BitmapInfo checkDiskCache(String path) {
		
		BitmapInfo bmInf = mDiskCacheMap.get(path);
		if (bmInf == null) {
//			debugLog("checkDiskCache miss");
		} else {
//			debugLog("checkDiskCache hit");
		}
		return bmInf;
	}
	
	private Bitmap loadOptimize(BitmapInfo bmInf, int sample, Context ct) throws Exception {
		
//		ActivityManager activityManager =  (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
//		MemoryInfo memoryInfo = new MemoryInfo();
//		activityManager.getMemoryInfo(memoryInfo);
//		debugLog("" + memoryInfo.availMem / 1024 + "/" + activityManager.getMemoryClass() + " MB");
		
		Options opt = new Options();
		opt.inScaled = false;
		opt.inSampleSize = sample;
		String cachePath = ct.getCacheDir().getAbsolutePath();
		FileInputStream in = new FileInputStream(cachePath + "/" + GlobalVariable.IMAGE_FILE_PATH + bmInf.hash);
		Bitmap bm = BitmapFactory.decodeStream(in, null, opt);
		if (bm == null)
			throw new Exception("Cannot decode bitmap " + bmInf.hash);
		return bm;
	}
	
	private void cacheMem(Bitmap bm, String path, int sample) {
		
		mMemCacheMap.put(String.format("%03d", sample) + path, bm);
//		debugLog("cacheMem " + path);
	}
	
	private void download(String path, Point expectedSize, Context ct, Listener listener) {
		
		// Generate file
		String fileName = generateRandomImageFileName(ct);
		
		// Download
		new HttpConnection(new DownloadImageHandler(listener, ct, expectedSize, path))
		.getFile(path, ct, fileName);
	}
	
	private class DownloadImageHandler extends Handler {

		private Listener mListener;
		private WeakReference<Context> ct;
		private Point mExpectedSize;
		private String path;

		public DownloadImageHandler(Listener listener, Context ct, Point size, String path) {

			mListener = listener;
			this.ct = new WeakReference<Context>(ct);
			this.mExpectedSize = size;
			this.path = path;
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case HttpConnection.DID_START:
//				debugLog("download start");
				mListener.startLoad(FROM_NETWORK);
				break;

			case HttpConnection.DID_SUCCEED:
				debugLog("download finish " + (String) msg.obj);
				Context c = ct.get();
				if (c != null)
					onDownloadSucceed(path, (String) msg.obj, mListener, mExpectedSize, ct.get());
				break;

			case HttpConnection.DID_ERROR:
				debugLog("download fail");
				mListener.loadFail((Exception) msg.obj);
				break;
			}
		}
	}
	
	private void onDownloadSucceed(String path, String fileName, 
			Listener listener, Point expectedSize, Context ct) {
		
		// Determine bitmap size
		Options opt = new Options();
		opt.inScaled = false;
		opt.inJustDecodeBounds = true;
		try {
			String cachePath = ct.getCacheDir().getAbsolutePath();
			FileInputStream in = new FileInputStream(cachePath + "/" + GlobalVariable.IMAGE_FILE_PATH + fileName);
			BitmapFactory.decodeStream(in, null, opt);
			in.close();
		} catch (Exception e) {
			listener.loadFail(e);
			return;
		}
		
		if (opt.outWidth == -1 || opt.outHeight == -1) {
			listener.loadFail(new Exception("Cannot decode bitmap " + fileName));
			return;
		}
			
		// cache on disk
		BitmapInfo bmInf = new BitmapInfo();
		bmInf.w = opt.outWidth;
		bmInf.h = opt.outHeight;
		bmInf.hash = fileName;
		cacheDisk(path, bmInf);
		
		// Load optimize
		int sample = calcSample(bmInf, expectedSize);
		Bitmap bm = null;
		try {
			bm = loadOptimize(bmInf, sample, ct);
		} catch (Exception e) {
			listener.loadFail(new Exception("Cannot decode bitmap " + fileName));
			return;
		}
		
		// cache on memory
		cacheMem(bm, path, sample);
		
		// finish
		listener.loadFinish(FROM_NETWORK, bm);
	}
	
	private void cacheDisk(String path, BitmapInfo bmInf) {
		
//		debugLog("cacheDisk " + path);
		mDiskCacheMap.put(path, bmInf);
	}
	
	private int calcSample(BitmapInfo bmInf, Point expectedSize) {
		
		int w = expectedSize.x;
		int h = expectedSize.y;
		int bmW = bmInf.w;
		int bmH = bmInf.h;
		int scale = 1;
		
		while (bmW / 2 > h && bmH / 2 > w) {
			scale = scale << 1;
			bmW = bmW >> 1;
			bmH = bmH >> 1;
		}

		return scale;
	}
	
	public static String generateRandomImageFileName(Context ct) {
		
		Random random = new Random(System.currentTimeMillis());
		String name = null;
		File f = null;
		do {
			// generate file name
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < GlobalVariable.IMAGE_FILE_NAME_LENGTH; i++) {
				builder.append((char) ('a' + random.nextInt('z' - 'a')));
			}
			
			// check existence
			name = builder.toString();
			f = ct.getFileStreamPath(name);
		} while (f.isFile());
		
		return name;
	}

	///////////////////////////////////////////////////////////////////////////
	// Inner class
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		
		private static final int LOAD_FINISH = 0;
		private static final int LOAD_FAIL	 = 1;
		
		private Handler mHandler;
		
		public void startLoad(int from) { }
		public void loadFinish(int from, Bitmap image) { }
		public void loadFail(Exception e) { }
		
		// Must be called from caller thread
		public final void createHandler() {
			
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
				
					switch (msg.what) {
					case LOAD_FINISH:
						loadFinish(msg.arg1, (Bitmap) msg.obj);
						break;
						
					case LOAD_FAIL:
						loadFail((Exception) msg.obj);
						break;
					}
				}
			};
		}
		
		public final void loadFinishAsync(int from, Bitmap image) {
			
			mHandler.sendMessage(mHandler.obtainMessage(LOAD_FINISH, from, 0, image));
		}
		
		public final void loadFailAsync(Exception e) {
			
			mHandler.sendMessage(mHandler.obtainMessage(LOAD_FAIL, e));
		}
	}
	
	private static class BitmapInfo {
		
		public String hash;
		public int h, w;
	}
	
	private class Downloader implements Runnable {
		
		public String mPath; 
		public List<Listener> mListenerList = new ArrayList<Listener>();
		
		public synchronized void addListener(Listener l) {
			
			mListenerList.add(l);
		}
		
		public boolean isSamePath(String path) {
			
			return mPath.equals(path);
		}
		
		@Override
		public void run() {

			// Download image from network
			HttpClient httpClient = new DefaultHttpClient();
			HttpParams params = httpClient.getParams();
	    	HttpConnectionParams.setSoTimeout(params, 10000);
	    	HttpConnectionParams.setConnectionTimeout(params, 10000);
	    	
	    	try {
				HttpResponse response = httpClient.execute(new HttpGet(mPath));
				StringBuilder fileName = new StringBuilder();
				String fullFilePath = saveToDisk(response.getEntity(), fileName);
				
				// Determine bitmap size
				Options opt = new Options();
				opt.inScaled = false;
				opt.inJustDecodeBounds = true;
				
				FileInputStream in = new FileInputStream(fullFilePath);
				BitmapFactory.decodeStream(in, null, opt);
				in.close();
				
				// cache on disk
				BitmapInfo bmInf = new BitmapInfo();
				bmInf.w = opt.outWidth;
				bmInf.h = opt.outHeight;
				bmInf.hash = fileName.toString();
				cacheDisk(mPath, bmInf);
								
			} catch (Exception e) {
				
				// Deliver exception to listener
				for (Listener l : mListenerList)
					l.loadFailAsync(e);
			}
		}
		
		private String saveToDisk(HttpEntity entity, StringBuilder outFileName) throws IOException {
	    	
	    	InputStream in = entity.getContent();	
	    	String cachePath = ct.getCacheDir().getAbsolutePath();
	    	File imageCacheFolder = new File(cachePath + "/" + GlobalVariable.IMAGE_FILE_PATH);
	    	imageCacheFolder.mkdirs();
	    	String fileName = generateRandomImageFileName(ct);
	    	String fullPath = cachePath + "/" + GlobalVariable.IMAGE_FILE_PATH + fileName;
	    	FileOutputStream out = new FileOutputStream(fullPath);
	    	
	    	byte[] buffer = new byte[1024 * 128];
	    	int justRead = 0;
	    	int hasRead = 0;
	    	while ((justRead = in.read(buffer)) != -1) {
	    		out.write(buffer, 0, justRead);
	    		hasRead += justRead;
//	    		handler.sendMessage(Message.obtain(handler, DID_STATUS, hasRead, (int) entity.getContentLength()));
	    	}
	    	
	    	in.close();
	    	out.close();
	    	
	    	outFileName.append(fileName);
	    	return fullPath;
	    }
	}
	
	private class DownloaderPool extends ThreadPoolExecutor {
		
		private BlockingQueue<Runnable> mQueue;
		private List<Runnable> mActive;

		public DownloaderPool() {
			
			super(CONNECTION_NUM, CONNECTION_NUM, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			mQueue = getQueue();
			mActive = new LinkedList<Runnable>();
		}
		
		public Downloader checkPending(String path) {
			
			for (Runnable r : mActive) {
				Downloader downloader = (Downloader) r;
				if (downloader.isSamePath(path))
					return downloader;
			}
			
			for (Runnable r : mQueue) {
				Downloader downloader = (Downloader) r;
				if (downloader.isSamePath(path))
					return downloader;
			}
			
			return null;
		}
		
		@Override
		protected void beforeExecute(Thread t, Runnable r) {
		
			super.beforeExecute(t, r);
			mActive.add(r);
		}
		
		@Override
		protected void afterExecute(Runnable r, Throwable t) {
		
			super.afterExecute(r, t);
			mActive.remove(r);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	
	private static final boolean isDebug = false;
	private static final String TAG = "CycrixDebug";
	private static final String HEADER = "ImageLoader";
	private static void debugLog(String message) {
		if (isDebug) Log.d(TAG, HEADER + " " + message);
	}
}