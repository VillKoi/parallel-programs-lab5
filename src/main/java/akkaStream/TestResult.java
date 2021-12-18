package akkaStream;

public class TestResult {
        private String url;
        private Integer requestNumber;
        private long time;

        public String getUrl() {
            return url;
        }

        public Integer getRequestNumber() {
            return requestNumber;
        }

    public long getTime() {
        return requestNumber / time;
    }

    public TestResult(String url, Integer con, long time) {
            this.url = url;
            this.requestNumber = con;
            this.time = time;
        }

        public TestResult add(TestResult result) {
            this.url = result.url;
            this.requestNumber += result.requestNumber;
            this.time += result.time;
            return this;
        }

        public boolean isReady() {
            return time != 0;

        }
}
