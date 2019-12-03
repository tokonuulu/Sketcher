package tokonuulu.sketcher.blockClass;

public class Class extends blockClass {
    private String className,classDesc;

    public Class(String className, String classDesc) {
        this.className = className;
        this.classDesc = classDesc;
    }

    public String getClassDesc() {
        return classDesc;
    }

    public String getClassName() {
        return className;
    }

    public void setClassDesc(String classDesc) {
        this.classDesc = classDesc;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
