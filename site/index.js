"use strict";

const redir = async () => {
    const here = await getLocation();

    const type = document.querySelector(`select[name="mode"]`).value;
    const route = document.querySelector(`input[name="line"]`).value;

    let direction = +document.querySelector(`select[name="direction"]`).value;

    if(type === "subway")
        direction = direction === 0 ? 3 : 1;

    const lang = document.querySelector(`select[name="lang"]`).value;

    window.location.href =
        "/live" +
        `?type=${type}` +
        `&route=${route.toUpperCase()}` +
        `&direction=${direction}` +
        `&latitude=${here[0]}` +
        `&longitude=${here[1]}` +
        `&lang=${lang}`;
}