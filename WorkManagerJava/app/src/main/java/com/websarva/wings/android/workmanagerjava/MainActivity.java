package com.websarva.wings.android.workmanagerjava;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.websarva.wings.android.workmanagerjava.databinding.ActivityMainBinding;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding _activityMainBinding;
	private LiveData<WorkInfo> _workInfoLiveData = new MutableLiveData<>();
	private final WorkInfoLiveDataObserver _workInfoLiveDataObserver = new WorkInfoLiveDataObserver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		_activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
		View contentView = _activityMainBinding.getRoot();
		setContentView(contentView);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		_activityMainBinding.btStartPassdata.setOnClickListener(new StartPassdataButtonClickListener());
		_activityMainBinding.btStartRetry.setOnClickListener(new StartRetryButtonClickListener());
		_activityMainBinding.btStartUniqueworkKeep.setOnClickListener(new StartUniqueworkKeepButtonClickListener());
		_activityMainBinding.btStartUniqueworkReplace.setOnClickListener(new StartUniqueworkReplaceButtonClickListener());
		_activityMainBinding.btStartUniqueworkAppend.setOnClickListener(new StartUniqueworkAppendButtonClickListener());
		_activityMainBinding.btWorkStart.setOnClickListener(new StartWorkButtonClickListener());
		_activityMainBinding.btWorkCancel.setOnClickListener(new CancelWorkButtonClickListener());
	}

	private class StartPassdataButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			int loopCount = (int) Math.round(Math.random() * 30);
			Data.Builder dataBuilder = new Data.Builder();
			dataBuilder.putString("loopMsg", "こんにちは");
			dataBuilder.putInt("loopCount", loopCount);
			Data inputData = dataBuilder.build();
			OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(ReceiveCountWorker.class);
			workRequestBuilder.setInputData(inputData);
			WorkRequest workRequest = workRequestBuilder.build();
			WorkManager workManager = WorkManager.getInstance(MainActivity.this);
			workManager.enqueue(workRequest);
		}
	}

	private class StartRetryButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(RetryWorker.class);
			workRequestBuilder.setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS);
			WorkRequest workRequest = workRequestBuilder.build();
			WorkManager workManager = WorkManager.getInstance(MainActivity.this);
			workManager.enqueue(workRequest);
		}
	}

	private class StartUniqueworkKeepButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(RetryWorker.class);
			OneTimeWorkRequest workRequest = workRequestBuilder.build();
			WorkManager workManager = WorkManager.getInstance(MainActivity.this);
			workManager.enqueueUniqueWork("UniqueKeepRetry", ExistingWorkPolicy.KEEP, workRequest);
		}
	}

	private class StartUniqueworkReplaceButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(RetryWorker.class);
			OneTimeWorkRequest workRequest = workRequestBuilder.build();
			WorkManager workManager = WorkManager.getInstance(MainActivity.this);
			workManager.enqueueUniqueWork("UniqueReplaceRetry", ExistingWorkPolicy.REPLACE, workRequest);
		}
	}

	private class StartUniqueworkAppendButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(RetryWorker.class);
			OneTimeWorkRequest workRequest = workRequestBuilder.build();
			WorkManager workManager = WorkManager.getInstance(MainActivity.this);
			workManager.enqueueUniqueWork("UniqueAppendRetry", ExistingWorkPolicy.APPEND, workRequest);
		}
	}

	private class StartWorkButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(RetryWorker.class);
			OneTimeWorkRequest workRequest = workRequestBuilder.build();
			UUID uuId = workRequest.getId();
			WorkManager workManager = WorkManager.getInstance(MainActivity.this);
			workManager.enqueueUniqueWork("ObserveWorkInfo", ExistingWorkPolicy.KEEP, workRequest);

			_workInfoLiveData.removeObserver(_workInfoLiveDataObserver);
			_workInfoLiveData = workManager.getWorkInfoByIdLiveData(uuId);
			_workInfoLiveData.observe(MainActivity.this, _workInfoLiveDataObserver);
		}
	}

	private class CancelWorkButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			WorkManager workManager = WorkManager.getInstance(MainActivity.this);
			workManager.cancelUniqueWork("ObserveWorkInfo");
		}
	}

	private class WorkInfoLiveDataObserver implements Observer<WorkInfo> {
		@Override
		public void onChanged(WorkInfo workInfo) {
			if(workInfo.getState() == WorkInfo.State.ENQUEUED) {
				_activityMainBinding.btWorkStart.setEnabled(false);
				_activityMainBinding.btWorkStart.setText(R.string.bt_start_work_enqueued);
				_activityMainBinding.btWorkCancel.setEnabled(true);
				_activityMainBinding.btWorkCancel.setText(R.string.bt_start_work_cancel);
			}
			else if(workInfo.getState() == WorkInfo.State.RUNNING) {
				_activityMainBinding.btWorkStart.setEnabled(false);
				_activityMainBinding.btWorkStart.setText(R.string.bt_start_work_running);
				_activityMainBinding.btWorkCancel.setEnabled(true);
				_activityMainBinding.btWorkCancel.setText(R.string.bt_start_work_cancel);
			}
			else if(workInfo.getState() == WorkInfo.State.SUCCEEDED) {
				_activityMainBinding.btWorkStart.setEnabled(true);
				_activityMainBinding.btWorkStart.setText(R.string.bt_start_work_start);
				_activityMainBinding.btWorkCancel.setEnabled(false);
				_activityMainBinding.btWorkCancel.setText(R.string.bt_start_work_finished);
			}
			else if(workInfo.getState() == WorkInfo.State.CANCELLED) {
				_activityMainBinding.btWorkStart.setEnabled(true);
				_activityMainBinding.btWorkStart.setText(R.string.bt_start_work_start);
				_activityMainBinding.btWorkCancel.setEnabled(false);
				_activityMainBinding.btWorkCancel.setText(R.string.bt_start_work_canceled);
			}
		}
	}
}
