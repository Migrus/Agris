package ttrmg.pef.agris;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class DownloadWebpageTask extends AsyncTask<String, Void, Void> {
    private static final String DEBUG_TAG = "DEBUG_TAG";

    // JSON Node names
    public static final String TAG_CONTACTS = "studenti";
    public static final String TAG_ID = "id";
    public static final String TAG_NAME = "jmeno";
    public static final String TAG_SURNAME = "prijmeni";
    public static final String TAG_EMAIL = "email";

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    public ArrayList<HashMap<String, String>> contactList;
    private ProgressDialog pDialog;
    private Context mContext;

    public DownloadWebpageTask(Context context, ArrayList<HashMap<String, String>> array){
        this.mContext = context;
        contactList = array;
    }


    // Showing progress dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Showing progress dialog
        pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Načítám data...");
        pDialog.setCancelable(false);
        pDialog.show();

    }

    @Override
    protected Void doInBackground(String... urls) {

        String jsonStr = null;
        String jsonStrEmail = null;
        // params comes from the execute() call: params[0] is the url.
        try {
            jsonStr = downloadUrl(urls[0]);
        } catch (IOException e) {
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
                    String name = c.getString(TAG_NAME);
                    String surname = c.getString(TAG_SURNAME);

                    try {
                        jsonStrEmail = downloadUrl("http://www.csita.cz/sklad/"+id+".json");
                    } catch (IOException e) {
                        return null;
                    }

                    JSONObject jsonEmailsObj = new JSONObject(jsonStrEmail);
                    String email = jsonEmailsObj.getString(TAG_EMAIL);

                    // tmp hashmap for single contact
                    HashMap<String, String> contact = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    contact.put(TAG_ID, id);
                    contact.put(TAG_NAME, name);
                    contact.put(TAG_SURNAME, surname);
                    contact.put(TAG_EMAIL, email);

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
    }


    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    public String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

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
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}
