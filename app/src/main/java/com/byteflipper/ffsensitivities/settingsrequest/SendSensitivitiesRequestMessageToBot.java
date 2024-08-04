package com.byteflipper.ffsensitivities.settingsrequest;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendSensitivitiesRequestMessageToBot extends AsyncTask<String, Void, String> {

    private static final String TAG = "SendMessageToGroupTask";

    private static final String BOT_TOKEN = "7043829891:AAHr-dUynjVTrkRVrqRixAm4XGUaV-k9t0s";

    private static final String GROUP_CHAT_ID = "-4120443586";

    @Override
    protected String doInBackground(String... params) {
        String message = params[0];

        try {
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("chat_id", GROUP_CHAT_ID)
                    .add("text", message)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage")
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                Log.e(TAG, "Ошибка отправки сообщения: " + response.code());
                return "Ошибка отправки сообщения";
            }

            return "Сообщение успешно отправлено в группу";

        } catch (IOException e) {
            Log.e(TAG, "Ошибка отправки сообщения: " + e.getMessage());
            return "Ошибка отправки сообщения";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, result);
    }
}