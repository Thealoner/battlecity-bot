package com.codenjoy.dojo.battlecity.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.battlecity.model.BoardLee;
import com.codenjoy.dojo.battlecity.model.Elements;
import com.codenjoy.dojo.battlecity.model.PointLee;
import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {

    private final static String URL = "http://algoritmix.dan-it.kiev.ua/codenjoy-contest/board/player/qfzvov5ud0q0drg8x3ug?code=5850506025977822212";

    private Dice dice;
    private Board board;
    private Board prevBoard;
    private final List<Point> bulletDeltas = new ArrayList<Point>(){{
        add(new PointImpl(0,-2));
        add(new PointImpl(-2,0));
        add(new PointImpl(+2,0));
        add(new PointImpl(0,+2));
        add(new PointImpl(0,-1));
        add(new PointImpl(-1,0));
        add(new PointImpl(+1,0));
        add(new PointImpl(0,+1));
    }};

    public YourSolver(Dice dice) {
        this.dice = dice;
    }

    @Override
    public String get(Board board) {
        this.board = board;
        if (board.isGameOver()) return "";
        String shootAfter = "";

        char[][] field = board.getField();
        int sizeY = field[0].length;

        Point destPoint = getClosestEnemy();
        Point me = board.getMe();

        PointLee dest = new PointLee(destPoint.getX(), invertVertical(destPoint.getY(), sizeY));

        Direction plannedDirection = getDirectionToDestination(dest);
        Direction direction = plannedDirection;
        Point nextPoint = me.copy();
        nextPoint.change(plannedDirection);
        List<Point> dangerBulletsNow = getDangerousBullets(me);
        List<Point> dangerBulletsNext = getDangerousBullets(nextPoint);
        List<Direction> directions = Direction.onlyDirections();


        if (!dangerBulletsNow.isEmpty()) {
            System.out.println("Danger Now!");
            Optional<Direction> safeDirection = directions.stream()
                    .filter(dir -> {
                        Point p = me.copy();
                        p.change(dir);
                        return !p.equals(dangerBulletsNow) && plannedDirection != dir && plannedDirection != dir.inverted() && !board.isBarrierAt(p);
                    })
                    .findFirst();
            if (safeDirection.isPresent()) {
                direction = safeDirection.get();
            }
        } else if (!dangerBulletsNext.isEmpty()) {
            System.out.println("Danger Next!");
            Optional<Direction> safeDirection = directions.stream()
                    .filter(dir -> {
                        Point p = me.copy();
                        p.change(dir);
                        return plannedDirection != dir && plannedDirection != dir.inverted() && !board.isBarrierAt(p);
                    })
                    .findFirst();
            if (safeDirection.isPresent()) {
                direction = safeDirection.get();
            }
        }


        // if distance to enemy < 5 && not on the same line
        // then don't shoot
        double distanceToEnemy = me.distance(destPoint);

        if (distanceToEnemy > 4 && distanceToEnemy < 7 || !sameLine(me, destPoint) || !lookingAt(me, destPoint)) {
            // don't shoot
        } else if (Math.abs(destPoint.getX() - me.getX()) == 1 && Math.abs(destPoint.getY() - me.getY()) == 1) {
            // what until the enemy comes to the next cell
            direction = Direction.STOP;
        } else if (Math.round(distanceToEnemy) == 1) {
            // when stuck, go to the prev enemy location and SHOOT!
            direction = Direction.STOP;
//            destPoint = getClosestEnemy(prevBoard);
//            dest = new PointLee(destPoint.getX(), invertVertical(destPoint.getY(), sizeY));
//            direction = getDirectionToDestination(board, dest);
            shootAfter = ',' + Direction.ACT.toString();
        } else if (Math.abs(destPoint.getX() - me.getX()) == 1 || Math.abs(destPoint.getY() - me.getY()) == 1) {
            // Doesn't seem to work
            System.out.println("Getting in the same line with enemy.");
            if (destPoint.getX() - me.getX() == 1) {
                direction = Direction.RIGHT;
            } if (destPoint.getX() - me.getX() == -1) {
                direction = Direction.LEFT;
            } if (destPoint.getY() - me.getY() == 1) {
                direction = Direction.UP;
            } if (destPoint.getX() - me.getX() == -1) {
                direction = Direction.DOWN;
            }
            shootAfter = ',' + Direction.ACT.toString();
        } else {
            shootAfter = ',' + Direction.ACT.toString();
        }

        this.prevBoard = board;
        return direction.toString() + shootAfter;
    }

    private boolean lookingAt(Point me, Point destPoint) {
        if (board.getAt(me).equals(Elements.TANK_UP) && me.getY() < destPoint.getY()) return true;
        if (board.getAt(me).equals(Elements.TANK_DOWN) && me.getY() > destPoint.getY()) return true;
        if (board.getAt(me).equals(Elements.TANK_RIGHT) && me.getX() < destPoint.getX()) return true;
        if (board.getAt(me).equals(Elements.TANK_LEFT) && me.getX() > destPoint.getX()) return true;

        return false;
    }

    private boolean sameLine(Point me, Point destPoint) {
        return me.getX() == destPoint.getX() || me.getY() == destPoint.getY();
    }

    public static void main(String[] args) {
        WebSocketRunner.runClient(
                // paste here board page url from browser after registration
                URL,
                new YourSolver(new RandomDice()),
                new Board());
    }
    
    private Direction getDirectionToDestination(PointLee dest) {
        char[][] field = board.getField();
        int sizeX = field.length;
        int sizeY = field[0].length;
        BoardLee boardLee = new BoardLee(sizeX, sizeY);
        List<Point> barriers = board.getBarriers();
//        List<Point> enemies = board.getEnemies();
        barriers.forEach(p -> boardLee.setObstacle(new PointLee(p.getX(), invertVertical(p.getY(), sizeY))));
//        enemies.forEach(p -> boardLee.setObstacle(new PointLee(p.getX(), invertVertical(p.getY(), sizeY))));
        Point me = board.getMe();
        PointLee src = new PointLee(me.getX(), invertVertical(me.getY(), sizeY));
        Optional<List<PointLee>> solution = boardLee.trace(src, dest);

        if (solution.isPresent()) {
            List<PointLee> path = solution.get();
            PointLee p = path.stream().skip(1).findFirst().get();
            System.out.printf("ME: x:%2d, y:%2d\n", me.getX(), me.getY());
            int to_x = p.x();
            int to_y = invertVertical(p.y(), sizeY);
            System.out.printf("TO: x:%2d, y:%2d\n", to_x, to_y);
            if (to_x < me.getX()) return Direction.LEFT;
            if (to_y > me.getY()) return Direction.UP;
            if (to_x > me.getX()) return Direction.RIGHT;
            if (to_y < me.getY()) return Direction.DOWN;
        }
        return Direction.ACT;
    }

    private Point getClosestEnemy() {
        List<Point> enemies = board.getEnemies();
        Point me = board.getMe();
        return enemies.stream()
                .min(Comparator.comparing(p -> Math.floor(p.distance(me))))
                .get();
    }

    private List<Point> getDangerousBullets(Point p) {
        return bulletDeltas.stream()
                .filter(delta -> {
                    Point bulletPoint = p.copy();
                    bulletPoint.change(delta);
                    if (this.board.isBulletAt(bulletPoint)) System.out.println("Danger! " + bulletPoint);
                    return this.board.isBulletAt(bulletPoint);
                })
                .collect(Collectors.toList());
    }

    private int invertVertical(int val, int dimY) {
        return dimY - val - 1;
    }
}
