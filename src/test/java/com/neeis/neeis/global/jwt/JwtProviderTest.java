package com.neeis.neeis.global.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private JwtProvider jwtProvider;

    private final String secretKey = "0123456789abcdefghijklmnopqrstuvwx"; // 32 chars
    private final long validTime = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        // set private fields via reflection
        ReflectionTestUtils.setField(jwtProvider, "key", secretKey);
        ReflectionTestUtils.setField(jwtProvider, "accessTokenValidTime", validTime);
        // initialize secretKey
        jwtProvider.init();
    }

    @Test
    @DisplayName("AccessToken 생성 후 getUsername 으로 파싱")
    void createToken_and_getUsername() {
        String username = "userA";
        String role = "ROLE_TEACHER";

        String token = jwtProvider.createAccessToken(username, role);
        assertThat(token).isNotNull();

        String parsed = jwtProvider.getUsername(token);
        assertThat(parsed).isEqualTo(username);
    }

    @Test
    @DisplayName("resolveToken: Bearer 헤더 파싱")
    void resolveToken_headerParsing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer mytoken123");

        String extracted = jwtProvider.resolveToken(request);
        assertThat(extracted).isEqualTo("mytoken123");
    }

    @Test
    @DisplayName("resolveToken: 잘못된 헤더는 null 반환")
    void resolveToken_invalidHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token abc");
        assertThat(jwtProvider.resolveToken(request)).isNull();

        MockHttpServletRequest empty = new MockHttpServletRequest();
        assertThat(jwtProvider.resolveToken(empty)).isNull();
    }

    @Test
    @DisplayName("getAuthentication: 토큰으로 Authentication 생성")
    void getAuthentication_success() {
        String username = "userB";
        String role = "ROLE_STUDENT";

        // create token and stub userDetailsService
        String token = jwtProvider.createAccessToken(username, role);
        UserDetails userDetails = User.withUsername(username)
                .password("pwd").roles("STUDENT").build();
        given(customUserDetailsService.loadUserByUsername(username))
                .willReturn(userDetails);

        Authentication auth = jwtProvider.getAuthentication(token);
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(username);
        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_STUDENT");
    }
}
