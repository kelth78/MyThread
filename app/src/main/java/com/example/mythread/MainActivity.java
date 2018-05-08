package com.example.mythread;

import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//
// 2 ways to create thread. Either declare class to be sub class of Thread or declare class that
// implements Runnable interface
//
// Use ThreadpoolExecutor to run multiple tasks at the same time
//
public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private MyThread mMyThread = new MyThread();            // Method 1: subclass Thread
    private MyRunnableThread mMyRunnableThread = new MyRunnableThread(); // Method 2: runnable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        mMyThread.start();
        new Thread(mMyRunnableThread /*runnable target*/).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        mMyThread.start();
        new Thread(mMyRunnableThread /*runnable target*/).start();
        new Thread(MyRunnableThreadWithinClass).start();
    }

    private Runnable MyRunnableThreadWithinClass = new Runnable() {
        @Override
        public void run() {
            //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

            for (int j = 0 ; j < 2000 ; j++) {
                Log.d("Main", "Running MyRunnableThreadWithinClass " + j);
            }
        }
    };


    /*
         * Gets the number of available cores
         * (not always the same as the maximum number of cores)
         */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1000;

    // Sets the Time Unit to Milliseconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;

    // Used to update UI with work progress
    private int count = 0;

    // This is the runnable task that we will run 100 times
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mRunnable: " + mRunnable);
            // Do some work that takes 100 milliseconds
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Update the UI with progress. Need to call this cos only original thread that
            // created the view can touch it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    count++;
                    String msg = count < 100 ? "working " : "done ";
                    updateStatus(msg + count);
                    Log.d(TAG,"" + count);
                }
            });
        }
    };


    // button click - performs work on a single thread
    public void buttonClickSingleThread(View view) {
        count = 0;
        Executor mSingleThreadExecutor = Executors.newSingleThreadExecutor();

        for (int i = 0; i < 100; i++) {
            mSingleThreadExecutor.execute(mRunnable);
        }
    }

    // button click - performs work using a thread pool
    public void buttonClickThreadPool(View view) {
        count = 0;
        ThreadPoolExecutor mThreadPoolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES + 5,   // Initial pool size
                NUMBER_OF_CORES + 8,   // Max pool size
                KEEP_ALIVE_TIME,       // Time idle thread waits before terminating
                KEEP_ALIVE_TIME_UNIT,  // Sets the Time Unit for KEEP_ALIVE_TIME
                new LinkedBlockingDeque<Runnable>());  // Work Queue

        for (int i = 0; i < 100; i++) {
            mThreadPoolExecutor.execute(mRunnable);
        }
    }

    private void updateStatus(String msg) {
        ((TextView) findViewById(R.id.text)).setText(msg);
    }
}

class MyThread extends Thread {

    @Override
    public void run() {
        super.run();
        for (int i = 0 ; i < 2000 ; i++) {
            Log.d("Main", "Running MyThread " + i);
        }
    }
}

// Method 2
class MyRunnableThread implements Runnable {
    @Override
    public void run() {
        // Optional, or use THREAD_PRIORITY_DEFAULT
        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

        for (int j = 0 ; j < 2000 ; j++) {
            Log.d("Main", "Running MyRunnableThread " + j);
        }
    }
}

