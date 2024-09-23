package com.elyesasimsek.kelimebilmece;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.elyesasimsek.kelimebilmece.databinding.ActivityMainBinding;
import com.elyesasimsek.kelimebilmece.databinding.CustomDialogChangeNameBinding;
import com.elyesasimsek.kelimebilmece.databinding.CustomDialogSettingsBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SQLiteDatabase db;
    private SQLiteStatement statement;
    private String SQLSorgusu;
    private Cursor cursor;
    private int heartIndex, nameIndex, heartCount, imgHeartDuration = 2000, sonCanDurumu;
    private AdView adView;
    private static String AD_UNIT_ID_BANNER = "ca-app-pub-4632976048436433/4037360733";
    private static String TEST_AD_UNIT_ID_BANNER = "ca-app-pub-3940256099942544/9214589741";
    private RewardedAd rewardedAd;
    private static String AD_UNIT_ID_REWARDED = "ca-app-pub-4632976048436433/3805436189";
    private static String TEST_AD_UNIT_ID_REWARDED = "ca-app-pub-3940256099942544/5224354917";
    private ImageView imageViewHeart;
    private Bitmap bitmapimgHeart;
    private ConstraintLayout.LayoutParams heartParams;
    private float imgHeartXPos, imgHeartYPos;
    private ObjectAnimator objectAnimatorHeartX, objectAnimatorHeartY;
    private AnimatorSet imgHeartAnimatorSet;

    WindowManager.LayoutParams params;
    private Dialog settingsDialog;
    private Dialog changeNameDialog;
    private String getChangeName;

    private int izinVerme = 0, izinVerildi = 1;
    private Bitmap resimBitmap;
    private Uri resimUri;
    private ActivityResultLauncher<Intent> resimDegistirLauncher;
    private ImageDecoder.Source resimDosyasi;
    private AlertDialog.Builder alertBuilder;

    private byte[] resimByte;
    private int resimIndex;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean muzikDurumu;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivityConstraint), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadRewardedAd();
        mediaPlayer = MediaPlayer.create(this, R.raw.gametheme);

        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();

        preferences = this.getSharedPreferences("com.elyesasimsek.kelimebilmece", MODE_PRIVATE);
        muzikDurumu = preferences.getBoolean("muzikDurumu", true);

        try {
            db = this.openOrCreateDatabase("KelimeBulmaca", MODE_PRIVATE, null);
            cursor = db.rawQuery("SELECT * FROM Ayarlar", null);

            heartIndex = cursor.getColumnIndex("kHeart");
            nameIndex = cursor.getColumnIndex("kAdi");
            resimIndex = cursor.getColumnIndex("kImage");
            cursor.moveToFirst();

            resimByte = cursor.getBlob(resimIndex);
            if (resimByte != null) {
                resimBitmap = BitmapFactory.decodeByteArray(resimByte, 0, resimByte.length);
                binding.circleImageViewMainActivityProfile.setImageBitmap(resimBitmap);
            }

            heartCount = Integer.valueOf(cursor.getString(heartIndex));
            binding.textViewMainActivityUserHeartCount.setText("+" + heartIndex);
            binding.textViewMainActivityUserName.setText(cursor.getString(nameIndex));

            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        binding.adViewMainActivityBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                binding.adViewMainActivityBanner.setVisibility(View.VISIBLE);
            }
        });

        imageViewHeart = new ImageView(this);
        bitmapimgHeart = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.heart);
        imageViewHeart.setImageBitmap(bitmapimgHeart);
        heartParams = new ConstraintLayout.LayoutParams(96,96);
        imageViewHeart.setLayoutParams(heartParams);
        imageViewHeart.setX(0);
        imageViewHeart.setY(0);
        imageViewHeart.setVisibility(View.INVISIBLE);
        binding.mainActivityConstraint.addView(imageViewHeart);

        loadBanner();
    }

    public void btnAyarlar(View view){
        ayarlariGoster();
    }

    private void ayarlariGoster(){
            CustomDialogSettingsBinding settingsBinding;
            settingsBinding = DataBindingUtil.inflate(LayoutInflater.from(MainActivity.this), R.layout.custom_dialog_settings, null, false);

            settingsDialog = new Dialog(this);
            params = new WindowManager.LayoutParams();
            params.copyFrom(settingsDialog.getWindow().getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            settingsDialog.setCancelable(false);
            settingsDialog.setContentView(settingsBinding.getRoot());

            if (muzikDurumu){
                settingsBinding.radioButtonCustomDialogSettingsMusicOpen.setChecked(true);
            }else {
                settingsBinding.radioButtonCustomDialogSettingsMusicClose.setChecked(true);
            }

            View.OnClickListener listener = view -> {
                if (view.getId() == R.id.radioButtonCustomDialogSettingsMusicOpen) {
                    settingsBinding.radioButtonCustomDialogSettingsMusicOpen.setChecked(true);
                    settingsBinding.radioButtonCustomDialogSettingsMusicClose.setChecked(false);
                    muzikAcKapaAyari(true);
                } else if (view.getId() == R.id.radioButtonCustomDialogSettingsMusicClose) {
                    settingsBinding.radioButtonCustomDialogSettingsMusicOpen.setChecked(false);
                    settingsBinding.radioButtonCustomDialogSettingsMusicClose.setChecked(true);
                    muzikAcKapaAyari(false);
                }
            };


        settingsBinding.radioButtonCustomDialogSettingsMusicOpen.setOnClickListener(listener);
        settingsBinding.radioButtonCustomDialogSettingsMusicClose.setOnClickListener(listener);

            settingsBinding.imageViewCustomDialogSettingsClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settingsDialog.dismiss();
                }
            });

            settingsBinding.buttonCustomDialogChangeName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settingsDialog.dismiss();
                    isimDegistirDiyalog();
                }
            });

            settingsBinding.buttonCustomDialogChangeProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Android 10 ve üzeri için
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_MEDIA_IMAGES)) {
                                // Kullanıcı daha önce izni reddetti, neden bu izne ihtiyaç duyduğunuzu açıklayın
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("İzin Gerekli")
                                        .setMessage("Galeriye erişmek için izin gereklidir.")
                                        .setPositiveButton("İzin Ver", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_MEDIA_IMAGES}, izinVerme))
                                        .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                                        .show();
                            } else {
                                // İzin iste
                                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_MEDIA_IMAGES}, izinVerme);
                            }
                        } else {
                            // İzin zaten verilmiş, galeriyi aç
                            openGallery();
                        }
                    } else {
                        // Android 10 öncesi için
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // Kullanıcı daha önce izni reddetti, neden bu izne ihtiyaç duyduğunuzu açıklayın
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("İzin Gerekli")
                                        .setMessage("Galeriye erişmek için izin gereklidir.")
                                        .setPositiveButton("İzin Ver", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, izinVerme))
                                        .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                                        .show();
                            } else {
                                // İzin iste
                                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, izinVerme);
                            }
                        } else {
                            // İzin zaten verilmiş, galeriyi aç
                            openGallery();
                        }
                    }
                }
            });
            settingsDialog.getWindow().setAttributes(params);
            settingsDialog.show();
    }

    private void muzikAcKapaAyari(boolean b){
        editor = preferences.edit();
        editor.putBoolean("muzikDurumu", b);
        editor.apply();

        if (b) {
            // Müzik aç
            mediaPlayer.start();
        } else {// Müzik kapat
            mediaPlayer.pause(); // veya mediaPlayer.stop();
        }

        Toast.makeText(getApplicationContext(), "Ayar Başarıyla Kayıt Edildi", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        resimDegistirLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    resimUri = data.getData();
                    showConfirmationDialog();
                }
            }
        });
    }

    private void profilResminiKaydet(Bitmap profilResmi){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            profilResmi.compress(Bitmap.CompressFormat.PNG, 75, outputStream);
            resimByte = outputStream.toByteArray();

            SQLSorgusu = "UPDATE Ayarlar SET kImage = ? WHERE kAdi = ?";
            statement = db.compileStatement(SQLSorgusu);
            statement.bindBlob(1, resimByte);
            statement.bindString(2, binding.textViewMainActivityUserName.getText().toString());
            statement.execute();

            if (settingsDialog.isShowing()){
                settingsDialog.dismiss();
            }
            Toast.makeText(getApplicationContext(), "Profil Resminiz Başarıyla Değiştirildi.", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void isimDegistirDiyalog(){
        CustomDialogChangeNameBinding nameBinding;
        nameBinding = DataBindingUtil.inflate(LayoutInflater.from(MainActivity.this), R.layout.custom_dialog_change_name, null, false);

        changeNameDialog = new Dialog(this);
        params = new WindowManager.LayoutParams();
        changeNameDialog.setCancelable(false);
        changeNameDialog.setContentView(nameBinding.getRoot());
        params.copyFrom(changeNameDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        changeNameDialog.getWindow().setAttributes(params);

        nameBinding.imageViewCustomDialogChangeNameClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNameDialog.dismiss();
            }
        });

        nameBinding.buttonCustomDialogChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChangeName = nameBinding.editTextCustomDialogChangeName.getText().toString();

                if (!TextUtils.isEmpty(getChangeName)){
                    if (!(getChangeName.matches(binding.textViewMainActivityUserName.getText().toString()))){
                        ismiGuncelle(getChangeName, binding.textViewMainActivityUserName.getText().toString());
                    }else {
                        Toast.makeText(MainActivity.this, "Zaten Bu İsmi Kullanıyorsunuz.", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "İsim Değeri Boş Olamaz.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        changeNameDialog.show();
    }

    private void ismiGuncelle(String yeniDeger, String eskiDeger){
        try {
            SQLSorgusu = "UPDATE Ayarlar SET kAdi = ? WHERE kAdi = ?";
            statement = db.compileStatement(SQLSorgusu);
            statement.bindString(1, yeniDeger);
            statement.bindString(2, eskiDeger);
            statement.execute();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.textViewMainActivityUserName.setText(yeniDeger);
                    Toast.makeText(MainActivity.this, "İsminiz Başarıyla Değiştirildi", Toast.LENGTH_SHORT).show();
                }
            });

            if (changeNameDialog.isShowing()){
                changeNameDialog.dismiss();
            }
        }catch (Exception e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "İsim Güncellenirken Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void btnHakKazan(View view) {
        showRewardAd();
    }

    private void loadBanner(){
        adView = new AdView(this);
        adView.setAdUnitId(TEST_AD_UNIT_ID_BANNER);
        adView.setAdSize(getAdSize());

        binding.adViewMainActivityBanner.removeAllViews();
        binding.adViewMainActivityBanner.addView(adView);

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


    private void loadRewardedAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(getApplicationContext(),  TEST_AD_UNIT_ID_REWARDED,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        rewardedAd = null;

                    }

                    @Override
                    public void onAdLoaded(@NonNull com.google.android.gms.ads.rewarded.RewardedAd ad) {
                        rewardedAd = ad;
                        Toast.makeText(getApplicationContext(), "Video Yüklendi.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRewardAd(){
        if (rewardedAd == null){
            return;
        }

        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                rewardedAd = null;
                loadRewardedAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                rewardedAd = null;
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
            }
        });

        if (rewardedAd != null) {
            rewardedAd.show(MainActivity.this, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Kullanıcı ödül kazandığında yapılacak işlemler,
                    imageViewHeart.setX(binding.mainActivityConstraint.getPivotX());
                    imageViewHeart.setY(binding.mainActivityConstraint.getPivotY());
                    imageViewHeart.setVisibility(View.VISIBLE);

                    imgHeartXPos = (binding.imageViewMainActivityHeartDesign.getX() + ((float) binding.imageViewMainActivityHeartDesign.getWidth() / 2.5f));
                    imgHeartYPos = (binding.imageViewMainActivityHeartDesign.getY() + ((float) binding.imageViewMainActivityHeartDesign.getHeight() / 2f) + 20);

                    objectAnimatorHeartX = ObjectAnimator.ofFloat(imageViewHeart, "x", imgHeartXPos);
                    objectAnimatorHeartX.setDuration(imgHeartDuration);

                    objectAnimatorHeartY = ObjectAnimator.ofFloat(imageViewHeart, "y", imgHeartYPos);
                    objectAnimatorHeartY.setDuration(imgHeartDuration);

                    imgHeartAnimatorSet = new AnimatorSet();
                    imgHeartAnimatorSet.playTogether(objectAnimatorHeartX);
                    imgHeartAnimatorSet.playTogether(objectAnimatorHeartY);
                    imgHeartAnimatorSet.start();
                    objectAnimatorHeartY.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            imageViewHeart.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "+1 Can Kazandın.", Toast.LENGTH_SHORT).show();
                            sonCanDurumu = heartCount;
                            heartCount++;
                            canMiktariniGuncelle(sonCanDurumu, heartCount);
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                            super.onAnimationRepeat(animation);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                        }

                        @Override
                        public void onAnimationPause(Animator animation) {
                            super.onAnimationPause(animation);
                        }

                        @Override
                        public void onAnimationResume(Animator animation) {
                            super.onAnimationResume(animation);
                        }
                    });

                    // Reklam kapatıldığında yeni bir reklam yükleyin
                    rewardedAd = null;
                }
            });
        } else {
            // Reklam yüklenmedi, hata mesajı gösterin veya tekrar yüklemeyi deneyin

        }
    }

    private void canMiktariniGuncelle(int sonCanSayisi, int canSayisi){
        try {
            SQLSorgusu = "UPDATE Ayarlar SET kHeart = ? WHERE kHeart = ?";
            statement = db.compileStatement(SQLSorgusu);
            statement.bindString(1, String.valueOf(canSayisi));
            statement.bindString(2, String.valueOf(sonCanSayisi));
            statement.execute();
            binding.textViewMainActivityUserHeartCount.setText("+" + canSayisi);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Galeriyi açan metot
    private void openGallery() {
        Intent resimDegistirIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resimDegistirLauncher.launch(resimDegistirIntent);
    }

    // İzin isteme sonucunu işleyen metot
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == izinVerme) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi, galeriyi aç
                openGallery();
            } else {// İzin reddedildi
                // Kullanıcıya izin gerekliliğini açıklayın veya alternatif bir çözüm sunun
            }
        }
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Kelime Bilmece")
                .setMessage("Profil Resminizi Değiştirmek İstediğinize Emin Misiniz?")
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setPositiveButton("Evet", (dialog, which) -> {
                    loadAndSetImage();
                    dialog.dismiss();
                })
                .setNegativeButton("Hayır", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadAndSetImage() {
        try {
            resimBitmap = getResizedBitmap(resimUri);
            binding.circleImageViewMainActivityProfile.setImageBitmap(resimBitmap);
            profilResminiKaydet(resimBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getResizedBitmap(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // Resim boyutunu hesaplayın ve yeniden boyutlandırın
        int scale = 1;
        while ((options.outWidth / scale) / 2 >= binding.circleImageViewMainActivityProfile.getWidth() &&
                (options.outHeight / scale) / 2 >= binding.circleImageViewMainActivityProfile.getHeight()) {
            scale *= 2;
        }

        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        return bitmap;
    }
}