package zerrium.Models;

import zerrium.Locato;

import java.util.logging.Logger;

public class LocatoZLocation {
    private final String place_id;
    private String dimension;
    private LocatoZChunk chunk1, chunk2;

    private final static Logger log = Locato.getPlugin(Locato.class).getLogger();

    public LocatoZLocation(String place_id, String dimension, LocatoZChunk chunk1, LocatoZChunk chunk2){
        this.place_id = place_id;
        this.dimension = dimension;
        this.chunk1 = chunk1;
        this.chunk2 = chunk2;
    }

    public LocatoZLocation(String place_id){
        this.place_id = place_id;
    }

    @Override
    public boolean equals (Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            log.fine("[Locato: "+this.getClass().toString()+"] "+"Comparing instance of itself");
            return true;
        }

        /* Check if o is an instance of ZPlayer or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof LocatoZLocation)) {
            log.fine("[Locato: "+this.getClass().toString()+"] "+"Not a ZLocation instance");
            return false;
        }

        // Compare the data members and return accordingly
        boolean result = ((LocatoZLocation) o).place_id.equals(place_id) || place_id.equals(((LocatoZLocation) o).place_id);
        log.fine("[Locato: "+this.getClass().toString()+"] "+"ZLocation instance, equal? "+result);
        return result;
    }

    @Override
    public int hashCode() {
        return place_id.hashCode();
    }

    public String getDimension() {
        return dimension;
    }

    public String getPlaceId() {
        return place_id;
    }

    public LocatoZChunk getChunk1() {
        return chunk1;
    }

    public LocatoZChunk getChunk2() {
        return chunk2;
    }
}
