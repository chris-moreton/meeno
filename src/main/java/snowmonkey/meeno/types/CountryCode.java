package snowmonkey.meeno.types;

public class CountryCode extends ImmutbleType {
    public final String code;
    public final String name;

    public CountryCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
