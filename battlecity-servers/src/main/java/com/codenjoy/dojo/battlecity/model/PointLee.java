package com.codenjoy.dojo.battlecity.model;

public class PointLee {
    private int x;
    private int y;

    public PointLee(int x, int y) {
        this.x = x;
        this.y = y;
    }

    PointLee move(PointLee delta) {
        return new PointLee(
                this.x + delta.x,
                this.y + delta.y);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointLee pointLee = (PointLee) o;

        if (x != pointLee.x) return false;
        return y == pointLee.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return String.format("[x:%2d, y:%2d]", x, y);
    }

    public static void main(String[] args) {
        PointLee p = new PointLee(3, 5);
        PointLee p2 = new PointLee(3, 5);
        System.out.println(p);
        System.out.println(p.equals(p2));
        PointLee p3 = p.move(new PointLee(1, 1));
        System.out.println(p3);
    }
}
