package controllers;

import com.google.gson.JsonObject;
import play.Play;
import play.data.validation.Error;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.results.RenderJson;
import play.utils.Java;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author jtremeaux
 */
public class RestSecure extends Controller {
    @Before(unless={"login", "authenticate", "logout"}, priority = 1)
    static void checkAccess() throws Throwable {
        // Checks
        Check check = getActionAnnotation(Check.class);
        if (check != null) {
            check(check);
        }
        check = getControllerInheritedAnnotation(Check.class);
        if (check != null) {
            check(check);
        }
    }

    private static void check(Check check) throws Throwable {
        for (String profile : check.value()) {
            boolean hasProfile = (Boolean) Security.invoke("check", profile);
            if (!hasProfile) {
                onCheckFailed(profile);
            }
        }
    }

    public static void authenticate(@Required String username, String password, boolean remember) throws Throwable {
        if (!Play.mode.isDev()) {
            Validation.required("password", password);
        } else {
            if (password == null) {
                password = "";
            }
        }
        if (Validation.hasErrors()) {
            badRequestJson();
        }

        // Check tokens
        String userId = (String) Security.invoke("authenticate", username, password);
        if (userId == null) {
            validation.addError("global", "login.error");
            forbiddenJson();
        }

        // Mark user as connected
        session.put("username", userId);

        // Remember if needed
        if (remember) {
            Date expiration = new Date();
            String duration = Play.configuration.getProperty("secure.rememberme.duration","30d");
            expiration.setTime(expiration.getTime() + Time.parseDuration(duration) * 1000 );
            response.setCookie("rememberme", Crypto.sign(username + "-" + expiration.getTime()) + "-" + username + "-" + expiration.getTime(), duration);
        }

        Security.invoke("afterAuthenticate", userId);

        okJson();
    }

    @Before
    public static void before() throws Throwable {
        Security.invoke("before");
    }

    @After
    public static void after() throws Throwable {
        Security.invoke("after");
    }

    protected static void okJson() {
        JsonObject json = new JsonObject();
        json.addProperty("status", "ok");
        renderJSON(json);
    }

    protected static void badRequestJson() {
        JPA.setRollbackOnly();
        response.status = 400;
        renderJSON(convertErrorMap(validation.errorsMap()));
    }

    protected static void forbiddenJson() {
        JPA.setRollbackOnly();
        response.status = 403;
        renderJSON(convertErrorMap(validation.errorsMap()));
    }

    /**
     * Normalize field names.
     * Ex. user[country][id] -> user.country.id
     *
     * @param errorMap
     * @return
     */
    protected static Map<String, List<Error>> convertErrorMap(Map<String, List<play.data.validation.Error>> errorMap) {
        Pattern p = Pattern.compile("\\[.+\\]");
        Map<String, List<play.data.validation.Error>> result = new HashMap<>();
        for (Map.Entry<String, List<play.data.validation.Error>> error : errorMap.entrySet()) {
            String label = error.getKey().replaceAll("\\[(.+?)\\]", ".$1");
            result.put(label, error.getValue());
        }
        return result;
    }

    public static void logout() throws Throwable {
        onDisconnect();
        session.clear();
        response.removeCookie("rememberme");
        onDisconnected();

        okJson();
    }

    /**
     * This method is called before a user tries to sign off.
     * You need to override this method if you wish to perform specific actions (eg. Record the name of the user who signed off)
     */
    static void onDisconnect() {
    }

    /**
     * This method is called after a successful sign off.
     * You need to override this method if you wish to perform specific actions (eg. Record the time the user signed off)
     */
    static void onDisconnected() {
    }

    /**
     * This method is called if a check does not succeed. By default it shows the not allowed page (the controller forbidden method).
     * @param profile
     */
    static void onCheckFailed(String profile) {
        forbidden();
    }

    private static Object invoke(String m, Object... args) throws Throwable {
        try {
            return Java.invokeChildOrStatic(RestSecure.class, m, args);
        } catch(InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public static class Security extends Controller {
        /**
         * This method is called during the authentication process. This is where you check if
         * the user is allowed to log in into the system. This is the actual authentication process
         * against a third party system (most of the time a DB).
         *
         * @param username
         * @param password
         * @return User ID if the authentication process succeeded
         */
        static String authenticate(String username, String password) {
            return null;
        }

        /**
         * This method checks that a profile is allowed to view this page/method. This method is called prior
         * to the method's controller annotated with the @Check method.
         *
         * @param profile
         * @return true if you are allowed to execute this controller method.
         */
        static boolean check(String profile) {
            return true;
        }

        /**
         * This method returns the current connected user ID
         * @return
         */
        public static String connected() {
            String id = null;
            if (isConnected()) {
                id = session.get("username");
            }
            return id;
        }

        /**
         * Indicate if a user is currently connected
         * @return  true if the user is connected
         */
        static boolean isConnected() {
            return session != null && session.contains("username");
        }

        /**
         * This method is called before before all actions.
         */
        static void before() {
        }

        /**
         * This method is called before after all actions.
         */
        static void after() {
        }

        /**
         * This method is called before after authentication.
         */
        static void afterAuthenticate(String userId) {
        }

        /**
         * This method is called after a successful sign off.
         * You need to override this method if you wish to perform specific actions (eg. Record the time the user signed off)
         */
        static void onDisconnected() {
        }

        /**
         * This method is called if a check does not succeed. By default it shows the not allowed page (the controller forbidden method).
         * @param profile
         */
        static void onCheckFailed(String profile) {
            forbidden();
        }

        private static Object invoke(String m, Object... args) throws Throwable {

            try {
                return Java.invokeChildOrStatic(Security.class, m, args);
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
