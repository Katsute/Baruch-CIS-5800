"use strict";

// ⇊ DO NOT EDIT THIS ⇊

const main = document.querySelector("main");

(async () => {
    const params = Object.fromEntries(new URLSearchParams(window.location.search).entries());

    console.info("Parameters:");
    console.info(params);

    let data;

    if(params.type == "bus"){
        data = await getBusByCoord(
            params.route ?? "M1",
            params.direction ?? 1,
            params.latitude ?? LAT,
            params.longitude ?? LON,
            params.lang
        );
    }else if(params.type == "subway"){
        data = await getSubwayByCoord(
            params.route ?? "6",
            params.direction ?? 1,
            params.latitude ?? LAT,
            params.longitude ?? LON,
            params.lang
        );
    }else return console.error(`Unknown type: ${params.type}`);

    generate(data);

    const m = params.type == "bus" ? getBusByID : getSubwayByID;

    setInterval(() => {
        main.innerHTML = "";
        m(data.vehicle.id, params.lang).then(generate);
    }, 60 * 1000);
})();

// ⇈ DO NOT EDIT THIS ⇈

const generate = (data) => {
    console.info(data); // open the console to see vehicle data

    let content = "";

    // generate the site here ↓

    content += "<b>Hello World!</b><br><br>";

    content += `<b>${data.route.shortName}</b> ${data.route.name}`;

    content += `<pre style="white-space:pre-wrap">${JSON.stringify(data, null, 2)}</pre>`;

    main.innerHTML = content; // write content onto the page
}