//package com.example.diplomnaya;
//
//import okhttp3.*;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//
//public class OpenAIHelper {
//    private static final String API_URL = "http://localhost:1337/v1/models/gpt-3.5-turbo";
//    private static final String MODEL = "gpt-3.5-turbo"; // Убедитесь, что модель корректная
//
//    public static void generateTaskContent(String apiKey, String prompt, Callback callback) throws JSONException {
//        OkHttpClient client = new OkHttpClient();
//
//        JSONObject json = new JSONObject();
//        json.put("model", MODEL);
//        json.put("prompt", prompt);
//        json.put("max_tokens", 50);
//
//        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
//        Request request = new Request.Builder()
//                .url(API_URL)
//                .post(body)
//                .addHeader("Authorization", "Bearer " + apiKey)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace(); // Добавьте лог ошибки
//                callback.onFailure(call, e);
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    System.out.println("Response code: " + response.code()); // Лог кода ответа
//                }
//                callback.onResponse(call, response);
//            }
//        });
//    }
//}
