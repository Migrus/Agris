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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;
    ArrayList<String> adapterList;

    private OnFragmentInteractionListener mListener;

    ListView list2;

    public static FragmentResult newInstance() {
        FragmentResult fragment = new FragmentResult();
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

    public boolean isConnect()
    {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            mListener.toast("Připojeno");
            triggerDownload("http://develop.agris.cz/Prices/Commodities/2?vratmi=json&mena=CZK");
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
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();


            for (int i = 0; i < contactList.size(); i++) {
                String date = contactList.get(i).get(TAG_DATUM).substring(6,16);
                String  datum = new SimpleDateFormat("MM/dd/yyyy").format(new Date(Integer.parseInt(date) * 1000L));
                String sr1 = "";
                sr1 =  datum + "  " + (contactList.get(i).get(TAG_NAZEV))+ "  " + (contactList.get(i).get(TAG_HODNOTA))
                        + "  " + (contactList.get(i).get(TAG_MENA)+"/"+contactList.get(i).get(TAG_MIRA));
                adapterList.add(sr1);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, adapterList);
            list2.setAdapter(adapter);

            /**
             * Updating parsed JSON data into ListView
             * */

            //Toast(contactList.get(0).get(0));
            //Toast(result);
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
}
