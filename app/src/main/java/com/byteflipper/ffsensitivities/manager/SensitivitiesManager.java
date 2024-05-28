package com.byteflipper.ffsensitivities.manager;

import static com.android.volley.RequestQueue.RequestEvent.REQUEST_CACHE_LOOKUP_FINISHED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_CACHE_LOOKUP_STARTED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_FINISHED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_NETWORK_DISPATCH_FINISHED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_NETWORK_DISPATCH_STARTED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_QUEUED;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.byteflipper.ffsensitivities.model.SensitivityDataModel;
import com.byteflipper.ffsensitivities.utils.NetworkCheckHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SensitivitiesManager {
    public static String GITHUB_SENSITIVITIES_FILES_PATH = "https://raw.githubusercontent.com/ByteFlipper-58/FFSensitivities/master/app/src/main/assets/sensitivity_settings/";
    private final List<SensitivityDataModel> sensitivitiesSet = new ArrayList<>();
    private final MutableLiveData<Boolean> isRequestFinished = new MutableLiveData<>();

    public List<SensitivityDataModel> getSensitivitiesSet() {
        return sensitivitiesSet;
    }

    public MutableLiveData<Boolean> isRequestFinished() {
        return isRequestFinished;
    }

    public void updateAdapterData(Context context, String manufacturer) {
        if (sensitivitiesSet.isEmpty()) {
            isRequestFinished.setValue(false);
            try {
                if (NetworkCheckHelper.isNetworkAvailable(context)) {
                    loadSensitivitiesFromGitHub(context, manufacturer);
                } else {
                    loadSensitivitiesFromAssets(context, manufacturer);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadSensitivitiesFromGitHub(Context context, String manufacturer) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GITHUB_SENSITIVITIES_FILES_PATH + manufacturer + ".json", null,
                response -> {
                    try {
                        parseResponse(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Volley", error.toString())
        );
        queue.addRequestEventListener(createRequestEventListener());
        queue.add(jsonObjectRequest);
    }

    private void loadSensitivitiesFromAssets(Context context, String manufacturer) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("sensitivity_settings/" + manufacturer + ".json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            JSONObject response = new JSONObject(jsonString);
            parseResponse(response);
        } catch (IOException | JSONException e) {
            Log.e("SensitivitiesManager", "Error loading sensitivities from assets", e);
        } finally {
            isRequestFinished.postValue(true);
        }
    }

    private void parseResponse(JSONObject response) throws JSONException {
        JSONArray jsonArray = response.getJSONArray("models");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String deviceName = jsonObject.getString("name");
            String manufacturerName = jsonObject.getString("manufacturer");
            String settingsSourceURL = jsonObject.getString("settings_source_url");
            int dpi = jsonObject.getInt("dpi");
            int fire_button = jsonObject.getInt("fire_button");
            int review = jsonObject.getJSONObject("sensitivities").getInt("review");
            int collimator = jsonObject.getJSONObject("sensitivities").getInt("collimator");
            int x2_scope = jsonObject.getJSONObject("sensitivities").getInt("x2_scope");
            int x4_scope = jsonObject.getJSONObject("sensitivities").getInt("x4_scope");
            int sniper_scope = jsonObject.getJSONObject("sensitivities").getInt("sniper_scope");
            int free_review = jsonObject.getJSONObject("sensitivities").getInt("free_review");
            sensitivitiesSet.add(new SensitivityDataModel(deviceName, manufacturerName, settingsSourceURL, dpi, fire_button,
                    review, collimator, x2_scope, x4_scope, sniper_scope,free_review
            ));
        }
    }

    private RequestQueue.RequestEventListener createRequestEventListener() {
        return (request, event) -> {
            switch (event) {
                case REQUEST_QUEUED, REQUEST_CACHE_LOOKUP_STARTED, REQUEST_NETWORK_DISPATCH_STARTED ->
                        isRequestFinished.postValue(false);
                case REQUEST_FINISHED, REQUEST_CACHE_LOOKUP_FINISHED, REQUEST_NETWORK_DISPATCH_FINISHED ->
                        isRequestFinished.postValue(true);
                default -> {
                }
            }
        };
    }
}