package com.elyesasimsek.kelimebilmece;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.elyesasimsek.kelimebilmece.databinding.ActivitySplashScreenBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class SplashScreenActivity extends AppCompatActivity {

    //sorular için listeler
    private String[] sorularListesi = {"Mutfakta iş yaparken veya yemek yerken kullanılan aletler nelerdir?", "İç Anadolu Bölgesindeki İller?"};
    private String[] sorularKodList = {"mutfakS1", "illerS1"};

    //kelimler için listeler
    private String[] kelimelerListesi = {"Çatal", "Bıçak", "Kaşık", "Tabak", "Bulaşık Süngeri", "Bulaşık Teli", "Tencere", "Tava", "Çaydanlık",
            "Mutfak Robotu", "Kesme Tahtası", "Süzgeç",
            "Aksaray", "Ankara", "Çankırı", "Eskişehir", "Karaman", "Kayseri", "Kırıkkale", "Kırşehir", "Konya",
            "Nevşehir", "Niğde", "Sivas", "Yozgat"};
    private String[] kelimelerKodList = {"mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1",
            "mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1",
            "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1",
            "illerS1", "illerS1", "illerS1", "illerS1"};

    private ActivitySplashScreenBinding binding;
    private SQLiteDatabase db;
    private Cursor cursor;
    private float maksimumProgres = 100, artacakProgres, progresMiktari = 0;
    private SQLiteStatement statement;
    private String sqlSorgusu;
    static public HashMap<String, String> sorularList;
    private MediaPlayer gameTheme;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private boolean muzikDurumu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sorularList = new HashMap<>();

        try {
            db = this.openOrCreateDatabase("KelimeBulmaca", MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS Ayarlar (kAdi VARCHAR, kHeart VARCHAR, kImage BLOB)");
            cursor = db.rawQuery("SELECT * FROM Ayarlar", null);
            if (cursor.getCount() < 1) {
                db.execSQL("INSERT INTO Ayarlar (kAdi, kHeart) VALUES ('Oyuncu', '0')");
            }

            db.execSQL("CREATE TABLE IF NOT EXISTS Sorular (id INTEGER PRIMARY KEY, sKod VARCHAR UNIQUE, soru VARCHAR)");
            db.execSQL("DELETE FROM Sorular");
            sqlSorulariEkle();

            db.execSQL("CREATE TABLE IF NOT EXISTS Kelimeler (kKod VARCHAR, kelime VARCHAR, FOREIGN KEY (kKod) REFERENCES Sorular (sKod))");
            db.execSQL("DELETE FROM Kelimeler");
            sqlKelimeleriEkle();

            cursor = db.rawQuery("SELECT * FROM Sorular", null);
            artacakProgres = maksimumProgres / cursor.getCount();

            int sKodIndex = cursor.getColumnIndex("sKod");
            int soruIndex = cursor.getColumnIndex("soru");

            binding.textViewSplashScreenActivityState.setText("Sorular Yükleniyor...");

            while (cursor.moveToNext()){
                sorularList.put(cursor.getString(sKodIndex), cursor.getString(soruIndex));
                progresMiktari += artacakProgres;
                binding.progressBarSplashScreenActivity.setProgress((int) progresMiktari);
            }

            binding.textViewSplashScreenActivityState.setText("Sorular alındı. Uygulama başlatılıyor...");
            cursor.close();

            new CountDownTimer(1100, 1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sqlSorulariEkle(){
        try {
            for (int i = 0; i < sorularListesi.length; i++){
                sqlSorgusu = "INSERT INTO Sorular (sKod, soru) VALUES (?, ?)";
                statement = db.compileStatement(sqlSorgusu);
                statement.bindString(1, sorularKodList[i]);
                statement.bindString(2, sorularListesi[i]);
                statement.execute();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sqlKelimeleriEkle(){
        try {
            for (int i = 0; i < kelimelerListesi.length; i++){
                sqlSorgusu = "INSERT INTO Kelimeler (kKod, kelime) VALUES (?, ?)";
                statement = db.compileStatement(sqlSorgusu);
                statement.bindString(1, kelimelerKodList[i]);
                statement.bindString(2, kelimelerListesi[i]);
                statement.execute();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}