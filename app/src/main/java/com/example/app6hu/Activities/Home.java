package com.example.app6hu.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.app6hu.Fragments.CalendarFragment;
import com.example.app6hu.Fragments.HocPhiFragment;
import com.example.app6hu.Fragments.ReportFragment;
import com.example.app6hu.Fragments.GoalsFragment;
import com.example.app6hu.Fragments.SettingsFragment;
import com.example.app6hu.Fragments.TransactionFragment;
import com.example.app6hu.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // 🟢 Hiển thị TransactionFragment (Thu/Chi) đầu tiên
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TransactionFragment())
                    .commit();
            // Đặt trạng thái được chọn cho menu
            bottomNavigationView.setSelectedItemId(R.id.nav_transaction);
        }

        // Xử lý khi chọn các item trong BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_transaction) {
                selectedFragment = new TransactionFragment();
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (itemId == R.id.nav_report) {
                selectedFragment = new ReportFragment();
            }
            else if (item.getItemId() == R.id.nav_school) {
                selectedFragment = new HocPhiFragment();
            }
            else if (itemId == R.id.nav_saving) {
                selectedFragment = new GoalsFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
}