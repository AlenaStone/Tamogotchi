package at.fhj.msd.model;

public class Bird extends Animal {

    // Конструктор, который принимает имя
    public Bird(String name) {
        super(name); // Передаёт имя в Animal
    }

    // Реализация makeSound()
    @Override
    public void makeSound() {
        System.out.println("Chirp-Chirp");
    }

    @Override
    public String getEmoji() {
        return "🐦";
    }
}
