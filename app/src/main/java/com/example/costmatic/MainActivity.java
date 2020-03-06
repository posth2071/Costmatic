package com.example.costmatic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextLinks;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "테스트";

    public static List<CosmaticItem> cosmaticItems = new ArrayList<>();
    public static HashMap<String, String> hashMap = new HashMap<>();
    public static RecyclerView recyclerView;
    public static RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public Spinner spinner_count, spinner_category;
    public EditText et_search;

    private int start_num;
    public static Button bt_plus;

    private InputMethodManager inputManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // KeyBoard 조작위한 Manager
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Keyword 입력상자
        et_search = findViewById(R.id.et_search);
        et_search.setOnClickListener(this);

        // 새로검색 버튼
        Button bt_start = findViewById(R.id.bt_start);
        bt_start.setOnClickListener(this);

        // 추가검색 버튼
        bt_plus = findViewById(R.id.bt_plus);
        bt_plus.setOnClickListener(this);

        // 검색 개수, Category 설정위한 Spinner
        spinner_category = findViewById(R.id.spinner_category);
        spinner_count = findViewById(R.id.spinner_count);

        recyclerView = findViewById(R.id.recycle_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            // 마지막 Item일 경우 추가검색 Button 활성화 위해 ScrollListener 등록
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotal = recyclerView.getAdapter().getItemCount() - 1;
                // Recycler Item 마지막 아이템인지 확인
                if (lastItemPosition == itemTotal) {
                    // 마지막 Item이면 추가검색 버튼 활성화
                    bt_plus.setVisibility(View.VISIBLE);
                } else {
                    // 마지막 Item이 아니면 추가검색 버튼 비활성화
                    bt_plus.setVisibility(View.GONE);
                }
            }
        });

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new Recycle_Adapter(cosmaticItems);
        // mAdapter 연결
        recyclerView.setAdapter(mAdapter);
        // 구분선 설정
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), 1));

        // 처음 기본 검색 start - 0, count - 10, keyword - ""
        rest_api("search");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 새로검색 클릭
            case R.id.bt_start:
                keyboard_state(false);          // 키보드 숨기기
                rest_api("search");       // REST API
                break;

            // 추가검색 클릭 - 이전 검색 조건(BODY) 그대로 start 늘려서 검색
            case R.id.bt_plus:
                rest_api("plus");
                break;

            // 입력상자 클릭 - 키보드 띄우기
            case R.id.et_search:
                keyboard_state(true);
                break;
        }
    }

    public void rest_api(String type) {
        Log.d(TAG, "rest_api: Type - " + type);

        ConnectServer connectServer = null;

        // 동일 검색조건에서 Plus(추가)인지 / 새로 검색(Search)인지 type 판단
        if(type.compareTo("search")==0){
            // REST BODY - Key / Value 세팅 (임시 HashMap, 검색결과 없을경우 전 HashMap 상태 유지위해)
            HashMap<String, String> tempMap = new HashMap<>();
            tempMap.put("type",type);
            tempMap.put("start", "0");
            tempMap.put("count",spinner_count.getSelectedItem().toString());
            tempMap.put("keyword", et_search.getText().toString().trim());
            tempMap.put("sub_category_ids", convert_Category(spinner_category.getSelectedItem().toString()));

            et_search.setText("");
            et_search.clearFocus();

            connectServer = new ConnectServer(this, tempMap, cosmaticItems);
        } else {
            int start_Num = Integer.parseInt(hashMap.get("start")) + Integer.parseInt(hashMap.get("count"));
            // REST BODY - Key / Value 세팅 - 이전 HashMap start 제외 다른요청 동일
            hashMap.put("type", type);
            hashMap.put("start", String.valueOf(start_Num));
            connectServer = new ConnectServer(this, hashMap, cosmaticItems);
        }
        try {
            // OkHttp POST 메소드 실행
            connectServer.requestPost();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 키보드 설정 true - 보이기, false - 숨기기
    private void keyboard_state(boolean state) {
        if (state) {
            //true인경우 키보드 보이기
            inputManager.showSoftInput(et_search, 0);
        } else {
            //false인경우 키보드 숨기기
            inputManager.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
        }
    }

    // Spinner Category 값 변환
    public String convert_Category(String category) {
        if (category.compareTo("스킨 / 토너") == 0) {
            return "0";
        } else if (category.compareTo("클렌징 크림") == 0) {
            return "10";
        } else if (category.compareTo("선블록") == 0) {
            return "20";
        } else if (category.compareTo("마스크 시트") == 0) {
            return "25";
        } else {
            return "0";
        }
    }
}
