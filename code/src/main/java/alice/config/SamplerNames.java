package alice.config;


import alice.samplers.CurveballBJDMSampler;
import alice.samplers.BJDMSampler;
import diffusr.samplers.GmmtSampler;

/*
 * Copyright (C) 2022 Alexander Lee, Giulia Preti, and Matteo Riondato
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
/**
 * A class for the names of the samplers we use.
 */
public class SamplerNames {

    public static final String gmmtSampler = GmmtSampler.class.getName();
    public static final String caterSampler = BJDMSampler.class.getName();
    public static final String caterCurveballSampler = CurveballBJDMSampler.class.getName();
}
