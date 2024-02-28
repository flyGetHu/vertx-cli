package webserver.helper;

import io.vertx.ext.web.RoutingContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
* 此类提供使用 Jakarta Validation API 验证对象的辅助方法。
 * 它使用Validation API提供的默认验证器。
 * 验证规则是使用被验证对象的字段上的注释来定义的。
 * <p>
 * validate 方法验证对象并返回错误消息列表。
 * 错误消息根据从 RoutingContext 获取的语言类型进行本地化。
 */
public class ValidatorHelper {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    /*
     * @Null 被注释的元素必须为 null
     * @NotNull 被注释的元素必须不为 null
     * @AssertTrue 被注释的元素必须为 true
     * @AssertFalse 被注释的元素必须为 false
     * @Min(value) 被注释的元素必须是一个数字，其值必须大于等于指定的最小值
     * @Max(value) 被注释的元素必须是一个数字，其值必须小于等于指定的最大值
     * @DecimalMin(value) 被注释的元素必须是一个数字，其值必须大于等于指定的最小值
     * @DecimalMax(value) 被注释的元素必须是一个数字，其值必须小于等于指定的最大值
     * @Size(max=, min=)   被注释的元素的大小必须在指定的范围内
     * @Digits (integer, fraction)     被注释的元素必须是一个数字，其值必须在可接受的范围内
     * @Past 被注释的元素必须是一个过去的日期
     * @Future 被注释的元素必须是一个将来的日期
     * @Pattern(regex=,flag=) 被注释的元素必须符合指定的正则表达式
     * -------------------------------------------------------------------------------------------------
     * Hibernate Validator提供的校验注解
     * @NotBlank(message =)   验证字符串非null，且长度必须大于0
     * @Email 被注释的元素必须是电子邮箱地址
     * @Length(min=,max=) 被注释的字符串的大小必须在指定的范围内
     * @NotEmpty 被注释的字符串的必须非空
     * @Range(min=,max=,message=) 被注释的元素必须在合适的范围内
     */
    public <T> List<String> validate(T object, RoutingContext context) {
        final Set<ConstraintViolation<T>> constraintViolationSet = validator.validate(object, Default.class);
        final List<String> errorList = new ArrayList<>();
        for (ConstraintViolation<T> tConstraintViolation : constraintViolationSet) {
            final String property = tConstraintViolation.getPropertyPath().toString();
            final String message = tConstraintViolation.getMessage();
            final String languageString = LanguageHelper.getLanguageString(message, LanguageHelper.getLanguageType(context));
            errorList.add(property + ":" + languageString + ";");
        }
        return errorList;
    }
}
