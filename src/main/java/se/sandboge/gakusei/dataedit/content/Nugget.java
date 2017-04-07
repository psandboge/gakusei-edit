package se.sandboge.gakusei.dataedit.content;


public class Nugget {
    private String id;
    private String type;
    private String description;
    private String hidden;

    public Nugget(String id, String type, String description, String hidden) {
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

    public String getHidden() {
        return hidden;
    }
}
