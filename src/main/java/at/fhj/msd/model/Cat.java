package at.fhj.msd.model;

public class Cat extends Animal {

    // Конструктор, который принимает имя
    public Cat(String name) {
        super(name); // Передаёт имя в Animal
    }

    // Реализация makeSound()
    @Override
    public void makeSound() {
        System.out.println("Miau-Miau");
    }

    @Override
    public String getEmoji() {
        return "🐱";
    }
}
