package domain;

public class Symbol {
    private String name;
    private String type;
    private Kind kind;
    private int idx;

    public Symbol() {
    }

    public Symbol(String name, String type, Kind kind, int idx) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.idx = idx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
}
