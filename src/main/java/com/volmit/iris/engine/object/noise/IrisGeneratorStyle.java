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

package com.volmit.iris.engine.object.noise;

import com.volmit.iris.core.loader.IrisData;
import com.volmit.iris.engine.data.cache.AtomicCache;
import com.volmit.iris.engine.object.annotations.Desc;
import com.volmit.iris.engine.object.annotations.MaxNumber;
import com.volmit.iris.engine.object.annotations.MinNumber;
import com.volmit.iris.engine.object.annotations.RegistryListResource;
import com.volmit.iris.util.math.RNG;
import com.volmit.iris.util.noise.CNG;
import com.volmit.iris.util.noise.ExpressionNoise;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Desc("A gen style")
@Data
public class IrisGeneratorStyle {
    @Desc("The chance is 1 in CHANCE per interval")
    private NoiseStyle style = NoiseStyle.FLAT;

    @MinNumber(0.00001)
    @Desc("The zoom of this style")
    private double zoom = 1;

    @Desc("Instead of using the style property, use a custom expression to represent this style.")
    @RegistryListResource(IrisExpression.class)
    private String expression = null;

    @MinNumber(0.00001)
    @Desc("The Output multiplier. Only used if parent is fracture.")
    private double multiplier = 1;

    @Desc("If set to true, each dimension will be fractured with a different order of input coordinates. This is usually 2 or 3 times slower than normal.")
    private boolean axialFracturing = false;

    @Desc("Apply a generator to the coordinate field fed into this parent generator. I.e. Distort your generator with another generator.")
    private IrisGeneratorStyle fracture = null;

    @MinNumber(0.01562)
    @MaxNumber(64)
    @Desc("The exponent")
    private double exponent = 1;

    private final transient AtomicCache<CNG> cng = new AtomicCache<>();

    public IrisGeneratorStyle(NoiseStyle s) {
        this.style = s;
    }

    public IrisGeneratorStyle zoomed(double z) {
        this.zoom = z;
        return this;
    }

    public CNG create(RNG rng, IrisData data) {
        return cng.aquire(() ->
        {
            if (getExpression() != null) {
                IrisExpression e = data.getExpressionLoader().load(getExpression());

                if (e != null) {
                    CNG cng = new CNG(rng, new ExpressionNoise(rng, e), 1D, 1)
                            .bake().scale(1D / zoom).pow(exponent).bake();
                    cng.setTrueFracturing(axialFracturing);

                    if (fracture != null) {
                        cng.fractureWith(fracture.create(rng.nextParallelRNG(2934), data), fracture.getMultiplier());
                    }

                    return cng;
                }
            }

            CNG cng = style.create(rng).bake().scale(1D / zoom).pow(exponent).bake();
            cng.setTrueFracturing(axialFracturing);

            if (fracture != null) {
                cng.fractureWith(fracture.create(rng.nextParallelRNG(2934), data), fracture.getMultiplier());
            }

            return cng;
        });
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isFlat() {
        return style.equals(NoiseStyle.FLAT);
    }

    public double getMaxFractureDistance() {
        return multiplier;
    }
}
