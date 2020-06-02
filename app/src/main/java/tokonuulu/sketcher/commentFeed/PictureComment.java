package tokonuulu.sketcher.commentFeed;

public class PictureComment extends Comment {
    private String picturePath;

    public PictureComment (String source, String block, String date, String path) {
        this.source = source;
        this.block = block;
        this.date = date;
        this.picturePath = path;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }
}
