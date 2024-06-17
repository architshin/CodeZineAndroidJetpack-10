package com.websarva.wings.android.workmanagerkotlin

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class RetryWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
	override fun doWork(): Result {
		val retryOrNot = (1..10).random()
		var returnVal = Result.success()
		var retryMsg = "成功"
		if(retryOrNot <= 5) {
			retryMsg = "リトライ"
			returnVal = Result.retry()
		}
		Log.i("RetryWorker${id}","ループ開始")
		for(i in 1..5) {
			Log.i("RetryWorker${id}", "ループ${i}回目")
			Thread.sleep(1000)
		}
		Log.i("RetryWorker${id}","ループ終了")
		Log.i("RetryWorker${id}","ステータスは${retryMsg}")
		return returnVal
	}

	override fun onStopped() {
		Log.i("RetryWorker${id}","ワーカーがキャンセルされました。")
	}
}
