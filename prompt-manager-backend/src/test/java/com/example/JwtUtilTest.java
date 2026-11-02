package com.example;
import com.example.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class JwtUtilTest {
    @Test void externalKeyMustBeStrongEnoughAndOtherKeysCannotForgeIdentity() {
        assertThatThrownBy(() -> new JwtUtil("")).isInstanceOf(IllegalArgumentException.class);
        var signing = new JwtUtil("test-only-key-12345678901234567890");
        var other = new JwtUtil("different-key-12345678901234567890");
        String token = signing.createToken(7L, "test");
        assertThat(signing.parseToken(token).getSubject()).isEqualTo("7");
        assertThatThrownBy(() -> other.parseToken(token)).isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
