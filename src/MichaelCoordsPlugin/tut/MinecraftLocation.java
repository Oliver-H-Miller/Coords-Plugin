package MichaelCoordsPlugin.tut;

public class MinecraftLocation {
    private final String name;
    private final int Xcoord;
    private final int Ycoord;
    private final int Zcoord;

    public MinecraftLocation(String name, int xcoord, int ycoord, int zcoord) {
        this.name = name;
        Xcoord = xcoord;
        Ycoord = ycoord;
        Zcoord = zcoord;
    }

    public String getName() {
        return name;
    }

    public int getXcoord() {
        return Xcoord;
    }

    public int getYcoord() {
        return Ycoord;
    }

    public int getZcoord() {
        return Zcoord;
    }

    public int[] getCoords() {
        return new int[] {Xcoord, Ycoord, Zcoord};
    }

}
