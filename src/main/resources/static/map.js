const root = document.getElementById("map");
const calculationId = root.dataset.id;
const panel = document.getElementById("status-panel");
const notice = document.getElementById("notice");
const legend = document.getElementById("legend");
const companionTracks = document.getElementById("companion-tracks");
const downloads = document.getElementById("downloads");
const reportCount = document.getElementById("report-count");
const companions = document.getElementById("companions");
const companionIds = new Set(
    (root.dataset.companions || "").split(",").filter(Boolean).map(Number)
);

const map = L.map(root, {scrollWheelZoom: true});
const tileUrl = (root.dataset.tileUrl || "").trim();
if (tileUrl) {
    L.tileLayer(tileUrl, {
        maxZoom: 19,
        attribution: root.dataset.tileAttribution || "",
        errorTileUrl: "/tiles/blank.png"
    }).addTo(map);
}
map.setView([Number(root.dataset.originLat), Number(root.dataset.originLon)], 8);

const layers = L.layerGroup().addTo(map);
const travelers = new Map();
const reportsByPlace = new Map();
let selectedIds = null;

function placeKey(xKm, yKm) {
    return `${Number(xKm).toFixed(5)}|${Number(yKm).toFixed(5)}`;
}

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
    reportsByPlace.clear();
    selectedIds = null;
    clearPlaceTimes();
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
            const key = placeKey(props.xKm, props.yKm);
            const report = {
                travelerId,
                days: Number(props.days),
                xKm: Number(props.xKm),
                yKm: Number(props.yKm)
            };
            const bucket = reportsByPlace.get(key) ?? [];
            bucket.push(report);
            reportsByPlace.set(key, bucket);
            const marker = L.circleMarker([lat, lon], {
                radius: 4,
                color: "#fffaf3",
                weight: 1,
                fillColor: stroke,
                fillOpacity: 0.95
            }).addTo(layers);
            marker.placeKey = key;
            marker.on("click", () => {
                if (selectedIds !== null && !selectedIds.has(travelerId)) {
                    return;
                }
                showPlaceTimes(marker);
            });
            const element = marker.getElement();
            if (element) {
                element.dataset.place = key;
            }
            traveler.markers.push(marker);
            bounds.push([lat, lon]);
        }
    }
    refreshCompanionButton();
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

function companionTrackIds() {
    return [...companionIds].filter((id) => travelers.get(id)?.line);
}

function refreshCompanionButton() {
    const available = companionTrackIds().length > 0;
    companionTracks.hidden = !available;
    if (!available) {
        companionTracks.setAttribute("aria-pressed", "false");
        if (selectedIds !== null && [...selectedIds].every((id) => isCompanion(id))) {
            selectedIds = null;
        }
    }
}

function sameSelection(ids) {
    if (selectedIds === null || selectedIds.size !== ids.length) {
        return false;
    }
    return ids.every((id) => selectedIds.has(id));
}

function sharedPlaceKeysFor(ids) {
    const counts = new Map();
    for (const [key, reports] of reportsByPlace) {
        const who = new Set();
        for (const report of reports) {
            if (ids === null || ids.has(report.travelerId)) {
                who.add(report.travelerId);
            }
        }
        if (who.size >= 2) {
            counts.set(key, who.size);
        }
    }
    return new Set(counts.keys());
}

function clearPlaceTimes() {
    map.closePopup();
}

function showPlaceTimes(marker) {
    const reports = reportsByPlace.get(marker.placeKey) ?? [];
    const ids = selectedIds === null
        ? [...new Set(reports.map((report) => report.travelerId))].sort((left, right) => left - right)
        : [...selectedIds].sort((left, right) => left - right);
    const content = document.createElement("div");
    content.className = "place-times";
    const heading = document.createElement("h2");
    heading.textContent = "Times at this place";
    content.appendChild(heading);
    const where = document.createElement("p");
    where.className = "hint";
    const sample = reports[0];
    where.textContent = sample
        ? `x ${sample.xKm.toFixed(2)} km, y ${sample.yKm.toFixed(2)} km`
        : "";
    content.appendChild(where);
    for (const id of ids) {
        const times = reports
            .filter((report) => report.travelerId === id)
            .map((report) => report.days)
            .sort((left, right) => left - right);
        const article = document.createElement("article");
        const title = document.createElement("h3");
        title.textContent = travelerLabel(id);
        title.style.borderLeftColor = color(id);
        article.appendChild(title);
        if (times.length === 0) {
            const empty = document.createElement("p");
            empty.className = "hint";
            empty.textContent = "No report at this place";
            article.appendChild(empty);
        } else {
            const list = document.createElement("ul");
            for (const days of times) {
                const item = document.createElement("li");
                item.textContent = `${days.toFixed(3)} days`;
                list.appendChild(item);
            }
            article.appendChild(list);
        }
        content.appendChild(article);
    }
    L.popup({
        className: "place-popup",
        maxWidth: 320,
        autoPan: true,
        closeButton: true
    })
        .setLatLng(marker.getLatLng())
        .setContent(content)
        .openOn(map);
}

function applySelection() {
    const narrowed = selectedIds !== null;
    const sharedPlaceKeys = sharedPlaceKeysFor(selectedIds);
    for (const [id, traveler] of travelers) {
        const active = !narrowed || selectedIds.has(id);
        if (traveler.line) {
            traveler.line.setStyle({
                weight: active && narrowed ? 5 : 3,
                opacity: active ? 0.95 : 0.12
            });
            if (active && narrowed) {
                traveler.line.bringToFront();
            }
        }
        for (const marker of traveler.markers) {
            const shared = active && sharedPlaceKeys.has(marker.placeKey);
            marker.setStyle({
                radius: shared ? 8 : 4,
                opacity: active ? 1 : 0.15,
                fillOpacity: active ? 0.95 : 0.08
            });
            if (active && narrowed) {
                marker.bringToFront();
            }
        }
    }
    for (const button of legend.querySelectorAll("button")) {
        const id = Number(button.dataset.travelerId);
        const selected = narrowed && selectedIds.size === 1 && selectedIds.has(id);
        button.setAttribute("aria-pressed", String(selected));
        button.classList.toggle("is-dim", narrowed && !selectedIds.has(id));
    }
    const companionsOnly = narrowed && sameSelection(companionTrackIds());
    companionTracks.setAttribute("aria-pressed", String(companionsOnly));
}

function fitSelection() {
    const bounds = [];
    for (const [id, traveler] of travelers) {
        if (selectedIds !== null && !selectedIds.has(id)) {
            continue;
        }
        if (traveler.line) {
            const lineBounds = traveler.line.getBounds();
            if (lineBounds.isValid()) {
                bounds.push(lineBounds);
            }
        }
    }
    if (bounds.length === 0) {
        return;
    }
    const combined = bounds[0];
    bounds.slice(1).forEach((item) => combined.extend(item));
    map.fitBounds(combined, {padding: [24, 24], maxZoom: 14});
}

function selectTraveler(travelerId) {
    selectedIds = sameSelection([travelerId]) ? null : new Set([travelerId]);
    clearPlaceTimes();
    applySelection();
    if (selectedIds !== null) {
        fitSelection();
    }
}

function toggleCompanionTracks() {
    const ids = companionTrackIds();
    selectedIds = sameSelection(ids) ? null : new Set(ids);
    clearPlaceTimes();
    applySelection();
    if (selectedIds !== null) {
        fitSelection();
    }
}

companionTracks.addEventListener("click", toggleCompanionTracks);

await poll();
