package at.fhj.msd.trick;

public class DanceTrick implements Trick {

    private String name;
    private String emoji;

    public DanceTrick(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public void performTrick() {
        System.out.println("Dance " + name + " " + emoji);
    }
}
