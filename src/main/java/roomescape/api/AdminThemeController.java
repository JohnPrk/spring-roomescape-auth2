package roomescape.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Theme;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.dto.ThemeResponses;
import roomescape.facade.ReservationFacade;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private final ThemeService themeService;
    private final ReservationFacade reservationFacade;

    public AdminThemeController(ThemeService themeService, ReservationFacade reservationFacade) {
        this.themeService = themeService;
        this.reservationFacade = reservationFacade;
    }

    @GetMapping
    public ResponseEntity<ThemeResponses> search() {
        return ResponseEntity.ok().body(ThemeResponses.from(themeService.getThemes()));
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> add(@RequestBody @Valid ThemeRequest request) {
        Theme theme = new Theme(null, request.name(), request.description(), request.thumbnailImageUrl());
        ThemeResponse response = ThemeResponse.from(themeService.addTheme(theme));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationFacade.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
