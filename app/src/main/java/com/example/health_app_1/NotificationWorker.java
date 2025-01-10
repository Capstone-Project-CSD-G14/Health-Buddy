package com.example.health_app_1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "health_buddy_notifications";

    public NotificationWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        // Retrieve the data passed in
        String notificationType = getInputData().getString("notificationType");
        String healthCondition = getInputData().getString("healthCondition");

        if (notificationType != null) {
            switch (notificationType) {
                case "hydration":
                    sendHydrationNotification();
                    break;
                case "health":
                    if (healthCondition != null) {
                        sendHealthConditionNotification(healthCondition);
                    }
                    break;
                case "morning_medicine":
                    sendMedicineNotification("Morning Medicine", "It's time to take your morning medicine!");
                    break;
                case "afternoon_medicine":
                    sendMedicineNotification("Afternoon Medicine", "It's time to take your afternoon medicine!");
                    break;
                case "night_medicine":
                    sendMedicineNotification("Night Medicine", "It's time to take your night medicine!");
                    break;
            }
        }

        return Result.success();
    }

    private void sendHydrationNotification() {
        Context context = getApplicationContext();
        String title = "Stay Hydrated!";
        String content = "It's time to drink water and stay hydrated!";
        sendNotification(context, title, content);
    }

    private void sendHealthConditionNotification(String healthCondition) {
        Context context = getApplicationContext();
        String title = "Health Reminder!";
        String content = "";

        switch (healthCondition) {
            case "Diabetes":
                content = "Remember to monitor your blood sugar and maintain a low-carb diet.";
                break;
            // Add other cases here...
            default:
                content = "Maintain a balanced diet and stay healthy.";
                break;
        }

        sendNotification(context, title, content);
    }

    private void sendMedicineNotification(String title, String content) {
        Context context = getApplicationContext();
        sendNotification(context, title, content);
    }

    private void sendNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, NutritionResultActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Add your notification icon here
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}