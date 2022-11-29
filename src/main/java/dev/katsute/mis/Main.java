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

import dev.katsute.onemta.*;
import dev.katsute.simplehttpserver.SimpleHttpServer;
import dev.katsute.simplehttpserver.handler.file.FileHandler;
import dev.katsute.simplehttpserver.handler.file.FileOptions;

import java.io.*;
import java.net.BindException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

final class Main {

    private static final Map<String,String> resources = new HashMap<String,String>(){{
        put("google_transit_subway"         , "http://web.mta.info/developers/data/nyct/subway/google_transit.zip");
        put("google_transit_bronx"         , "http://web.mta.info/developers/data/nyct/bus/google_transit_bronx.zip");
        put("google_transit_brooklyn"       , "http://web.mta.info/developers/data/nyct/bus/google_transit_brooklyn.zip");
        put("google_transit_manhattan"      , "http://web.mta.info/developers/data/nyct/bus/google_transit_manhattan.zip");
        put("google_transit_queens"         , "http://web.mta.info/developers/data/nyct/bus/google_transit_queens.zip");
        put("google_transit_staten_island"  , "http://web.mta.info/developers/data/nyct/bus/google_transit_staten_island.zip");
        put("google_transit_lirr"           , "http://web.mta.info/developers/data/lirr/google_transit.zip");
        put("google_transit_mnr"            , "http://web.mta.info/developers/data/mnr/google_transit.zip");
        put("google_transit_bus_company"    , "http://web.mta.info/developers/data/busco/google_transit.zip");
    }};

    public static void main(String[] args) throws Throwable {
        System.out.println("Checking static data");
        {
            for(final Map.Entry<String,String> entry : resources.entrySet())
                try(final BufferedInputStream IN = new BufferedInputStream(new URL(entry.getValue()).openStream())){
                    final File file = new File(entry.getKey() + ".zip");
                    System.out.println("Checking for data resource " + file.getName());
                    if(!file.exists()){
                        System.out.println("" + file.getName() + " not found, downloading from the MTA ...");
                        try(final FileOutputStream OUT = new FileOutputStream(file)){
                            byte[] buffer = new byte[1024];
                            int bytesReads;
                            while((bytesReads = IN.read(buffer, 0, 1024)) != -1)
                                OUT.write(buffer, 0, bytesReads);
                        }
                    }
                    System.out.println("Added " + file.getName() + " as " + entry.getKey());
                }
        }
        System.out.println("Checking tokens");
        // read tokens
        String busToken, subwayToken;
        {
            final File bt = new File("bus-token.txt");
            if(!bt.exists()) throw new FileNotFoundException("File 'bus-token.txt' is missing");
            busToken = String.join("\n", Files.readAllLines(bt.toPath())).trim();
            if(busToken.isEmpty())  throw new NullPointerException("Bus token is missing from 'bus-token.txt', request a token at https://bt.mta.info/wiki/Developers/Index");
        }
        {
            final File bt = new File("subway-token.txt");
            if(!bt.exists()) throw new FileNotFoundException("File 'subway-token.txt' is missing");
            subwayToken = String.join("\n", Files.readAllLines(bt.toPath())).trim();
            if(subwayToken.isEmpty())  throw new NullPointerException("Subway token is missing from 'subway-token.txt', request a token at https://api.mta.info/#/signup");
        }
        System.out.println("Initializing MTA");
        // initialize MTA
        final MTA mta = MTA.create(
            busToken,
            subwayToken,
            DataResource.create(DataResourceType.Bus_Bronx, new File("google_transit_bronx.zip")),
            DataResource.create(DataResourceType.Bus_Brooklyn, new File("google_transit_brooklyn.zip")),
            DataResource.create(DataResourceType.Bus_Manhattan, new File("google_transit_manhattan.zip")),
            DataResource.create(DataResourceType.Bus_Queens, new File("google_transit_queens.zip")),
            DataResource.create(DataResourceType.Bus_StatenIsland, new File("google_transit_staten_island.zip")),
            DataResource.create(DataResourceType.Bus_Company, new File("google_transit_bus_company.zip")),
            DataResource.create(DataResourceType.Subway, new File("google_transit_subway.zip"))
        );
        System.out.println("Initializing server");
        // initialize server
        {
            try{
                final SimpleHttpServer server = SimpleHttpServer.create(8080);
                server.setExecutor(Executors.newCachedThreadPool());

                final FileHandler handler = new MimeFileHandler();
                final FileOptions options = new FileOptions.Builder()
                    .setLoadingOption(FileOptions.FileLoadingOption.LIVE)
                    .setWalk(true)
                    .build();
                handler.addFile(new File("site/index.html"), "/", options);
                handler.addDirectory(new File("site"), "/", options);
                server.createContext("/", handler);
                server.createContext("/request", new RequestHandler(mta));

                server.start();

                System.out.println("Server started at localhost:8080");
            }catch(final BindException e){
                System.out.println("Failed to start server, server is already running or port is occupied");
            }
        }
    }

}
