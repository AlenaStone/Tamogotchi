package at.fhj.msd.model;

/**
 * Data model for saving pet state.
 * Stores pet name, type and current stat values.
 */
public class PetSaveData {
    /** The name of the pet. */
    public String petName;

    /** The type of the pet (cat, dog, fish, etc.). */
    public String petType;

    /** Hunger level from 0.0 to 1.0. */
    public double hunger;

    /** Energy level from 0.0 to 1.0. */
    public double energy;

    /** Mood level from 0.0 to 1.0. */
    public double mood;

    /** Health level from 0.0 to 1.0. */
    public double health;
}
// This class is used to serialize and deserialize pet state for saving/loading.