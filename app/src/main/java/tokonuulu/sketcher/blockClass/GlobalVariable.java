package tokonuulu.sketcher.blockClass;

public class GlobalVariable extends blockClass{
    private String variableName;
    private String variableValue;
    private String variableDesc;

    public GlobalVariable(String variableName, String variableValue, String variableDesc) {
        this.variableName = variableName;
        this.variableDesc = variableDesc;
        this.variableValue = variableValue;
    }

    public GlobalVariable(String variableDesc) {
        this.variableDesc = variableDesc;
        this.variableName = null;
        this.variableValue = null;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public String getVariableDesc() {
        return variableDesc;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }

    public void setVariableDesc(String variableDesc) {
        this.variableDesc = variableDesc;
    }
}
