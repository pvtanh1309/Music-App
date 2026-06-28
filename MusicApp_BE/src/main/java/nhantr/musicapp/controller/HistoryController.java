package nhantr.musicapp.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import nhantr.musicapp.dto.request.HistoryRequest;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.HistoryResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<HistoryResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(APIResponse.success(historyService.getHistory(page, size, q)));
    }

    @PostMapping
    public ResponseEntity<APIResponse<String>> add(@Valid @RequestBody HistoryRequest request) {
        historyService.add(request);
        return ResponseEntity.ok(APIResponse.success("History recorded"));
    }

    @DeleteMapping
    public ResponseEntity<APIResponse<String>> clearAll() {
        historyService.clearAll();
        return ResponseEntity.ok(APIResponse.success("History cleared"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable UUID id) {
        historyService.delete(id);
        return ResponseEntity.ok(APIResponse.success("History item deleted"));
    }
}
