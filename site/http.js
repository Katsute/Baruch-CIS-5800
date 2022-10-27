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

const request = async (method, path, params) => {
    return new Promise((res, rej) => {
        const xhr = new XMLHttpRequest();
        const query = !params ? "" : '?' + Object.keys(params).map(key => `${key}=${encodeURIComponent(params[key])}`).join('&');

        xhr.open(method.toUpperCase(), path + query);
        xhr.onload = () => (xhr.status == 200 ? res : rej)(JSON.parse(xhr.responseText));
        xhr.onerror = () => rej({status: xhr.status, text: xhr.statusText});
        xhr.send();
    });
}

const get = async (path, params) => await request("GET", path, params);