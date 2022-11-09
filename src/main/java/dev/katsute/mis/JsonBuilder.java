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

import java.util.ArrayList;
import java.util.List;

final class JsonBuilder {

    final StringBuilder json = new StringBuilder();

    public JsonBuilder(){
        json.append('{');
    }

    public final JsonBuilder set(final String key, final boolean bool){
        return append(key, bool ? "true" : "false");
    }

    public final JsonBuilder set(final String key, final Number num){
        return append(key, num);
    }

    public final JsonBuilder set(final String key, final String object){
        return append(key, object == null ? null : '"' + sanitize(object) + '"');
    }

    public final JsonBuilder set(final String key, final JsonBuilder object){
        return append(key, object.build());
    }

    public final JsonBuilder set(final String key, final String[] strings){
        final List<String> buf = new ArrayList<>();
        for(final String s : strings)
            buf.add('"' + sanitize(s) + '"');
        return append(key, '[' + String.join(", ", buf) + ']');
    }

    public final JsonBuilder set(final String key, final List<JsonBuilder> objects){
        final List<String> buf = new ArrayList<>();
        for(final JsonBuilder obj : objects)
            buf.add(obj.build());
        return append(key, '[' + String.join(", ", buf) + ']');
    }

    private JsonBuilder append(final String key, final Object value){
        json.append('"').append(sanitize(key)).append('"').append(':').append(' ').append(value == null ? "null" : value).append(',');
        return this;
    }

    private String sanitize(final String raw){
        return raw
            .replace("\t", "\\t")
            .replace("\n", "\\n")
            .replace("\"", "\\\"");
    }

    public final String build(){
        if(json.length() > 1)
            json.setLength(json.length() - 1);
        json.append('}');
        return json.toString();
    }

    public static String singleton(final String key, final String value){
        return new JsonBuilder().set(key, value).build();
    }

}
