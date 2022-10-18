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

import dev.katsute.onemta.MTA;
import dev.katsute.onemta.bus.Bus;
import dev.katsute.onemta.subway.Subway;
import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.regex.Pattern;

final class RequestHandler implements SimpleHttpHandler {

    private static final Pattern newline = Pattern.compile("\\n\\r?");
    private static final Pattern quote = Pattern.compile("\"");

    private final MTA mta;

    RequestHandler(final MTA mta){
        this.mta = mta;
    }

    @Override
    public final void handle(final SimpleHttpExchange exchange){
        try{
            // enforce GET
            if(!exchange.getRequestMethod().equalsIgnoreCase("GET")){
                exchange.send(String.format("{\n    \"error\": \"Method not allowed '%s'\"\n}", exchange.getRequestMethod().toUpperCase()), HttpURLConnection.HTTP_BAD_METHOD);
                return;
            }

            // parameters
            final Map<String, String> query = exchange.getGetMap();

            final String type = query.get("type");
            {
                if(!"bus".equalsIgnoreCase(type) && !"subway".equalsIgnoreCase(type)){
                    if(type == null)
                        exchange.send("{\n    \"error\": \"Missing type\"\n}", HttpURLConnection.HTTP_BAD_REQUEST);
                    else
                        exchange.send(String.format("{\n    \"error\": \"Unknown type '%s'\"\n}", type), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
            }

            final String route = query.get("route");
            {
                if(route == null){
                    exchange.send("{\n    \"error\": \"Missing route\"\n}", HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
            }

            final double lat, lon;
            {
                if(!query.containsKey("latitude")){
                    exchange.send("{\n    \"error\": \"Missing latitude\"\n}", HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
                if(!query.containsKey("longitude")){
                    exchange.send("{\n    \"error\": \"Missing longitude\"\n}", HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }

                try{
                    lat = Double.parseDouble(query.get("latitude"));
                }catch(final NumberFormatException ignored){
                    exchange.send(String.format("{\n    \"error\": \"Unknown latitude '%s'\"\n}", query.get("latitude")), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
                try{
                    lon = Double.parseDouble(query.get("longitude"));
                }catch(final NumberFormatException ignored){
                    exchange.send(String.format("{\n    \"error\": \"Unknown longitude '%s'\"\n}", query.get("longitude")), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
            }

            if(type.equalsIgnoreCase("bus")){
                final Bus.Route r;
                try{
                    r = mta.getBusRoute(route);
                }catch(final NullPointerException ignored){
                     exchange.send(String.format("{\n    \"error\": \"Failed to find route '%s'\"\n}", route), HttpURLConnection.HTTP_NOT_FOUND);
                    return;
                }

                final Bus.Vehicle bus;

                // todo: guess vehicle
            }else if(type.equalsIgnoreCase("subway")){
                final Subway.Route s;
                try{
                    s = mta.getSubwayRoute(route);
                }catch(final NullPointerException ignored){
                     exchange.send(String.format("{\n    \"error\": \"Failed to find route '%s'\"\n}", route), HttpURLConnection.HTTP_NOT_FOUND);
                    return;
                }

                final Subway.Vehicle subway;

                // todo: guess vehicle
            }
        }catch(final Throwable e){ // uncaught errors
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            e.printStackTrace();
            try{
                exchange.send(String.format("{\n    \"error\": \"%s\"\n}", newline.matcher(quote.matcher(sw.toString()).replaceAll("'")).replaceAll("\\n")), HttpURLConnection.HTTP_INTERNAL_ERROR);
            }catch(final IOException ignored){}
        }finally{
            exchange.close();
        }
    }

}
