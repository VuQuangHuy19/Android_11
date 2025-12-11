package com.example.app6hu.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Adapter.TruongAdapter;
import com.example.app6hu.R;
import com.example.app6hu.model.Truong;

import java.util.ArrayList;

public class ChonTruongActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Truong> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_truong);

        recyclerView = findViewById(R.id.recyclerTruong);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list.add(new Truong("ĐH Công Nghiệp Hà Nội", "https://sv.haui.edu.vn", R.drawable.haui));
        list.add(new Truong("ĐH Bách Khoa Hà Nội", "https://ctt-sis.hust.edu.vn", R.drawable.hust));
        list.add(new Truong("ĐH FPT", "https://my.fpt.edu.vn", R.drawable.fpt));
        list.add(new Truong("ĐH Kinh Tế Quốc Dân", "https://student.neu.edu.vn", R.drawable.neu));

        TruongAdapter adapter = new TruongAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }
}
