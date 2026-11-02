package com.example.sandbox;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class TemplateRendererTest {
    @Test void preservesLiteralValuesAndDoesNotRecursivelyRenderInsertedPlaceholders() {
        assertThat(TemplateRenderer.render("{{ a.b }} / {{name}} / {{a.b}}", Map.of("a.b", "$&\\x{{name}}", "name", "李萌")))
                .isEqualTo("$&\\x{{name}} / 李萌 / $&\\x{{name}}");
    }
    @Test void rejectsMissingVariablesAndExcessiveOutput() {
        assertThatThrownBy(() -> TemplateRenderer.render("{{名字}}", Map.of())).hasMessageContaining("名字");
        assertThatThrownBy(() -> TemplateRenderer.render("{{a}}{{a}}{{a}}", Map.of("a", "x".repeat(10000))))
                .hasMessageContaining("20000");
    }
}
