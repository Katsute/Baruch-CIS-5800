"use strict";

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
            params.longitude ?? LON
        );
    }else if(params.type == "subway"){
        data = await getSubwayByCoord(
            params.route ?? "6",
            params.direction ?? 1,
            params.latitude ?? LAT,
            params.longitude ?? LON
        );
    }else return console.error(`Unknown type: ${params.type}`);

    generate(data);
    setInterval(() => {
        main.innerHTML = "";

        (params.type == "bus" ? getBusByID : getSubwayByID)(data.vehicle.id).then(generate);
    }, 60 * 1000);
})();

const generate = (data) => {
    console.info(data);

    // generate site here

    main.innerHTML = JSON.stringify(data); // modify me!
}