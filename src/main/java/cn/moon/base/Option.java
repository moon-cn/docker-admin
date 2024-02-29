package cn.moon.base;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class Option {
    String text;
    String value;


    public Option(String value, String text) {
        this.text = text;
        this.value = value;
    }

    public Option() {
    }

    public String getLabel(){
        return text;
    }


    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Option) {
            Option other = (Option) obj;
            return StringUtils.equals(value, other.value);
        }

        return false;
    }
}
