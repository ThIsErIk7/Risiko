package com.risiko.map;

import java.util.ArrayList;
import java.util.List;

import com.risiko.model.Player;
import com.risiko.view.Territory;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class MapGenerator {

    private static final double HEX_SIZE = 34;

    private static final double X_SPACING = HEX_SIZE * Math.sqrt(3);
    private static final double Y_SPACING = HEX_SIZE * 1.5;

    // Insel Offsets
    private static final double I1_X = 140, I1_Y = 135;
    private static final double I2_X = 780, I2_Y = 135;
    private static final double I3_X = 480, I3_Y = 560;

    // Bridge-Infos fürs Zeichnen im Main
    public static record Bridge(String from, String to) {}
    private final List<Bridge> bridges = new ArrayList<>();

    public List<Bridge> getBridges() {
        return bridges;
    }

    public List<Territory> generate(Pane mapPane) {
        bridges.clear();

        List<Territory> territories = new ArrayList<>();

        // =========================================================
        // INSEL 1
        // =========================================================
        Territory a = createState("A", Player.NONE, I1_X, I1_Y, new int[][]{
                {0,1},{0,2},{0,3},
                {1,0},{1,1},{1,2},{1,3}
        });

        Territory b = createState("B", Player.NONE, I1_X, I1_Y, new int[][]{
                {2,0},{2,1},{2,2},
                {3,0},{3,1}
        });

        Territory c = createState("C", Player.NONE, I1_X, I1_Y, new int[][]{
                {4,0},{4,1},{4,2},
                {5,1},{5,2}
        });

        Territory d = createState("D", Player.NONE, I1_X, I1_Y, new int[][]{
                {2,3},{2,4},
                {3,2},{3,3},{3,4},
                {4,3},{4,4}
        });

        Territory e = createState("E", Player.NONE, I1_X, I1_Y, new int[][]{
                {5,3},{5,4},
                {4,5},{5,5}
        });

        link(a,b); link(b,c); link(b,d); link(a,d); link(d,e); link(c,e);
        territories.addAll(List.of(a,b,c,d,e));

        // =========================================================
        // INSEL 2
        // =========================================================
        Territory f = createState("F", Player.NONE, I2_X, I2_Y, new int[][]{
                {1,0},{2,0},{3,0},
                {1,1},{2,1}
        });

        Territory h = createState("H", Player.NONE, I2_X, I2_Y, new int[][]{
                {0,0},{0,1},{0,2},
                {1,2},{1,3}
        });

        Territory j = createState("J", Player.NONE, I2_X, I2_Y, new int[][]{
                {2,2},{3,1},{3,2},{2,3},{3,3}
        });

        Territory i = createState("I", Player.NONE, I2_X, I2_Y, new int[][]{
                {2,4},{3,4},{4,4},
                {4,3},{5,4}
        });

        Territory g = createState("G", Player.NONE, I2_X, I2_Y, new int[][]{
                {4,1},{5,1},{5,2},
                {5,3},{4,2}
        });

        link(f, j);
        link(f, h);
        link(h, j);
        link(j, g);
        link(j, i);
        link(g, i);

        territories.addAll(List.of(f,g,h,i,j));

        // =========================================================
        // INSEL 3
        // =========================================================
        Territory k = createState("K", Player.NONE, I3_X, I3_Y, new int[][]{
                {0,1},{0,2},{0,3},
                {1,1},{1,2}
        });

        Territory l = createState("L", Player.NONE, I3_X, I3_Y, new int[][]{
                {1,0},{2,0},{3,0},
                {2,1},{3,1}
        });

        Territory m = createState("M", Player.NONE, I3_X, I3_Y, new int[][]{
                {2,2},{3,2},{4,2},
                {2,3},{3,3}
        });

        Territory n = createState("N", Player.NONE, I3_X, I3_Y, new int[][]{
                {4,0},{4,1},{5,1},
                {5,2},{4,3}
        });

        Territory o = createState("O", Player.NONE, I3_X, I3_Y, new int[][]{
                {1,3},{1,4},{2,4},{3,4}
        });

        link(k,l);
        link(k,m);
        link(l,m);
        link(m,n);
        link(m,o);
        link(k,o);

        territories.addAll(List.of(k,l,m,n,o));

        // =========================================================
        // BRÜCKEN (ALTE RAUS, NUR DEINE REIN)
        // =========================================================
        // C <-> H
        link(c, h);
        bridges.add(new Bridge("C", "H"));

        // E <-> H
        link(e, h);
        bridges.add(new Bridge("E", "H"));

        // N <-> I
        link(n, i);
        bridges.add(new Bridge("N", "I"));

        mapPane.getChildren().addAll(territories);
        return territories;
    }

    private void link(Territory a, Territory b) {
        a.addNeighbor(b);
        b.addNeighbor(a);
    }

    private Territory createState(String name, Player owner, double offsetX, double offsetY, int[][] hexCoords) {
        List<Shape> hexes = new ArrayList<>();

        double sumX = 0;
        double sumY = 0;

        for (int[] c : hexCoords) {
            int q = c[0];
            int r = c[1];

            double centerX = q * X_SPACING + (r % 2) * (X_SPACING / 2) + offsetX;
            double centerY = r * Y_SPACING + offsetY;

            sumX += centerX;
            sumY += centerY;

            hexes.add(createHex(centerX, centerY));
        }

        Shape area = unionAll(hexes);

        // Badge Anchor = Durchschnitt der Hex-Zentren
        double badgeX = sumX / hexCoords.length;
        double badgeY = sumY / hexCoords.length;

        // Start: 0 Armeen
        return new Territory(name, area, owner, 0, badgeX, badgeY);
    }

    private Polygon createHex(double cx, double cy) {
        Polygon p = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angleDeg = 60 * i - 30;
            double angleRad = Math.toRadians(angleDeg);
            double x = cx + HEX_SIZE * Math.cos(angleRad);
            double y = cy + HEX_SIZE * Math.sin(angleRad);
            p.getPoints().addAll(x, y);
        }
        p.setFill(Color.TRANSPARENT);
        p.setStroke(Color.TRANSPARENT);
        return p;
    }

    private Shape unionAll(List<Shape> shapes) {
        Shape out = shapes.get(0);
        for (int i = 1; i < shapes.size(); i++) {
            out = Shape.union(out, shapes.get(i));
        }
        return out;
    }
}