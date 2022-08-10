package com.prgrms.tenwonmoa.domain.user.security.oauth2.handler;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.prgrms.tenwonmoa.domain.user.dto.TokenResponse;
import com.prgrms.tenwonmoa.domain.user.security.jwt.service.JwtService;
import com.prgrms.tenwonmoa.domain.user.security.oauth2.OAuth2Const;
import com.prgrms.tenwonmoa.domain.user.security.oauth2.OAuth2UserPrincipal;
import com.prgrms.tenwonmoa.domain.user.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.prgrms.tenwonmoa.domain.user.security.oauth2.util.CookieUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws ServletException, IOException {

		String redirectUrl = determineTargetUrl(request, response, authentication);

		if (response.isCommitted()) {
			log.debug("응답이 이미 커밋되어서 다음으로 redirect 할 수 없음 {}", redirectUrl);
			return;
		}

		clearAuthenticationAttributes(request, response);
		getRedirectStrategy().sendRedirect(request, response, redirectUrl);
	}

	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) {

		Optional<String> redirectUri = CookieUtils.getCookie(request, OAuth2Const.REDIRECT_URI_PARAM_COOKIE_NAME)
			.map(Cookie::getValue);    // HttpCookieOAuth2AuthorizationRequestRepository 에서 지정한 redirect_uri 꺼내옴

		TokenResponse tokens = createToken(authentication);

		String redirectUrl = redirectUri.orElse(getDefaultTargetUrl());

		// 클라이언트에게 응답할 redirect_url 생성
		return UriComponentsBuilder.fromUriString(redirectUrl)
			.queryParam("access-token", tokens.getAccessToken())
			.queryParam("refresh-token", tokens.getRefreshToken())
			.build()
			.toUriString();
	}

	private TokenResponse createToken(Authentication authentication) {
		OAuth2UserPrincipal principal = (OAuth2UserPrincipal)authentication.getPrincipal();
		Long userId = principal.getId();
		String email = principal.getEmail();
		return jwtService.generateToken(userId, email);
	}

	private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

}
