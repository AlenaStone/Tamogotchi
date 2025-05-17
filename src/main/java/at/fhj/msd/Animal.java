package at.fhj.msd;

import java.util.Random;

abstract class Animal {

    private String name;
    private int hungerLevel = 0;
    private int energyLevel = 50;
    private int moodLevel = 50;
    Random random = new Random();
    private Trick trick;

    public Animal(String name) {
        this.name = name;
    }

    public abstract void makeSound();

    public abstract String getEmoji();

    public void eat() {
        int randomNumber = 5 + random.nextInt(11);
        hungerLevel -= randomNumber;
        if (hungerLevel < 0) {
            hungerLevel = 0;
        }
        moodLevel = moodLevel + randomNumber / 2;
        if (moodLevel > 100) {
            moodLevel = 100;
        }
    }

    public void play() {
        int randomNumber = 5 + random.nextInt(11);
        moodLevel = moodLevel + randomNumber / 2;
        energyLevel = energyLevel - randomNumber;
        if (energyLevel < 0) {
            energyLevel = 0;
        }
        if (moodLevel > 100) {
            moodLevel = 100;
        }
    }

    public void sleep() {
        int randomNumber = 1 + random.nextInt(11);
        energyLevel = energyLevel + randomNumber;
        hungerLevel = hungerLevel + randomNumber;
        if (hungerLevel > 100) {
            hungerLevel = 100;
        }
        if (energyLevel > 100) {
            energyLevel = 100;
        }
    }

    public String showStatus() {
        return (getEmoji() + " " + name + " | ❤️ " + moodLevel + " | 🍗 " + hungerLevel + " | ⚡ " + energyLevel);

    }

    public void degradeState() {
        Random random = new Random();
        int randomNumber = 1 + random.nextInt(11);
        hungerLevel = hungerLevel + randomNumber;
        energyLevel = energyLevel - randomNumber;
        moodLevel = moodLevel - randomNumber;
        if (energyLevel < 0) {
            energyLevel = 0;
        }
        if (moodLevel < 0) {
            moodLevel = 0;
        }
        if (hungerLevel > 100) {
            hungerLevel = 100;
        }

        System.out.println(name + "is a little bit hungry. " + name + " has " + hungerLevel + " hunger");
        System.out.println("+------------------");
        System.out.println(name + " is a little bit tired. " + name + " has " + energyLevel + " energy");
        System.out.println("+------------------");
        System.out.println(name + " is a little bit sad. " + name + " has " + moodLevel + " mood");
        System.out.println("+------------------");
    }

    public void setTrick(Trick trick) {
        this.trick = trick;
    }

    public void performTrick() {
        if (trick != null) {
            trick.performTrick();
        } else {
            System.out.println(name + " doesn't know any trick yet!");
        }
    }

}
