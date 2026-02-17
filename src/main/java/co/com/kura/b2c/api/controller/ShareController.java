package co.com.kura.b2c.api.controller;

import co.com.kura.b2c.api.dto.ShareResponse;
import co.com.kura.b2c.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @GetMapping("/{shareUuid}")
    public ResponseEntity<ShareResponse> getSharedResult(@PathVariable String shareUuid) {
        ShareResponse response = shareService.getSharedResult(shareUuid);
        return ResponseEntity.ok(response);
    }
}
