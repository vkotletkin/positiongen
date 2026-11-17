package ru.kotletkin.positiongen.calculation.places;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads a place file of latitude and longitude. Longitude is x, latitude is y.
 * A header may name the columns; two numeric columns and no header are read
 * in that same order.
 */
public final class PlaceCsvReader {

    private PlaceCsvReader() {
    }

    public static List<PlacePoint> read(final InputStream input) throws IOException {
        String text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        if (text.isBlank()) {
            throw new IllegalArgumentException("Places file is empty");
        }
        char delimiter = delimiter(text);
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get();
        try (CSVParser parser = CSVParser.parse(text, format)) {
            List<CSVRecord> rows = parser.getRecords().stream()
                    .filter(row -> !isBlank(row))
                    .toList();
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("Places file is empty");
            }
            if (isHeader(rows.getFirst())) {
                return readWithHeader(rows);
            }
            return readGeographic(rows);
        }
    }

    private static List<PlacePoint> readWithHeader(final List<CSVRecord> rows) {
        CSVRecord header = rows.getFirst();
        Integer latitude = null;
        Integer longitude = null;
        for (int index = 0; index < header.size(); index++) {
            switch (normalize(header.get(index))) {
                case "lat", "latitude" -> latitude = index;
                case "lon", "lng", "long", "longitude" -> longitude = index;
                default -> {
                    // Extra columns are ignored when both coordinates are present.
                }
            }
        }
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Places file needs columns latitude,longitude");
        }
        List<PlacePoint> places = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            CSVRecord row = rows.get(index);
            long line = row.getRecordNumber();
            places.add(new PlacePoint(
                    number(cell(row, latitude, line, "latitude"), line, "latitude"),
                    number(cell(row, longitude, line, "longitude"), line, "longitude")));
            if (places.size() > PlaceCoordinates.MAX_PLACES) {
                throw new IllegalArgumentException("At most 20,000 places");
            }
        }
        return List.copyOf(places);
    }

    private static List<PlacePoint> readGeographic(final List<CSVRecord> rows) {
        List<PlacePoint> places = new ArrayList<>();
        for (CSVRecord row : rows) {
            long line = row.getRecordNumber();
            if (row.size() < 2) {
                throw new IllegalArgumentException("Line " + line + ": expected latitude and longitude");
            }
            places.add(new PlacePoint(
                    number(row.get(0), line, "latitude"),
                    number(row.get(1), line, "longitude")));
            if (places.size() > PlaceCoordinates.MAX_PLACES) {
                throw new IllegalArgumentException("At most 20,000 places");
            }
        }
        return List.copyOf(places);
    }

    private static String cell(final CSVRecord row, final int index, final long line, final String name) {
        if (index >= row.size()) {
            throw new IllegalArgumentException("Line " + line + ": missing " + name);
        }
        return row.get(index);
    }

    private static boolean isHeader(final CSVRecord row) {
        if (row.size() < 2) {
            return true;
        }
        return !isNumber(row.get(0)) || !isNumber(row.get(1));
    }

    private static boolean isBlank(final CSVRecord row) {
        for (String cell : row) {
            if (!cell.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNumber(final String raw) {
        try {
            double value = Double.parseDouble(normalizeNumber(raw));
            return Double.isFinite(value);
        } catch (NumberFormatException _) {
            return false;
        }
    }

    private static double number(final String raw, final long line, final String name) {
        try {
            double value = Double.parseDouble(normalizeNumber(raw));
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Line " + line + ": " + name + " must be a finite number");
            }
            return value;
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("Line " + line + ": " + name + " must be a number");
        }
    }

    private static String normalizeNumber(final String raw) {
        String text = raw.trim();
        if (text.contains(",") && !text.contains(".")) {
            text = text.replace(',', '.');
        }
        return text;
    }

    private static String normalize(final String header) {
        return header.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static char delimiter(final String text) {
        String first = text.lines().filter(line -> !line.isBlank()).findFirst().orElse("");
        if (first.indexOf(';') >= 0 && first.indexOf(',') < 0) {
            return ';';
        }
        if (first.indexOf('\t') >= 0 && first.indexOf(',') < 0) {
            return '\t';
        }
        return ',';
    }
}
