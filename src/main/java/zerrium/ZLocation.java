package zerrium;


public class ZLocation {
    private final String place_id;
    private String dimension;
    private ZChunk chunk1, chunk2;

    public ZLocation (String place_id, String dimension, ZChunk chunk1, ZChunk chunk2){
        this.place_id = place_id;
        this.dimension = dimension;
        this.chunk1 = chunk1;
        this.chunk2 = chunk2;
    }

    public ZLocation (String place_id){
        this.place_id = place_id;
    }

    @Override
    public boolean equals (Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            if(Locato.debug) System.out.println("Comparing instance of itself");
            return true;
        }

        /* Check if o is an instance of ZPlayer or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof ZLocation)) {
            if(Locato.debug) System.out.println("Not a ZLocation instance");
            return false;
        }

        // Compare the data members and return accordingly
        boolean result = ((ZLocation) o).place_id.equals(place_id) || place_id.equals(((ZLocation) o).place_id);
        if(Locato.debug) System.out.println("ZLocation instance, equal? "+result);
        return result;
    }

    @Override
    public int hashCode() {
        return place_id.hashCode();
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
