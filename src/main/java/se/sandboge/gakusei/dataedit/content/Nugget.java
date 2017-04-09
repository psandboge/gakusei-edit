package se.sandboge.gakusei.dataedit.content;


public class Nugget {
    private String id;
    private String type;
    private String description;
    private boolean hidden;

    public Nugget(String id, String type, String description, boolean hidden) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.hidden = hidden;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public boolean getHidden() {
        return hidden;
    }

    @Override
    public String toString() {
        return "Nugget{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", hidden=" + hidden +
                '}';
    }
}
