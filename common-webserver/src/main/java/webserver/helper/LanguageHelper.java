package webserver.helper;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.language.LanguageData;
import com.vertx.common.core.enums.LanguageTypeEnum;
import com.vertx.common.core.enums.SharedLockSharedLockEnum;
import com.vertx.common.core.helper.SharedLockHelper;
import com.vertx.common.core.utils.StrUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.vertx.common.core.config.VertxLoadConfig.vertx;
import static io.vertx.core.Future.await;

public class LanguageHelper {
    private static final Map<String, LanguageData> languageDataMap = new java.util.HashMap<>();

    /**
     * 根据提供的名称和语言类型检索特定于语言的字符串。
     *
     * @param name             字符串的名称。
     * @param languageTypeEnum 表示语言类型的枚举。
     * @param args             用于字符串格式化的可选参数。
     * @return 特定于语言的字符串，如果未找到语言数据，则返回名称本身。
     */
    public static String getLanguageString(String name, LanguageTypeEnum languageTypeEnum, String... args) {
        if (languageDataMap.isEmpty()) {
            final String filePath = "i18/language.json";
            if (!await(vertx.fileSystem().exists(filePath))) {
                StaticLog.warn("language.json文件不存在,无法获取语言信息");
                return name;
            }
            // 添加锁 防止多次读取文件
            SharedLockHelper.withLockWithTimeout(SharedLockSharedLockEnum.INIT_LANGUAGE, TimeUnit.SECONDS.toMillis(6), new String[]{}, () -> {
                if (languageDataMap.isEmpty()) {
                    final AsyncFile asyncFile = await(vertx.fileSystem().open(filePath, new OpenOptions().setRead(true)));
                    final Buffer buffer = await(asyncFile.read(Buffer.buffer(), 0, 0, 1024 * 1024));
                    final JsonArray languageDataList = buffer.toJsonArray();
                    final Map<String, Integer> languageDataMapUnique = new java.util.HashMap<>();
                    for (Object item : languageDataList) {
                        final LanguageData languageData = Json.decodeValue(item.toString(), LanguageData.class);
                        final String key = languageData.getName();
                        if (!languageDataMapUnique.containsKey(key)) {
                            languageDataMapUnique.put(key, 1);
                        } else {
                            StaticLog.warn("language.json文件中存在重复的name为{}的语言信息,请检查");
                        }
                        languageDataMap.put(key, languageData);
                    }
                    await(asyncFile.close());
                }
            });
        }
        if (languageDataMap.isEmpty()) {
            System.err.println("language.json 文件为空,无法获取语言信息");
            return name;
        }
        LanguageData languageData = languageDataMap.get(name);
        if (languageData == null) {
            System.err.println("language.json 文件中不存在 name 为" + name + "的语言信息,请补充");
            return name;
        }
        String msg = switch (languageTypeEnum.name().toLowerCase()) {
            case "zh" -> languageData.getZh();
            case "pu" -> languageData.getPu();
            default -> languageData.getEn();
        };
        return StrUtil.format(msg, args);
    }

    /**
     * 根据提供的 RoutingContext 的“Accept-Language”标头检索语言类型。
     *
     * @param context 表示传入 HTTP 请求的 RoutingContext 对象。
     * @return 与“Accept-Language”标头值对应的 LanguageTypeEnum。
     * 如果标头不存在或与任何支持的语言类型不匹配，则默认值为 LanguageTypeEnum.ZH。
     */
    public static LanguageTypeEnum getLanguageType(RoutingContext context) {
        final String languageHeader = context.request().getHeader("Accept-Language");
        String header = StrUtil.isBlank(languageHeader) ? LanguageTypeEnum.ZH.name() : languageHeader;
        return switch (header) {
            case "en" -> LanguageTypeEnum.EN;
            case "pu" -> LanguageTypeEnum.PU;
            default -> LanguageTypeEnum.ZH;
        };
    }
}