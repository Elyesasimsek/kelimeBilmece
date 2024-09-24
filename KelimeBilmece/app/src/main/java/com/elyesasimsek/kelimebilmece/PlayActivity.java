package com.elyesasimsek.kelimebilmece;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.elyesasimsek.kelimebilmece.databinding.ActivityPlayBinding;
import com.elyesasimsek.kelimebilmece.databinding.CustomDialogStatisticTableBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    private AlertDialog.Builder alert;

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
    private AdView adView;
    private static String AD_UNIT_ID_BANNER = "ca-app-pub-4632976048436433/1647001111";
    private static String TEST_AD_UNIT_ID_BANNER = "ca-app-pub-3940256099942544/9214589741";

    private Dialog statisticTableDialog;
    WindowManager.LayoutParams params;
    private int cozulenKelimeSayisi = 0, cozulenSoruSayisi = 0, yapilanYanlisSayisi = 0, maksimumSoruSayisi, maksimumKelimeSayisi;


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

        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();

        sorularListesi = new ArrayList<>();
        sorularKodList = new ArrayList<>();
        kelimelerLisetesi = new ArrayList<>();
        randomSoru = new Random();
        randomKelime = new Random();
        randomHarf = new Random();

        getIntent = getIntent();
        hakSayisi = getIntent.getIntExtra("heartCount", 0);
        binding.textViewPlayActivityUserHeartCount.setText("+" + hakSayisi);

        for (Map.Entry soru: SplashScreenActivity.sorularList.entrySet()){
            sorularListesi.add(String.valueOf(soru.getValue()));
            sorularKodList.add(String.valueOf(soru.getKey()));
        }

        randomSoruGetir();
        loadBanner();
    }

    public void btnIstatistikTablosu(View view){
        maksimumVerileriHesapla("");
    }

    private void istatistikTablosunuGoster(String oyunDurumu, int maksimumSoruSayisi, int maksimumKelimeSayisi, int cozulenSoruSayisi, int cozulenKelimeSayisi, int yapilanYanlisSayisi){
        CustomDialogStatisticTableBinding statisticTableBinding;
        statisticTableBinding = DataBindingUtil.inflate(LayoutInflater.from(PlayActivity.this), R.layout.custom_dialog_statistic_table, null, false);

        statisticTableDialog = new Dialog(PlayActivity.this);
        params = new WindowManager.LayoutParams();
        params.copyFrom(statisticTableDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        statisticTableDialog.setContentView(statisticTableBinding.getRoot());

        if (oyunDurumu.matches("oyunBitti")){
            yapilanYanlisSayisi++;
            statisticTableDialog.setCancelable(false);
            statisticTableBinding.linearLayoutCustomDialogStatisticTable.setVisibility(View.VISIBLE);
            statisticTableBinding.imageViewCustomDialogStatisticTableClose.setVisibility(View.GONE);
        }

        statisticTableBinding.textViewCustomDialogStatisticTableQuestionCount.setText(cozulenSoruSayisi + " / " + maksimumSoruSayisi);
        statisticTableBinding.textViewCustomDialogStatisticTableWordCount.setText(cozulenKelimeSayisi + " / " + maksimumKelimeSayisi);
        statisticTableBinding.textViewCustomDialogStatisticTableFalseGuessCount.setText(yapilanYanlisSayisi + " / " + (cozulenKelimeSayisi + yapilanYanlisSayisi));

        statisticTableBinding.progressBarCustomDialogStatisticTableQuestionCount.setProgress(cozulenSoruSayisi);
        statisticTableBinding.progressBarCustomDialogStatisticTableQuestionCount.setMax(maksimumSoruSayisi);
        statisticTableBinding.progressBarCustomDialogStatisticTableWordCount.setProgress(cozulenKelimeSayisi);
        statisticTableBinding.progressBarCustomDialogStatisticTableWordCount.setMax(maksimumKelimeSayisi);
        statisticTableBinding.progressBarCustomDialogStatisticTableFalseGuessCount.setProgress(yapilanYanlisSayisi);
        statisticTableBinding.progressBarCustomDialogStatisticTableFalseGuessCount.setMax((cozulenKelimeSayisi + yapilanYanlisSayisi));

        statisticTableBinding.imageViewCustomDialogStatisticTableClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statisticTableDialog.dismiss();
            }
        });

        statisticTableBinding.buttonCustomDialogStatisticTableMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainIntent();
            }
        });

        statisticTableBinding.buttonCustomDialogStatisticTablePlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent thisIntent = new Intent(PlayActivity.this, PlayActivity.class);
                thisIntent.putExtra("heartCount", Integer.valueOf(binding.textViewPlayActivityUserHeartCount.getText().toString()));
                finish();
                startActivity(thisIntent);
            }
        });

        statisticTableDialog.getWindow().setAttributes(params);
        statisticTableDialog.show();
    }

    private void maksimumVerileriHesapla(String oyunDurumu){
        maksimumKelimeSayisi = 0;
        maksimumSoruSayisi = 0;

        try {
            cursor = db.rawQuery("SELECT * FROM Kelimeler, Sorular WHERE Kelimeler.kKod = Sorular.sKod", null);
            maksimumKelimeSayisi = cursor.getCount();

            cursor = db.rawQuery("SELECT * FROM Sorular", null);
            maksimumSoruSayisi = cursor.getCount();

            cursor.close();

            istatistikTablosunuGoster(oyunDurumu, maksimumSoruSayisi, maksimumKelimeSayisi, cozulenSoruSayisi, cozulenKelimeSayisi, yapilanYanlisSayisi);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadBanner(){
        adView = new AdView(this);
        adView.setAdUnitId(TEST_AD_UNIT_ID_BANNER);
        adView.setAdSize(getAdSize());

        binding.adViewPlayActivityBanner.removeAllViews();
        binding.adViewPlayActivityBanner.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize(){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int adWidthPixels = displayMetrics.widthPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            adWidthPixels = windowMetrics.getBounds().width();
        }

        float density = displayMetrics.density;

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    @Override
    public void onBackPressed() {
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Kelime Bilmece");
        alert.setMessage("Geri Dönmek İstediğinize Emin Misiniz?");
        alert.setIcon(R.mipmap.ic_kelimebilmece);
        alert.setPositiveButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainIntent();
            }
        });
        alert.show();
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
            binding.textViewPlayActivityUserHeartCount.setText("+" + hsayisi);
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
                cozulenKelimeSayisi++;

                if (kelimelerLisetesi.size() > 0){
                    randomKelimeGetir();
                }else {
                    if (sorularListesi.size() > 0){
                        randomSoruGetir();
                        cozulenSoruSayisi++;
                    }else {
                        maksimumVerileriHesapla("oyunBitti");
                    }

                }
            }else{
                if (hakSayisi > 0){
                    sonHakSayisi = sonHakSayisi;
                    hakSayisi--;
                    yapilanYanlisSayisi++;
                    kalanHakkiKaydet(hakSayisi, sonHakSayisi);
                    Toast.makeText(getApplicationContext(), "Yanliş Tahminde Bulundunuz, Can Sayınız Bir Azaldı.", Toast.LENGTH_SHORT).show();
                }else {
                    maksimumVerileriHesapla("oyunBitti");
                    Toast.makeText(PlayActivity.this, "Oyun Bitti.", Toast.LENGTH_SHORT).show();
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