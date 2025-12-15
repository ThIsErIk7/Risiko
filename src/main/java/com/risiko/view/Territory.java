package com.risiko.view;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.risiko.model.Player;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

public class Territory extends Group {

    private final String name;
    private Player owner;
    private int armyCount;

    private final Shape area;
    private final Label armyLabel;

    private double badgeX;
    private double badgeY;
    private double badgeOffsetX = 0;
    private double badgeOffsetY = 0;

    private final Set<Territory> neighbors = new HashSet<>();
    private boolean highlighted = false;

    // Hover state
    private boolean hovered = false;

    private Consumer<Territory> onSelected;

    // Stroke defaults
    private static final double STROKE_NORMAL = 2.0;
    private static final double STROKE_HOVER = 4.0;

    public Territory(String name, Shape area, Player owner, int armyCount) {
        this(name, area, owner, armyCount,
                area.getBoundsInParent().getCenterX(),
                area.getBoundsInParent().getCenterY());
    }

    // Badge-Anchor (in Parent-Koordinaten)
    public Territory(String name, Shape area, Player owner, int armyCount, double badgeX, double badgeY) {
        this.name = name;
        this.area = area;
        this.owner = owner;
        this.armyCount = armyCount;
        this.badgeX = badgeX;
        this.badgeY = badgeY;

        area.setStroke(Color.color(0, 0, 0, 0.65));
        area.setStrokeWidth(STROKE_NORMAL);

        // ✅ wichtig: Stroke liegt außerhalb, damit Nachbar-Staaten ihn nicht "wegdecken"
        area.setStrokeType(StrokeType.OUTSIDE);

        armyLabel = new Label(String.valueOf(armyCount));
        armyLabel.setStyle(
                "-fx-font-weight: bold; " +
                "-fx-text-fill: black; " +
                "-fx-background-color: rgba(255,255,255,0.65); " +
                "-fx-padding: 2 6 2 6; " +
                "-fx-background-radius: 10;"
        );

        // Label soll Klicks nicht blocken
        armyLabel.setMouseTransparent(true);

        updateFill();
        updateHighlight();
        updateStroke();

        getChildren().addAll(area, armyLabel);

        // nach CSS/Layout korrekt positionieren
        Platform.runLater(this::repositionBadge);

        setOnMouseClicked(e -> {
            if (onSelected != null) onSelected.accept(this);
        });
    }

    // ===================== HOVER =====================
    public void setHovered(boolean hovered) {
        this.hovered = hovered;

        if (hovered) {
            // ✅ Hover-Linie soll IMMER über angrenzenden Staaten sichtbar sein
            this.toFront();
            armyLabel.toFront();
        }

        updateStroke();
    }

    public boolean isHovered() {
        return hovered;
    }

    private void updateStroke() {
        // Highlight hat Priorität über Hover
        if (highlighted) {
            area.setStrokeWidth(STROKE_HOVER);
            area.setStroke(Color.GOLD);
            return;
        }

        area.setStroke(Color.color(0, 0, 0, 0.65));
        area.setStrokeWidth(hovered ? STROKE_HOVER : STROKE_NORMAL);
    }

    // ===================== BADGE =====================
    public void setBadgeOffset(double dx, double dy) {
        this.badgeOffsetX = dx;
        this.badgeOffsetY = dy;
        repositionBadge();
    }

    public void setBadgeAnchor(double x, double y) {
        this.badgeX = x;
        this.badgeY = y;
        repositionBadge();
    }

    private void repositionBadge() {
        armyLabel.applyCss();
        Bounds lb = armyLabel.getLayoutBounds();

        double x = (badgeX + badgeOffsetX) - (lb.getWidth() / 2.0);
        double y = (badgeY + badgeOffsetY) - (lb.getHeight() / 2.0);

        armyLabel.setLayoutX(x);
        armyLabel.setLayoutY(y);

        armyLabel.toFront();
    }

    // ===================== NEIGHBORS =====================
    public void addNeighbor(Territory other) {
        if (other == null || other == this) return;
        neighbors.add(other);
    }

    public boolean isNeighborOf(Territory other) {
        return other != null && neighbors.contains(other);
    }

    // ===================== HIGHLIGHT =====================
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        updateHighlight();
        updateStroke();
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    private void updateHighlight() {
        setEffect(highlighted ? new DropShadow(20, Color.GOLD) : null);
    }

    // ===================== OWNERSHIP =====================
    private void updateFill() {
        if (owner == Player.BLUE) area.setFill(Color.LIGHTBLUE);
        else if (owner == Player.RED) area.setFill(Color.SALMON);
        else area.setFill(Color.LIGHTGRAY);
    }

    public void setOnTerritorySelectedListener(Consumer<Territory> listener) {
        this.onSelected = listener;
    }

    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; updateFill(); }

    public int getArmyCount() { return armyCount; }
    public void setArmyCount(int armyCount) {
        this.armyCount = armyCount;
        armyLabel.setText(String.valueOf(armyCount));
        repositionBadge();
    }

    public String getName() { return name; }
    public Shape getArea() { return area; }

    @Override
    public String toString() {
        return name;
    }
}