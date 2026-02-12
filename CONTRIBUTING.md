# Contributing to Rhai Language Support

Thank you for your interest in contributing to the Rhai Language Support plugin for JetBrains IDEs! This document provides guidelines and instructions for contributing.

## Getting Started

### Prerequisites

- **JDK 17+** - Required for building the plugin
- **IntelliJ IDEA** - Recommended for development (Community or Ultimate)
- **Git** - For version control

### Setting Up the Development Environment

1. **Fork and clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/rhai-jetbrains-plugin.git
   cd rhai-jetbrains-plugin
   ```

2. **Open in IntelliJ IDEA**
   - Open IntelliJ IDEA
   - Select "Open" and navigate to the cloned directory
   - Wait for Gradle to sync

3. **Build the plugin**
   ```bash
   ./gradlew buildPlugin
   ```

4. **Run the plugin in a sandbox IDE**
   ```bash
   ./gradlew runIde
   ```

## Development Workflow

### Project Structure

```
src/
├── main/
│   ├── grammars/           # BNF grammar (Rhai.bnf) and JFlex lexer (RhaiLexer.flex)
│   ├── kotlin/org/rhai/
│   │   ├── features/       # Completion, folding, formatting, structure view
│   │   ├── highlighting/   # Syntax highlighter
│   │   ├── inspections/    # Code inspections and quick fixes
│   │   ├── intentions/     # Intention actions
│   │   ├── lang/           # Language and file type definitions
│   │   ├── navigation/     # Go to declaration
│   │   ├── parser/         # Parser definition
│   │   ├── refactoring/    # Rename refactoring
│   │   ├── registry/       # Rust integration
│   │   ├── run/            # Run configuration
│   │   ├── settings/       # Settings pages
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── META-INF/       # Plugin descriptor (plugin.xml)
│       ├── icons/          # Plugin icons
│       └── liveTemplates/  # Live template definitions
└── test/
    └── kotlin/org/rhai/    # Unit tests
```

### Making Changes

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Follow existing code style and patterns
   - Add tests for new functionality
   - Update documentation if needed

3. **Run tests**
   ```bash
   ./gradlew test
   ```

4. **Run code quality checks**
   ```bash
   ./gradlew detekt
   ```

5. **Verify plugin compatibility**
   ```bash
   ./gradlew verifyPlugin
   ```

### Grammar Changes

If you modify the grammar files:

1. Edit `src/main/grammars/Rhai.bnf` for parser changes
2. Edit `src/main/grammars/RhaiLexer.flex` for lexer changes
3. Regenerate parser/lexer:
   ```bash
   ./gradlew generateParser generateLexer
   ```

### Adding New Features

When adding new IDE features:

1. Create implementation class in the appropriate package
2. Register the extension in `src/main/resources/META-INF/plugin.xml`
3. Add tests in `src/test/kotlin/org/rhai/`
4. Update README.md if it's a user-facing feature

## Code Style

### Kotlin Guidelines

- Use Kotlin idioms and conventions
- Prefer immutable data (`val` over `var`)
- Use meaningful names for classes, functions, and variables
- Keep functions small and focused
- Add KDoc comments for public APIs

### Formatting

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Run `./gradlew detekt` to check code style

### Naming Conventions

- Classes: `PascalCase` (e.g., `RhaiCompletionContributor`)
- Functions: `camelCase` (e.g., `getCompletionVariants`)
- Constants: `SCREAMING_SNAKE_CASE` (e.g., `MAX_SUGGESTIONS`)
- Files: Match the main class name

## Testing

### Writing Tests

- Place tests in `src/test/kotlin/org/rhai/`
- Extend `BasePlatformTestCase` for IDE integration tests
- Use descriptive test method names
- Cover edge cases and error scenarios

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "org.rhai.parser.RhaiParserTest"

# Run with verbose output
./gradlew test --info
```

## Submitting Changes

### Pull Request Process

1. **Ensure all checks pass**
   ```bash
   ./gradlew test detekt verifyPlugin
   ```

2. **Update documentation**
   - Update README.md for new features
   - Update CHANGELOG.md with your changes

3. **Create a pull request**
   - Use a clear, descriptive title
   - Reference any related issues
   - Describe what changes you made and why

4. **Code review**
   - Address reviewer feedback
   - Keep commits clean and focused

### Commit Messages

Follow conventional commit format:

```
type(scope): short description

Longer description if needed.

Fixes #123
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build, CI, or other maintenance

## Reporting Issues

### Bug Reports

When reporting bugs, please include:

- IDE version and OS
- Plugin version
- Steps to reproduce
- Expected vs actual behavior
- Error messages or stack traces (if any)
- Sample code that triggers the issue

### Feature Requests

For feature requests, please describe:

- The problem you're trying to solve
- Your proposed solution
- Alternative solutions you considered

## Questions?

If you have questions:

1. Check existing [issues](https://github.com/GlebMikhailov/rhai-jetbrains-plugin/issues)
2. Open a new issue with the "question" label
3. Contact the maintainer via email: gleb.mikhailov.04@gmail.com

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing!
