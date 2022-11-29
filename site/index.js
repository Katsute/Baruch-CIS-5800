"use strict";

const redir = async () => {
    const here = await getLocation();

    // todo

    window.location.href =
        "/tracking" +
        `?type=${type}` +
        `&route=${route}` +
        `&direction=${direction}` +
        `&latitude=${here[0]}` +
        `&longitude=${here[1]}` +
        `&lang=${lang}`;
}