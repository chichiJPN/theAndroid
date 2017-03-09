package team.virtualnanny;


import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;

public class MapFence {
    private Db_fence db_fence;
    private Marker marker;
    private Polygon polygon;
    private Circle circle;

    public MapFence(Db_fence db_fence, Marker marker, Polygon polygon, Circle circle) {
        this.db_fence = db_fence;
        this.polygon = polygon;
        this.marker = marker;
        this.circle = circle;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public Marker getMarker() {
        return marker;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public Db_fence getDb_fence() {
        return db_fence;
    }

    public void setDb_fence(Db_fence db_fence) {
        this.db_fence = db_fence;
    }
}
