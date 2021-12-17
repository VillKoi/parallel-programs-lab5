package akkaStream;

public class TestInformation {
    private String url;
    private Integer requestNumber;

    public String getUrl() {
        return url;
    }

    public Integer getRequestNumber() {
        return requestNumber;
    }

    public TestInformation(String url, Integer con) {
        this.url = url;
        this.requestNumber = con;
    }
}
