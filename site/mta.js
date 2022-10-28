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

"use strict";

// sample latitude, longitude coords; use for testing

const LAT = 40.74058128194693;
const LON = -73.98325077286562;

const __testMTA = async () => {
    const bus = await getBusByCoord("M1", 0, LAT, LON);
    const subway = await getSubwayByCoord(6, 0, LAT, LON);
    console.log(bus);
    console.log(await getBusByID(bus.vehicle.id));
    console.log(subway);
    console.log(await getSubwayByID(subway.vehicle.id));
}

const getBusByCoord = async (route, dir, lat, lon) => {
    return await get("/request", {
        "type": "bus",
        "route": route,
        "direction": dir,
        "latitude": lat,
        "longitude": lon
    });
}

const getBusByID = async (id) => {
    return await get("/request", {
        "type": "bus",
        "id": id
    });
}

const getSubwayByCoord = async (route, dir, lat, lon) => {
    return await get("/request", {
        "type": "subway",
        "route": route,
        "direction": dir,
        "latitude": lat,
        "longitude": lon
    });
}

const getSubwayByID = async (id) => {
    return await get("/request", {
        "type": "subway",
        "id": id
    });
}