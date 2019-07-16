package com.weather.troubleshooter.test.model;

import android.content.Context;
import android.databinding.ObservableField;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.weather.troubleshooter.test.R;
import com.weather.troubleshooter.test.bean.City;
import com.weather.troubleshooter.test.bean.CityList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class MainModel {

    private static String TAG = "MainModel";

    public ObservableField<String> mCityName = new ObservableField<>();
    public ObservableField<String> mUpDataTime = new ObservableField<>();
    public ObservableField<String> mWeather = new ObservableField<>();
    public ObservableField<String> mTemperature = new ObservableField<>();
    public ObservableField<String> mWind = new ObservableField<>();
    public ObservableField<ArrayAdapter> adapter = new ObservableField<>();

    private Context mContext;

    private CityList cityList;

    private ArrayList<City> mCity;

    private String cid;
    private String city;


    public MainModel(Context context) {
        this.mContext = context;

        initData();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        city = mCity.get(position).getCityName();
        getCityCid(mCity.get(position).getCityName());
    }

    private void getCityCid(String city) {
        HeWeather.getSearch(mContext, city, "world", 0, null, new HeWeather.OnResultSearchBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i(TAG, "Weather Now onError: ", throwable);
            }

            @Override
            public void onSuccess(Search search) {
               Search s = search;
                Log.i(TAG, search.getBasic().get(0).getCid());
                cid = search.getBasic().get(0).getCid();
                searchWeather();
            }
        });
    }

    private void searchWeather() {
        HeWeather.getWeatherNow(mContext, cid, Lang.CHINESE_SIMPLIFIED
                , Unit.METRIC, new HeWeather.OnResultWeatherNowBeanListener() {
                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "Weather Now onError: ", e);
                    }

                    @Override
                    public void onSuccess(Now dataObject) {
                        Log.i(TAG, " Weather Now onSuccess: " + new Gson().toJson(dataObject));
                        //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                        if (Code.OK.getCode().equalsIgnoreCase(dataObject.getStatus())) {
                            //此时返回数据

                            NowBase now = dataObject.getNow();
                            refreshData(dataObject.getUpdate().getLoc()
                                    , now.getCond_txt()
                                    , now.getTmp()
                                    , now.getWind_dir());

                        } else {
                            //在此查看返回数据失败的原因
                            String status = dataObject.getStatus();
                            Code code = Code.toEnum(status);
                            Log.i(TAG, "failed code: " + code);
                        }
                    }
                });

    }

    private void refreshData(String time, String w, String temp, String wind) {
        mCityName.set(city);
        mUpDataTime.set(time);
        mWeather.set(w);
        mTemperature.set(temp);
        mWind.set(wind);
    }

    private void initData() {
        Gson gson = new Gson();
        cityList = gson.fromJson(initAsset(), CityList.class);
        mCity = (ArrayList<City>) cityList.getCitys();
        ArrayList<String> citys = new ArrayList<>();
        for (int i = 0; i < mCity.size(); i++) {
            citys.add(mCity.get(i).getCityName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mContext,
                R.layout.item_select, citys);
        adapter.set(spinnerAdapter);
    }

    private String initAsset() {

        try {
            InputStreamReader inputReader = new
                    InputStreamReader(mContext.getAssets().open("city.txt"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }
}
