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
import com.android.volley.RequestQueue.RequestEventListener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import com.byteflipper.ffsensitivities.model.ManufacturersDataModel;

public class ManufacturersManager {
    private static final String GITHUB_MANUFACTURERS_FILES_PATH = "https://raw.githubusercontent.com/ByteFlipper-58/FFSensitivities/master/app/src/main/assets/sensitivity_settings/manufacturers.json";
    private final List<ManufacturersDataModel> manufacturersSet = new ArrayList<>();
    private final MutableLiveData<Boolean> isRequestFinished = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isReadyLiveData = new MutableLiveData<>();
    private static ManufacturersManager instance;

    // Private constructor to prevent instantiation from outside
    private ManufacturersManager() {
    }

    // Public method to get the singleton instance
    public static synchronized ManufacturersManager getInstance() {
        if (instance == null) {
            instance = new ManufacturersManager();
        }
        return instance;
    }

    public List<ManufacturersDataModel> getManufacturersSet() {
        return manufacturersSet;
    }

    public MutableLiveData<Boolean> isRequestFinished() {
        return isRequestFinished;
    }

    public MutableLiveData<Boolean> isReadyLiveData() {
        return isReadyLiveData;
    }

    public boolean isReady() {
        return isReadyLiveData.getValue() != null && isReadyLiveData.getValue();
    }

    public void updateAdapterData(Context context) {
        if (manufacturersSet.isEmpty()) {
            isRequestFinished.setValue(false);
            isReadyLiveData.setValue(false);
            try {
                if (NetworkCheckHelper.isNetworkAvailable(context)) {
                    loadManufacturersFromGitHub(context);
                } else {
                    loadManufacturersFromAssets(context);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadManufacturersFromGitHub(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GITHUB_MANUFACTURERS_FILES_PATH, null,
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

    private void loadManufacturersFromAssets(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("sensitivity_settings/manufacturers.json");
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
            Log.e("ManufacturersManager", "Error loading manufacturers from assets", e);
        } finally {
            isReadyLiveData.postValue(true);
            isRequestFinished.postValue(true);
        }
    }

    private void parseResponse(JSONObject response) throws JSONException {
        JSONArray jsonArray = response.getJSONArray("manufacturers");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("name");
            String model = jsonObject.getString("model");
            Boolean showInProductionApp = jsonObject.getBoolean("showInProductionApp");
            Boolean isAvailable = jsonObject.getBoolean("isAvailable");
            manufacturersSet.add(new ManufacturersDataModel(name, model, showInProductionApp, isAvailable));
        }
        isReadyLiveData.postValue(true);
        isRequestFinished.postValue(true);
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