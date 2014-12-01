package ttrmg.pef.agris;

import android.content.Context;
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




public class MainActivity extends ActionBarActivity implements FragmentView.OnFragmentInteractionListener, FragmentResult.OnFragmentInteractionListener {

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

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
    public final static boolean isValidPhone(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return Patterns.PHONE.matcher(target).matches();
        }
    }

    private void loopLayoutReset(LinearLayout ll){
        int count = ll.getChildCount();
        for (int i = 0;i <= count;i++){
            View v = ll.getChildAt(i);
            if (v instanceof LinearLayout) loopLayoutReset((LinearLayout) v);
            if (v != null && v instanceof EditText) {
                clearText(v);
            }
        }
    }

    private void clearText(View v){
        ((TextView) v).setText("");
    }


    @Override
    public void sendButton() {
        FragmentResult formFragment = FragmentResult.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, formFragment).addToBackStack(null)
                .commit();
    }

    @Override
    public void toast(String toast) {
        showToast(toast);
    }
}
