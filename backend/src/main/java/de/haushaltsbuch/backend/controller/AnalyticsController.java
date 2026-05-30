package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.AnalyticsResponse;
import de.haushaltsbuch.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


/**
 * REST controller that exposes aggregated financial analytics.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Returns aggregated analytics for the given date range.
     *
     * <p>Both parameters are optional:
     * <ul>
     *   <li>{@code from} defaults to the first day of the current year.</li>
     *   <li>{@code to} defaults to today.</li>
     * </ul>
     *
     * @param from start of the period (inclusive), ISO date format {@code yyyy-MM-dd}
     * @param to   end of the period (inclusive), ISO date format {@code yyyy-MM-dd}
     * @return {@code 200 OK} with the analytics payload
     */
    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate effectiveFrom = from != null ? from : LocalDate.now().withDayOfYear(1);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();

        return ResponseEntity.ok(analyticsService.getAnalytics(effectiveFrom, effectiveTo));
    }
}
