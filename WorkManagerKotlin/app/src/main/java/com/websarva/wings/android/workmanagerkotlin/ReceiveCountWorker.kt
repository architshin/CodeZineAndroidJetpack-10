package com.websarva.wings.android.workmanagerkotlin

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReceiveCountWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
	override fun doWork(): Result {
		val failureCount = (1..20).random()
		val retryCount = (1..20).random()
		val loopMsg = inputData.getString("loopMsg")
		val loopCount = inputData.getInt("loopCount", 30)
		var returnVal = Result.success()
		Log.i("ReceiveCountWorker","メッセージ「${loopMsg}」でループを${loopCount}回行います。")
		Log.i("ReceiveCountWorker","失敗と判定する数値: ${failureCount}")
		Log.i("ReceiveCountWorker","リトライと判定する数値: ${retryCount}")
		for(i in 1..loopCount) {
			Log.i("ReceiveCountWorker", "${loopMsg}: ${i}回目")
			if(i == failureCount) {
				returnVal = Result.failure()
				break
			}
			else if(i == retryCount) {
				returnVal = Result.retry()
				break
			}
			else {
				Thread.sleep(1000)
			}
		}
		return returnVal
	}
}
