package no.java.ems.server.restlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 */
public class AuthenticationFilter extends Filter {
    private Log log = LogFactory.getLog(getClass());

    private static final String KEY = AuthenticationFilter.class.getName() + ".key";

    private boolean enabled;

    public AuthenticationFilter(Context context, boolean enabled) {
        super(context);
        this.enabled = enabled;
    }

    protected void doHandle(Request request, Response response) {
        if (!enabled) {
            request.getAttributes().put(KEY, true);
            super.doHandle(request, response);
            return;
        }

        boolean authenticated = authenticate(request);

        // Allow GETs for unauthenticated, and everything for autenticated
        if (request.getMethod() == Method.GET || authenticated) {
            super.doHandle(request, response);
            return;
        }

        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }

    public boolean authenticate(Request request) {
        final ChallengeResponse challengeResponse = request.getChallengeResponse();

        boolean authenticated = false;

        try {
            // No username/password is ok, but results will be filtered later on
            if (challengeResponse == null) {
                return false;
            }

            LoginContext loginContext = new LoginContext("ems", new EmsCallbackHandler(challengeResponse));

            loginContext.login();

            log.info("Login successful: " + challengeResponse.getIdentifier());

            return authenticated = true;
        } catch (LoginException e) {
            // Dump the exception unless the user gave the wrong password.
            if (e instanceof FailedLoginException) {
                log.info("Login failed: " + challengeResponse.getIdentifier());
            } else {
                log.info("Login failed: " + challengeResponse.getIdentifier(), e);
            }
            return false;
        } finally {
            request.getAttributes().put(KEY, authenticated);
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static boolean isAuthenticated(Request request) {
        Object authenticated = request.getAttributes().get(KEY);
        return authenticated != null && (Boolean) authenticated;
    }

    private static class EmsCallbackHandler implements CallbackHandler {
        private final ChallengeResponse challengeResponse;

        public EmsCallbackHandler(ChallengeResponse challengeResponse) {
            this.challengeResponse = challengeResponse;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(challengeResponse.getIdentifier());
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(challengeResponse.getSecret());
                } else {
                    throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
                }
            }
        }
    }
}
