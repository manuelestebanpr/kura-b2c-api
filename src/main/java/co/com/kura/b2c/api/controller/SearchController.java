package co.com.kura.b2c.api.controller;

import co.com.kura.b2c.api.dto.PosResponse;
import co.com.kura.b2c.api.dto.SearchResponse;
import co.com.kura.b2c.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/services")
    public ResponseEntity<List<SearchResponse>> searchServices(
            @RequestParam String q,
            @RequestParam(required = false) UUID posId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.searchServices(q, posId, limit));
    }

    @GetMapping("/services/{code}")
    public ResponseEntity<SearchResponse> getServiceByCode(@PathVariable String code) {
        return ResponseEntity.ok(searchService.getServiceByCode(code));
    }

    @GetMapping("/pos")
    public ResponseEntity<List<PosResponse>> getPointsOfService() {
        return ResponseEntity.ok(searchService.getActivePointsOfService());
    }
}
