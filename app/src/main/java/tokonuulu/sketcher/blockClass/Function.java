package tokonuulu.sketcher.blockClass;

public class Function extends blockClass {
    private String functionName,functionDecs;

    public Function(String functionName, String functionDecs) {
        this.functionName = functionName;
        this.functionDecs = functionDecs;
    }

    public String getFunctionName() {
        return functionName;
    }
    public String getFunctionDecs() {
        return functionDecs;
    }

    public void setFunctionDecs(String functionDecs) {
        this.functionDecs = functionDecs;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
