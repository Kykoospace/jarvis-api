package jarvisapi.security;

import ch.qos.logback.core.net.SyslogOutputStream;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.processing.SupportedSourceVersion;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * JwtAuthenticationFilter
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${spring.jwt.header}")
    private String TOKEN_HEADER;
    @Value("${spring.jwt.prefix}")
    private String TOKEN_PREFIX;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws IOException, ServletException {
        final String headerRequest = req.getHeader(TOKEN_HEADER);
        String userEmail = null;
        String authToken = null;
        if (headerRequest != null && headerRequest.startsWith(TOKEN_PREFIX)) {
            authToken = headerRequest.replace(TOKEN_PREFIX + " ", "");
            try {
                userEmail = jwtTokenUtil.getUserEmailFromToken(authToken);
                if (userEmail == null) {
                    logger.error("Token expired");
                }
            } catch (final IllegalArgumentException e) {
                logger.error("an error occurred during getting username from token", e);
            } catch (final ExpiredJwtException e) {
                logger.warn("the token is expired and not valid anymore", e);
            } catch (final SignatureException e) {
                logger.error("Authentication Failed. Username or Password not valid.", e);
            }
        }
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            final UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            System.out.println(userDetails.getAuthorities());

            if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                final UsernamePasswordAuthenticationToken authentication = jwtTokenUtil.getAuthentication(authToken, userDetails);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                logger.info("authenticated user " + userEmail + ", setting security context");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(req, res);
    }

}
