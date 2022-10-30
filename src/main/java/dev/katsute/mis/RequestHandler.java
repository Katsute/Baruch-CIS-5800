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
import dev.katsute.onemta.bus.BusDirection;
import dev.katsute.onemta.subway.Subway;
import dev.katsute.onemta.subway.SubwayDirection;
import dev.katsute.simplehttpserver.SimpleHttpExchange;
import dev.katsute.simplehttpserver.SimpleHttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
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
            System.out.println(exchange.getRequestMethod().toUpperCase() + " : " + exchange.getGetMap());
            // enforce GET
            if(!exchange.getRequestMethod().equalsIgnoreCase("GET")){
                exchange.send(JsonBuilder.singleton("error", "Method not allowed '" + exchange.getRequestMethod().toUpperCase() + "'"), HttpURLConnection.HTTP_BAD_METHOD);
                return;
            }

            // parameters
            final Map<String, String> query = exchange.getGetMap();

            final String type = query.get("type");
            {
                if(!"bus".equalsIgnoreCase(type) && !"subway".equalsIgnoreCase(type)){
                    exchange.send(JsonBuilder.singleton("error", type == null ? "Missing type" : ("Unknown type '" + type + "'")), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
            }

            final String id = query.get("id");
            final String route = query.get("route");

            final Double lat, lon;
            final Integer direction;
            if(id == null){
                if(route == null){
                    exchange.send(JsonBuilder.singleton("error", "Missing route"), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
                if(!query.containsKey("latitude")){
                    exchange.send(JsonBuilder.singleton("error", "Missing latitude"), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
                if(!query.containsKey("longitude")){
                    exchange.send(JsonBuilder.singleton("error", "Missing longitude"), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
                if(!query.containsKey("direction")){
                    exchange.send(JsonBuilder.singleton("error", "Missing direction"), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }

                try{
                    direction = Integer.parseInt(query.get("direction"));
                }catch(final NumberFormatException ignored){
                    exchange.send(JsonBuilder.singleton("error", "Unknown direction '" + query.get("direction") + "'"), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
                try{
                    lat = Double.parseDouble(query.get("latitude"));
                }catch(final NumberFormatException ignored){
                    exchange.send(JsonBuilder.singleton("error", "Unknown latitude '" + query.get("latitude") + "'"), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
                try{
                    lon = Double.parseDouble(query.get("longitude"));
                }catch(final NumberFormatException ignored){
                    exchange.send(JsonBuilder.singleton("error", "Unknown longitude '" + query.get("longitude") + "'"), HttpURLConnection.HTTP_BAD_REQUEST);
                    return;
                }
            }else{
                lat = lon = null;
                direction = null;
            }

            if(type.equalsIgnoreCase("bus")){
                final Bus.Vehicle bus;
                if(id == null){
                    final Bus.Route r;
                    try{
                        r = mta.getBusRoute(route);
                    }catch(final NullPointerException ignored){
                        exchange.send(JsonBuilder.singleton("error", "Failed to find route '" + route + '"'), HttpURLConnection.HTTP_NOT_FOUND);
                        return;
                    }

                    Bus.Vehicle buf = null;
                    Double min = null;

                    final BusDirection dir = BusDirection.asDirection(direction);
                    for(final Bus.Vehicle vehicle : r.getVehicles()){
                        if(!vehicle.getDirection().equals(dir))
                            continue;
                        double dist = distance(lon, lat, vehicle.getLongitude(), vehicle.getLatitude());
                        if(min == null || dist < min){
                            min = dist;
                            buf = vehicle;
                        }
                    }
                    bus = buf;
                }else
                    try{
                        bus = mta.getBus(Integer.parseInt(id));
                    }catch(final NumberFormatException ignored){
                        exchange.send(JsonBuilder.singleton("error", "Failed to find bus '" + id + '"'), HttpURLConnection.HTTP_NOT_FOUND);
                        return;
                    }

                if(bus == null){
                    exchange.send(JsonBuilder.singleton("error", "Failed to find bus '" + id + '"'), HttpURLConnection.HTTP_NOT_FOUND);
                    return;
                }

                final Bus.Route r = bus.getRoute();
                final Bus.Trip t = bus.getTrip();

                final List<JsonBuilder> stops = new ArrayList<>();
                for(final Bus.TripStop stop : t.getTripStops()){
                    final Bus.Stop s = stop.getStop();
                    stops.add(new JsonBuilder()
                        .set("id", stop.getStopID())
                        .set("name", stop.getStopName())
                        .set("latitude", s.getLatitude())
                        .set("longitude", s.getLongitude())
                    );
                }

                final JsonBuilder json = new JsonBuilder()
                    .set("vehicle", new JsonBuilder()
                        .set("id", bus.getVehicleID())
                        .set("express", bus.isExpress())
                        .set("limited", bus.isLimited())
                        .set("select+", bus.isSelectBusService())
                        .set("direction", bus.getDirection().name())
                        .set("bearing", bus.getBearing())
                        .set("latitude", bus.getLatitude())
                        .set("longitude", bus.getLongitude())
                    )
                    .set("route", new JsonBuilder()
                        .set("name", r.getRouteName())
                        .set("shortName", r.getRouteShortName())
                        .set("description", r.getRouteDescription())
                        .set("color", r.getRouteColor())
                        .set("textColor", r.getRouteTextColor())
                    )
                    .set("trip", stops);

                exchange.send(json.build());
            }else if(type.equalsIgnoreCase("subway")){
                final Subway.Vehicle subway;
                if(id == null){
                    final Subway.Route r;
                    try{
                        r = mta.getSubwayRoute(route);
                    }catch(final NullPointerException ignored){
                        exchange.send(JsonBuilder.singleton("error", "Failed to find route '" + route + '"'), HttpURLConnection.HTTP_NOT_FOUND);
                        return;
                    }

                    Subway.Vehicle buf = null;
                    Double min = null;

                    final SubwayDirection dir = SubwayDirection.asDirection(direction);
                    for(final Subway.Vehicle vehicle : r.getVehicles()){
                        if(vehicle.getTrip().getDirection().equals(dir))
                            continue;
                        double dist = distance(lon, lat, vehicle.getStop().getLongitude(), vehicle.getStop().getLatitude());
                        if(min == null || dist < min){
                            min = dist;
                            buf = vehicle;
                        }
                    }
                    subway = buf;
                }else
                    try{
                        subway = mta.getSubwayTrain(id);
                    }catch(final NumberFormatException ignored){
                        exchange.send(JsonBuilder.singleton("error", "Failed to find subway '" + id + '"'), HttpURLConnection.HTTP_NOT_FOUND);
                        return;
                    }

                if(subway == null){
                    exchange.send(JsonBuilder.singleton("error", "Failed to find subway '" + id + '"'), HttpURLConnection.HTTP_NOT_FOUND);
                    return;
                }

                final Subway.Route r = subway.getRoute();
                final Subway.Trip t = subway.getTrip();

                final List<JsonBuilder> stops = new ArrayList<>();
                for(final Subway.TripStop stop : t.getTripStops()){
                    final Subway.Stop s = stop.getStop();
                    stops.add(new JsonBuilder()
                        .set("id", stop.getStopID())
                        .set("name", s.getStopName())
                        .set("latitude", s.getLatitude())
                        .set("longitude", s.getLongitude())
                    );
                }

                final JsonBuilder json = new JsonBuilder()
                    .set("vehicle", new JsonBuilder()
                        .set("id", subway.getVehicleID())
                        .set("express", subway.isExpress())
                        .set("direction", t.getDirection().name())
                    )
                    .set("route", new JsonBuilder()
                        .set("name", r.getRouteName())
                        .set("shortName", r.getRouteShortName())
                        .set("description", r.getRouteDescription())
                        .set("color", r.getRouteColor())
                        .set("textColor", r.getRouteTextColor())
                    )
                    .set("trip", stops);

                exchange.send(json.build());
            }
        }catch(final Throwable e){ // uncaught errors
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            e.printStackTrace();
            try{
                exchange.send(JsonBuilder.singleton("error", sw.toString()), HttpURLConnection.HTTP_INTERNAL_ERROR);
            }catch(final IOException ignored){}
        }finally{
            exchange.close();
        }
    }

    @SuppressWarnings("NonAsciiCharacters")
    private static double distance(final double long1, final double lat1, final double long2, final double lat2){
        final double φ1 = Math.toRadians(lat1);
        final double φ2 = Math.toRadians(lat2);
        final double λ1 = Math.toRadians(long1);
        final double λ2 = Math.toRadians(long2);
        final int rad = 6_371_000; // Earth's radius in meters

        return 2 * rad * Math.asin(
            Math.sqrt(
                Math.pow(Math.sin((φ2-φ1)/2), 2) +
                (Math.cos(φ1) *
                Math.cos(φ2) *
                Math.pow(Math.sin((λ2-λ1)/2), 2))
            )
        );
    }

}
