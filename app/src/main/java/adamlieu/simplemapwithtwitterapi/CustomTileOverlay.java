package adamlieu.simplemapwithtwitterapi;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomTileOverlay implements TileProvider {
    private List<newPoint> points = new ArrayList<newPoint>();
    private List<Paint> paintColor = new ArrayList<Paint>();


    public final int TILE_SIZE_DP = 256;
    public final int mScaleFactor = 2;
    private MercatorProjection mercatorprojection = new MercatorProjection(TILE_SIZE_DP);
    private int dimension = TILE_SIZE_DP * mScaleFactor;


    @Override
    public Tile getTile(int x, int y, int zoom) {

        float scale = (float) Math.pow(2, zoom) * mScaleFactor;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postTranslate(-x * dimension, -y * dimension);

        Bitmap bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.setMatrix(matrix);

        //Paint exteriorPaint = new Paint();
        //Paint interiorPaint = new Paint();

        //exteriorPaint.setColor(0x3F96B0FF);
        //interiorPaint.setColor(0x7F45E3C1);

        for(int i = 0; i < points.size(); i++){
            canvas.drawCircle((float) points.get(i).x, (float) points.get(i).y, 0.001f, paintColor.get(i));
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return new Tile(dimension, dimension, stream.toByteArray());
    }

    public void addPoint(LatLng pos, Paint paint) {
        points.add(mercatorprojection.toPoint(pos));
        paintColor.add(paint);
    }
}