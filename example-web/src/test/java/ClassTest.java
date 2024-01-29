import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.util.ClassUtil;
import com.vertx.common.core.annotations.UniqueAddress;
import com.vertx.eventbus.handler.BusHandler;
import com.vertx.example.web.model.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ClassTest {

    @Test
    public void test1() throws Exception {
        final Set<Class<?>> classes = ClassUtil.scanPackage();
        for (Class<?> aClass : classes) {
           //判断类是否有指定注解,
            if(aClass.isAnnotationPresent(UniqueAddress.class)){
                System.out.println(aClass);
            }
            //判断类是否实现了指定接口
            if(aClass.isAssignableFrom(BusHandler.class)){
                System.out.println(aClass);
            }
        }
        final Func1<User, Integer> getId = User::getId;

        final Integer call = getId.call(new User());
        System.out.println(call);
    }
}
