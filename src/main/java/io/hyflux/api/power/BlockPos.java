package io.fabrica.api.power;

import java.util.Objects;

/**
 * Simple immutable 3D block position.
 * Used by the power API to identify block locations.
 */
public final class BlockPos {
    private final int x;
    private final int y;
    private final int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    /**
     * Returns the position offset by the given amounts.
     */
    public BlockPos offset(int dx, int dy, int dz) {
        return new BlockPos(x + dx, y + dy, z + dz);
    }

    /**
     * Returns the position one block north (negative Z).
     */
    public BlockPos north() {
        return offset(0, 0, -1);
    }

    /**
     * Returns the position one block south (positive Z).
     */
    public BlockPos south() {
        return offset(0, 0, 1);
    }

    /**
     * Returns the position one block east (positive X).
     */
    public BlockPos east() {
        return offset(1, 0, 0);
    }

    /**
     * Returns the position one block west (negative X).
     */
    public BlockPos west() {
        return offset(-1, 0, 0);
    }

    /**
     * Returns the position one block up (positive Y).
     */
    public BlockPos up() {
        return offset(0, 1, 0);
    }

    /**
     * Returns the position one block down (negative Y).
     */
    public BlockPos down() {
        return offset(0, -1, 0);
    }

    /**
     * Returns all 6 adjacent positions (cardinal directions).
     */
    public BlockPos[] getAdjacent() {
        return new BlockPos[] {
            north(), south(), east(), west(), up(), down()
        };
    }

    /**
     * Calculates the Manhattan distance to another position.
     */
    public int manhattanDistance(BlockPos other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y) + Math.abs(z - other.z);
    }

    /**
     * Calculates the squared Euclidean distance to another position.
     */
    public double distanceSquared(BlockPos other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Checks if this position is directly adjacent to another (Manhattan distance of 1).
     */
    public boolean isAdjacent(BlockPos other) {
        return manhattanDistance(other) == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos blockPos = (BlockPos) o;
        return x == blockPos.x && y == blockPos.y && z == blockPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
