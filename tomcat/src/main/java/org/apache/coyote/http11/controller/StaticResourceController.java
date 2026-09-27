package org.apache.coyote.http11.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.coyote.http11.HttpRequest;
import org.apache.coyote.http11.HttpResponse;
import org.apache.coyote.http11.HttpStatus;

public class StaticResourceController extends AbstractController {

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            ".html", "text/html;charset=utf-8",
            ".css", "text/css",
            ".js", "text/javascript"
    );

    private final String resourceRoot;

    public StaticResourceController(String resourceRoot) {
        this.resourceRoot = resourceRoot;
    }

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
        serve(request.getPath(), response);
    }

    public void serve(String path, HttpResponse response) throws IOException {
        if (!path.startsWith("/") || path.contains("..")) {
            response.sendError(HttpStatus.NOT_FOUND);
            return;
        }

        String resourceName = resourceRoot + path;

        try (InputStream resource = getClass()
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (resource == null) {
                response.sendError(HttpStatus.NOT_FOUND);
                return;
            }

            response.setBody(resource.readAllBytes(), contentType(path));
        }
    }

    public boolean supports(String path) {
        return CONTENT_TYPES.containsKey(extension(path));
    }

    private String contentType(String path) {
        return CONTENT_TYPES.getOrDefault(
                extension(path),
                "text/html;charset=utf-8"
        );
    }

    private static String extension(String path) {
        int dot = path.lastIndexOf('.');

        if (dot < 0) {
            return "";
        }
        return path.substring(dot);
    }
}
