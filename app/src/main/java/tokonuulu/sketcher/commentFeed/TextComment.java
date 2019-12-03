package tokonuulu.sketcher.commentFeed;

public class TextComment extends Comment {
    private String text;

    public TextComment (String source, String block, String date, String text) {
        this.source = source;
        this.block = block;
        this.date = date;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
