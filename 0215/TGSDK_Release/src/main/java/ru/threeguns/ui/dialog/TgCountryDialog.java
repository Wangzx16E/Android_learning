package ru.threeguns.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kh.hyper.utils.HL;
import ru.threeguns.R;
import ru.threeguns.utils.CountryBean;
import ru.threeguns.utils.TgCountryBean;

public class TgCountryDialog extends Dialog {
    public final String[] langs = {"en", "zh", "zh", "id", "ru", "es"};
    private JSONArray jsonArray ;
    private GridView gridView ,tg_hot_gv,tg_search_gv;
    private Context context;
    private OnItemClick onItemClick;
    private OnhotItemClick onhotItemClick;
    private OnsearchItemClick onsearchItemClick;
    private EditText tg_search;
    private ScrollView tg_country_sv,search_choose;
    private List<CountryBean> searchlist = new ArrayList();

    public String appLanguage;
    private JSONArray hotjsonArray;
    public TgCountryDialog(Context context , JSONArray jsonArray, JSONArray hotjsonArray) {
        super(context, R.style.exit_cast_activity_style);
        this.jsonArray = jsonArray;
        this.hotjsonArray = hotjsonArray;
        this.context = context;
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_tgcountry,null,false);
        gridView = (GridView) v.findViewById(R.id.tg_gv);
        tg_search = (EditText) v.findViewById(R.id.tg_search);
        tg_hot_gv = (GridView) v.findViewById(R.id.tg_hot_gv);
        tg_country_sv = (ScrollView) v.findViewById(R.id.tg_country_sv);
        tg_search_gv = (GridView) v.findViewById(R.id.tg_search_gv);
        search_choose = (ScrollView) v.findViewById(R.id.search_choose);
        initData();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(v,params);
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity= Gravity.BOTTOM;
        layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height= WindowManager.LayoutParams.MATCH_PARENT;

        getWindow().getDecorView().setPadding(0, 0, 0, 0);

        getWindow().setAttributes(layoutParams);
    }

    private void initData(){

        String lang = "1";
        if (!TextUtils.isEmpty(lang)) {
            Locale locale = getContext().getResources().getConfiguration().locale;
            appLanguage = locale.getLanguage();
            HL.w(">>>>>>>>>>>>>>>>>>>>>>  Language" + appLanguage);
            for (int i = 1; i <= langs.length; i++) {
                if (appLanguage.startsWith(langs[i-1])) {
                    lang = String.valueOf(i);
                    if (langs[i-1].equals("zh")) {
                        HL.w(">>>>>>>>>>>>>>>>>>>>>>   Country" + locale.getCountry());
                        if (locale.getCountry().indexOf("cn") == 0 || locale.getCountry().indexOf("CN") == 0) {
                            lang = "2";
                        } else {
                            lang = "3";
                        }
                        break;
                    }
                }
            }
            if (TextUtils.isEmpty(lang)) {
                lang = "1";
            }
        }else{
            lang = "2";
        }
        appLanguage = lang;

        Log.e("TAGlalalalla", "initData: " + appLanguage );
            setCanceledOnTouchOutside(false);
            GridViewAdapter gridViewAdapter = new GridViewAdapter(context,jsonArray);
            gridView.setAdapter(gridViewAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onItemClick.Onclick(view , position);
                }
            });

            GridViewAdapter hotgridViewAdapter = new GridViewAdapter(context,hotjsonArray);
            tg_hot_gv.setAdapter(hotgridViewAdapter);

            tg_hot_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onhotItemClick.Onclick(view , position);
                }
            });

        tg_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    String content = tg_search.getText().toString();
                    if (null == content || "".equals(content)){
                        tg_country_sv.setVisibility(View.VISIBLE);
                        search_choose.setVisibility(View.GONE);
                    }else{
                        searchlist.clear();
                        tg_country_sv.setVisibility(View.GONE);
                        search_choose.setVisibility(View.VISIBLE);
                        for (int i=0;i<jsonArray.length();i++){
                            if (jsonArray.getJSONObject(i).getString("chineseName").contains(content) ||
                                    jsonArray.getJSONObject(i).getString("country").contains(content) ||
                                    jsonArray.getJSONObject(i).getString("region").contains(content) ){
                                CountryBean countryBean = new CountryBean();
                                countryBean.setChineseName(jsonArray.getJSONObject(i).getString("chineseName"));
                                countryBean.setCountry(jsonArray.getJSONObject(i).getString("country"));
                                countryBean.setRegion(jsonArray.getJSONObject(i).getString("region"));
                                searchlist.add(countryBean);
                            }
                        }

                        GridViewSearchAdapter gridViewSearchAdapter = new GridViewSearchAdapter(context,searchlist);
                        tg_search_gv.setAdapter(gridViewSearchAdapter);
                        tg_search_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                onsearchItemClick.Onclick(view,position,searchlist);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void setOnItemClick (OnItemClick onItemClick){
        this.onItemClick = onItemClick;
    }

    public void setOnhotItemClick (OnhotItemClick onhotItemClick){
        this.onhotItemClick = onhotItemClick;
    }

    public void setOnsearchItemClick (OnsearchItemClick onsearchItemClick){
        this.onsearchItemClick = onsearchItemClick;
    }

    public interface OnItemClick {
        void Onclick(View v, int pos);
    }

    public interface OnhotItemClick {
        void Onclick(View v, int pos);
    }

    public interface OnsearchItemClick {
        void Onclick(View v, int pos, List<CountryBean> searchlist);
    }


    class GridViewAdapter extends BaseAdapter {

        private Context context;
        private JSONArray jsonArray;
        private Holder holder;

        public GridViewAdapter (Context context, JSONArray jsonArray){
            this.jsonArray = jsonArray;
            this.context = context;
        }

        @Override
        public int getCount() {
            return jsonArray.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return jsonArray.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.tg_item_gridview,parent,false);
                holder = new Holder();
                holder.tv1 = (TextView) convertView.findViewById(R.id.tgcountry);
                holder.tv2 = (TextView) convertView.findViewById(R.id.tgquhao);
                convertView.setTag(holder);
            }else{
                holder = (Holder) convertView.getTag();
            }
            try {
                if ("3".equals(appLanguage) || "2".equals(appLanguage)){
                    holder.tv1.setText(jsonArray.getJSONObject(position).getString("chineseName"));
                    Log.e("TAGlalalalla111111111", "initData: " + appLanguage );
                }else{
                    holder.tv1.setText(jsonArray.getJSONObject(position).getString("country"));
                    Log.e("TAGlalalalla222222222", "initData: " + appLanguage );
                }

                holder.tv2.setText(jsonArray.getJSONObject(position).getString("region"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class Holder{
            private TextView tv1,tv2;
        }

    }


    class GridViewSearchAdapter extends BaseAdapter {

        private Context context;
        private List<CountryBean> searchlist;
        private Holder holder;

        public GridViewSearchAdapter (Context context, List searchlist){
            this.searchlist = searchlist;
            this.context = context;
        }

        public void Refresh(List<CountryBean> hotlist){
            this.searchlist = hotlist;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return searchlist.size();
        }

        @Override
        public Object getItem(int position) {
            return searchlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.tg_item_gridview,parent,false);
                holder = new Holder();
                holder.tv1 = (TextView) convertView.findViewById(R.id.tgcountry);
                holder.tv2 = (TextView) convertView.findViewById(R.id.tgquhao);
                convertView.setTag(holder);
            }else{
                holder = (Holder) convertView.getTag();
            }

            if ("3".equals(appLanguage) || "2".equals(appLanguage)){
                holder.tv1.setText(searchlist.get(position).getChineseName());
            }else{
                holder.tv1.setText(searchlist.get(position).getCountry());
            }
                holder.tv2.setText(searchlist.get(position).getRegion());

            return convertView;
        }

        class Holder{
            private TextView tv1,tv2;
        }

    }
}
