package edu.iastate.cs309.studybuddy.meta.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by Tyler on 2/9/2015.
 */
public class VolleyUtil {
    private static VolleyUtil mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    /**
     * Private constructor so a developer can't
     * instantiate an extra VolleyUtil class.
     * @param context - Application context.
     */
    private VolleyUtil(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    /**
     * Lazily instantiates Volley singleton class and returns it.
     * @param context - The application's context info.
     * @return an instance of this class.
     */
    public static synchronized VolleyUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyUtil(context);
        }
        return mInstance;
    }

    /**
     * Grabs the global network request queue.
     * @return the request queue.
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * This adds any Volley.Request subclass to be threaded and
     * scheduled by Volley.
     * @param req - The request for Volley to thread.
     * @param <T> Any subclass of the Volley.Request class.
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /**
     * @return global ImageLoader.
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}