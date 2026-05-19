package roomescape.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.ThemeResponses;
import roomescape.service.ThemeService;

import java.time.LocalDate;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<ThemeResponses> search() {
        return ResponseEntity.ok().body(ThemeResponses.from(themeService.getThemes()));
    }

    @GetMapping("/popular")
    public ResponseEntity<ThemeResponses> searchPopular(
            @RequestParam(required = false) LocalDate now,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        LocalDate baseDate = (now != null) ? now : LocalDate.now();
        return ResponseEntity.ok().body(ThemeResponses.from(themeService.getPopularThemes(baseDate, days, limit)));
    }
}
