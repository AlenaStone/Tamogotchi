package at.fhj.msd.trick;

public class RollOverTrick implements Trick {

    private String name;
    private String emoji;

    public RollOverTrick(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    @Override
    public void performTrick() {
        System.out.println("Roll over " + name + " " + emoji);
    }

}
