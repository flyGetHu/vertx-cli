import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;

public class ClassColumTest {
    @Data
    static class User {
        private String name;
        private String age;
    }

    @Test
    public void getClassColumTest() {
        final String colum = getColum(User.class, User::getName);
        System.out.println(colum);
    }

    private <T> String getColum(Class<T> tClass, Function<T, ?> function) {
        return Arrays.stream(tClass.getDeclaredFields())
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        T instance = tClass.getDeclaredConstructor().newInstance();
                        return function.apply(instance) != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .map(Field::getName)
                .findFirst()
                .orElse(null);
    }
}
