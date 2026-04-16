package com.example.app.auth.cookie;

import com.example.app.auth.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class RefreshTokenCookieService {

	private final RefreshTokenCookieProperties properties;
	private final JwtProperties jwtProperties;

	public RefreshTokenCookieService(RefreshTokenCookieProperties properties, JwtProperties jwtProperties) {
		this.properties = properties;
		this.jwtProperties = jwtProperties;
	}

	public Optional<String> extractRefreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length == 0) {
			return Optional.empty();
		}

		return Arrays.stream(cookies)
			.filter(cookie -> properties.getName().equals(cookie.getName()))
			.map(Cookie::getValue)
			.filter(value -> value != null && !value.isBlank())
			.findFirst();
	}

	// Метод для добавления cookie с refresh токеном в ответ, который будет отправляться клиенту при успешной аутентификации или обновлении токена
	public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		
		// Создаем cookie с помощью ResponseCookie, устанавливая необходимые атрибуты безопасности и срок действия, который соответствует сроку действия refresh токена
		ResponseCookie cookie = ResponseCookie.from(properties.getName(), refreshToken)
			.httpOnly(true)
			.secure(properties.isSecure())
			.path(properties.getPath())
			.sameSite(properties.getSameSite())
			.maxAge(jwtProperties.getRefreshExpiration())
			.build();

		// Добавляем cookie в заголовок ответа, чтобы клиент мог сохранить его и использовать для последующих запросов на обновление токена
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void clearRefreshTokenCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(properties.getName(), "")
			.httpOnly(true)
			.secure(properties.isSecure())
			.path(properties.getPath())
			.sameSite(properties.getSameSite())
			.maxAge(0)
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
