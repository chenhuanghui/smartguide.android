package vn.infory.infory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.NetworkManager;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
//import vn.smartguide2.network.CyAsyncTask;

/**
 * 
 * @author CYCRIXLAPTOP-PC
 * 	Disk cache
 *  Memory cache
 *  Resample image 
 */
public class CyImageLoader {

	public static int MAX_MEM_CACHE 				= 10 * 1024 * 1024;
	public static final int IMAGE_FILE_NAME_LENGTH  = 20;
	public static final int SAMPLE_LENGTH 			= 3;
	public static final String IMAGE_FILE_PATH 		= "imageCache/";

	public static String[] DUMMY_PATH = new String[] {
		"http://vtcdn.com/sites/default/files/images/2010/10/23/img-1287809436-1.jpg",
		"http://d.f1.photo.zdn.vn/upload/original/2010/03/24/12/12694081522091761785_574_574.jpg",
		"http://farm5.staticflickr.com/4146/5050228406_4ff6e56931.jpg",
		"http://farm3.static.flickr.com/2755/4136613545_53203632c5_o.jpg",
		"http://d.f5.photo.zdn.vn/upload/original/2011/04/13/20/23/13027009861988334791_574_0.jpg",
		"http://farm4.staticflickr.com/3091/5859306523_3017a73158_z.jpg",
		"http://img2.news.zing.vn/2012/10/28/b6.jpg",
		"http://root.vn/up/share-full-code-zing.vn.png",
		"http://img2.news.zing.vn/2012/12/10/anh-41.jpg",
		"http://img.news.zing.vn/img/343/t343646.jpg?c=86",
		"http://d.f7.photo.zdn.vn/upload/original/2011/12/08/10/40/1323315634539339526_574_0.jpg",
		"http://vtcdn.com/sites/default/files/images/2010/6/5/zingvn.jpg",
		"http://img168.imageshack.us/img168/2307/253312458314781.jpg",
		"http://d.f1.photo.zdn.vn/upload/original/2009/11/14/10/1258167636163589776_574_574.jpg",
		"http://d.f6.photo.zdn.vn/upload/original/2011/10/05/2/39/13177571641903604649_574_0.jpg",
		"http://media.thethaovanhoa.vn/2012/05/26/06/26/Vy-Oanh-Custom.jpg",
		"http://www.myfreecoursesonline.com/wp-content/uploads/2013/09/Zing.jpg",
		"http://picture.playpark.vn/AS/ImgUp/323201223_3_2012_zing5.jpg",
		"http://img2.news.zing.vn/2012/10/24/ho-khanh3.jpg",
		"http://files.myopera.com/nguoicodoccuatheky/blog/shakira-orig.jpg"
	};

	public static final int FROM_MEMORY	= 0;
	public static final int FROM_DISK	= 1;
	public static final int FROM_NETWORK= 2;

	// Singletance
	private static CyImageLoader instance;

	// Task queue
	private TaskQueue mTaskQueue = new TaskQueue();

	// <Sample+Path, Bitmap>
	private MemCache mMemCacheMap;

	// <Path, hash>
	private DiskCache mDiskCacheMap = new DiskCache();

	private Context ct;
	private CyLogger mLoger = new CyLogger("CyImageLoader", true);

	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////

	private CyImageLoader(Context ct) {
		this.ct = ct;
		ActivityManager activityManager = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
		MAX_MEM_CACHE = activityManager.getLargeMemoryClass() / 2 * 1024 * 1024;
		debugLog("Initial cache " + (MAX_MEM_CACHE / 1024 / 1024) + " MB");
		mMemCacheMap = new MemCache();
		clearCache(ct);
	}
	
	public static void initInstance(Context ct) {
		if (instance == null)
			instance = new CyImageLoader(ct); 
	}

	public static void release() {
		if (instance != null) {
			instance.ct = null;
			instance = null;
		}
	}
	
	public static CyImageLoader instance() {
		return instance;
	}

	public CyAsyncTask showImage(String path, ImageView imgView) {
		return showImage(path, imgView, new Point(imgView.getWidth(), imgView.getHeight()));
	}
	
	public void showImageListView(String path, final ImageView imgView, 
			Point expectedSize, final List<CyAsyncTask> taskList) {
		imgView.setTag(path);
		CyAsyncTask task = loadImage(path, new CyImageLoader.Listener() {
			@Override
			public void startLoad(int from) {
				switch (from) {
				case CyImageLoader.FROM_DISK:
				case CyImageLoader.FROM_NETWORK:
					imgView.setImageBitmap(null);
					break;
				}
			}

			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				taskList.remove(task);
				if (imgView.getTag().equals(url))
					imgView.setImageBitmap(image);
			}
			
			@Override
			public void loadFail(Exception e, CyAsyncTask task) {
				taskList.remove(task);
			}
		}, new Point(), imgView.getContext());
		
		if (task != null)
			taskList.add(task);
	}
	
	public CyAsyncTask showImageSmooth(String path, View view, Point expectedSize, 
			final List<CyAsyncTask> taskList) {

		CyAsyncTask task = loadImage(path, new Listener() {

			private View view;

			public Listener init(View img) {
				view = img;
				return this;
			}

			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				taskList.remove(task);
				
				switch (from) {
				case CyImageLoader.FROM_MEMORY:
				case CyImageLoader.FROM_DISK:
					view.setBackgroundDrawable(new BitmapDrawable(image));
					break;
					
				case CyImageLoader.FROM_NETWORK:
					TransitionDrawable trans = new TransitionDrawable(new Drawable[] {
							new ColorDrawable(0),
							new BitmapDrawable(image)
					});
					
					view.setBackgroundDrawable(trans);
					trans.startTransition(300);
					break;
				}
			}
			
			@Override
			public void loadFail(Exception e, CyAsyncTask task) {
				taskList.remove(task);
			};
			
		}.init(view), expectedSize, view.getContext());
		
		if (task != null)
			taskList.add(task);
		
		return task;
	}
	
	public CyAsyncTask showImageSmooth(String path, View view, Point expectedSize) {

		return loadImage(path, new Listener() {

			private View view;

			public Listener init(View img) {
				view = img;
				return this;
			}

			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				switch (from) {
				case CyImageLoader.FROM_MEMORY:
				case CyImageLoader.FROM_DISK:
					view.setBackgroundDrawable(new BitmapDrawable(image));
					break;
					
				case CyImageLoader.FROM_NETWORK:
					TransitionDrawable trans = new TransitionDrawable(new Drawable[] {
							new ColorDrawable(0),
							new BitmapDrawable(image)
					});
					
					view.setBackgroundDrawable(trans);
					trans.startTransition(300);
					break;
				}
			}
		}.init(view), expectedSize, view.getContext());
	}
	
	public CyAsyncTask showImage(String path, ImageView imgView, Point expectedSize) {

		return loadImage(path, new Listener() {

			private ImageView imgView;

			public Listener init(ImageView img) {
				imgView = img;
				return this;
			}

			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				imgView.setImageBitmap(image);
			}
		}.init(imgView), expectedSize, imgView.getContext());
	}

	public CyAsyncTask loadImage(String path, Listener listener, Point expectedSize, Context ct) {

		//		debugLog("Incoming request: " + right(path));
		if (path == null)
			return null;
		
		CyAsyncTask task = null;

		listener.mExpectedSize = expectedSize;

		// Check mem cache
		Bitmap bm = mMemCacheMap.checkMemCache(path, expectedSize);

		// If hit mem cache, deliver bitmap to listener
		if (bm != null) {
			listener.startLoad(FROM_MEMORY);
			listener.loadFinish(FROM_MEMORY, bm, path, null);
			return task;
		}

		// Check disk cache
		BitmapInfo bmInf = mDiskCacheMap.checkDiskCache(path);

		// If hit disk cache, check task queue
		if (bmInf != null) {
			int sample = calcSample(bmInf, expectedSize);
			listener.startLoad(FROM_DISK);
			Task t = mTaskQueue.checkDecoding(path, sample);

			if (t != null) {
				// If decoding, add listener
				debugLog("Decoding, add listener to " + right(path));
				t.addListener(listener);
			} else {
				// Add new decode task
				debugLog("Add new decoding task " + right(path));
				TaskDecode decode = new TaskDecode(path, bmInf, sample, ct);
				decode.addListener(listener);
				mTaskQueue.addTask(decode);
				t = decode;
			}
			return t;
		}

		// if miss disk cache, check download task
		listener.startLoad(FROM_NETWORK);
		Task t = mTaskQueue.checkDownloading(path);
		if (t != null) {
			// if downloading, add new listener
			debugLog("Downloading, add new listener " + right(path));
			t.addListener(listener);
		} else {
			// Otherwise add new task
			debugLog("Add new downloading task " + right(path));
			TaskDownload download = new TaskDownload(path, ct);
			download.addListener(listener);
			mTaskQueue.addTask(download);
			t = download;
		}
		
		return t;
	}

	public void clearCache(Context ct) {
		File cacheDir = ct.getCacheDir();
		for (File f : cacheDir.listFiles())
			f.delete();
	}

	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////	

	private Bitmap loadOptimize(BitmapInfo bmInf, int sample) throws Exception {

		//		ActivityManager activityManager =  (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
		//		MemoryInfo memoryInfo = new MemoryInfo();
		//		activityManager.getMemoryInfo(memoryInfo);
		//		debugLog("" + memoryInfo.availMem / 1024 + "/" + activityManager.getMemoryClass() + " MB");

		Options opt = new Options();
		opt.inScaled = false;
		opt.inSampleSize = sample;
		String cachePath = ct.getCacheDir().getAbsolutePath();
		FileInputStream in = new FileInputStream(cachePath + "/" + IMAGE_FILE_PATH + bmInf.hash);
		Bitmap bm = BitmapFactory.decodeStream(in, null, opt);
		if (bm == null)
			throw new Exception("Cannot decode bitmap " + bmInf.hash);
		return bm;
	}

	private int calcSample(BitmapInfo bmInf, Point expectedSize) {

		if (expectedSize.x == 0 || expectedSize.y == 0)
			return 1;

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
			for (int i = 0; i < IMAGE_FILE_NAME_LENGTH; i++) {
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

	private static class TaskQueue extends ArrayList<Task> {

		public void addTask(Task t) {
			t.executeOnExecutor(NetworkManager.THREAD_POOL);
			add(t);
		}

		public void removeTask(Task t) {
			remove(t);
		}

		public Task checkDecoding(String path, int sample) {
			for (Task t : this)
				if (t.mType == Task.TYPE_DECODE && t.mPath.equals(path))
					if (((TaskDecode) t).mSample == sample)
						return t;
			return null;
		}

		public Task checkDownloading(String path) {
			for (Task t : this)
				if (t.mType == Task.TYPE_DOWNLOAD && t.mPath.equals(path))
					return t;
			return null;
		}
	}

	private class MemCache extends LruCache<String, Bitmap> {

		public MemCache() {
			super(MAX_MEM_CACHE);
		}

		@Override
		protected int sizeOf(String key, Bitmap value) {
			//			return value.getByteCount();
			
			return value.getRowBytes() * value.getHeight();
		}

		public Bitmap checkMemCache(String path, Point expectedSize) {
			BitmapInfo bmInf = mDiskCacheMap.get(path);
			if (bmInf == null) {
				return null;
			}

			int expectedSample = calcSample(bmInf, expectedSize);
			Bitmap bm = get(String.format("%03d", expectedSample) + path);
			if (bm == null) {
				//				debugLog("checkMemCache miss");
			} else {
				debugLog("checkMemCache hit");
			}
			return bm;
		}

		public void cacheMem(Bitmap bm, String path, int sample) {
			this.size();
			put(String.format("%03d", sample) + path, bm);
			debugLog("Memcache size = " + size() / 1024 + " KB");
			//			debugLog("cacheMem " + path);
		}
	}

	private static class DiskCache extends HashMap<String, BitmapInfo> {

		public BitmapInfo checkDiskCache(String path) {
			BitmapInfo bmInf = get(path);
			if (bmInf == null) {
				//				debugLog("checkDiskCache miss");
			} else {
				debugLog("checkDiskCache hit");
			}
			return bmInf;
		}

		public void cacheDisk(String path, BitmapInfo bmInf) {
			//			debugLog("cacheDisk " + path);
			put(path, bmInf);
		}
	}

	private abstract class Task extends CyAsyncTask {

		public static final int TYPE_DECODE 	= 1;
		public static final int TYPE_DOWNLOAD 	= 2;

		public int mType;
		public String mPath;
		public List<Listener> mListenerList = new ArrayList<Listener>();

		public Task(String path, Context ct) {
			super(ct);
			mPath = path;
		}

		public synchronized void addListener(Listener listener) {
			mListenerList.add(listener);
		}

		public synchronized List<Listener> cloneListenerList() {
			return new ArrayList<Listener>(mListenerList);
		}
	}

	private class TaskDownload extends Task {

		public ArrayList<Integer> sampleSet;
		public BitmapInfo bmInf;
		public Exception mEx;

		public TaskDownload(String path, Context ct) {
			super(path, ct);
			mType = TYPE_DOWNLOAD;
			mPath = path;
		}

		@Override
		protected Object doInBackground(Object... shit) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpParams params = httpClient.getParams();
				HttpConnectionParams.setSoTimeout(params, 10000);
				HttpConnectionParams.setConnectionTimeout(params, 10000);

				// Download
				HttpResponse response = httpClient.execute(new HttpGet(mPath));
				String fileName = generateRandomImageFileName(ct);
				processGetFile(response.getEntity(), fileName);

				// Determine bitmap size
				Options opt = new Options();
				opt.inScaled = false;
				opt.inJustDecodeBounds = true;

				String cachePath = ct.getCacheDir().getAbsolutePath();
				FileInputStream in = new FileInputStream(cachePath + "/" + 
						IMAGE_FILE_PATH + fileName);
				BitmapFactory.decodeStream(in, null, opt);
				in.close();

				if (opt.outWidth == -1 || opt.outHeight == -1)
					throw new Exception("Cannot decode bitmap " + fileName);

				// Create bitmpa info
				bmInf = new BitmapInfo();
				bmInf.w = opt.outWidth;
				bmInf.h = opt.outHeight;
				bmInf.hash = fileName;

				//////////////// load optimize ////////////////
				List<Listener> listenerList = null;

				// copy array list
				listenerList = cloneListenerList();

				// get sample set
				sampleSet = new ArrayList<Integer>();
				for (Listener l : listenerList) {
					int sample = calcSample(bmInf, l.mExpectedSize);
					if (!sampleSet.contains(sample))
						sampleSet.add(sample);
				}
				Collections.sort(sampleSet);

				// load image
				Bitmap[] bmArr = new Bitmap[sampleSet.size()];
				for (int i = 0; i < bmArr.length; i++)
					bmArr[i] = loadOptimize(bmInf, sampleSet.get(i));

				super.doInBackground(shit);
				return bmArr;

			} catch (Exception e) {
				mEx = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			
			try{
				if (mEx == null) {
					Bitmap[] bmArr = (Bitmap[]) result;

					// cache disk
					mDiskCacheMap.cacheDisk(mPath, bmInf);

					// cache mem
					for (int i = 0; i < sampleSet.size(); i++)
						mMemCacheMap.cacheMem(bmArr[i], mPath, sampleSet.get(i));

					// deliver to listener
					debugLog("downloading task complete, path=" + right(mPath) + ", sample:");
					for (Listener l : mListenerList) {
						int sample = calcSample(bmInf, l.mExpectedSize);
						int pos = Collections.binarySearch(sampleSet, sample);
						if (pos >= 0) {
							// Everything allright :)
							//						debugLog("sample = " + sample);
							l.loadFinish(FROM_NETWORK, bmArr[pos], mPath, this);
						} else {
							// Oh shit! Some requests come after decoding :(
							debugLog("Sample not found, add new decode task, sample = " + sample);
							TaskDecode decode = new TaskDecode(mPath, bmInf, sample, mContext);
							decode.addListener(l);
							mTaskQueue.addTask(decode);
						}
					}

				} else {
					// deliver
					debugLog("downloading task fail, path=" + right(mPath) + mEx.getMessage());
					for (Listener l : mListenerList)
						l.loadFail(mEx, this);
				}

				mTaskQueue.removeTask(this);
			}catch(Exception ex){

			}
		}

		private void processGetFile(HttpEntity entity, String fileName) throws Exception {
			InputStream in = entity.getContent();    	
			String cachePath = ct.getCacheDir().getAbsolutePath();
			File imageCacheFolder = new File(cachePath + "/" + IMAGE_FILE_PATH);
			imageCacheFolder.mkdirs();
			FileOutputStream out = new FileOutputStream(cachePath + "/" 
					+ IMAGE_FILE_PATH + fileName);

			byte[] buffer = new byte[1024 * 5];
			int justRead = 0;
			int hasRead = 0;
			while ((justRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, justRead);
				hasRead += justRead;
			}
			in.close();
			out.close();
		}
	}

	private class TaskDecode extends Task {

		public BitmapInfo mBmInfo;
		public int mSample;
		public String mPath;
		public Exception mEx;

		public TaskDecode(String path, BitmapInfo bmInfo, int sample, Context ct) {
			super(path, ct);
			this.mBmInfo = bmInfo;
			this.mSample = sample;
			this.mPath = path;
		}

		@Override
		protected Object doInBackground(Object... params) {

			// Load optimize
			Bitmap bm = null;
			try {
				bm = loadOptimize(mBmInfo, mSample);
			} catch (Exception e) {
				mEx = e;
			}

			super.doInBackground(params);
			return bm;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);

			if (mEx == null) {

				// Cache mem
				Bitmap bm = (Bitmap) result;
				mMemCacheMap.cacheMem(bm, mPath, mSample);

				// Deliver to listener
				debugLog("Completed, deliver decoding task listener, path=" + right(mPath));
				for (Listener l : mListenerList) {
					l.loadFinish(FROM_DISK, bm, mPath, this);
				}
			} else {
				// Deliver to listener
				debugLog("Failed, deliver decoding task listener with sample = " + mSample + ", " + mEx.getMessage());
				for (Listener l : mListenerList)
					l.loadFail(mEx, this);
			}

			mBmInfo = null;
			mTaskQueue.removeTask(this);
		}
	}

	public static class Listener {

		public Point mExpectedSize;

		public void startLoad(int from) { }
		public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) { }
		public void loadFail(Exception e, CyAsyncTask task) { }
	}

	private static class BitmapInfo {

		public String hash;
		public int h, w;
	}

	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////

	private static final boolean isDebug = false;
	private static final String TAG = "CycrixDebug";
	private static final String HEADER = "ImageLoader";
	private static final int RIGHT_LEN = 10;
	private static void debugLog(String message) {
		if (isDebug) Log.d(TAG, HEADER + " " + message);
	}
//	private static void debugLogA(String message) {
//		Log.d(TAG, HEADER + " " + message);
//	}
	private static final String right(String s) {
		return "..." + CyUtils.right(s, RIGHT_LEN);
	}
}