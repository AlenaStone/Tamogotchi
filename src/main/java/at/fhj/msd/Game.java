package at.fhj.msd;

import java.util.Scanner;

public class Game {

    private Animal animal;
    Scanner scanner = new Scanner(System.in);
    String name;

    public void play() {
        try {

            System.out.println("Welcome to Tamogotchi!");
            do {
                System.out.print("Please enter the name of your pet: ");
                name = scanner.nextLine();

                // Проверка на пустое, "123", пробел, или цифры
                if (name.isEmpty() || name.equals("123") || name.trim().isEmpty() || containsDigits(name)) {
                    System.out.println("Invalid name. Please enter a valid name without numbers.");
                    name = null; // чтобы повторить ввод
                }
            } while (name == null);
            do {
                System.out.println("1.🐶 Dog ");
                System.out.println("2.🐱 Cat");
                System.out.println("3.🐟 Fish");
                System.out.println("4.🐦 Bird");
                System.out.println("+------------------");
                System.out.print("Please choose the type of your pet: ");
                int choice = scanner.nextInt();
                animal = null; // сбрасываем animal перед выбором
                switch (choice) {
                    case 1:
                        animal = new Dog(name);
                        break;
                    case 2:
                        animal = new Cat(name);
                        break;
                    case 3:
                        animal = new Fish(name);
                        break;
                    case 4:
                        animal = new Bird(name);
                        break;
                    default:
                        System.out.println("Invalid choice. Please select a number between 1 and 4.");
                        animal = null;
                }
            } while (animal == null);
            System.out.println("+------------------");
            System.out.println("Your pet is a " + animal.getClass().getSimpleName() + " named " + name);
            System.out.println("Your pet's status: " + animal.showStatus());
            System.out.println("+------------------");

            boolean running = true;

            while (running == true) {
                System.out.println("1.🎵 Make sound");
                System.out.println("2.🍽️ Eat");
                System.out.println("3.🎾 Play");
                System.out.println("4.🛌 Sleep");
                System.out.println("5.📊 Show status");
                System.out.println("6. Make a trick");
                System.out.println("7. Exit");
                System.out.print("Please choose an action: ");

                int action = scanner.nextInt();
                System.out.println("+------------------");
                scanner.nextLine(); // чтобы "почистить" буфер

                switch (action) {
                    case 1:
                        animal.makeSound();
                        System.out.println("+------------------");

                        animal.degradeState();

                        break;
                    case 2:
                        animal.eat();
                        System.out.println("+------------------");

                        animal.degradeState();
                        break;
                    case 3:
                        animal.play();
                        System.out.println("+------------------");

                        animal.degradeState();
                        break;
                    case 4:
                        animal.sleep();
                        System.out.println("+------------------");

                        animal.degradeState();
                        break;
                    case 5:
                        animal.showStatus();
                        System.out.println("+------------------");

                        animal.degradeState();
                        break;
                    case 6:
                        System.out.println("Choose a trick for your pet:");
                        System.out.println("1. Roll over");
                        System.out.println("2. Jump");
                        System.out.println("3. Dance");
                        System.out.println("+------------------");
                        System.out.print("Please choose a trick: ");
                        int trickChoice = scanner.nextInt();
                        Trick trick = null;
                        switch (trickChoice) {
                            case 1:
                                trick = new RollOverTrick(name, "🐾");
                                break;
                            case 2:
                                trick = new JumpTrick(name, "🐾");
                                break;
                            case 3:
                                trick = new DanceTrick(name, "🐾");
                                break;
                            default:
                                System.out.println("Invalid choice. Please select a number between 1 and 3.");
                        }
                        if (trick != null) {
                            trick.performTrick();
                        }
                        System.out.println("+------------------");

                        animal.degradeState();
                        break;
                    case 7:
                        System.out.println("Exiting the game. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        running = true;
                }

            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    boolean containsDigits(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (Character.isDigit(name.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
