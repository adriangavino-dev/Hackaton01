package com.tuckersoft.tropelcare.signal;

import com.tuckersoft.tropelcare.careresponse.CareResponseRepository;
import com.tuckersoft.tropelcare.careresponse.dto.CareResponseDto;
import com.tuckersoft.tropelcare.common.ApiException;
import com.tuckersoft.tropelcare.common.PageResponse;
import com.tuckersoft.tropelcare.notification.NotificationLogRepository;
import com.tuckersoft.tropelcare.notification.dto.NotificationLogDto;
import com.tuckersoft.tropelcare.signal.dto.SignalCreateRequest;
import com.tuckersoft.tropelcare.signal.dto.SignalResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/signals")
public class SignalController {

    private final SignalService service;
    private final CareResponseRepository careResponses;
    private final NotificationLogRepository notifications;

    public SignalController(SignalService service,
                            CareResponseRepository careResponses,
                            NotificationLogRepository notifications) {
        this.service = service;
        this.careResponses = careResponses;
        this.notifications = notifications;
    }

    @PostMapping
    public ResponseEntity<SignalResponse> create(@Valid @RequestBody SignalCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public PageResponse<SignalResponse> list(
            @RequestParam(required = false) String signalType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long tropelId,
            @RequestParam(required = false) Long guardianId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(signalType, severity, status, tropelId, guardianId, from, to, page, size);
    }

    @GetMapping("/{id}")
    public SignalResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/care-response")
    public CareResponseDto careResponse(@PathVariable Long id) {
        return careResponses.findBySignalId(id)
                .map(CareResponseDto::from)
                .orElseThrow(() -> ApiException.notFound(
                        "No existe una respuesta de cuidado para la senal #" + id));
    }

    @GetMapping("/{id}/notifications")
    public List<NotificationLogDto> notifications(@PathVariable Long id) {
        return notifications.findBySignalIdOrderByCreatedAtAsc(id)
                .stream().map(NotificationLogDto::from).toList();
    }
}
