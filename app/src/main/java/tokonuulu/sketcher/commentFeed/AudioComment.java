package tokonuulu.sketcher.commentFeed;

public class AudioComment extends Comment {
    private String audioName;

    public AudioComment (String source, String block, String date, String name) {
        this.source = source;
        this.block = block;
        this.date = date;
        this.audioName = name;
    }

    public String getAudioName() {
        return audioName;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }
}
