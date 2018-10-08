package com.kengou.mapsigndemo;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.TextOptions;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN";
    MapView mapView;
    ProgressDialog progressDialog;

    AMap mAMap;
    Map<String, Float> mDistanceMap;
    ArrayList<TextOptions> mTextOptionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        initMap(mapView);

        mapView.onCreate(savedInstanceState);

        initMarkers();
    }

    private void initMap(MapView mapView){
        mAMap = mapView.getMap();
        // 因为地图旋转之后 label 也会跟着旋转 会导致后面的公式计算无效 所以先做这个不能旋转的设定
        mAMap.getUiSettings().setRotateGesturesEnabled(false);
        // 在地图缩放之后 添加重设 label 的函数
        mAMap.setAMapGestureListener(new AMapGestureListener() {
            @Override
            public void onDoubleTap(float v, float v1) {

            }

            @Override
            public void onSingleTap(float v, float v1) {

            }

            @Override
            public void onFling(float v, float v1) {

            }

            @Override
            public void onScroll(float v, float v1) {

            }

            @Override
            public void onLongPress(float v, float v1) {

            }

            @Override
            public void onDown(float v, float v1) {

            }

            @Override
            public void onUp(float v, float v1) {

            }

            @Override
            public void onMapStable() {
                resetTextOptionsList(mTextOptionsList, mDistanceMap);
            }
        });
    }

    private void initMarkers(){
        mAMap.addMarkers(MarkerData.MARKER_OPTIONS_LIST, false);
    }

    private void initMarkersText(){
        addMarkerLabel(MarkerData.MARKER_OPTIONS_LIST);
    }

    // 根据传入的 marker options 数组生成 label 然后添加到地图中
    private void addMarkerLabel(ArrayList<MarkerOptions> markerOptionsList){
        new InitTextOptionsTask(this).execute(markerOptionsList);
    }

    /**
     * 重新调整label的显示
     * @param textOptionsList label的数组
     * @param distanceMap     两个label之间的距离 包括横向和纵向
     */
    private void resetTextOptionsList(ArrayList<TextOptions> textOptionsList, Map<String, Float> distanceMap) {
        mAMap.clear();
        // 由于之前clear了 所以需要把添加的marker重新添加回来
        initMarkers();
        // 计算哪些label需要显示 存储在数组中
        boolean[] visibleArray = calVisibleLabelArray(textOptionsList, distanceMap, mAMap.getScalePerPixel());
        // 根据数组判断是否需要显示 需要显示的添加到地图中去
        for (int i = 0; i < textOptionsList.size(); i++) {
            if (visibleArray[i]) {
                mAMap.addText(textOptionsList.get(i));
            }
        }
    }

    /**
     * 计算所有label的显示隐藏
     * @param textOptionsList
     * @param distanceMap
     * @param scalePerPixel
     * @return
     */
    private boolean[] calVisibleLabelArray(ArrayList<TextOptions> textOptionsList, Map<String, Float> distanceMap, float scalePerPixel) {
        boolean[] visibleArray = new boolean[textOptionsList.size()];
        // 初始化数组 都为true
        for (int i = 0; i < visibleArray.length; i++) {
            visibleArray[i] = true;
        }
        // 比较规则 当前的这个label和这个label之前的所有label比较
        for (int i = 1; i < textOptionsList.size(); i++) {
            TextOptions options2 = textOptionsList.get(i);
            // 和之前的所有的label进行比较
            // j >=0 && visibleArray[i]  如果这个label已经判断为不显示 则直接跳出循环 提高效率
            for (int j = i - 1; j >= 0 && visibleArray[i]; j--) {
                TextOptions options1 = textOptionsList.get(j);
                float distanceVertical = distanceMap.get(j + "|" + i);
                float distanceHorizontal = distanceMap.get(j + "-" + i);
                visibleArray[i] &= isSecondLabelVisible(visibleArray[j], options1, options2, distanceVertical, distanceHorizontal, scalePerPixel);
            }
        }
        return visibleArray;
    }

    /**
     * 在第一个label显示的前提下 判断第二个label是否需要显示
     * @param options1             第一个label
     * @param options2             第二个label
     * @param distanceVertical     两个label地理位置的纵向距离 单位米
     * @param distanceHorizontal   两个label地理位置的横向距离 单位米
     * @param scalePerPixel        每个像素在地图上的长度 单位米
     * @return
     */
    private boolean isSecondLabelVisible(boolean isTextOptions1Visible, TextOptions options1, TextOptions options2, float distanceVertical, float distanceHorizontal, float scalePerPixel) {
        Log.i(TAG, "判断第二个是否显示, 第一个是否显示: " + isTextOptions1Visible + ", 判断第二个是否显示, 第一个点: " + options1.getText() + ", 第二个点: " + options2.getText());
        // 如果1不显示 则后一个直接判断为可以显示
        if (!isTextOptions1Visible){
            Log.i(TAG, "判断第二个是否显示: 第一个点不显示 所以直接判断可以显示");
            return true;
        } else {
            Log.i(TAG, "判断第二个是否显示: 第一个点显示 所以需要继续判断");
        }
        // 计算纵向距离是否重叠
        int options1TextSize = options1.getFontSize();
        int options2TextSize = options2.getFontSize();

        float currentDistanceVertical = (options1TextSize + options2TextSize) * scalePerPixel / 2;
        // 如果当前缩放比例下 两个label之间的纵向距离小于对应地理位置的纵向距离 则说明纵向不重叠 横向肯定也不重叠 所以显示
        if (currentDistanceVertical < distanceVertical) {
            Log.i(TAG, "判断第二个是否显示: 标签纵距离: " + currentDistanceVertical + ", 地理位置距离: " + distanceVertical + ", 小于实际距离 可以显示");
            return true;
        } else {
            Log.i(TAG, "判断第二个是否显示: 标签纵距离: " + currentDistanceVertical + ", 地理位置距离: " + distanceVertical + ", 大于实际距离 继续判断");
        }

        // 计算横向距离是否重叠
        // 如果当前缩放比例下 两个label之间的横向距离小于对应地理位置的横向距离 则说明横向不重叠 所以显示
        float currentDistanceHorizontal = ((options1.getText().length() * options1TextSize + options2.getText().length() * options2TextSize) * scalePerPixel / 2);
        if (currentDistanceHorizontal < distanceHorizontal) {
            Log.i(TAG, "判断第二个是否显示: 标签横距离: " + currentDistanceHorizontal + ", 地理位置距离: " + distanceHorizontal + ", 小于实际距离 可以显示");
            return true;
        } else {
            Log.i(TAG, "判断第二个是否显示: 标签横距离: " + currentDistanceHorizontal + ", 地理位置距离: " + distanceHorizontal + ", 大于实际距离 不能显示");
        }
        return false;
    }

    private void showLoadingDialog(String message){
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, "请稍等", message);
        }
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    public void setDistanceMap(Map<String, Float> distanceMap) {
        this.mDistanceMap = distanceMap;
    }

    public void setTextOptionsList(ArrayList<TextOptions> textOptionsList) {
        this.mTextOptionsList = textOptionsList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_init_label:
                initMarkersText();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    class InitTextOptionsTask extends AsyncTask<ArrayList<MarkerOptions>, Void, Void> {

        final double EARTH_R = 6371;

        WeakReference<MainActivity> mMainActivityWeakReference;
        Map<String, Float> mDistanceMap;
        ArrayList<TextOptions> mTextOptionsList;
        float mDensity;

        public InitTextOptionsTask(MainActivity mainActivity) {
            mMainActivityWeakReference = new WeakReference<>(mainActivity);
            mDensity = mainActivity.getResources().getDisplayMetrics().density;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mMainActivityWeakReference != null && mMainActivityWeakReference.get() != null) {
                mMainActivityWeakReference.get().showLoadingDialog("正在初始化标签");
            }
        }

        @Override
        protected Void doInBackground(ArrayList<MarkerOptions>... arrayLists) {
            ArrayList<MarkerOptions> markerOptionsList = arrayLists[0];
            // 生成label
            mTextOptionsList = generateTextOptionsList(markerOptionsList);
            // 计算距离
            mDistanceMap = generateMarkerDistanceList(markerOptionsList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainActivity mainActivity = mMainActivityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.hideLoadingDialog();
                mainActivity.setDistanceMap(mDistanceMap);
                mainActivity.setTextOptionsList(mTextOptionsList);
                mainActivity.resetTextOptionsList(mTextOptionsList, mDistanceMap);
            }
        }

        // 生成label数组
        private ArrayList<TextOptions> generateTextOptionsList(ArrayList<MarkerOptions> markerOptionsList) {
            ArrayList<TextOptions> textOptionsList = new ArrayList<>();
            for (MarkerOptions markerOptions : markerOptionsList) {
                textOptionsList.add(
                        new TextOptions()
                                .text(markerOptions.getTitle())
                                .position(markerOptions.getPosition())
                                .fontSize((int)(14 * mDensity))
                                .backgroundColor(Color.parseColor("#50000000")));
            }
            return textOptionsList;
        }

        // 生成地理位置之间 横纵两个距离
        // 横距离使用 "index1-index2"存储 总距离使用 "index1|index2"存储
        private Map<String, Float> generateMarkerDistanceList(ArrayList<MarkerOptions> markerOptionsList) {
            mDistanceMap = new HashMap<>();
            for (int i = 0; i < markerOptionsList.size() - 1; i++) {
                for (int j = i + 1; j < markerOptionsList.size(); j++) {
                    LatLng latLng1 = markerOptionsList.get(i).getPosition();
                    LatLng latLng2 = markerOptionsList.get(j).getPosition();
                    mDistanceMap.put(i + "-" + j, getDistanceX(latLng1, latLng2));
                    mDistanceMap.put(i + "|" + j, getDistanceY(latLng1, latLng2));
                }
            }
            return mDistanceMap;
        }

        /**
         * 大致计算两个经度的距离
         * 假设这两个坐标在同一个纬度上 通过经度之差来计算两个坐标之间的弧线距离
         * 如果觉得精度不够 可以通过更加精确的式子来计算
         * @param latLng1       坐标1
         * @param latLng2       坐标2
         * @return
         */
        private float getDistanceX(LatLng latLng1, LatLng latLng2){
            return (float)((Math.PI / 180) * (Math.abs(latLng2.longitude - latLng1.longitude)) * EARTH_R * 1000);
        }

        /**
         * 大致计算两个坐标的纬度距离
         * 假设这两个坐标在同一个纬度上 通过纬度之差来计算连个坐标之间的弧线距离
         * @param latLng1
         * @param latLng2
         * @return
         */
        private float getDistanceY(LatLng latLng1, LatLng latLng2){
            return (float)((Math.PI / 180) * (Math.abs(latLng2.latitude - latLng1.latitude)) * EARTH_R * 1000);
        }
    }
}
