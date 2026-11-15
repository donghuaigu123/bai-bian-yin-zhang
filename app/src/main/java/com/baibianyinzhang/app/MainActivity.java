package com.baibianyinzhang.app;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 百变印章主界面。
 *
 * 设计目标：真正的傻瓜式操作 —— 打开就能看到成品，
 * 只需「打字 → 点形状 → 点字体 → 点保存」，所有变化都实时预览。
 * 不引入任何第三方库，全部使用系统原生控件。
 */
public class MainActivity extends Activity {

    private static final int REQ_STORAGE = 1001;
    private static final int OUTPUT_SIZE = 1600;      // 导出高清图尺寸
    private static final int PREVIEW_SIZE = 760;      // 界面预览尺寸
    private static final int TEXT_MAIN = 0xFF2B2B2B;
    private static final int TEXT_SUB = 0xFF8A8378;
    private static final int SEAL_RED = 0xFFC8282E;

    private EditText etText;
    private LinearLayout llQuick;
    private LinearLayout llTemplates;
    private LinearLayout llFonts;
    private ImageView ivPreview;
    private Button btnSave;
    private Button btnShare;

    private final List<SealTemplate> templates = SealTemplate.defaults();
    private final List<FontItem> fonts = new ArrayList<FontItem>();
    private final List<View> templateCards = new ArrayList<View>();
    private final List<View> fontCards = new ArrayList<View>();
    private final Map<String, Typeface> typefaceCache = new HashMap<String, Typeface>();

    private int selectedTemplate = 0;
    private int selectedFont = 0;
    private boolean hintedFallback = false;
    private float density = 1f;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingRender;

    private static final String[] QUICK_WORDS = {
            "张三", "李明", "平安喜乐", "福", "知足常乐",
            "岁月静好", "大吉大利", "一帆风顺", "墨韵", "长乐未央"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        density = getResources().getDisplayMetrics().density;

        etText = (EditText) findViewById(R.id.et_text);
        llQuick = (LinearLayout) findViewById(R.id.ll_quick);
        llTemplates = (LinearLayout) findViewById(R.id.ll_templates);
        llFonts = (LinearLayout) findViewById(R.id.ll_fonts);
        ivPreview = (ImageView) findViewById(R.id.iv_preview);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnShare = (Button) findViewById(R.id.btn_share);

        initFonts();
        buildQuickWords();
        buildTemplateCards();
        buildFontCards();
        refreshSelection();
        bindEvents();
        requestStoragePermissionIfNeeded();
        renderPreview();
    }

    private void initFonts() {
        fonts.add(new FontItem("fonts/kaishu.ttf", "马善政楷书", "楷书", false));
        fonts.add(new FontItem("fonts/xingkai.ttf", "龙藏行楷", "行楷", false));
        fonts.add(new FontItem("fonts/xingshu.ttf", "志莽行书", "行书", false));
        fonts.add(new FontItem("fonts/caoshu.ttf", "刘建毛草", "草书", false));
        fonts.add(new FontItem("fonts/lishu.ttf", "教育部隶书", "隶书", true));
        fonts.add(new FontItem("fonts/zhuanshu.ttf", "说文小篆", "篆书", true));
        fonts.add(new FontItem("fonts/songti.ttf", "站酷小薇", "宋韵", false));
        fonts.add(new FontItem("fonts/heiti.ttf", "庆科黄油", "现代", false));
        fonts.add(new FontItem("fonts/quwei.ttf", "站酷快乐", "趣味", false));
    }

    // ------------------------------------------------------------ 界面构建

    private void buildQuickWords() {
        for (int i = 0; i < QUICK_WORDS.length; i++) {
            final String word = QUICK_WORDS[i];
            TextView chip = new TextView(this);
            chip.setText(word);
            chip.setTextSize(12f);
            chip.setTextColor(TEXT_MAIN);
            chip.setPadding(dp(12), dp(6), dp(12), dp(6));
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(0xFFF0EBE2);
            gd.setCornerRadius(dp(14));
            gd.setStroke(1, 0xFFDCD3C4);
            chip.setBackground(gd);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(7), 0);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etText.setText(word);
                    etText.setSelection(word.length());
                    scheduleRender();
                }
            });
            llQuick.addView(chip);
        }
    }

    private void buildTemplateCards() {
        Typeface sampleFont = typeface(fonts.get(0));
        for (int i = 0; i < templates.size(); i++) {
            SealTemplate tpl = templates.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER_HORIZONTAL);
            card.setPadding(dp(7), dp(7), dp(7), dp(6));
            card.setClickable(true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    dp(96), LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(5), dp(2), dp(5), dp(2));
            card.setLayoutParams(lp);

            ImageView thumb = new ImageView(this);
            thumb.setImageBitmap(SealRenderer.render(tpl, sampleFont, null, "印", 180));
            thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
            thumb.setLayoutParams(new LinearLayout.LayoutParams(dp(78), dp(78)));
            card.addView(thumb);

            TextView label = new TextView(this);
            label.setText(tpl.name);
            label.setTextSize(11f);
            label.setTextColor(TEXT_MAIN);
            label.setSingleLine(true);
            label.setGravity(Gravity.CENTER);
            label.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            card.addView(label);

            final int index = i;
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTemplate = index;
                    refreshSelection();
                    scheduleRender();
                }
            });
            llTemplates.addView(card);
            templateCards.add(card);
        }
    }

    private void buildFontCards() {
        for (int i = 0; i < fonts.size(); i++) {
            FontItem item = fonts.get(i);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER_HORIZONTAL);
            card.setPadding(dp(7), dp(7), dp(7), dp(6));
            card.setClickable(true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    dp(84), LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(5), dp(2), dp(5), dp(2));
            card.setLayoutParams(lp);

            ImageView sample = new ImageView(this);
            sample.setImageBitmap(SealRenderer.renderFontSample(typeface(item), "印", 120));
            sample.setScaleType(ImageView.ScaleType.FIT_CENTER);
            sample.setLayoutParams(new LinearLayout.LayoutParams(dp(58), dp(58)));
            card.addView(sample);

            TextView label = new TextView(this);
            label.setText(item.name);
            label.setTextSize(11f);
            label.setTextColor(TEXT_MAIN);
            label.setSingleLine(true);
            label.setGravity(Gravity.CENTER);
            card.addView(label);

            TextView style = new TextView(this);
            style.setText(item.style);
            style.setTextSize(9f);
            style.setTextColor(TEXT_SUB);
            style.setSingleLine(true);
            style.setGravity(Gravity.CENTER);
            card.addView(style);

            final int index = i;
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedFont = index;
                    refreshSelection();
                    scheduleRender();
                    if (fonts.get(index).needFallback && !hintedFallback) {
                        hintedFallback = true;
                        Toast.makeText(MainActivity.this,
                                "古体字库收录字有限，个别生僻字会自动用楷体补上",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
            llFonts.addView(card);
            fontCards.add(card);
        }
    }

    private void refreshSelection() {
        for (int i = 0; i < templateCards.size(); i++) {
            applyCardBackground(templateCards.get(i), i == selectedTemplate);
        }
        for (int i = 0; i < fontCards.size(); i++) {
            applyCardBackground(fontCards.get(i), i == selectedFont);
        }
    }

    private void applyCardBackground(View card, boolean selected) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(selected ? 0x33C8282E : 0xFFF0EBE2);
        gd.setCornerRadius(dp(10));
        gd.setStroke(selected ? dp(2) : 1, selected ? SEAL_RED : 0xFFDCD3C4);
        card.setBackground(gd);
    }

    // ------------------------------------------------------------ 交互与预览

    private void bindEvents() {
        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleRender();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveBitmap();
                    Toast.makeText(MainActivity.this,
                            "已保存！到相册「百变印章」文件夹查看高清 PNG",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                            "保存失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = saveBitmap();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/png");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "分享印章"));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                            "分享失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /** 输入防抖，避免每敲一个字都重绘 */
    private void scheduleRender() {
        if (pendingRender != null) {
            handler.removeCallbacks(pendingRender);
        }
        pendingRender = new Runnable() {
            @Override
            public void run() {
                renderPreview();
            }
        };
        handler.postDelayed(pendingRender, 120);
    }

    private void renderPreview() {
        FontItem item = fonts.get(selectedFont);
        Typeface font = typeface(item);
        Typeface fallback = item.needFallback ? typeface(fonts.get(0)) : null;
        Bitmap bitmap = SealRenderer.render(templates.get(selectedTemplate), font, fallback,
                etText.getText().toString(), PREVIEW_SIZE);
        ivPreview.setImageBitmap(bitmap);
    }

    private Typeface typeface(FontItem item) {
        Typeface tf = typefaceCache.get(item.asset);
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(getAssets(), item.asset);
            } catch (Exception e) {
                tf = Typeface.DEFAULT;
            }
            typefaceCache.put(item.asset, tf);
        }
        return tf;
    }

    // ------------------------------------------------------------ 导出图片

    /** 生成 1600×1600 高清 PNG 并写入系统相册，返回可分享的 Uri */
    private Uri saveBitmap() throws Exception {
        FontItem item = fonts.get(selectedFont);
        Typeface font = typeface(item);
        Typeface fallback = item.needFallback ? typeface(fonts.get(0)) : null;
        String text = SealRenderer.sanitize(etText.getText().toString());
        Bitmap bitmap = SealRenderer.render(templates.get(selectedTemplate), font, fallback,
                text, OUTPUT_SIZE);

        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String fileName = "百变印章_" + safeFileName(text) + "_" + stamp + ".png";

        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/百变印章");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            Uri uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                throw new IllegalStateException("无法创建相册条目");
            }
            OutputStream out = getContentResolver().openOutputStream(uri);
            if (out == null) {
                throw new IllegalStateException("无法写入图片");
            }
            try {
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw new IllegalStateException("PNG 编码失败");
                }
                out.flush();
            } finally {
                out.close();
            }
            ContentValues done = new ContentValues();
            done.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, done, null, null);
            return uri;
        }

        String url = MediaStore.Images.Media.insertImage(
                getContentResolver(), bitmap, fileName, "由「百变印章」生成");
        if (url == null) {
            throw new IllegalStateException("写入相册失败");
        }
        return Uri.parse(url);
    }

    private static String safeFileName(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");
    }

    private void requestStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_STORAGE);
        }
    }

    private int dp(int value) {
        return Math.round(value * density);
    }
}
