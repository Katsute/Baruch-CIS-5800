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

import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.handler.file.FileAdapter;
import dev.katsute.simplehttpserver.handler.file.FileHandler;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

final class MimeFileHandler extends FileHandler {

    private static final FileAdapter adapter = new FileAdapter(){
        @Override
        public final String getName(final File file){
            final String name = file.getName();
            return name.endsWith(".html") ? name.substring(0, name.length() - 5) : name;
        }
    };

    public MimeFileHandler(){
        super(adapter);
    }

    @Override
    public final void handle(final SimpleHttpExchange exchange, final File source, final byte[] bytes) throws IOException {
        final String name = source.getName();
        String mime;
        if(name.endsWith(".html"))
            mime = "text/html";
        else if(name.endsWith(".css"))
            mime = "text/css";
        else if(name.endsWith(".js"))
            mime = "application/javascript";
        else
            mime = "text/plain";
        exchange.getResponseHeaders().add("Content-Type", mime);
        exchange.send(bytes, HttpURLConnection.HTTP_OK);
    }

}