/*
 * Iris is a World Generator for Minecraft Bukkit Servers
 * Copyright (c) 2021 Arcane Arts (Volmit Software)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.volmit.iris.engine.mantle.components;

import com.volmit.iris.engine.data.cache.Cache;
import com.volmit.iris.engine.mantle.EngineMantle;
import com.volmit.iris.engine.mantle.IrisMantleComponent;
import com.volmit.iris.engine.mantle.MantleWriter;
import com.volmit.iris.engine.object.biome.IrisBiome;
import com.volmit.iris.engine.object.carving.IrisCarving;
import com.volmit.iris.engine.object.feature.IrisFeaturePositional;
import com.volmit.iris.engine.object.feature.IrisFeaturePotential;
import com.volmit.iris.engine.object.regional.IrisRegion;
import com.volmit.iris.util.collection.KList;
import com.volmit.iris.util.documentation.ChunkCoordinates;
import com.volmit.iris.util.mantle.MantleFlag;
import com.volmit.iris.util.math.RNG;

import java.util.function.Consumer;

public class MantleCarvingComponent extends IrisMantleComponent {
    public MantleCarvingComponent(EngineMantle engineMantle) {
        super(engineMantle, MantleFlag.CARVED);
    }

    @Override
    public void generateLayer(MantleWriter writer, int x, int z, Consumer<Runnable> post) {
        RNG rng = new RNG(Cache.key(x, z) + seed());
        int xxx = 8 + (x << 4);
        int zzz = 8 + (z << 4);
        IrisRegion region = getComplex().getRegionStream().get(xxx, zzz);
        IrisBiome biome = getComplex().getTrueBiomeStreamNoFeatures().get(xxx, zzz);
        carve(writer, rng, x, z, region, biome);
    }

    @ChunkCoordinates
    private void carve(MantleWriter writer, RNG rng, int cx, int cz, IrisRegion region, IrisBiome biome) {
        carve(getDimension().getCarving(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
        carve(biome.getCarving(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
        carve(region.getCarving(), writer, new RNG((rng.nextLong() * cx) + 490495 + cz), cx, cz);
    }

    @ChunkCoordinates
    private void carve(IrisCarving carving, MantleWriter writer, RNG rng, int cx, int cz) {
        carving.doCarving(writer, rng, getEngineMantle().getEngine(), cx << 4, cz << 4);
    }
}