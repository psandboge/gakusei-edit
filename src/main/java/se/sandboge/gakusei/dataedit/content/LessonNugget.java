package se.sandboge.gakusei.dataedit.content;


public class LessonNugget {
    private final long lessonId;
    private final String nuggetId;

    public LessonNugget(long lessonId, String nuggetId) {
        this.lessonId = lessonId;
        this.nuggetId = nuggetId;
    }

    public long getLessonId() {
        return lessonId;
    }

    public String getNuggetId() {
        return nuggetId;
    }

    @Override
    public String toString() {
        return "LessonNugget{" +
                "lessonId=" + lessonId +
                ", nuggetId='" + nuggetId + '\'' +
                '}';
    }
}
