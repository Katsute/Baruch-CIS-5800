/*
 * Copyright (C) 2022 Katsute <https://github.com/Katsute>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package dev.katsute.mis;

import java.util.Random;

abstract class Mock {

    private static Random rd = new Random();

    public static boolean mock(double chance){
        return rd.nextDouble() <= chance;
    }

    public static JsonBuilder mockSubwayDelay(final String line, final String station, final String lang){
        final String d = rd.nextBoolean() ? "NYPD" : "FDNY";
        return mockAlert(
            line + " trains are running with delays in both directions while the " + d + " finishes an investigation at " + station,
            lang
        );
    }

    public static JsonBuilder mockSubwayWeather(final String line, final String lang){
        return mockAlert(
            line + " trains are running with extensive delays in both directions due to inclement weather",
            lang
        );
    }

    public static JsonBuilder mockBusDelay(final String line, final String stop, final String lang){
        return mockAlert(
            line + " buses are running with delays in both directions due to construction at " + stop,
            lang
        );
    }

    public static JsonBuilder mockBusWeather(final String line, final String lang){
        return mockAlert(
            line + " buses are running with delays in both directions due to inclement weather",
            lang
        );
    }

    private static JsonBuilder mockAlert(final String desc, final String lang){
        return new JsonBuilder()
            .set("header", desc)
            .set("header_translated", RequestHandler.translate(desc.trim(), "en", lang))
            .set("description", desc.trim())
            .set("description_translated", RequestHandler.translate(desc.trim(), "en", lang))
            .set("type", "Delays")
            .set("effect", "UNKNOWN_EFFECT")
            .set("slow", desc.contains("slow") || desc.contains("delay"))
            .set("skip", desc.startsWith("no") || desc.contains("skips") || desc.contains("skipped"))
            .set("construction", desc.contains("construction"))
            .set("weather", desc.contains("weather"))
            .set("police", desc.contains("police") || desc.contains("nypd"))
            .set("fire", desc.contains("fire") || desc.contains("fdny"))
            .set("ems", desc.contains("ems"));
    }

}
