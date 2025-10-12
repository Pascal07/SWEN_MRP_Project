package at.technikum.server.http;

import java.nio.charset.StandardCharsets;

public class Response {

    private Status status;

    private ContentType contentType;

    private byte[] body; // war vorher String

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getStatusCode() {
        return status.getCode();
    }

    public String getStatusMessage() {
        return status.getMessage();
    }

    public String getContentType() {
        return contentType != null ? contentType.getMimeType() : ContentType.TEXT_PLAIN.getMimeType();
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public byte[] getBodyBytes() {
        return body;
    }

    public String getBody() {
        return body == null ? null : new String(body, StandardCharsets.UTF_8);
    }

    public void setBody(String body) {
        this.body = body == null ? null : body.getBytes(StandardCharsets.UTF_8);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
