package com.djrapitops.permissionsex.backends.web.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * JWT Utility for token verification.
 *
 * @author Rsl1122
 */
public class TokenVerifier {

	private Algorithm algorithmHS;

	public TokenVerifier() throws UnsupportedEncodingException {
		String secret = new RandomString(100).nextString();
		algorithmHS = Algorithm.HMAC256(secret);
	}

	public String generateToken(String user) throws JWTCreationException {
		return JWT.create()
				.withSubject(user)
				.sign(algorithmHS);
	}

	public boolean isTokenValid(String token) {
		try {
			JWT.require(algorithmHS)
					.build()
					.verify(token);
			return true;
		} catch (JWTVerificationException e) {
			return false;
		}
	}

	public Optional<String> getSubject(String token) {
		try {
			return Optional.of(
					JWT.require(algorithmHS)
							.build()
							.verify(token)
							.getSubject()
			);
		} catch (JWTVerificationException e) {
			return Optional.empty();
		}
	}
}