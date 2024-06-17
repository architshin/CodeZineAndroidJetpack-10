package com.websarva.wings.android.workmanagerjava;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RetryWorker extends Worker {
	public RetryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		long retryOrNot = Math.round(Math.random());
		Result returnVal = Result.success();
		String retryMsg = "成功";
		if(retryOrNot == 0) {
			retryMsg = "リトライ";
			returnVal = Result.retry();
		}
		Log.i("RetryWorker" + getId(), "ループ開始: ");
		for(int i = 1; i <= 5; i++) {
			Log.i("RetryWorker" + getId(), "ループ" + i + "回目");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				retryMsg = "失敗";
				returnVal = Result.failure();
			}
		}
		Log.i("RetryWorker" + getId(), "ループ終了");
		Log.i("RetryWorker" + getId(), "ステータスは" + retryMsg);
		return returnVal;
	}

	@Override
	public void onStopped() {
		Log.i("RetryWorker" + getId(), "ワーカーがキャンセルされました。");
	}
}
