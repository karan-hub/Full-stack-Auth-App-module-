package com.app.auth.auth_app_backend.Security;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CookieService {
    private  final String  refreshTokenCookieName;
    private  final boolean  cookieHttpOnly;
    private  final boolean  cookieSecure;
    private  final String  cookieDomain;
    private  final String  cookieSameSite;


    public CookieService(
           @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
           @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
           @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
           @Value("${security.jwt.cookie-domain}") String cookieDomain,
           @Value("${security.jwt.cookie-same-site}") String cookieSameSite) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }


    public void  attachRefreshCookie(HttpServletResponse response , String value , int maxAge){
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName , value)
                .maxAge(maxAge)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite);

        if (cookieDomain != null || !cookieDomain.isBlank())
            responseCookieBuilder.domain(cookieDomain);

        ResponseCookie  responseCookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE , responseCookie.toString());

    }


    public  void  clearRefreshCookie(HttpServletResponse response){
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName ,"")
                .maxAge(0)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite);

        if (cookieDomain != null || !cookieDomain.isBlank())
            responseCookieBuilder.domain(cookieDomain);

        ResponseCookie  responseCookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE , responseCookie.toString());
    }

    public  void addNoStoreHeaders(HttpServletResponse  response){
        response.setHeader(HttpHeaders.CACHE_CONTROL ,"no-store");
        response.setHeader("Pragma" , "no-cache");
    }

}
