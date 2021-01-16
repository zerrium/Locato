package zerrium;

public class ZChunk {
    private final int x, z, elevation;

    public ZChunk(int x, int z, int elevation){
        this.x = x;
        this.z = z;
        this.elevation = elevation;
    }

    public int[] getCoord(){
        return new int[]{this.x, this.z, this.elevation};
    }
}
