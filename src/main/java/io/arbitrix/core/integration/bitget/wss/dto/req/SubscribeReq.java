package io.arbitrix.core.integration.bitget.wss.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.arbitrix.core.integration.bitget.wss.enums.ChannelEnum;
import io.arbitrix.core.integration.bitget.wss.enums.InstType;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeReq {

    private String instType;
    private String channel;
    private String instId;

    public static SubscribeReq ticker(String instId) {
        return SubscribeReq.builder()
                .instType(InstType.SP.getCode())
                .channel(ChannelEnum.TICKER.getCode())
                .instId(instId).build();
    }
    public static SubscribeReq orders(String instId) {
        return SubscribeReq.builder()
                .instType(InstType.SPBL.getCode())
                .channel(ChannelEnum.ORDERS.getCode())
                .instId(instId).build();
    }
    public boolean isBooksChannel() {
        return ChannelEnum.BOOKS.getCode().equalsIgnoreCase(channel);
    }

    @Override
    public boolean equals(Object o) {
        SubscribeReq that = (SubscribeReq) o;
        return Objects.equals(instType.toUpperCase(), that.instType.toUpperCase()) && Objects.equals(channel.toUpperCase(), that.channel.toUpperCase()) && Objects.equals(instId.toUpperCase(), that.instId.toUpperCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(instType.toUpperCase(), channel.toUpperCase(), instId.toUpperCase());
    }
}
