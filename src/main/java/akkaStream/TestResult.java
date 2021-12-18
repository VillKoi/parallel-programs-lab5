package akkaStream;

public class TestResult {
        private String url;
        private Integer requestNumber;
        private long time;

        private static final Integer startRequestNumber = 1;

        public String getUrl() {
            return url;
        }

        public Integer getRequestNumber() {
            return requestNumber;
        }

        public long getTime() {
            return time;
        }

        public long getAvrTime() {
            return time / requestNumber;
        }

        public TestResult(String url, Integer con, long time) {
            this.url = url;
            this.requestNumber = con;
            this.time = time;
        }

        public TestResult(String url, long time) {
            this.url = url;
            this.requestNumber = startRequestNumber;
            this.time = time;
        }

        // empty struct
        public TestResult() {
            this.url = "";
            this.requestNumber = 0;
            this.time = 0;
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
