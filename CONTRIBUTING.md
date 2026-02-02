# Contributing to Arbitrix

Thank you for your interest in contributing to Arbitrix! This document provides guidelines and information for contributors.

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## How to Contribute

### Reporting Bugs

Before creating a bug report, please check existing issues to avoid duplicates. When creating a bug report, include:

- **Clear title** describing the issue
- **Steps to reproduce** the behavior
- **Expected behavior** vs **actual behavior**
- **Environment details** (OS, Java version, exchange)
- **Logs and error messages** (sanitize any sensitive data)

### Suggesting Features

Feature requests are welcome! Please provide:

- **Clear description** of the feature
- **Use case** explaining why this feature would be useful
- **Possible implementation** approach (optional)

### Pull Requests

1. **Fork** the repository
2. **Create a branch** from `develop`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes** following our coding standards
4. **Write tests** for new functionality
5. **Run tests** to ensure nothing is broken:
   ```bash
   mvn clean test
   ```
6. **Commit** with clear messages:
   ```bash
   git commit -m "feat: add new trading strategy"
   ```
7. **Push** to your fork and create a Pull Request

## Development Setup

### Prerequisites

- JDK 17 or higher
- Maven 3.8+
- Git

### Building

```bash
# Clone the repository
git clone https://github.com/yourusername/arbitrix.git
cd arbitrix

# Build the project
mvn clean compile

# Run tests
mvn test

# Package
mvn package -DskipTests
```

### Configuration

1. Copy `application.yml.example` to `application.yml`
2. Fill in your exchange API credentials
3. Configure your trading parameters

## Coding Standards

### Java Style

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Follow standard Java naming conventions
- Use Lombok annotations to reduce boilerplate

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` new feature
- `fix:` bug fix
- `docs:` documentation changes
- `style:` formatting, no code change
- `refactor:` code refactoring
- `test:` adding tests
- `chore:` maintenance tasks

### Code Review

All submissions require review. We use GitHub Pull Requests for this purpose.

## Project Structure

```
arbitrix/
├── src/main/java/io/arbitrix/core/
│   ├── common/          # Common utilities and configurations
│   ├── controller/      # REST API controllers
│   ├── integration/     # Exchange integrations
│   │   ├── binance/
│   │   ├── bitget/
│   │   ├── bybit/
│   │   └── okx/
│   ├── strategy/        # Trading strategies
│   │   ├── avellaneda_stoikov/
│   │   ├── grid/
│   │   ├── profit_market_making/
│   │   └── pure_market_making/
│   └── utils/           # Utility classes
├── src/main/resources/  # Configuration files
└── src/test/            # Test files
```

## Testing

- Write unit tests for new functionality
- Ensure existing tests pass before submitting PR
- Use meaningful test names that describe the scenario

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ClassName

# Run with coverage
mvn test jacoco:report
```

## Security

- **Never commit API keys or secrets**
- Use environment variables for sensitive configuration
- Report security vulnerabilities privately to maintainers

## Questions?

Feel free to open an issue for any questions about contributing.

Thank you for contributing to Arbitrix!
