package com.toxicant.hua.pulltorefreshdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    List<String> datas;
    PullToRefreshLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv= (ListView) findViewById(R.id.lv_demo);
        layout= (PullToRefreshLayout) findViewById(R.id.refresh_layout);
        //填充数据
        datas=new ArrayList<>();
        for (int i=0;i<40;i++){
            datas.add("item=>"+i);
        }
        View head=LayoutInflater.from(this).inflate(R.layout.head_view_layout,null);
        lv.addHeaderView(head);
        lv.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,datas));
        layout.setListener(new PullToRefreshLayout.RefreshListener() {
            @Override
            public void onRefresh() {
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layout.stopRefresh();
                        Toast.makeText(MainActivity.this,"刷新完成",Toast.LENGTH_SHORT).show();
                    }
                },3000);
            }
        });
    }
}
