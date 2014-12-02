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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements FragmentView.OnFragmentInteractionListener, FragmentResult.OnFragmentInteractionListener {
    public ArrayList<HashMap<String, String>> contactList;
    public String textkpredani;
    String text = "text co se ma ulozit, pak nacist, predat do fragmentu a pak zobrazit";
    String textuloz = "SouborKUlozeni";



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
        WriteIntoFile();
        textkpredani = OpenFileDialog(textuloz);
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
            FragmentResult formFragment = FragmentResult.newInstance(textkpredani);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, formFragment).addToBackStack(null)
                    .commit();
        } else {
            FragmentResult formFragment = FragmentResult.newInstance(textkpredani);
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


    //VYTVORI SOUBOR "VypisStudentu", do ktereho vypise hodnoty 1. objektu z pole. DOKAZU VYPSAT PUBLIC VECI --- TAMTA METODA BYLA PRIVATE!
    public void WriteIntoFile() {
        /* try {
            fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(new DownloadWebpageTask(MainActivity.this,contactList).contactList.indexOf(1));
         // fos.write(new DownloadWebpageTask(MainActivity.this,contactList).downloadUrl("http..."));
         // TOHLE BY MI MELO VYPSAT CELY OBSAH URL DO SOUBORU, ale ta metoda nebere ten vstup, pritom tam rvu string..
         // Výstupem tý metody je string "contentAsString", ktery obsahuje vsechna data tech studentu a ten potrebujeme zapsat do souboru
         // Zkus jestli ti to vypise alespon neco z toho contactListu. Nebo zkonstroluj jestli se ti vytvori alespon prazdny soubor VypisStudentu.
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /* DALSI ZPUSOB - pouze jinak*/

        try {
            FileOutputStream fos = openFileOutput(textuloz, Context.MODE_PRIVATE);
            //fos.write(text.getBytes());
            fos.write(new DownloadWebpageTask(MainActivity.this,contactList).downloadUrl("http://www.csita.cz/sklad/studenti.json").getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String OpenFileDialog(String file){

        //Read file in Internal Storage
        FileInputStream fis;
        String content = "";
        try {
            fis = openFileInput(file);
            byte[] input = new byte[fis.available()];
            while (fis.read(input) != -1) {}
            content += new String(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;

    }

}
