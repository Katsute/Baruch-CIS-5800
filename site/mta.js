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

/**
 * @returns [latitude, longitude]
 */
const getLocation = async () => new Promise((res, rej) => {
    if(!navigator.geolocation){
        console.error("geolocation not supported");
        rej("geolocation not supported");
    }else{
        navigator.geolocation.getCurrentPosition(
            (success) => {
                res([success.coords.latitude, success.coords.longitude]);
            },
            (err) => {
                console.error(err);
                rej(err);
            }
        );
    }
});

/**
 * @param {*} route route code
 * @param {*} dir direction 0 | 1
 * @param {*} lat latitude
 * @param {*} lon longitude
 * @returns bus
 */
const getBusByCoord = async (route, dir, lat, lon, lang) => {
    return await get("/request", {
        "type": "bus",
        "route": route,
        "direction": dir,
        "latitude": lat,
        "longitude": lon,
        "lang": lang ?? "en"
    });
}

/**
 * @param {*} id vehicle id
 * @returns bus
 */
const getBusByID = async (id, lang) => {
    return await get("/request", {
        "type": "bus",
        "id": id,
        "lang": lang ?? "en"
    });
}

/**
 * @param {*} route route code
 * @param {*} dir direction 0 | 1
 * @param {*} lat latitude
 * @param {*} lon longitude
 * @returns bus
 */
const getSubwayByCoord = async (route, dir, lat, lon, lang) => {
    return await get("/request", {
        "type": "subway",
        "route": route,
        "direction": dir,
        "latitude": lat,
        "longitude": lon,
        "lang": lang ?? "en"
    });
}

/**
 * @param {*} id vehicle id
 * @returns bus
 */
const getSubwayByID = async (id, lang) => {
    return await get("/request", {
        "type": "subway",
        "id": id,
        "lang": lang ?? "en"
    });
}

/**
 * @param {*} path path
 * @param {*} params object params
 * @returns object response
 */
const get = async (path, params) => {
    return new Promise((res, rej) => {
        const xhr = new XMLHttpRequest();
        const query = !params ? "" : '?' + Object.keys(params).map(key => `${key}=${encodeURIComponent(params[key])}`).join('&');

        xhr.open("GET", path + query);
        xhr.onload = () => {
            try{
                (xhr.status == 200 ? res : rej)(JSON.parse(xhr.responseText));
            }catch(error){
                console.error(`Failed to parse json: \n${xhr.responseText}`);
                rej(error);
            }
        };
        xhr.onerror = () => rej({status: xhr.status, text: xhr.statusText});
        xhr.send();
    });
}