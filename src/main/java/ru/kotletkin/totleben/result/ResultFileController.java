package ru.kotletkin.totleben.result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/calculations/{id}/files")
@RequiredArgsConstructor
@Tag(name = "Results", description = "Файлы результатов расчёта")
public class ResultFileController {

    private final ResultArchive results;

    @GetMapping("/xy")
    @Operation(summary = "Download coordinate CSV")
    public ResponseEntity<StreamingResponseBody> xy(@PathVariable final UUID id) {
        StoredArtifacts files = results.files(id);
        return download(new StoredObjectDTO(files.xyObjectKey(), "XY.csv", files.xyByteSize()));
    }

    @GetMapping("/places")
    @Operation(summary = "Download places CSV")
    public ResponseEntity<StreamingResponseBody> places(@PathVariable final UUID id) {
        StoredArtifacts files = results.files(id);
        return download(new StoredObjectDTO(files.placesObjectKey(), "PLACES.csv", files.placesByteSize()));
    }

    private ResponseEntity<StreamingResponseBody> download(final StoredObjectDTO file) {
        StreamingResponseBody body = output -> {
            try (InputStream input = results.open(file.key())) {
                input.transferTo(output);
            }
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(file.size())
                .body(body);
    }
}
