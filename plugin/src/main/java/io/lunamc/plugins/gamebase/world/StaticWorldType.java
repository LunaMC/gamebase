package io.lunamc.plugins.gamebase.world;

import io.lunamc.gamebase.world.WorldType;

public class StaticWorldType implements WorldType {

    private final boolean supportsSkyLight;

    public StaticWorldType(boolean supportsSkyLight) {
        this.supportsSkyLight = supportsSkyLight;
    }

    @Override
    public boolean supportsSkyLight() {
        return supportsSkyLight;
    }
}
