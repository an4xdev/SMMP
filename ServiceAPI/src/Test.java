import org.json.JSONPropertyName;

public record Test(String name, String surname) {
    @JSONPropertyName("Name")
    public String name() {
        return name;
    }

    @JSONPropertyName("Surname")
    public String surname() {
        return surname;
    }

}
