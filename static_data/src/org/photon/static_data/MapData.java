package org.photon.static_data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = MapData.class)
public class MapData {

    public static class Position {
        private final int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private final long id;
    private final Position position;
    private final String key;
    private final String date;
    private final String data;

    public MapData(long id, Position position, String key, String date, String data) {
        this.id = id;
        this.position = position;
        this.key = key;
        this.date = date;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public String getKey() {
        return key;
    }

    public String getDate() {
        return date;
    }

    public String getData() {
        return data;
    }
}
