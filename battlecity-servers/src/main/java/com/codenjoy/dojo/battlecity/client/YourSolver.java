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

import java.util.*;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {

    private final static String URL = "http://algoritmix.dan-it.kiev.ua/codenjoy-contest/board/player/qfzvov5ud0q0drg8x3ug?code=5850506025977822212";

    private Dice dice;
    private Board board;
    private Direction currentDirection = Direction.RIGHT;

    public YourSolver(Dice dice) {
        this.dice = dice;
    }

    @Override
    public String get(Board board) {
        this.board = board;
        if (board.isGameOver()) return "";

//        PointLee dest = new PointLee(21,21);
        char[][] field = board.getField();
        int sizeY = field[0].length;
        Point destPoint = getClosestEnemy();
        PointLee dest = new PointLee(destPoint.getX(), invertVertical(destPoint.getY(), sizeY));


        return getDirectionToDestination(dest).toString() + ',' + Direction.ACT.toString();
//        String move = moveDir(board);
//
//        return move + ',' + Direction.ACT.toString();
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
        barriers.forEach(p -> boardLee.setObstacle(p.getX(), invertVertical(p.getY(), sizeY)));
//        enemies.forEach(p -> boardLee.setObstacle(p.getX(), invertVertical(p.getY(), sizeY)));
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

    private int invertVertical(int val, int dimY) {
        return dimY - val - 1;
    }

    private String moveDir(Board board) {
        Direction move;

        if (enemyNeighbourDirection(board) != null) {
            move = Direction.ACT;
        } else {
            if (!isBarrierAhead(board)) {
                move = currentDirection;
            } else {
                move = findAvailableDirection(board);
            }
        }

        currentDirection = move;
        return move.toString();
    }

    private boolean isBarrierAhead(Board board) {
        Point point = getCurrentMePoint(board);
        Point potentialMove = currentDirection.change(point);
        return board.isBarrierAt(potentialMove.getX(), potentialMove.getY());
    }

    private Direction findAvailableDirection(Board board) {
        Point point = getCurrentMePoint(board);
        Direction randomDirection = Direction.UP;

        for (int i = 0; i < 100; i++) {
            randomDirection = Direction.random();
            Point potentialMove = randomDirection.change(point);

            if (!board.isBarrierAt(potentialMove.getX(), potentialMove.getY())) {
                return randomDirection;
            }
        }

        // default
        return randomDirection;
    }

    private Direction enemyNeighbourDirection(Board board) {
        Point point = getCurrentMePoint(board);
        int x = point.getX();
        int y = point.getY();

        Direction direction = null;
        List<Elements> neighbourCells = board.getNear(point);

        List<Elements> enemies = new ArrayList<Elements>(
            Arrays.asList(
                Elements.AI_TANK_UP,
                Elements.AI_TANK_DOWN,
                Elements.AI_TANK_LEFT,
                Elements.AI_TANK_RIGHT,
                Elements.OTHER_TANK_UP,
                Elements.OTHER_TANK_DOWN,
                Elements.OTHER_TANK_LEFT,
                Elements.OTHER_TANK_RIGHT
            )
        );

        enemies.forEach(enemy -> {
            if (board.isAt(x, y, enemy))
            return;
        });


        return direction;
    }

    private Point nearbyEnemyLocation() {
        int range = 5;
        Point point = PointImpl.pt(0, 0);
        return point;
    }

    private Point getCurrentMePoint(Board board) {
        int meX = board.getMe().getX();
        int meY = board.getMe().getY();
        return PointImpl.pt(meX, meY);
    }
}
