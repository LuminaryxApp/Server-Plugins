package com.luminary.mines.schematic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;

/**
 * Wrapper for a WorldEdit schematic/clipboard representing a mine layout.
 */
public class MineSchematic {

    private final String name;
    private final Clipboard clipboard;
    private final int width;
    private final int height;
    private final int length;
    private int wallThickness = 1; // Default wall thickness

    // Custom mine region (offsets from schematic origin)
    private boolean hasCustomRegion = false;
    private int regionOffsetMinX, regionOffsetMinY, regionOffsetMinZ;
    private int regionOffsetMaxX, regionOffsetMaxY, regionOffsetMaxZ;

    public MineSchematic(String name, Clipboard clipboard) {
        this.name = name;
        this.clipboard = clipboard;

        Region region = clipboard.getRegion();
        this.width = region.getWidth();
        this.height = region.getHeight();
        this.length = region.getLength();
    }

    public String getName() {
        return name;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public int getVolume() {
        return width * height * length;
    }

    public int getWallThickness() {
        return wallThickness;
    }

    public void setWallThickness(int thickness) {
        this.wallThickness = thickness;
    }

    /**
     * Check if this schematic has a custom-defined mine region.
     */
    public boolean hasCustomRegion() {
        return hasCustomRegion;
    }

    /**
     * Set the custom mine region offsets (relative to schematic min corner).
     */
    public void setCustomRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.regionOffsetMinX = minX;
        this.regionOffsetMinY = minY;
        this.regionOffsetMinZ = minZ;
        this.regionOffsetMaxX = maxX;
        this.regionOffsetMaxY = maxY;
        this.regionOffsetMaxZ = maxZ;
        this.hasCustomRegion = true;
    }

    /**
     * Clear the custom region (fall back to wall thickness calculation).
     */
    public void clearCustomRegion() {
        this.hasCustomRegion = false;
    }

    // Region offset getters
    public int getRegionOffsetMinX() { return regionOffsetMinX; }
    public int getRegionOffsetMinY() { return regionOffsetMinY; }
    public int getRegionOffsetMinZ() { return regionOffsetMinZ; }
    public int getRegionOffsetMaxX() { return regionOffsetMaxX; }
    public int getRegionOffsetMaxY() { return regionOffsetMaxY; }
    public int getRegionOffsetMaxZ() { return regionOffsetMaxZ; }

    /**
     * Get the interior volume (excluding walls).
     */
    public int getInteriorVolume() {
        if (hasCustomRegion) {
            int w = regionOffsetMaxX - regionOffsetMinX + 1;
            int h = regionOffsetMaxY - regionOffsetMinY + 1;
            int l = regionOffsetMaxZ - regionOffsetMinZ + 1;
            return w * h * l;
        }
        int interiorWidth = Math.max(0, width - (wallThickness * 2));
        int interiorHeight = Math.max(0, height - (wallThickness * 2));
        int interiorLength = Math.max(0, length - (wallThickness * 2));
        return interiorWidth * interiorHeight * interiorLength;
    }
}
