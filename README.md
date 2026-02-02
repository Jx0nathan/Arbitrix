# Arbitrix

A high-performance cryptocurrency market making and trading engine built with Java and Spring Boot.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.5-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Features

- **Multi-Exchange Support** - Seamlessly connect to OKX, Bybit, Bitget, and Binance
- **Real-time WebSocket Streaming** - Low-latency market data via WebSocket connections
- **Multiple Trading Strategies**
  - Pure Market Making - Aggressive best bid/ask capturing
  - Profit Market Making - Spread-based profit optimization
  - Avellaneda-Stoikov Algorithm - Academic market making model with inventory risk management
  - Grid Trading - Automated grid-based order placement
- **High Performance**
  - CPU affinity binding for reduced context switching
  - Dedicated thread pools for order placement and cancellation
  - Caffeine cache for ultra-fast data access
- **Robust Order Management**
  - Concurrent order pool with automatic cleanup
  - Fallback order cancellation mechanism
  - Real-time order status tracking via WebSocket

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Arbitrix Engine                         │
├─────────────────────────────────────────────────────────────────┤
│  Exchange Adapters     │  Strategy Layer    │  Order Manager    │
│  ├── Binance WSS       │  ├── PureMarketMaking                  │
│  ├── OKX REST/WSS      │  ├── ProfitMarketMaking                │
│  ├── Bybit REST/WSS    │  ├── Avellaneda-Stoikov                │
│  └── Bitget REST       │  └── GridTrading    │                  │
├─────────────────────────────────────────────────────────────────┤
│                    Core Infrastructure                          │
│  ├── Thread Pool Executors (CPU Affinity)                       │
│  ├── WebSocket Connection Manager                               │
│  └── Order Cache Pool (ConcurrentHashMap)                       │
└─────────────────────────────────────────────────────────────────┘
```

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- API keys from supported exchanges

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/arbitrix.git
cd arbitrix

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Configuration

Create `application.yml` in `src/main/resources/`:

```yaml
# Exchange Configuration
exchange: OKX  # Options: BINANCE, OKX, BYBIT, BITGET

# Trading Pairs
symbol:
  pairs: BTCUSDT,ETHUSDT

# Strategy Configuration
strategy:
  execute: pure_market_making  # Options: pure_market_making, profit_market_making

# Market Making Parameters
market:
  making:
    price:
      spread: '{"OKX_BTCUSDT_BUY":"0.01","OKX_BTCUSDT_SELL":"0.01"}'
    quote-coin-value: '{"OKX_BTCUSDT_BUY":"100","OKX_BTCUSDT_SELL":"100"}'

# OKX API Configuration
okx:
  api:
    key: your-api-key
    secret: your-secret-key
    passphrase: your-passphrase
    base-url: https://www.okx.com
    ws-public-url: wss://ws.okx.com:8443/ws/v5/public
    ws-private-url: wss://ws.okx.com:8443/ws/v5/private
```

## Supported Exchanges

| Exchange | Spot | Futures | WebSocket | REST API |
|----------|:----:|:-------:|:---------:|:--------:|
| Binance  |  ✅  |   ✅    |    ✅     |    ✅    |
| OKX      |  ✅  |   ✅    |    ✅     |    ✅    |
| Bybit    |  ✅  |   ✅    |    ✅     |    ✅    |
| Bitget   |  ✅  |   ❌    |    ❌     |    ✅    |

## Trading Strategies

### Pure Market Making

Aggressive strategy that continuously places orders at the best bid/ask prices to capture the spread.

```yaml
strategy:
  execute: pure_market_making
```

### Profit Market Making

Conservative strategy with configurable spread levels and profit targets.

```yaml
strategy:
  execute: profit_market_making

# Additional options
order_level_spread_cmd: "0.00003"
profit_ask_price_base_on_best_bid_price_cmd: "0.00002"
```

### Avellaneda-Stoikov

Academic market making algorithm that dynamically adjusts quotes based on inventory risk and market volatility.

Key parameters:
- `gamma` - Risk aversion coefficient
- `sigma` - Volatility estimation
- `k` - Order arrival intensity

## Performance Tuning

### CPU Affinity

The engine uses CPU affinity to bind trading threads to specific cores:

| Thread Pool | CPU Core | Purpose |
|-------------|----------|---------|
| PlaceBuyOrderExecutor | Core 3 | Buy order placement |
| PlaceSellOrderExecutor | Core 4 | Sell order placement |
| CancelOrderExecutor | Core 5 | Order cancellation |

### Thread Pool Configuration

All executors use `DiscardOldestPolicy` to prioritize fresh market data over stale orders.

## API Documentation

### Order Placement

```java
ExchangeOrder order = ExchangeOrder.limitMarketBuy(
    "BTCUSDT",      // symbol
    "0.001",        // quantity
    "50000.00",     // price
    UUID.randomUUID().toString()  // clientOrderId
);
```

### Strategy Implementation

Implement the `SpotStrategy` interface to create custom strategies:

```java
@Component
@ExecuteStrategyConditional(executeStrategyName = "my_strategy")
public class MyStrategy implements SpotStrategy {
    @Override
    public void execute(SpotOrderExecutionContext context) {
        // Your strategy logic here
    }
}
```

## Monitoring

The engine exposes metrics via `StrategyMonitor`:

- Order execution latency
- WebSocket connection status
- Order fill rates
- P&L tracking

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting a PR.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Disclaimer

**USE AT YOUR OWN RISK.** This software is for educational purposes only. Cryptocurrency trading involves substantial risk of loss. The authors are not responsible for any financial losses incurred through the use of this software.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Avellaneda & Stoikov (2008)](https://www.math.nyu.edu/~avellane/HighFrequencyTrading.pdf) - High-frequency trading in a limit order book
- Spring Boot Team
- OpenHFT for Chronicle-Map and Affinity libraries

---

**Star this repo** if you find it useful!
