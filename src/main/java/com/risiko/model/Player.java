package com.risiko.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String name;
    private final String color;
    private int armies;
    private final List<com.risiko.model.Territory> territories;

    public Player(String name, String color, int initialArmies) {
        this.name = name;
        this.color = color;
        this.armies = initialArmies;
        this.territories = new ArrayList<>();
    }

    //Getter 
    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getArmies() {
        return armies;
    }

    public List<Territory> getTerritories() {
        return territories;
    }

    //Setter / Methoden
    public void addTerritory(Territory territory) {
        if (!territories.contains(territory)) {
            territories.add(territory);
        }
    }

    public void removeTerritory(Territory territory) {
        territories.remove(territory);
    }

    public void addArmies(int amount) {
        this.armies += amount;
    }

    public void removeArmies(int amount) {
        if (this.armies - amount >= 0) {
            this.armies -= amount;
        } else {
            this.armies = 0;
        }
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", armies=" + armies +
                ", territories=" + territories.size() +
                '}';
    }
}
