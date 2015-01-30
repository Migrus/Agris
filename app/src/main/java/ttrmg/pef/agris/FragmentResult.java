package ttrmg.pef.agris;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;


public class FragmentResult extends Fragment {
    // TODO: Rename and change types of parameters
    private Boolean back = false;
    private static final String DEBUG_TAG = "DEBUG_TAG";
    private ProgressDialog pDialog;
    // JSON Node names
    private static final String TAG_CONTACTS = "ceny";
    private static final String TAG_ID = "id";
    private static final String TAG_ID_KOMODITY = "id_komodity";
    private static final String TAG_NAZEV = "nazev";
    private static final String TAG_HODNOTA = "hodnota";
    private static final String TAG_JENODTKA_NAZEV = "jednotka_nazev";
    private static final String TAG_DATUM = "datum";
    private static final String TAG_MENA = "mena";
    private static final String TAG_MIRA = "mira";
    private static final String TAG_ALT_MENA = "alt_mena";

    private static final Double EUR = 27.8;
    private static final Double USD = 24.1;
    private static final Double USc = 100.0;

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;
    ArrayList<String> adapterList;
    private RadioGroup radioGroup,radioGroup2;

    private OnFragmentInteractionListener mListener;
    public static Integer komodity,razeniMena = 1;
    public static String razeniNazev = TAG_ID;


    ListView list2;

    public static FragmentResult newInstance(Integer pozice) {
        FragmentResult fragment = new FragmentResult();
        komodity = pozice;
        return fragment;
    }

    public FragmentResult() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        contactList = new ArrayList<HashMap<String, String>>();
        isConnect();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mListener.toast("onConfigurationChanged");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_result, container, false);
        adapterList = new ArrayList<>();
        list2 = (ListView) v.findViewById(R.id.list2);

        radioGroup2 = (RadioGroup)v.findViewById(R.id.mena);
        radioGroup = (RadioGroup)v.findViewById(R.id.sort);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioDefault) {
                    razeniNazev = TAG_ID;
                } else if(checkedId == R.id.radioName) {
                    razeniNazev = TAG_NAZEV;
                } else {
                    razeniNazev = TAG_HODNOTA;
                }
                razeni(razeniNazev,razeniMena);
            }

        });
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioCZK) {
                    razeniMena = 1;
                } else {
                    razeniMena = 0;
                }
                razeni(razeniNazev,razeniMena);
            }

        });


        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        back = true;
        mListener.toast("onPause");
    }

    @Override
    public void onResume() {
        super.onPause();
        if (back) {
            mListener.toast("onResume");
            back = false;
        }

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void toast(String toast);
    }
    String Zaokrouhli(String d) {
        int mezera = 0;
        for (int i=0; i < d.length();i++){
                if (d.substring(i, i) == ".") mezera = i;
        }
        mezera = mezera+2;
        return d.substring(0,mezera);
    }

    public void razeni(final String coRadime, Integer cena){
        Collections.sort(contactList, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> first, HashMap<String, String> second) {
                //return obj1.name.compareToIgnoreCase(obj2.name);
                String firstValue = first.get(coRadime);
                String secondValue = second.get(coRadime);
                return firstValue.compareTo(secondValue);
            }
        });

        ArrayList<SearchResults> results = new ArrayList<SearchResults>();
        for (int i = 0; i < contactList.size(); i++) {
            String date = contactList.get(i).get(TAG_DATUM).substring(6, 16);
            String datum = new SimpleDateFormat("MM/dd/yyyy").format(new Date(Integer.parseInt(date) * 1000L));

            Double ciziCena = 0.0;
            String ciziCenaS ="";

            SearchResults sr = new SearchResults();
            sr.setName(datum);
            sr.setCityState(contactList.get(i).get(TAG_NAZEV));
            DecimalFormat formatter = new DecimalFormat("#.##");
            if (cena==1){
                switch(contactList.get(i).get(TAG_MENA)) {
                    case "Kč  ":
                        sr.setPhone(contactList.get(i).get(TAG_HODNOTA));
                        break;
                    case "EUR ":
                        ciziCena = (Float.parseFloat(contactList.get(i).get(TAG_HODNOTA))*EUR);
                        ciziCenaS = formatter.format(ciziCena);
                        sr.setPhone(ciziCenaS);
                        break;
                    case "USD ":
                        ciziCena = (Float.parseFloat(contactList.get(i).get(TAG_HODNOTA))*USD);
                        ciziCenaS = formatter.format(ciziCena);
                        sr.setPhone(ciziCenaS);
                        break;
                    case "USc ":
                        ciziCena = (Float.parseFloat(contactList.get(i).get(TAG_HODNOTA))*USc);
                        ciziCenaS = formatter.format(ciziCena);
                        sr.setPhone(ciziCenaS);
                        break;
                    default:
                        // singleChar is a consonant! Execute this code instead!
                        break;
                }
                sr.setJednotky("Kč/" + contactList.get(i).get(TAG_MIRA));
            }
            if (cena==0){
                switch(contactList.get(i).get(TAG_MENA)) {
                    case "Kč  ":
                        ciziCena = (Float.parseFloat(contactList.get(i).get(TAG_HODNOTA))/EUR);
                        ciziCenaS = formatter.format(ciziCena);
                        sr.setPhone(ciziCenaS);
                        break;
                    case "EUR ":
                        sr.setPhone(contactList.get(i).get(TAG_HODNOTA));
                        break;
                    case "USD ":
                        ciziCena = (Float.parseFloat(contactList.get(i).get(TAG_HODNOTA))*(EUR/USD));
                        ciziCenaS = formatter.format(ciziCena);
                        sr.setPhone(ciziCenaS);
                        break;
                    case "USc ":
                        ciziCena = (Float.parseFloat(contactList.get(i).get(TAG_HODNOTA))*(EUR/USc));
                        ciziCenaS = formatter.format(ciziCena);
                        sr.setPhone(ciziCenaS);
                        break;
                    default:
                        // singleChar is a consonant! Execute this code instead!
                        break;
                }
                sr.setJednotky("EUR/" + contactList.get(i).get(TAG_MIRA));
            }
            results.add(sr);
        }

        list2.setAdapter(new MyCustomBaseAdapter(getActivity(), results));
    }


    public boolean isConnect()
    {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            mListener.toast("Připojeno");
            triggerDownload("http://develop.agris.cz/Prices/Commodities/"+komodity+"?vratmi=json&mena=CZK");
        }
        else {
            connected = false;
            mListener.toast("Nepřipojeno");
        }
        return  connected;
    }

    public void triggerDownload(String stringUrl) {
        ConnectivityManager connMgr = (ConnectivityManager)getActivity().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            mListener.toast("Připojení k internetu není k dispozici!");
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Načítám data...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... urls) {

            String jsonStr = null;
            // params comes from the execute() call: params[0] is the url.
            try {
                jsonStr = downloadUrl(urls[0]);
            } catch (IOException e) {
                Log.e("ServiceHandler", "problem");
                return null;
            }

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    contacts = jsonObj.getJSONArray(TAG_CONTACTS);

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String nazev = c.getString(TAG_NAZEV);
                        String id_komodity = c.getString(TAG_ID_KOMODITY);
                        String hodnota = c.getString(TAG_HODNOTA);
                        String jednotka_nazev = c.getString(TAG_JENODTKA_NAZEV);
                        String datum = c.getString(TAG_DATUM);
                        String mena = c.getString(TAG_MENA);
                        String mira = c.getString(TAG_MIRA);
                        String alt_mena = c.getString(TAG_ALT_MENA);


                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_ID, id);
                        contact.put(TAG_NAZEV, nazev);
                        contact.put(TAG_ID_KOMODITY, id_komodity);
                        contact.put(TAG_HODNOTA, hodnota);
                        contact.put(TAG_JENODTKA_NAZEV, jednotka_nazev);
                        contact.put(TAG_DATUM, datum);
                        contact.put(TAG_MENA, mena);
                        contact.put(TAG_MIRA, mira);
                        contact.put(TAG_ALT_MENA, alt_mena);


                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

           razeni(razeniNazev,1);
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = convertStreamToString(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }




    public class SearchResults {
        private String name = "";
        private String cityState = "";
        private String phone = "";
        private String jednotka = "";

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setCityState(String cityState) {
            this.cityState = cityState;
        }

        public String getCityState() {
            return cityState;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPhone() {
            return phone;
        }
        public void setJednotky(String jednotka) {
            this.jednotka = jednotka;
        }

        public String getJednotka() {
            return jednotka;
        }
    }

    public class MyCustomBaseAdapter extends BaseAdapter {
        private ArrayList<SearchResults> searchArrayList;

        private LayoutInflater mInflater;

        public MyCustomBaseAdapter(Context context, ArrayList<SearchResults> results) {
            searchArrayList = results;
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return searchArrayList.size();
        }

        public Object getItem(int position) {
            return searchArrayList.get(position);
        }


        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.txtName = (TextView) convertView.findViewById(R.id.txtdatum);
                holder.txtCityState = (TextView) convertView.findViewById(R.id.txtnazev);
                holder.txtPhone = (TextView) convertView.findViewById(R.id.txthodnota);
                holder.txtJednotky = (TextView)convertView.findViewById(R.id.txtjednotky);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.txtName.setText(searchArrayList.get(position).getName());
            holder.txtCityState.setText(searchArrayList.get(position).getCityState());
            holder.txtPhone.setText(searchArrayList.get(position).getPhone());
            holder.txtJednotky.setText(searchArrayList.get(position).getJednotka());

            return convertView;
        }

        public class ViewHolder {
            TextView txtName;
            TextView txtCityState;
            TextView txtPhone;
            TextView txtJednotky;
        }
    }
}
