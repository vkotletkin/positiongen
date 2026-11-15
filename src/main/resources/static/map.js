const root = document.getElementById("map");
const calculationId = root.dataset.id;
const panel = document.getElementById("status-panel");
const notice = document.getElementById("notice");
const legend = document.getElementById("legend");
const downloads = document.getElementById("downloads");
const reportCount = document.getElementById("report-count");
const companions = document.getElementById("companions");
const companionIds = new Set(
    (root.dataset.companions || "").split(",").filter(Boolean).map(Number)
);

const map = L.map(root, {scrollWheelZoom: true});
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "&copy; OpenStreetMap"
}).addTo(map);
map.setView([Number(root.dataset.originLat), Number(root.dataset.originLon)], 8);

const layers = L.layerGroup().addTo(map);
const travelers = new Map();
let selectedTravelerId = null;

function color(id) {
    const hue = (Number(id) * 47) % 360;
    return `hsl(${hue} 62% 36%)`;
}

function isCompanion(id) {
    return companionIds.has(Number(id));
}

function travelerLabel(id) {
    return isCompanion(id) ? `Traveler ${id}, companion` : `Traveler ${id}`;
}

function showCompanions(ids) {
    companionIds.clear();
    if (!Array.isArray(ids)) {
        return;
    }
    ids.forEach((id) => companionIds.add(Number(id)));
    if (!companions) {
        return;
    }
    companions.hidden = ids.length === 0;
    companions.textContent = ids.length === 0 ? "" : `Companions: ${ids.join(", ")}`;
}

function showNotice(text) {
    notice.hidden = false;
    notice.textContent = text;
}

async function poll() {
    const response = await fetch(`/api/calculations/${calculationId}`);
    if (!response.ok) {
        showNotice("Could not load the calculation status");
        return;
    }
    const data = await response.json();
    panel.textContent = data.statusTitle;
    panel.dataset.status = data.status;
    if (data.reportCount !== null && data.reportCount !== undefined) {
        reportCount.hidden = false;
        reportCount.textContent = `Reports: ${data.reportCount}`;
    }
    if (Array.isArray(data.coTravelerIds)) {
        showCompanions(data.coTravelerIds);
    }
    if (data.status === "SUCCEEDED") {
        downloads.hidden = false;
        if (data.reportCount === 0) {
            showNotice("No reports: the observation window is shorter than the travel time, or the report count came out zero.");
            return;
        }
        await draw();
        return;
    }
    if (data.status === "FAILED") {
        showNotice(data.errorMessage || "Calculation failed");
        return;
    }
    window.setTimeout(poll, 2000);
}

async function draw() {
    const response = await fetch(`/api/calculations/${calculationId}/geojson`);
    if (!response.ok) {
        showNotice("Could not read the result from storage");
        return;
    }
    const payload = await response.json();
    if (payload.truncated) {
        showNotice(`The map shows ${payload.drawnPoints} of ${payload.reportCount} reports. The full files are in the CSV.`);
    }
    const bounds = [];
    layers.clearLayers();
    travelers.clear();
    selectedTravelerId = null;
    for (const feature of payload.collection.features) {
        const travelerId = feature.properties.travelerId;
        const stroke = color(travelerId);
        const traveler = travelers.get(travelerId) ?? {line: null, markers: []};
        travelers.set(travelerId, traveler);
        if (feature.geometry.type === "LineString") {
            const latLngs = feature.geometry.coordinates.map(([lon, lat]) => [lat, lon]);
            traveler.line = L.polyline(latLngs, {color: stroke, weight: 3, opacity: 0.9}).addTo(layers);
            latLngs.forEach((point) => bounds.push(point));
        } else if (feature.geometry.type === "Point") {
            const [lon, lat] = feature.geometry.coordinates;
            const props = feature.properties;
            const marker = L.circleMarker([lat, lon], {
                radius: 4,
                color: "#fffaf3",
                weight: 1,
                fillColor: stroke,
                fillOpacity: 0.95
            }).bindPopup(
                `${travelerLabel(props.travelerId)}<br>` +
                `Time ${Number(props.days).toFixed(3)} days<br>` +
                `x ${Number(props.xKm).toFixed(2)} km, y ${Number(props.yKm).toFixed(2)} km`
            ).addTo(layers);
            traveler.markers.push(marker);
            bounds.push([lat, lon]);
        }
    }
    legend.replaceChildren();
    for (const [travelerId, traveler] of travelers) {
        if (!traveler.line) {
            continue;
        }
        const item = document.createElement("button");
        item.type = "button";
        item.dataset.travelerId = String(travelerId);
        item.textContent = travelerLabel(travelerId);
        item.classList.toggle("is-companion", isCompanion(travelerId));
        item.style.borderLeftColor = color(travelerId);
        item.addEventListener("click", () => selectTraveler(travelerId));
        legend.appendChild(item);
    }
    if (bounds.length === 1) {
        map.setView(bounds[0], 12);
    } else if (bounds.length > 1) {
        map.fitBounds(bounds, {padding: [28, 28], maxZoom: 14});
    }
}

function selectTraveler(travelerId) {
    selectedTravelerId = selectedTravelerId === travelerId ? null : travelerId;
    for (const [id, traveler] of travelers) {
        const active = selectedTravelerId === null || id === selectedTravelerId;
        if (traveler.line) {
            traveler.line.setStyle({
                weight: active && selectedTravelerId !== null ? 5 : 3,
                opacity: active ? 0.95 : 0.12
            });
            if (active && selectedTravelerId !== null) {
                traveler.line.bringToFront();
            }
        }
        for (const marker of traveler.markers) {
            marker.setStyle({
                opacity: active ? 1 : 0.15,
                fillOpacity: active ? 0.95 : 0.08
            });
            if (active && selectedTravelerId !== null) {
                marker.bringToFront();
            }
        }
    }
    for (const button of legend.querySelectorAll("button")) {
        const id = Number(button.dataset.travelerId);
        const selected = id === selectedTravelerId;
        button.setAttribute("aria-pressed", String(selected));
        button.classList.toggle("is-dim", selectedTravelerId !== null && !selected);
    }
    const line = travelers.get(travelerId)?.line;
    if (selectedTravelerId !== null && line) {
        const lineBounds = line.getBounds();
        if (lineBounds.isValid()) {
            map.fitBounds(lineBounds, {padding: [24, 24], maxZoom: 14});
        }
    }
}

await poll();
