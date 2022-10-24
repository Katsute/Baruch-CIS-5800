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
import dev.katsute.onemta.attribute.Location;
import dev.katsute.onemta.bus.Bus;
import dev.katsute.onemta.subway.Subway;
import dev.katsute.onemta.types.TransitVehicle;
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

            final String id = query.get("id");

            final Double lat, lon;
            if(id == null){
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
            }else
                lat = lon = null;

            if(type.equalsIgnoreCase("bus")){
                final Bus.Vehicle bus;
                if(id == null){
                    final Bus.Route r;
                    try{
                        r = mta.getBusRoute(route);
                    }catch(final NullPointerException ignored){
                        exchange.send(String.format("{\n    \"error\": \"Failed to find route '%s'\"\n}", route), HttpURLConnection.HTTP_NOT_FOUND);
                        return;
                    }

                    Bus.Vehicle buf = null;
                    Double min = null;
                    for(final Bus.Vehicle vehicle : r.getVehicles()){
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
                        exchange.send(String.format("{\n    \"error\": \"Failed to find bus '%s'\"\n}", route), HttpURLConnection.HTTP_NOT_FOUND);
                        return;
                    }

                if(bus == null){
                    exchange.send("{\n    \"error\": \"Failed to find bus\"\n}", HttpURLConnection.HTTP_NOT_FOUND);
                    return;
                }

                final Bus.Route r = bus.getRoute();
                final Bus.Trip t = bus.getTrip();

                final StringBuilder trip = new StringBuilder();
                trip.append("[");
                if(t != null){
                    Bus.TripStop[] tripStops = t.getTripStops();
                    for(int i = 0; i < tripStops.length; i++){
                        final Bus.TripStop ts = tripStops[i];
                        trip.append('"').append(ts.getStopName()).append('"');
                        if(i < tripStops.length - 1)
                            trip.append(", ");
                    }
                }
                trip.append("]");

                exchange.send(
                    "{" +
                        "\"vehicle\": {" +
                            "\"id\": " + bus.getVehicleID() + ", " +
                            "\"express\": " + bus.isExpress() + ", " +
                            "\"limited\": " + bus.isLimited() + ", " +
                            "\"select\": " + bus.isSelectBusService() + ", " +
                            "\"shuttle\": " + bus.isShuttle() +
                        "}," +
                        "\"route\": {" +
                            "\"name\": \"" + r.getRouteName() + "\", " +
                            "\"shortName\": \"" + r.getRouteShortName() + "\", " +
                            "\"description\": \"" + r.getRouteDescription() + "\", " +
                            "\"color\": \"" + r.getRouteColor() + "\", " +
                            "\"textColor\": \"" + r.getRouteTextColor() +
                        "}," +
                        "\"trip\": " + trip +
                    "}"
                );
            }else if(type.equalsIgnoreCase("subway")){
                final Subway.Vehicle subway;
                if(id == null){
                    final Subway.Route r;
                    try{
                        r = mta.getSubwayRoute(route);
                    }catch(final NullPointerException ignored){
                        exchange.send(String.format("{\n    \"error\": \"Failed to find route '%s'\"\n}", route), HttpURLConnection.HTTP_NOT_FOUND);
                        return;
                    }

                    Subway.Vehicle buf = null;
                    Double min = null;
                    for(final Subway.Vehicle vehicle : r.getVehicles()){
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
                        exchange.send(String.format("{\n    \"error\": \"Failed to find bus '%s'\"\n}", route), HttpURLConnection.HTTP_NOT_FOUND);
                        return;
                    }

                if(subway == null){
                    exchange.send("{\n    \"error\": \"Failed to find subway\"\n}", HttpURLConnection.HTTP_NOT_FOUND);
                    return;
                }

                final Subway.Route r = subway.getRoute();
                final Subway.Trip t = subway.getTrip();

                final StringBuilder trip = new StringBuilder();
                trip.append("[");
                if(t != null){
                    Subway.TripStop[] tripStops = t.getTripStops();
                    for(int i = 0; i < tripStops.length; i++){
                        final Subway.TripStop ts = tripStops[i];
                        trip.append('"').append(ts.getStop().getStopName()).append('"');
                        if(i < tripStops.length - 1)
                            trip.append(", ");
                    }
                }
                trip.append("]");

                exchange.send(
                    "{" +
                        "\"vehicle\": {" +
                            "\"id\": " + subway.getVehicleID() + ", " +
                            "\"express\": " + subway.isExpress() +
                        "}," +
                        "\"route\": {" +
                            "\"name\": \"" + r.getRouteName() + "\", " +
                            "\"shortName\": \"" + r.getRouteShortName() + "\", " +
                            "\"description\": \"" + r.getRouteDescription() + "\", " +
                            "\"color\": \"" + r.getRouteColor() + "\", " +
                            "\"textColor\": \"" + r.getRouteTextColor() +
                        "}," +
                        "\"trip\": " + trip +
                    "}"
                );
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
