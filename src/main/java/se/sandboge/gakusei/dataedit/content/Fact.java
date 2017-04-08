package se.sandboge.gakusei.dataedit.content;

public class Fact {
    private final long id;
    private final String type;
    private final String data;
    private final String description;
    private final String nuggetid;

    public Fact(long id, String type, String data, String description, String nuggetid) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.description = description;
        this.nuggetid = nuggetid;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public String getDescription() {
        return description;
    }

    public String getNuggetid() {
        return nuggetid;
    }

    @Override
    public String toString() {
        return "Fact{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                ", description='" + description + '\'' +
                ", nuggetid='" + nuggetid + '\'' +
                '}';
    }
}
