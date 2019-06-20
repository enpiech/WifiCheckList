package tdc.edu.vn.wifichecklist.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import tdc.edu.vn.wifichecklist.dal.DataSource;

public class UpdateDataFromFireBaseWorker extends Worker {
    public static final String TAG = "update_data_from_fire_base";

    public UpdateDataFromFireBaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userUuid = getInputData().getString("userUuid");

        if (userUuid != null) { DataSource.updateToFirebase(getApplicationContext(), userUuid); }

        return Result.success();
    }
}
