package cn.moon.base;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class BaseEvent extends ApplicationEvent {
    public BaseEvent(Object source) {
        super(source);
    }




    Map<String,Object> data = new HashMap<>();

}
