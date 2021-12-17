package akkaStream;

public class TestInformation {
    private String url;
    private Integer requestNumber;
    private long time;

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

    public TestInformation(String url, Integer con, long time) {
        this.url = url;
        this.requestNumber = con;
        this.time = time;
    }

    public TestInformation add(TestInformation information) {
        this.requestNumber += information.requestNumber;
        this.time += information.time;
    }
}
