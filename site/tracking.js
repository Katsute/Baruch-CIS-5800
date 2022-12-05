"use strict";

const main = document.querySelector("div#live-tracking");

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
        m(data.vehicle.id, params.lang).then(generate);
    }, 60 * 1000);
})();

const generate = (data) => {
    console.info(data);

    let content = "";

    content += `<h1 style="color: white">${data.route.shortName} ${data.route.name}</h1>`;

    content += "<div>";

    if(data.alerts.length > 0){
        content += "<h3>⚠️ Alerts</h3>";
        content += "<ul>";
        for(const alert of data.alerts){
            content += "<li>";
            content += "<span>";
            if(alert.construction) content += "🚧";
            if(alert.ems) content += "🚑";
            if(alert.express) content += "💨";
            if(alert.fire) content += "🔥";
            if(alert.local) content += "🐌";
            if(alert.police) content += "👮‍♂️";
            if(alert.shuttle) content += "🚌";
            if(alert.skip) content += "⚠️";
            if(alert.slow) content += "🐌";
            if(alert.weather) content += "🌧";
            content += "</span>";
            content += `<p>${alert.description}</p>`;
            if(alert.description != alert.description_translated)
                content += `<p>${alert.description_translated}</p>`;
            content += "</li>";
        }
        content += "</ul>";
    }

    content += "</div>";

    content += "<h3>🚏 Stops</h3>";

    content += "<ol>";

    for(const stop of data.trip){
        content += "<li>";

        content += `<h3>${stop.name}</h3>`;

        if(stop.alerts.length > 0){
            content += "<ul>";
            for(const alert of stop.alerts){
                content += "<li>";
                content += "<span>";
                if(alert.construction) content += "🚧";
                if(alert.ems) content += "🚑";
                if(alert.express) content += "💨";
                if(alert.fire) content += "🔥";
                if(alert.local) content += "🐌";
                if(alert.police) content += "👮‍♂️";
                if(alert.shuttle) content += "🚌";
                if(alert.skip) content += "⚠️";
                if(alert.slow) content += "🐌";
                if(alert.weather) content += "🌧";
                content += "</span>";
                content += `<p>${alert.description}</p>`;
                if(alert.description != alert.description_translated)
                    content += `<p>${alert.description_translated}</p>`;
                content += "</li>";
            }
            content += "</ul>";
        }

        content += "</li>";
    }

    content += "</ol>";

    main.innerHTML = content; // write content onto the page
}