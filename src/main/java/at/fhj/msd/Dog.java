package at.fhj.msd;

public class Dog extends Animal {

    // Конструктор, который принимает имя
    public Dog(String name) {
        super(name); // Передаёт имя в Animal
    }

    // Реализация makeSound()
    @Override
    public void makeSound() {
        System.out.println("Gav-Gav");
    }

    @Override
    public String getEmoji() {
        return "🐶";
    }
}
