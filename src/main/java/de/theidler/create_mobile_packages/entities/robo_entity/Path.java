package de.theidler.create_mobile_packages.entities.robo_entity;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class Path {
    private final Vec3[] points;
    private int currentIndex;

    public Path(Vec3[] points) {
        this.points = points;
        this.currentIndex = 0;
    }

    public Path(Vec3 point) {
        this(new Vec3[]{point});
    }

    public Path() {
        this(new Vec3[]{});
    }

    public @Nullable Vec3 getNextPoint() {
        if (currentIndex < points.length) {
            return points[currentIndex++];
        } else {
            return null;
        }
    }

}
