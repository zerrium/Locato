package zerrium;


public class ZLocation {
    private final String place_id;
    private String dimension;
    private ZChunk chunk1, chunk2;

    public  ZLocation (String place_id, String dimension, ZChunk chunk1, ZChunk chunk2){
        this.place_id = place_id;
        this.dimension = dimension;
        this.chunk1 = chunk1;
        this.chunk2 = chunk2;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getPlaceId() {
        return place_id;
    }

    public ZChunk getChunk1() {
        return chunk1;
    }

    public void setChunk1(ZChunk chunk1) {
        this.chunk1 = chunk1;
    }

    public ZChunk getChunk2() {
        return chunk2;
    }

    public void setChunk2(ZChunk chunk2) {
        this.chunk2 = chunk2;
    }
}
