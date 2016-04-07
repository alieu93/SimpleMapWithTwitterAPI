package adamlieu.simplemapwithtwitterapi;

import com.google.android.gms.maps.model.LatLng;

/**
 * MercatorProjection: A class used to calculate the proper translation between geographical coordinates to screen points on the map
 */
public class MercatorProjection {
    final double worldWidth;

    public MercatorProjection(final double worldwidth) {
        worldWidth = worldwidth;
    }

    public newPoint toPoint(final LatLng latlng) {
        double x = latlng.longitude / 360 + 0.5;
        double y = 0.5 * Math.log((1 + Math.sin(Math.toRadians(latlng.latitude)))
                / (1 - Math.sin(Math.toRadians(latlng.latitude)))) / (-2 * Math.PI) + 0.5;
        return new newPoint(x * worldWidth, y * worldWidth);
    }
}