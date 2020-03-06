package com.example.costmatic;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectServer extends Activity {
    private final String TAG = "테스트";
    private final String API_URL = "https://blb-test.morulabs.com/api/fetch/cosmetics/";
    private final String API_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNTgzOTk2MDM3LCJqdGkiOiI2ZWVhMjFiN2ZlOGY0OTk1YThkMzY0YWUwMjhmOWMyOSIsInVzZXJfaWQiOjUzM30.b2UplWQZ5JembwSmdM3ZqCBKXTkepqhevu2C_tK9jUo";

    // 임시 HashMap위해 개별 생성 - 검색결과 미존재시 이전 BODY조건들 유지하기 위해
    private HashMap<String, String> hashMap;

    OkHttpClient client = new OkHttpClient();

    private Context context;
    private List<CosmaticItem> items;

    private ProgressDialog progressDialog;

    public ConnectServer(Context context, HashMap<String, String> hashMap, List<CosmaticItem> cosmaticItems) {
        this.hashMap = hashMap;
        this.context = context;
        this.items = cosmaticItems;
    }

    // POST 메소드
    public void requestPost() throws JSONException {
        Log.d(TAG, "requestPost: RequestPost 진입");

        // ProgressDialog 선언
        progressDialog = new ProgressDialog(context, android.R.style.Theme_Material_Dialog_Alert);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);      // 원형 스타일
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);                    // 화면 Touch시 ProgressDialog Cancel 막기
        progressDialog.show();

        // Reques Body 세팅 (Type - "application/json")
        JSONObject jsonObject = new JSONObject();
        jsonObject.
                put("start", Integer.parseInt(hashMap.get("start"))).
                put("count", Integer.parseInt(hashMap.get("count"))).
                put("keyword", hashMap.get("keyword")).
                put("sub_category_ids", new JSONArray().put(Integer.parseInt(hashMap.get("sub_category_ids"))));

        // 세팅된 JsonObject 확인 - Log.d
        Log.d(TAG, "requestPost: jsonObject - " + jsonObject.toString());

        // REST BODY 객체 생성 - 타입 Json
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        // REST POST REQUEST 세팅
        Request request = new Request.Builder().
                addHeader("Authorization", API_TOKEN).                 // 개인 인증 토큰
                addHeader("Content-Type", "application/json").  // 요청Body타입 - Json 타입
                addHeader("Accept", "application/json").        // 응답타입 - Json 타입
                url(API_URL).
                post(requestBody1).                                          // request Body - Json 타입
                build();

        // REST POST 요청 실행, 비동기 작업 (enququ - callBack 처리)
        client.newCall(request).enqueue(new Callback() {
            // 응답 실패
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.toString());
            }

            // 응답 성공
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // 응답 결과 확인 - Log.d
                    String result = response.body().string();
                    Log.d(TAG, "onResponse: " + result);

                    // Json Items 배열 나누기
                    JSONObject jsonObject1 = new JSONObject(result);

                    // 응답 Item 개수가 0개면 break;
                    if (jsonObject1.getInt("total") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"검색결과가 없습니다.", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                        return;
                    }

                    // Items 배열 객체 생성
                    JSONArray jsonArray1 = jsonObject1.getJSONArray("items");

                    // 검색 Type - 새로검색(search) / 추가검색(plus)
                    if (hashMap.get("type").compareTo("search") == 0) {
                        // 새로검색일 경우 기존 리스트 비우기
                        items.clear();
                    }

                    for (int i = 0; i < jsonArray1.length(); i++) {
                        // 요청 결과 아이템 개수만큼 반복
                        CosmaticItem cosmaticItem = new CosmaticItem(jsonArray1.getJSONObject(i));
                        items.add(cosmaticItem);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "error: " + e.toString());
                }

                // 화장품 이미지 다운로드 AsynTask 실행
                Download download = new Download(hashMap.get("type"));
                download.execute();
            }
        });
    }


    class Download extends AsyncTask<String, Void, Void> {

        String type;

        public Download(String type) {
            super();
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            // 다운로드 받아 저장할 Bitmap 객체
            Bitmap bmImg = null;
            URL myFileUrl = null;
            HttpURLConnection conn = null;
            CosmaticItem cosmaticItem = null;
            // Items 개수만큼 반복
            for (int i = 0; i < items.size(); i++) {
                try {
                    cosmaticItem = items.get(i);
                    myFileUrl = new URL(cosmaticItem.getImage());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    conn = (HttpURLConnection) myFileUrl.openConnection();
                    conn.setDoInput(true);                  // 받아오기 위해 InPut - True
                    conn.connect();

                    InputStream is = conn.getInputStream();

                    bmImg = BitmapFactory.decodeStream(is);
                    cosmaticItem.setBmImg(bmImg);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    conn.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // ProgressDialog 종료
            progressDialog.dismiss();

            // 정상적으로 받아온경우 임시 TempHash 값 저장
            MainActivity.hashMap = hashMap;

            // 새로검색인경우 Recycler Layout,Adapter 재연결
            if(type.compareTo("search")==0){
                MainActivity.recyclerView.removeAllViewsInLayout();
                MainActivity.recyclerView.setAdapter(MainActivity.mAdapter);
            } else {
                // 추가검색인 경우 Plus버튼 숨기고, Adapter 갱신
                MainActivity.bt_plus.setVisibility(View.GONE);
                MainActivity.mAdapter.notifyDataSetChanged();
            }
        }
    }
}
