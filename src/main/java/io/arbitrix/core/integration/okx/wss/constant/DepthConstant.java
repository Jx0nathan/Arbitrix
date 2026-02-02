package io.arbitrix.core.integration.okx.wss.constant;

/**
 * @author jonathan.ji
 */
public class DepthConstant {

    /**
     * 首次推400档快照数据，以后增量推送，每100毫秒推送一次变化的数据
     */
    public static final String BOOKS = "books";

    /**
     * 首次推5档快照数据，以后定量推送，每100毫秒当5档快照数据有变化推送一次5档数据
     */
    public static final String BOOKS5 = "books5";

    /**
     * 首次推1档快照数据，以后定量推送，每10毫秒当1档快照数据有变化推送一次1档数据
     */
    public static final String BBO_TBT = "bbo-tbt";

    /**
     * 首次推400档快照数据，以后增量推送，每10毫秒推送一次变化的数据
     * 只允许交易手续费等级VIP5及以上的API用户订阅
     */
    public static final String BOOKS_L2_TBT = "books-l2-tbt";

    /**
     * 首次推50档快照数据，以后增量推送，每10毫秒推送一次变化的数据
     */
    public static final String BOOKS50_L2_TBT = "books50-l2-tbt";

}
