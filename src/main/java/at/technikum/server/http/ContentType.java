package at.technikum.server.http;

public enum ContentType {
    TEXT_PLAIN("text/plain"),
    APPLICATION_JSON("application/json"),
    IMAGE_PNG("image/png"); // hinzugefügt

    private final String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
