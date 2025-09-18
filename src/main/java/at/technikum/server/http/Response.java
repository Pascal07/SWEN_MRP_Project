package at.technikum.server.http;

public class Response {

    private Status status;

    private ContentType contentType;

    private String body;

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
        return contentType.getMimeType();
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
