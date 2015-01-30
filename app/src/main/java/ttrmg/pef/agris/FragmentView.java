package ttrmg.pef.agris;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentView extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DEBUG_TAG = "DEBUG_TAG";
    private ProgressDialog pDialog;
    // JSON Node names
    private static final String TAG_CONTACTS = "ceny";
    private static final String TAG_ID = "id";
    private static final String TAG_ID_SKUPINA = "id_skupina";
    private static final String TAG_NAZEV = "nazev";
    ListView list1;


    boolean connected = false;
    public String name = "";


    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;
    ArrayList<String> adapterList;

    // TODO: Rename and change types of parameters


    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p/>
     * //     * @param param1 Parameter 1.
     * //     * @param param2 Parameter 2.
     *
     * @return A new instance of fragment FormFragment.
     */
    // TO_DO: Rename and change types and number of parameters
    public static FragmentView newInstance() {
        FragmentView fragment = new FragmentView();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    public FragmentView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_form, container, false);
        view.findViewById(R.id.send).setOnClickListener(this);
        contactList = new ArrayList<HashMap<String, String>>();
        list1 = (ListView) view.findViewById(R.id.list1);
        adapterList = new ArrayList<>();
        isConnect();


        list1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String poziceKomodity = contactList.get(position).get(TAG_ID);
                Integer intPoziceKomodity = Integer.parseInt(poziceKomodity);

                mListener.itemClick(intPoziceKomodity);

            }

        });


        return view;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                mListener.sendButton();
                break;
        }
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
        public void sendButton();
        public void itemClick(Integer pozice);
        public void toast(String toast);
    }



    public boolean isConnect() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            mListener.toast("Připojeno");
            triggerDownload("http://develop.agris.cz/Prices?vratmi=json");
        } else {
            connected = false;
            mListener.toast("Nepřipojeno");
            triggerDownload("http://develop.agris.cz/Prices?vratmi=json");
        }
        return connected;
    }

    public void triggerDownload(String stringUrl) {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            mListener.toast("Připojení k internetu není k dispozici!");
            new DownloadWebpageTask().execute(stringUrl);
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
            if (connected == true) {
                try {
                    jsonStr = downloadUrl(urls[0]);
                } catch (IOException e) {
                    Log.e("ServiceHandler", "problem");
                    return null;
                }
            }
            if (connected == false) {
                FileInputStream fis;
                String content = "";
                try {
                    fis = getActivity().openFileInput("index");
                    byte[] input = new byte[fis.available()];
                    while (fis.read(input) != -1) {
                    }
                    content += new String(input);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                jsonStr = content;
            }
            if (jsonStr != null) {
                if (connected != false) {
                    FileOutputStream outputStream;
                    try {
                        outputStream = getActivity().openFileOutput("index", Context.MODE_PRIVATE);
                        outputStream.write(jsonStr.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    contacts = jsonObj.getJSONArray(TAG_CONTACTS);

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String id_skupina = c.getString(TAG_ID_SKUPINA);
                        String nazev = c.getString(TAG_NAZEV);


                        //JSONObject jsonEmailsObj = new JSONObject(jsonStrEmail);
                        //String email = jsonEmailsObj.getString(TAG_EMAIL);

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_ID, id);
                        contact.put(TAG_ID_SKUPINA, id_skupina);
                        contact.put(TAG_NAZEV, nazev);


                        name = nazev;

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

                        String sr1 = "";
                        sr1 = (contactList.get(i).get(TAG_NAZEV));
                        adapterList.add(sr1);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, adapterList);
                    list1.setAdapter(adapter);

                    //ArrayAdapter<String> adapterDatum = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, ceny);

                    //list1.setAdapter(adapterDatum);

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

