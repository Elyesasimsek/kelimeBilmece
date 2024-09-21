package com.elyesasimsek.kelimebilmece;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.elyesasimsek.kelimebilmece.databinding.ActivityPlayBinding;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    private ActivityPlayBinding binding;
    private SQLiteDatabase db;
    private SQLiteStatement statement;
    private String sqlSorgusu;
    private ArrayList<String> sorularListesi;
    private ArrayList<String> sorularKodList;
    private ArrayList<String> kelimelerLisetesi;
    private ArrayList<Character> kelimeHarfleri;
    private Cursor cursor;

    private Random randomSoru, randomKelime, randomHarf;
    private int randomSoruSayi, randomKelimeSayi, randomHarfSayi;
    private String rastgeleSoru, rastgeleSoruKodu, rastgeleKelime, kelimeBilgisi;
    private int rastgeleBelirlenecekHarfSayisi;
    private String textTahminDegeri;

    private Intent getIntent;
    private int hakSayisi, sonHakSayisi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sorularListesi = new ArrayList<>();
        sorularKodList = new ArrayList<>();
        kelimelerLisetesi = new ArrayList<>();
        randomSoru = new Random();
        randomKelime = new Random();
        randomHarf = new Random();

        getIntent = getIntent();
        hakSayisi = getIntent.getIntExtra("heartCount", 0);

        for (Map.Entry soru: SplashScreenActivity.sorularList.entrySet()){
            sorularListesi.add(String.valueOf(soru.getValue()));
            sorularKodList.add(String.valueOf(soru.getKey()));
        }

       randomSoruGetir();
    }

    @Override
    public void onBackPressed() {
        mainIntent();
        super.onBackPressed();
    }

    public void butonHarfAl(View view){
       if (hakSayisi > 0){
           rastgeleHarfAl();
           sonHakSayisi = hakSayisi;
           hakSayisi--;
           kalanHakkiKaydet(hakSayisi, sonHakSayisi);
       }else {
           Toast.makeText(getApplicationContext(), "Harf Alabilmek İçin Kalp Sayısı Yetersiz.", Toast.LENGTH_SHORT).show();
       }
    }

    private void kalanHakkiKaydet(int hsayisi, int sonHSayisi){
        try {
            sqlSorgusu = "UPDATE Ayarlar SET kHeart = ? WHERE kHeart = ?";
            statement = db.compileStatement(sqlSorgusu);
            statement.bindString(1, String.valueOf(hsayisi));
            statement.bindString(2, String.valueOf(sonHSayisi));
            statement.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void butonTahminEt(View view){
        textTahminDegeri = binding.editTextPlayActivityGuess.getText().toString();

        if (!TextUtils.isEmpty(textTahminDegeri)){
            if (textTahminDegeri.matches(rastgeleKelime)){
                Toast.makeText(getApplicationContext(), "Doğru Tahmin.", Toast.LENGTH_SHORT).show();
                binding.editTextPlayActivityGuess.setText("");

                if (kelimelerLisetesi.size() > 0){
                    randomKelimeGetir();
                }else {
                    if (sorularListesi.size() > 0){
                        randomSoruGetir();
                    }else {
                        Toast.makeText(getApplicationContext(), "Sorular Bitti.", Toast.LENGTH_SHORT).show();
                    }

                }
            }else{
                if (hakSayisi > 0){
                    sonHakSayisi = sonHakSayisi;
                    hakSayisi--;
                    Toast.makeText(getApplicationContext(), "Yanliş Tahminde Bulundunuz, Kalp Sayınız Bir Azaldı.", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Devam Edebilmek İçin Kalp Sayısı Yetersiz. Oyun Bitti", Toast.LENGTH_LONG).show();

                    new CountDownTimer(1100, 1000){

                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            mainIntent();
                        }
                    }.start();
                }
            }
        }else {
            Toast.makeText(getApplicationContext(), "Tahmin Değeri Boş Olamaz.", Toast.LENGTH_SHORT).show();
            new CountDownTimer(1100, 1000){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    mainIntent();
                }
            }.start();
        }
    }

    private void rastgeleHarfAl(){
        if (kelimeHarfleri.size() > 0){
            randomHarfSayi = randomHarf.nextInt(kelimeHarfleri.size());
            String[] txtHarfler = binding.textViewPlayActivityQuest.getText().toString().split(" ");
            char[] gelenKelimeHarfler = rastgeleKelime.toCharArray();

            for (int i = 0; i < rastgeleKelime.length();i++){
                if (txtHarfler[i].equals("_") && gelenKelimeHarfler[i] == kelimeHarfleri.get(randomHarfSayi)){
                    txtHarfler[i] = String.valueOf(kelimeHarfleri.get(randomHarfSayi));
                    kelimeBilgisi = "";

                    for (int j = 0; j < txtHarfler.length; j++){
                        if (j < txtHarfler.length -1){
                            kelimeBilgisi += txtHarfler[j] + " ";
                        }else {
                            kelimeBilgisi += txtHarfler[j];
                        }
                    }
                    break;
                }
            }

            binding.textViewPlayActivityQuest.setText(kelimeBilgisi);
            kelimeHarfleri.remove(randomHarfSayi);
        }
    }

    private void mainIntent(){
        Intent backIntent = new Intent(PlayActivity.this, MainActivity.class);
        finish();
        startActivity(backIntent);
        overridePendingTransition(R.anim.slide_out_up, R.anim.slide_in_down);
    }

    private void randomSoruGetir(){
        randomSoruSayi  = randomSoru.nextInt(sorularKodList.size());
        rastgeleSoru = sorularListesi.get(randomSoruSayi);
        rastgeleSoruKodu = sorularKodList.get(randomSoruSayi);
        sorularListesi.remove(randomSoruSayi);
        sorularKodList.remove(randomSoruSayi);

        binding.textViewPlayActivityQuestion.setText(rastgeleSoru);

        try {
            db = this.openOrCreateDatabase("KelimeBulmaca", MODE_PRIVATE, null);
            cursor = db.rawQuery("SELECT * FROM Kelimeler WHERE kKod = ?", new String[]{rastgeleSoruKodu});
            int kelimeIndex = cursor.getColumnIndex("kelime");

            while (cursor.moveToNext()){
                kelimelerLisetesi.add(cursor.getString(kelimeIndex));
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        randomKelimeGetir();
    }

    private void randomKelimeGetir(){
        kelimeBilgisi = "";
        randomKelimeSayi = randomKelime.nextInt(kelimelerLisetesi.size());
        rastgeleKelime = kelimelerLisetesi.get(randomKelimeSayi);
        kelimelerLisetesi.remove(randomKelimeSayi);

        for (int i = 0; i < rastgeleKelime.length(); i++) {
            if (i < rastgeleKelime.length() - 1) {
                kelimeBilgisi += "_ ";
            } else {
                kelimeBilgisi += "_";
            }
        }

        binding.textViewPlayActivityQuest.setText(kelimeBilgisi);
        System.out.println("Gelen Kelime = " + rastgeleKelime);
        System.out.println("Gelen Kelime Harf Sayısı = " + rastgeleKelime.length());
        kelimeHarfleri = new ArrayList<>();

        for (char harf : rastgeleKelime.toCharArray()){
            kelimeHarfleri.add(harf);
        }

        if (rastgeleKelime.length() >= 5 && rastgeleKelime.length() <= 7){
            rastgeleBelirlenecekHarfSayisi = 1;
        }else if (rastgeleKelime.length() >= 8 && rastgeleKelime.length() <= 10){
            rastgeleBelirlenecekHarfSayisi = 2;
        } else if (rastgeleKelime.length() >= 11 && rastgeleKelime.length() <= 14) {
            rastgeleBelirlenecekHarfSayisi = 3;
        } else if (rastgeleKelime.length() >= 15) {
            rastgeleBelirlenecekHarfSayisi = 4;
        }else {
            rastgeleBelirlenecekHarfSayisi = 0;
        }

        for (int i = 0; i < rastgeleBelirlenecekHarfSayisi; i++){
            rastgeleHarfAl();
        }
    }
}