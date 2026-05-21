package roomescape.support;

import roomescape.domain.Theme;

public final class ThemeFixture {

    private ThemeFixture() {
    }

    public static Theme horror() {
        return new Theme(null, "공포", "무서운 테마", "https://example.com/horror.jpg");
    }
}
