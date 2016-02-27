package adamlieu.simplemapwithtwitterapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

public class TestSeekBar extends AppCompatActivity {

    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private TextView textView;
    private TextView textView2;
    private TextView textView3;

    int upperRange;
    int lowerRange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_seek_bar);
        initializeVariables();

        upperRange = seekBar2.getProgress() + seekBar1.getMax();
        //lowerRange = seekBar1.getProgress

        seekBar1.setMax(50);
        seekBar2.setMax(30);
        seekBar2.setProgress(seekBar2.getMax());

        textView.setText("Seek1: " + seekBar1.getProgress() + "/" + seekBar1.getMax());
        textView2.setText("Seek2: " + seekBar2.getProgress() + "/" + seekBar2.getMax());
        final int absoluteTotal = seekBar1.getMax() + seekBar2.getMax();
        textView3.setText("Range: " + seekBar1.getProgress() + "-" + upperRange + " (Total:  "+ absoluteTotal + ")");


        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;


            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                upperRange = seekBar2.getProgress() + seekBar1.getMax();
                seekBar1.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Seek1: " + seekBar1.getProgress() + "/" + seekBar1.getMax());
                textView3.setText("Range: " + seekBar1.getProgress() + "-" + upperRange + " (Total:  "+ absoluteTotal + ")");
            }
        });
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser){
                progress = progressValue;
                upperRange = seekBar2.getProgress() + seekBar1.getMax();
                seekBar2.setProgress(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
                textView2.setText("Seek2: " + seekBar2.getProgress() + "/" + seekBar2.getMax());
                textView3.setText("Range: " + seekBar1.getProgress() + "-" + upperRange + " (Total:  "+ absoluteTotal + ")");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_seek_bar, menu);
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

    private void initializeVariables(){
        seekBar1 = (SeekBar) findViewById(R.id.seekBar);
        seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
    }
}
