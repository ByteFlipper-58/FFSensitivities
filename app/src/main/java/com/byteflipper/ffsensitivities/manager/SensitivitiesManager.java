package com.byteflipper.ffsensitivities.manager;

import static com.android.volley.RequestQueue.RequestEvent.REQUEST_CACHE_LOOKUP_FINISHED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_CACHE_LOOKUP_STARTED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_FINISHED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_NETWORK_DISPATCH_FINISHED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_NETWORK_DISPATCH_STARTED;
import static com.android.volley.RequestQueue.RequestEvent.REQUEST_QUEUED;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.byteflipper.ffsensitivities.model.SensitivityDataModel;

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
                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, GITHUB_SENSITIVITIES_FILES_PATH + manufacturer + ".json", null,
                        response -> {
                            try {
                                parseResponse(response);
                                //sortSensitivitiesByName(manufacturer); // Сортировка после парсинга
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.e("Volley", error.toString())
                );
                queue.addRequestEventListener(createRequestEventListener());
                queue.add(jsonObjectRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

    private void sortSensitivitiesByName(String manufacturer) {
        if (manufacturer.equalsIgnoreCase("Samsung") ||
                manufacturer.equalsIgnoreCase("iPhone") ||
                manufacturer.equalsIgnoreCase("Oppo")) {
            Collections.sort(sensitivitiesSet, new Comparator<SensitivityDataModel>() {
                private static final String[] SAMSUNG_ORDER = {"S", "Note", "A", "M", "F"};
                private static final String[] SAMSUNG_ADDITIONAL_WORDS = {"Ultra", "Plus", "Mini", "Edge", "s"};
                private static final String[] IPHONE_ORDER = {"14", "13", "12", "11", "SE"};
                private static final String[] IPHONE_ADDITIONAL_WORDS = {"Plus", "Pro", "Pro Max", "Mini"};
                private static final String[] OPPO_ORDER = {"A", "F", "Reno"};
                private static final String[] OPPO_ADDITIONAL_WORDS = {"F", "s"};

                @Override
                public int compare(SensitivityDataModel o1, SensitivityDataModel o2) {
                    String name1 = o1.getDeviceName();
                    String name2 = o2.getDeviceName();

                    if (manufacturer.equalsIgnoreCase("Samsung")) {
                        return compareSamsung(name1, name2);
                    } else if (manufacturer.equalsIgnoreCase("iPhone")) {
                        return compareiPhone(name1, name2);
                    } else if (manufacturer.equalsIgnoreCase("Oppo")) {
                        return compareOppo(name1, name2);
                    } else {
                        // Default sorting if manufacturer is not recognized
                        return name1.compareTo(name2);
                    }
                }

                private int compareSamsung(String name1, String name2) {
                    // Извлечение буквенного значения и числового значения
                    int index1 = findOrderIndex(name1, SAMSUNG_ORDER);
                    int index2 = findOrderIndex(name2, SAMSUNG_ORDER);

                    // Сравнение буквенного значения
                    if (index1 != index2) {
                        return index2 - index1; // Сортировка по убыванию
                    }

                    // Извлечение числового значения
                    int num1 = extractNumber(name1);
                    int num2 = extractNumber(name2);

                    // Сравнение числового значения
                    if (num1 != num2) {
                        return num2 - num1; // Сортировка по убыванию
                    }

                    // Сравнение дополнительных слов (Ultra, Plus, Mini, Edge, s)
                    return compareAdditionalWords(name1, name2, SAMSUNG_ADDITIONAL_WORDS);
                }

                private int compareiPhone(String name1, String name2) {
                    // Извлечение числового значения
                    int num1 = extractNumber(name1);
                    int num2 = extractNumber(name2);

                    // Сравнение числового значения
                    if (num1 != num2) {
                        return num2 - num1; // Сортировка по убыванию
                    }

                    // Сравнение дополнительных слов (Plus, Pro, Pro Max, Mini)
                    return compareAdditionalWords(name1, name2, IPHONE_ADDITIONAL_WORDS);
                }

                private int compareOppo(String name1, String name2) {
                    // Извлечение буквенного значения и числового значения
                    int index1 = findOrderIndex(name1, OPPO_ORDER);
                    int index2 = findOrderIndex(name2, OPPO_ORDER);

                    // Сравнение буквенного значения
                    if (index1 != index2) {
                        return index2 - index1; // Сортировка по убыванию
                    }

                    // Извлечение числового значения
                    int num1 = extractNumber(name1);
                    int num2 = extractNumber(name2);

                    // Сравнение числового значения
                    if (num1 != num2) {
                        return num2 - num1; // Сортировка по убыванию
                    }

                    // Сравнение дополнительных слов (F, s)
                    return compareAdditionalWords(name1, name2, OPPO_ADDITIONAL_WORDS);
                }

                private int findOrderIndex(String name, String[] order) {
                    for (int i = 0; i < order.length; i++) {
                        if (name.startsWith(order[i])) {
                            return i;
                        }
                    }
                    return -1;
                }

                private int extractNumber(String name) {
                    // Находим индекс первого символа, не являющегося буквой (возможно, это число или пробел)
                    int numberStart = 0;
                    while (numberStart < name.length() && Character.isLetter(name.charAt(numberStart))) {
                        numberStart++;
                    }

                    // Проверяем, что после буквы есть число
                    if (numberStart < name.length() && Character.isDigit(name.charAt(numberStart))) {
                        // Извлекаем число
                        int numberEnd = numberStart;
                        while (numberEnd < name.length() && Character.isDigit(name.charAt(numberEnd))) {
                            numberEnd++;
                        }
                        return Integer.parseInt(name.substring(numberStart, numberEnd));
                    } else {
                        // Если число не найдено, возвращаем 0
                        return 0;
                    }
                }

                private int compareAdditionalWords(String name1, String name2, String[] additionalWords) {
                    // Извлекаем части после числа (Ultra, Plus, Mini, Edge, s)
                    String[] parts1 = name1.split("[0-9]+");
                    String[] parts2 = name2.split("[0-9]+");

                    // Сравниваем части, начиная с конца, с проверкой на длину массивов
                    for (int i = parts1.length - 1, j = parts2.length - 1; i >= 0 && j >= 0; i--, j--) {
                        // Проверяем, что индексы не выходят за пределы массивов
                        if (i >= 0 && j >= 0) {
                            int index1 = findAdditionalWordIndex(parts1[i], additionalWords);
                            int index2 = findAdditionalWordIndex(parts2[i], additionalWords);

                            // Сравниваем индексы в массиве ADDITIONAL_WORDS
                            if (index1 != index2) {
                                return index2 - index1;
                            }
                        }
                    }

                    // Если все части одинаковые, возвращаем 0
                    return 0;
                }

                private int findAdditionalWordIndex(String word, String[] additionalWords) {
                    for (int i = 0; i < additionalWords.length; i++) {
                        if (word.equals(additionalWords[i])) {
                            return i;
                        }
                    }
                    return -1; // Если слово не найдено, возвращаем -1
                }
            });
        }
    }

    private RequestQueue.RequestEventListener createRequestEventListener() {
        return (request, event) -> {
            switch (event) {
                case REQUEST_QUEUED:
                case REQUEST_CACHE_LOOKUP_STARTED:
                case REQUEST_NETWORK_DISPATCH_STARTED:
                    isRequestFinished.postValue(false);
                    break;
                case REQUEST_FINISHED:
                case REQUEST_CACHE_LOOKUP_FINISHED:
                case REQUEST_NETWORK_DISPATCH_FINISHED:
                    isRequestFinished.postValue(true);
                    break;
                default:
                    break;
            }
        };
    }
}