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

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.concurrent.Executors;

final class Main {

    private static SimpleHttpServer server = null;

    private static MTA mta;

    public static void main(String[] args) throws Throwable {
        System.out.println("Checking tokens");
        // read tokens
        String busToken, subwayToken;
        {
            final File bt = new File("bus-token.txt");
            if(!bt.exists()) throw new FileNotFoundException("File 'bus-token.txt' is missing");
            busToken = String.join("\n", Files.readAllLines(bt.toPath())).trim();
            if(busToken.isEmpty())  throw new NullPointerException("Bus token is missing from 'bus-token.txt'");
        }
        {
            final File bt = new File("subway-token.txt");
            if(!bt.exists()) throw new FileNotFoundException("File 'subway-token.txt' is missing");
            subwayToken = String.join("\n", Files.readAllLines(bt.toPath())).trim();
            if(subwayToken.isEmpty())  throw new NullPointerException("Subway token is missing from 'subway-token.txt'");
        }
        System.out.println("Initializing MTA");
        // initialize MTA
        mta = MTA.create(
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
            server = SimpleHttpServer.create(8080);
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
        }
    }

}
