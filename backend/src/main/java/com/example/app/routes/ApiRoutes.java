package com.example.app.routes;

/**
 * The route catalog: every URL path the API exposes, declared in one place.
 *
 * This is the Spring equivalent of an Express "routes" file. In Express you
 * list routes and point them at controller functions:
 *
 *     router.post('/register', authController.register);
 *
 * In Spring the mapping lives on the controller method via an annotation, but
 * we keep the actual path strings here so you can see the whole API at a glance
 * and change a URL in exactly one spot. Controllers reference these constants:
 *
 *     @RequestMapping(ApiRoutes.AUTH)          // on the class
 *     @PostMapping(ApiRoutes.REGISTER)         // on the method
 *
 * The full URL is the class base + the method path (shown in the comments).
 */
public final class ApiRoutes {

    private ApiRoutes() {
        // Constants holder - not meant to be instantiated.
    }

    // ---- Authentication (base: /api/auth) ----
    public static final String AUTH = "/api/auth";
    public static final String REGISTER = "/register";   // POST /api/auth/register
    public static final String LOGIN = "/login";         // POST /api/auth/login

    // ---- Tasks (base: /api/tasks) ----
    public static final String TASKS = "/api/tasks";
    public static final String TASK_BY_ID = "/{id}";     // GET/PUT/DELETE /api/tasks/{id}
}
