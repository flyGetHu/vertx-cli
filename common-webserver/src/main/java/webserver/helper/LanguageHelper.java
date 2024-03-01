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
import io.vertx.core.shareddata.Lock;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

import static com.vertx.common.core.config.VertxLoadConfig.vertx;
import static io.vertx.core.Future.await;

/**
 * LanguageHelper 类提供了根据给定名称和语言类型检索特定于语言的字符串的方法。
 * 它还提供了一种根据所提供的 RoutingContext 的“Accept-Language”标头确定语言类型的方法。
 * 当类加载到 JVM 中时，语言数据会从“language.json”文件加载。
 * 加载的数据存储在静态地图中，以便高效检索。
 * 如果“language.json”文件不存在或为空，将记录警告，并且提供的名称将作为语言字符串返回。
 * 如果语言数据中不存在名称，则会记录警告，并且提供的名称将作为语言字符串返回。
 */
public class LanguageHelper {

    /**
     * 语言数据的静态数据
     */
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
            if (!loadConfigFile()) return name;
        }
        if (languageDataMap.isEmpty()) {
            StaticLog.warn("language.json 文件为空,无法获取语言信息");
            return name;
        }
        LanguageData languageData = languageDataMap.get(name);
        if (languageData == null) {
            StaticLog.warn("language.json 文件中不存在 name 为" + name + "的语言信息,请补充");
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
     * 加载语言配置文件
     *
     * @return 是否加载成功
     */
    private static boolean loadConfigFile() {
        final String filePath = "i18/language.json";
        // 添加锁 防止多次读取文件
        final Lock localLock = SharedLockHelper.getLocalLock(SharedLockSharedLockEnum.INIT_LANGUAGE, null);
        try {
            if (!languageDataMap.isEmpty()) {
                return true;
            }
            AsyncFile asyncFile = null;
            try {
                if (!await(vertx.fileSystem().exists(filePath))) {
                    StaticLog.warn("language.json文件不存在,无法获取语言信息");
                    return false;
                }
                asyncFile = await(vertx.fileSystem().open(filePath, new OpenOptions().setRead(true)));
                final Long fileSize = await(asyncFile.size());
                final Buffer buffer = await(asyncFile.read(Buffer.buffer(), 0, 0, Math.toIntExact(fileSize)));
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
            } finally {
                if (asyncFile != null) {
                    await(asyncFile.close());
                }
            }
        } finally {
            localLock.release();
        }
        return true;
    }

    /**
     * 根据提供的名称和 RoutingContext 的“Accept-Language”标头检索特定于语言的字符串。
     *
     * @param name    字符串的名称。
     * @param context 表示传入 HTTP 请求的 RoutingContext 对象。
     * @param args    用于字符串格式化的可选参数。
     * @return 特定于语言的字符串，如果未找到语言数据，则返回名称本身。
     */
    public static String getLanguageString(RoutingContext context, String name, String... args) {
        return getLanguageString(name, getLanguageType(context), args);
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