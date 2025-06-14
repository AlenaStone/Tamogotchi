package at.fhj.msd.model;

public class Fish extends Animal {

    // Конструктор, который принимает имя
    public Fish(String name) {
        super(name); // Передаёт имя в Animal
    }

    // Реализация makeSound()
    @Override
    public void makeSound() {
        System.out.println("Blub-Blub");
    }

    @Override
    public String getEmoji() {
        return "🐟";
    }

}
