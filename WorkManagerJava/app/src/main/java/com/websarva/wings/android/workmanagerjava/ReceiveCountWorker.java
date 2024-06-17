package com.websarva.wings.android.workmanagerjava;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReceiveCountWorker extends Worker {
	public ReceiveCountWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		long failureCount = Math.round(Math.random() * 20);
		long retryCount = Math.round(Math.random() * 20);
		Data inputData = getInputData();
		String loopMsg = inputData.getString("loopMsg");
		int loopCount = inputData.getInt("loopCount", 30);
		Result returnVal = Result.success();
		Log.i("ReceiveCountWorker", "メッセージ「" + loopMsg + "」でループを" + loopCount + "回行います。");
		Log.i("ReceiveCountWorker", "失敗と判定する数値: " + failureCount);
		Log.i("ReceiveCountWorker", "リトライと判定する数値: " + retryCount);
		for(int i = 1; i <= loopCount; i++) {
			Log.i("ReceiveCountWorker", loopMsg + ": " + i + "回目");
			if(i == failureCount) {
				returnVal = Result.failure();
				break;
			}
			else if(i == retryCount) {
				returnVal = Result.retry();
				break;
			}
			else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					returnVal = Result.failure();
				}
			}
		}
		return returnVal;
	}
}
