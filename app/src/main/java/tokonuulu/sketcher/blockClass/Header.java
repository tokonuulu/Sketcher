package tokonuulu.sketcher.blockClass;

public class Header extends blockClass{
    private String headerName;
    private String headerDesc;

    public Header(String headerName, String headerDesc) {
        this.headerName = headerName;
        this.headerDesc = headerDesc;
    }

    public Header(String headerName) {
        this.headerName = headerName;
        this.headerDesc = null;
    }

    public String getHeaderDesc() {
        return headerDesc;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderDesc(String headerDesc) {
        this.headerDesc = headerDesc;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
}
