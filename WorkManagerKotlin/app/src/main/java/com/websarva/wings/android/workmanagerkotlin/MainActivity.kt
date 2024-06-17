package com.websarva.wings.android.workmanagerkotlin

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.websarva.wings.android.workmanagerkotlin.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
	private lateinit var _activityMainBinding: ActivityMainBinding
	private var _workInfoLiveData: LiveData<WorkInfo> = MutableLiveData()
	private val _workInfoLiveDataObserver = WorkInfoLiveDataObserver()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		_activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(_activityMainBinding.root)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

		_activityMainBinding.btStartPassdata.setOnClickListener(StartPassdataButtonClickListener())
		_activityMainBinding.btStartRetry.setOnClickListener(StartRetryButtonClickListener())
		_activityMainBinding.btStartUniqueworkKeep.setOnClickListener(StartUniqueworkKeepButtonClickListener())
		_activityMainBinding.btStartUniqueworkReplace.setOnClickListener(StartUniqueworkReplaceButtonClickListener())
		_activityMainBinding.btStartUniqueworkAppend.setOnClickListener(StartUniqueworkAppendButtonClickListener())
		_activityMainBinding.btWorkStart.setOnClickListener(StartWorkButtonClickListener())
		_activityMainBinding.btWorkCancel.setOnClickListener(CancelWorkButtonClickListener())
	}

	private inner class StartPassdataButtonClickListener : View.OnClickListener {
		override fun onClick(view: View?) {
			val loopCount = (1..30).random()
			val dataBuilder = Data.Builder()
			dataBuilder.putString("loopMsg", "こんにちは")
			dataBuilder.putInt("loopCount", loopCount)
			val data = dataBuilder.build()
			val workRequestBuilder = OneTimeWorkRequestBuilder<ReceiveCountWorker>()
			workRequestBuilder.setInputData(data)
			val workRequest = workRequestBuilder.build()
			val workManager = WorkManager.getInstance(this@MainActivity)
			workManager.enqueue(workRequest)
		}
	}

	private inner class StartRetryButtonClickListener : View.OnClickListener {
		override fun onClick(view: View?) {
			val workRequestBuilder = OneTimeWorkRequestBuilder<RetryWorker>()
			workRequestBuilder.setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
			val workRequest = workRequestBuilder.build()
			val workManager = WorkManager.getInstance(this@MainActivity)
			workManager.enqueue(workRequest)
		}
	}

	private inner class StartUniqueworkKeepButtonClickListener : View.OnClickListener {
		override fun onClick(view: View?) {
			val workRequestBuilder = OneTimeWorkRequestBuilder<RetryWorker>()
			val workRequest = workRequestBuilder.build()
			val workManager = WorkManager.getInstance(this@MainActivity)
			workManager.enqueueUniqueWork("UniqueKeepRetry", ExistingWorkPolicy.KEEP, workRequest)
		}
	}

	private inner class StartUniqueworkReplaceButtonClickListener : View.OnClickListener {
		override fun onClick(view: View?) {
			val workRequestBuilder = OneTimeWorkRequestBuilder<RetryWorker>()
			val workRequest = workRequestBuilder.build()
			val workManager = WorkManager.getInstance(this@MainActivity)
			workManager.enqueueUniqueWork("UniqueReplaceRetry", ExistingWorkPolicy.REPLACE, workRequest)
		}
	}

	private inner class StartUniqueworkAppendButtonClickListener : View.OnClickListener {
		override fun onClick(view: View?) {
			val workRequestBuilder = OneTimeWorkRequestBuilder<RetryWorker>()
			val workRequest = workRequestBuilder.build()
			val workManager = WorkManager.getInstance(this@MainActivity)
			workManager.enqueueUniqueWork("UniqueAppendRetry", ExistingWorkPolicy.APPEND, workRequest)
		}
	}

	private inner class StartWorkButtonClickListener : View.OnClickListener {
		override fun onClick(view: View?) {
			val workRequestBuilder = OneTimeWorkRequestBuilder<RetryWorker>()
			val workRequest = workRequestBuilder.build()
			val workManager = WorkManager.getInstance(this@MainActivity)
			workManager.enqueueUniqueWork("ObserveWorkInfo", ExistingWorkPolicy.KEEP, workRequest)

			_workInfoLiveData.removeObserver(_workInfoLiveDataObserver)
			_workInfoLiveData = workManager.getWorkInfoByIdLiveData(workRequest.id)
			_workInfoLiveData.observe(this@MainActivity, _workInfoLiveDataObserver)
		}
	}

	private inner class CancelWorkButtonClickListener : View.OnClickListener {
		override fun onClick(view: View?) {
			val workManager = WorkManager.getInstance(this@MainActivity)
			workManager.cancelUniqueWork("ObserveWorkInfo")
		}
	}

	private inner class WorkInfoLiveDataObserver : Observer<WorkInfo> {
		override fun onChanged(workInfo: WorkInfo) {
			if(workInfo.state == WorkInfo.State.ENQUEUED) {
				_activityMainBinding.btWorkStart.isEnabled = false
				_activityMainBinding.btWorkStart.setText(R.string.bt_start_work_enqueued)
				_activityMainBinding.btWorkCancel.isEnabled = true
				_activityMainBinding.btWorkCancel.setText(R.string.bt_start_work_cancel)
			}
			else if(workInfo.state == WorkInfo.State.RUNNING) {
				_activityMainBinding.btWorkStart.isEnabled = false
				_activityMainBinding.btWorkStart.setText(R.string.bt_start_work_running)
				_activityMainBinding.btWorkCancel.isEnabled = true
				_activityMainBinding.btWorkCancel.setText(R.string.bt_start_work_cancel)
			}
			else if(workInfo.state == WorkInfo.State.SUCCEEDED) {
				_activityMainBinding.btWorkStart.isEnabled = true
				_activityMainBinding.btWorkStart.setText(R.string.bt_start_work_start)
				_activityMainBinding.btWorkCancel.isEnabled = false
				_activityMainBinding.btWorkCancel.setText(R.string.bt_start_work_finished)
			}
			else if(workInfo.state == WorkInfo.State.CANCELLED) {
				_activityMainBinding.btWorkStart.isEnabled = true
				_activityMainBinding.btWorkStart.setText(R.string.bt_start_work_start)
				_activityMainBinding.btWorkCancel.isEnabled = false
				_activityMainBinding.btWorkCancel.setText(R.string.bt_start_work_canceled)
			}
		}
	}
}
