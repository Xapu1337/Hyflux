package io.fabrica.transport;

import io.fabrica.api.power.BlockPos;

/**
 * Cardinal directions for conveyor belts.
 */
public enum Direction {
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    EAST(1, 0, 0),
    WEST(-1, 0, 0);

    private final int dx;
    private final int dy;
    private final int dz;

    Direction(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDz() {
        return dz;
    }

    /**
     * Gets the position in this direction from the given position.
     */
    public BlockPos offset(BlockPos pos) {
        return pos.offset(dx, dy, dz);
    }

    /**
     * Gets the opposite direction.
     */
    public Direction getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    /**
     * Gets the direction rotated 90 degrees clockwise.
     */
    public Direction rotateClockwise() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    /**
     * Gets the direction rotated 90 degrees counter-clockwise.
     */
    public Direction rotateCounterClockwise() {
        return switch (this) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            case EAST -> NORTH;
        };
    }

    /**
     * Gets the direction from one position to another.
     * Returns null if positions are not adjacent in a cardinal direction.
     */
    public static Direction fromPositions(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();

        // Must be exactly 1 block apart in one direction
        if (dy != 0) return null;

        if (dx == 1 && dz == 0) return EAST;
        if (dx == -1 && dz == 0) return WEST;
        if (dx == 0 && dz == 1) return SOUTH;
        if (dx == 0 && dz == -1) return NORTH;

        return null;
    }

    /**
     * Gets the rotation angle in degrees for rendering.
     */
    public int getRotationDegrees() {
        return switch (this) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
        };
    }
}
