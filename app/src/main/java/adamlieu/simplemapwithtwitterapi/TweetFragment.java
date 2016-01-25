package adamlieu.simplemapwithtwitterapi;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class TweetFragment extends DialogFragment {
    ImageView im;

    static TweetFragment newInstance(String title, String text, String URL){
        TweetFragment t = new TweetFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("text", text);
        args.putString("embedURL", URL);
        t.setArguments(args);

        return t;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.activity_tweet_fragment, container, false);

        getDialog().setTitle(getArguments().getString("title"));

        TextView tv = (TextView) rootView.findViewById(R.id.text);
        tv.setText(getArguments().getString("text"));

        im = (ImageView) rootView.findViewById(R.id.embed);

        //TODO: Check for null URL
        String embedURL = getArguments().getString("embedURL");
        if(embedURL != null || embedURL != "null"){
            new DownloadEmbedImage().execute(embedURL);
        }

        Button dismiss = (Button) rootView.findViewById(R.id.dismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                dismiss();
            }
        });
        return rootView;
    }

    private class DownloadEmbedImage extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls){
            String url = urls[0];
            Bitmap embed = null;
            try{
                InputStream in = new BufferedInputStream(new URL(url).openStream(), 4096);
                embed = BitmapFactory.decodeStream(in);
            } catch (IOException e){
                Log.e("URL IO", "Error reading URL");
            }
            return embed;
        }

        protected void onPostExecute(Bitmap result){
            im.setImageBitmap(result);
        }
    }
}
