package ttrmg.pef.agris;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements FragmentView.OnFragmentInteractionListener, FragmentResult.OnFragmentInteractionListener {
    public ArrayList<HashMap<String, String>> contactList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentView formFragment = FragmentView.newInstance();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, formFragment)
                    .commit();
        }
        contactList = new ArrayList<HashMap<String, String>>();
        isConnect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    @Override
    public void sendButton() {
        if (findViewById(R.id.result) == null) {
            FragmentResult formFragment = FragmentResult.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, formFragment).addToBackStack(null)
                    .commit();
        } else {
            FragmentResult formFragment = FragmentResult.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.result, formFragment).addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void toast(String toast) {
        showToast(toast);
    }

    public boolean isConnect()
    {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            this.showToast("Připojeno");
            triggerDownload("http://www.csita.cz/sklad/studenti.json");
        }
        else {
            connected = false;
            this.showToast("Nepřipojeno");
        }
        return  connected;
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void triggerDownload(String stringUrl) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
             new DownloadWebpageTask(MainActivity.this,contactList).execute(stringUrl);
        } else {
            showToast("Připojení k internetu není k dispozici!");
        }
    }
}
