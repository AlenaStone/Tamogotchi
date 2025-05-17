package at.fhj.msd;

public class JumpTrick implements Trick {

    private String name;
    private String emoji;

    public JumpTrick(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    @Override
    public void performTrick() {
        System.out.println("Jump " + name + " " + emoji);
    }
}
