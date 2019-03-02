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

import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {

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

        String move = moveDir(board);

        return move + ',' + Direction.ACT.toString();
    }

    public static void main(String[] args) {
        WebSocketRunner.runClient(
                // paste here board page url from browser after registration
                "http://algoritmix.dan-it.kiev.ua/codenjoy-contest/board/player/qfzvov5ud0q0drg8x3ug?code=5850506025977822212",
                new YourSolver(new RandomDice()),
                new Board());
    }

    private String moveDir(Board board) {
        int meX = board.getMe().getX();
        int meY = board.getMe().getY();
        Direction move = Direction.DOWN;

        if (!isBarrierAhead(board)) {
            move = currentDirection;
        } else {
            // find empty cell
            if (!board.isBarrierAt(meX + 1, meY)) {
                move = Direction.RIGHT;
            } else if (!board.isBarrierAt(meX, meY + 1)) {
                move = Direction.UP;
            } else if (!board.isBarrierAt(meX - 1, meY)) {
                move = Direction.LEFT;
            } else if (!board.isBarrierAt(meX, meY - 1)) {
                move = Direction.DOWN;
            }
        }

        currentDirection = move;
        return move.toString();
    }

    private boolean isBarrierAhead(Board board) {
        int meX = board.getMe().getX();
        int meY = board.getMe().getY();
        Point point = new PointImpl(meX, meY);
        Point potentialMove = currentDirection.change(point);
        return board.isBarrierAt(potentialMove.getX(), potentialMove.getY());
    }
}
