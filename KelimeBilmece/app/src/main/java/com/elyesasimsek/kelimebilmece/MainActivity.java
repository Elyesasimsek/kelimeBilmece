package com.elyesasimsek.kelimebilmece;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.elyesasimsek.kelimebilmece.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SQLiteDatabase db;
    private Cursor cursor;
    private int heartIndex, heartCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        try {
            db = this.openOrCreateDatabase("KelimeBulmaca", MODE_PRIVATE, null);
            cursor = db.rawQuery("SELECT kHeart FROM Ayarlar", null);

            heartIndex = cursor.getColumnIndex("kHeart");
            cursor.moveToFirst();

            heartCount = Integer.valueOf(cursor.getString(heartIndex));
            binding.textViewMainActivityUserHeartCount.setText("+" + heartIndex);

            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void mainBtnHemenOynaClick(View view){
        Intent intent = new Intent(MainActivity.this, PlayActivity.class);
        finish();
        intent.putExtra("heartCount", heartCount);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_out_up, R.anim.slide_in_down);
    }

    public void mainBtnMarketClick(View view){

    }

    public void mainBtnCikisClick(View view){

    }

    @Override
    public void onBackPressed() {
        //alertDialog
        super.onBackPressed();
    }
}