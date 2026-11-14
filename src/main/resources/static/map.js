const root = document.getElementById("map");
const calculationId = root.dataset.id;
const panel = document.getElementById("status-panel");
const notice = document.getElementById("notice");
const legend = document.getElementById("legend");
const downloads = document.getElementById("downloads");
const reportCount = document.getElementById("report-count");

const map = L.map(root, {scrollWheelZoom: true});
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "&copy; OpenStreetMap"
}).addTo(map);
map.setView([Number(root.dataset.originLat), Number(root.dataset.originLon)], 8);

const layers = L.layerGroup().addTo(map);
const travelerLines = new Map();

function color(id) {
    const hue = (Number(id) * 47) % 360;
    return `hsl(${hue} 62% 36%)`;
}

function showNotice(text) {
    notice.hidden = false;
    notice.textContent = text;
}

async function poll() {
    const response = await fetch(`/api/calculations/${calculationId}`);
    if (!response.ok) {
        showNotice("Не удалось получить статус расчёта");
        return;
    }
    const data = await response.json();
    panel.textContent = data.statusTitle;
    panel.dataset.status = data.status;
    if (data.reportCount !== null && data.reportCount !== undefined) {
        reportCount.hidden = false;
        reportCount.textContent = `Отчётов: ${data.reportCount}`;
    }
    if (data.status === "SUCCEEDED") {
        downloads.hidden = false;
        if (data.reportCount === 0) {
            showNotice("Отчётов нет: окно наблюдения короче времени в пути или число отчётов выпало нулевым.");
            return;
        }
        await draw();
        return;
    }
    if (data.status === "FAILED") {
        showNotice(data.errorMessage || "Расчёт завершился с ошибкой");
        return;
    }
    window.setTimeout(poll, 2000);
}

async function draw() {
    const response = await fetch(`/api/calculations/${calculationId}/geojson`);
    if (!response.ok) {
        showNotice("Не удалось прочитать результат из хранилища");
        return;
    }
    const payload = await response.json();
    if (payload.truncated) {
        showNotice(`На карте ${payload.drawnPoints} из ${payload.reportCount} отчётов. Полные файлы лежат в CSV.`);
    }
    const bounds = [];
    layers.clearLayers();
    travelerLines.clear();
    for (const feature of payload.collection.features) {
        const travelerId = feature.properties.travelerId;
        const stroke = color(travelerId);
        if (feature.geometry.type === "LineString") {
            const latLngs = feature.geometry.coordinates.map(([lon, lat]) => [lat, lon]);
            const line = L.polyline(latLngs, {color: stroke, weight: 3, opacity: 0.9}).addTo(layers);
            travelerLines.set(travelerId, line);
            latLngs.forEach((point) => bounds.push(point));
        } else if (feature.geometry.type === "Point") {
            const [lon, lat] = feature.geometry.coordinates;
            const props = feature.properties;
            L.circleMarker([lat, lon], {
                radius: 4,
                color: "#fffaf3",
                weight: 1,
                fillColor: stroke,
                fillOpacity: 0.95
            }).bindPopup(
                `Путешественник ${props.travelerId}<br>` +
                `Время ${Number(props.days).toFixed(3)} сут<br>` +
                `x ${Number(props.xKm).toFixed(2)} км, y ${Number(props.yKm).toFixed(2)} км`
            ).addTo(layers);
            bounds.push([lat, lon]);
        }
    }
    legend.replaceChildren();
    for (const [travelerId, line] of travelerLines) {
        const item = document.createElement("button");
        item.type = "button";
        item.textContent = `Путешественник ${travelerId}`;
        item.style.borderLeftColor = color(travelerId);
        item.addEventListener("click", () => {
            const lineBounds = line.getBounds();
            if (lineBounds.isValid()) {
                map.fitBounds(lineBounds, {padding: [24, 24], maxZoom: 14});
            }
        });
        legend.appendChild(item);
    }
    if (bounds.length === 1) {
        map.setView(bounds[0], 12);
    } else if (bounds.length > 1) {
        map.fitBounds(bounds, {padding: [28, 28], maxZoom: 14});
    }
}

poll();
