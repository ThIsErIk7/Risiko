package com.risiko.model;

public class Territory {

    private final String name;
    private Player owner;
    private int armies;

    public Territory(String name) {
        this.name = name;
        this.owner = owner;
        this.armies = armies; 
    }

    // Getter

    public String getName() {
        return name;
    }
    public Player getOwner() {
        return owner;
    }
    public int getArmies() {
        return armies;
    }
    // Setter 
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    
    public void addArmies(int amount) {
        this.armies += amount;
    }
    public void removeArmies(int amount) {
        this.armies = Math.max(0, this.armies - amount);
    }

    @Override
    public String toString() {
        return name + " (" + armies + " Armeen, Besitzer: " + 
        (owner != null ? owner.getName() : "Kein Besitzer") + ")";
        
        }
    }