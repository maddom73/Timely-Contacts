/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuxmind.timelyfcontacts.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.app.FragmentManager;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

import com.tuxmind.timelyfcontacts.BuildConfig;


public abstract class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    private int mImageSize;
    private Resources mResources;

    protected ImageLoader(Context context, int imageSize) {
        mResources = context.getResources();
        mImageSize = imageSize;
    }

    public int getImageSize() {
        return mImageSize;
    }


    public void loadImage(Object data, ImageView imageView) {
        if (data == null) {
            imageView.setImageBitmap(mLoadingBitmap);
            return;
        }

        Bitmap bitmap = null;

        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(data);
        }
    }


    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    public void addImageCache(FragmentManager fragmentManager, float memCacheSizePercent) {
        mImageCache = ImageCache.getInstance(fragmentManager, memCacheSizePercent);
    }

    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    protected abstract Bitmap processBitmap(Object data);

    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            if (BuildConfig.DEBUG) {
                final Object bitmapData = bitmapWorkerTask.data;
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
            }
        }
    }

    public static boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + data);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

  
    private class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
        private Object data;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }

            data = params[0];
            final String dataString = String.valueOf(data);
            Bitmap bitmap = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            if (!isCancelled() && getAttachedImageView() != null) {
                bitmap = processBitmap(params[0]);
            }


            if (bitmap != null && mImageCache != null) {
                mImageCache.addBitmapToCache(dataString, bitmap);
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work");
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            final ImageView imageView = getAttachedImageView();
            if (bitmap != null && imageView != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostExecute - setting bitmap");
                }
                setImageBitmap(imageView, bitmap);
            }
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

 
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }


    @SuppressWarnings("deprecation")
	private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (mFadeInBitmap) {
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(android.R.color.transparent),
                            new BitmapDrawable(mResources, bitmap)
                    });
            imageView.setBackgroundDrawable(imageView.getDrawable());
            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            final float totalPixels = width * height;

            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
}
