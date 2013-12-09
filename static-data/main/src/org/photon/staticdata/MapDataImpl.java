package org.photon.staticdata;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import scala.Option;
import scala.collection.JavaConversions$;
import scala.collection.Seq;
import scala.collection.convert.WrapAsJava$;

import java.io.IOException;
import java.util.List;

public class MapDataImpl implements MapData, MapData.Builder {

    private int id;
    @JsonSerialize(using = MapPositionSerializer.class)
    @JsonDeserialize(using = MapPositionDeserializer.class)
    private MapPosition pos;
    private short width;
    private short height;
    @JsonSerialize(contentAs = Integer.class)
    @JsonDeserialize(contentAs = Integer.class)
    private Option<Object> subareaId;
    @JsonManagedReference
    private List<Cell> cells;
    private byte[] key;
    private byte[] date;
    private boolean premium;

    public static class Cell implements MapCell, MapCell.Builder {

        private short id;
        @JsonBackReference
        private MapDataImpl map;
        private boolean los;
        private short groundLevel;
        @JsonSerialize(using = MovementTypeSerializer.class)
        @JsonDeserialize(using = MovementTypeDeserializer.class)
        private MovementType movementType;
        private short groundSlope;
        @JsonSerialize(contentAs = Integer.class)
        @JsonDeserialize(contentAs = Integer.class)
        private Option<Object> interactiveObject;

        @Override
        public short id() {
            return getId();
        }

        @Override
        public MapData map() {
            return getMap();
        }

        @Override
        public boolean los() {
            return isLos();
        }

        @Override
        public short groundLevel() {
            return getGroundLevel();
        }

        @Override
        public MovementType movementType() {
            return getMovementType();
        }

        @Override
        public short groundSlope() {
            return getGroundSlope();
        }

        @Override
        public Option<Object> interactiveObject() {
            return getInteractiveObject();
        }

        @Override
        public Builder withId(short id) {
            setId(id);
            return this;
        }

        @Override
        public Builder withGroundSlope(short groundSlope) {
            setGroundSlope(groundSlope);
            return this;
        }

        @Override
        public Builder withGroundLevel(short groundLevel) {
            setGroundLevel(groundLevel);
            return this;
        }

        @Override
        public Builder withMovementType(MovementType movementType) {
            setMovementType(movementType);
            return this;
        }

        @Override
        public Builder withMap(MapData map) {
            setMap((MapDataImpl) map);
            return this;
        }

        @Override
        public Builder withLos(boolean los) {
            setLos(los);
            return this;
        }

        @Override
        public Builder withInteractiveObject(Option<Object> interactiveObject) {
            setInteractiveObject(interactiveObject);
            return this;
        }

        @Override
        public MapCell result() {
            return this;
        }

        @Override
        public MapCell lazyResult() {
            return this;
        }

        public short getId() {
            return id;
        }

        public void setId(short id) {
            this.id = id;
        }

        public MapDataImpl getMap() {
            return map;
        }

        public void setMap(MapDataImpl map) {
            this.map = map;
        }

        public boolean isLos() {
            return los;
        }

        public void setLos(boolean los) {
            this.los = los;
        }

        public short getGroundLevel() {
            return groundLevel;
        }

        public void setGroundLevel(short groundLevel) {
            this.groundLevel = groundLevel;
        }

        public MovementType getMovementType() {
            return movementType;
        }

        public void setMovementType(MovementType movementType) {
            this.movementType = movementType;
        }

        public short getGroundSlope() {
            return groundSlope;
        }

        public void setGroundSlope(short groundSlope) {
            this.groundSlope = groundSlope;
        }

        public Option<Object> getInteractiveObject() {
            return interactiveObject;
        }

        public void setInteractiveObject(Option<Object> interactiveObject) {
            this.interactiveObject = interactiveObject;
        }
    }

    @Override
    public int id() {
        return getId();
    }

    @Override
    public MapPosition pos() {
        return getPos();
    }

    @Override
    public short width() {
        return getWidth();
    }

    @Override
    public short height() {
        return getHeight();
    }

    @Override
    public Option<Object> subareaId() {
        return getSubareaId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Seq<MapCell> cells() {
        return JavaConversions$.MODULE$.asScalaBuffer((List) getCells());
    }

    @Override
    public byte[] key() {
        return getKey();
    }

    @Override
    public byte[] date() {
        return getDate();
    }

    @Override
    public boolean premium() {
        return isPremium();
    }

    @Override
    public Builder withId(int id) {
        setId(id);
        return this;
    }

    @Override
    public MapData result() {
        return this;
    }

    @Override
    public MapData lazyResult() {
        return this;
    }

    @Override
    public Builder withWidth(short width) {
        setWidth(width);
        return this;
    }

    @Override
    public Builder withSubareaId(Option<Object> subareaId) {
        setSubareaId(subareaId);
        return this;
    }

    @Override
    public Builder withPremium(boolean premium) {
        setPremium(premium);
        return this;
    }

    @Override
    public Builder withPos(MapPosition pos) {
        setPos(pos);
        return this;
    }

    @Override
    public Builder withDate(byte[] date) {
        setDate(date);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder withCells(Seq<MapCell> cells) {
        setCells(WrapAsJava$.MODULE$.<Cell>seqAsJavaList((Seq) cells));
        return this;
    }

    @Override
    public Builder withKey(byte[] key) {
        setKey(key);
        return this;
    }

    @Override
    public Builder withHeight(short height) {
        setHeight(height);
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MapPosition getPos() {
        return pos;
    }

    public void setPos(MapPosition pos) {
        this.pos = pos;
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    public short getHeight() {
        return height;
    }

    public void setHeight(short height) {
        this.height = height;
    }

    public Option<Object> getSubareaId() {
        return subareaId;
    }

    public void setSubareaId(Option<Object> subareaId) {
        this.subareaId = subareaId;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getDate() {
        return date;
    }

    public void setDate(byte[] date) {
        this.date = date;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    private static class MapPositionSerializer extends JsonSerializer<MapPosition> {
        @Override
        public void serialize(MapPosition value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartArray();
            jgen.writeNumber(value.x());
            jgen.writeNumber(value.y());
            jgen.writeEndArray();
        }
    }

    private static class MapPositionDeserializer extends JsonDeserializer<MapPosition> {
        @Override
        public MapPosition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ArrayNode node = jp.getCodec().readTree(jp);

            if (node.size() != 2) {
                throw new IllegalStateException("a 2-fixed-length array was expected");
            }

            JsonNode x = node.get(0), y = node.get(1);

            if (!x.canConvertToInt() || !y.canConvertToInt()) {
                throw new IllegalStateException("a number was expected");
            }

            return new MapPosition(x.asInt(), y.asInt());
        }
    }

    private static class MovementTypeSerializer extends JsonSerializer<MovementType> {
        @Override
        public void serialize(MovementType value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeNumber(value.id());
        }
    }

    private static class MovementTypeDeserializer extends JsonDeserializer<MovementType> {
        @Override
        public MovementType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);

            if (!node.canConvertToInt()) {
                throw new IllegalStateException("a number was expected");
            }

            return MovementType$.MODULE$.of(node.asInt());
        }
    }
}
