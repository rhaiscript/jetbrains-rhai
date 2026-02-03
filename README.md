# Rhai Language Support for JetBrains IDEs

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange)](https://plugins.jetbrains.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Full-featured language support for the [Rhai](https://rhai.rs/) scripting language in JetBrains IDEs (IntelliJ IDEA, CLion, RustRover, etc.).

## Features

### Syntax Highlighting
- Full syntax highlighting for Rhai scripts
- Customizable colors via **Settings > Editor > Color Scheme > Rhai**
- Support for all Rhai constructs: functions, variables, strings, numbers, operators, etc.

### Code Intelligence
- **Code completion** for keywords, built-in functions, and user-defined symbols
- **Go to definition** (Ctrl+Click or Ctrl+B)
- **Find usages** (Alt+F7)
- **Parameter info** for function calls
- **Documentation on hover**

### Code Navigation
- **Structure view** with filtering (Alt+7)
- **Breadcrumb navigation**
- **Go to Symbol** (Ctrl+Alt+Shift+N)

### Code Editing
- **Code folding** for functions, blocks, and comments
- **Brace matching** and auto-closing
- **Smart Enter** completion
- **Comment/uncomment** (Ctrl+/)
- **Surround with** templates (Ctrl+Alt+T)
- **Live templates** for common patterns

### Code Quality
- **Inspections:**
  - Unused variable detection
  - Unresolved reference detection
  - Duplicate function definition detection
- **Quick fixes** for common issues
- **Spell checking** in comments and strings

### Run Configuration
- **Run Rhai scripts** directly from the IDE
- **Right-click to run** any `.rhai` file
- **Run gutter icon** for quick execution
- Configurable interpreter path

## Installation

### From JetBrains Marketplace
1. Open **Settings/Preferences > Plugins**
2. Search for "Rhai Language Support"
3. Click **Install**
4. Restart the IDE

### Manual Installation
1. Download the plugin `.zip` from [Releases](https://github.com/example/rhai-intellij-plugin/releases)
2. Open **Settings/Preferences > Plugins**
3. Click the gear icon and select **Install Plugin from Disk...**
4. Select the downloaded `.zip` file
5. Restart the IDE

## Running Rhai Scripts

### Prerequisites
Install a Rhai interpreter:

```bash
# Install rhai-repl (recommended)
cargo install rhai-repl

# Or use a custom Rust binary with embedded Rhai engine
```

### Running Scripts
1. **Right-click** on any `.rhai` file in the Project view
2. Select **Run 'filename'**
3. The script output appears in the Run tool window

### Run Configuration
To customize the run configuration:
1. Go to **Run > Edit Configurations...**
2. Click **+** and select **Rhai Script**
3. Configure:
   - **Script**: Path to the `.rhai` file
   - **Interpreter**: Path to `rhai-repl` or custom interpreter
   - **Arguments**: Script arguments
   - **Working directory**: Execution directory

## Live Templates

Type the following abbreviations and press Tab to expand:

| Abbreviation | Expands to |
|-------------|-----------|
| `fn` | Function definition |
| `let` | Variable declaration |
| `if` | If statement |
| `ife` | If-else statement |
| `for` | For loop |
| `while` | While loop |
| `loop` | Infinite loop |
| `match` | Switch statement |

## File Templates

Create new Rhai files with predefined templates:
1. Right-click on a folder in Project view
2. Select **New > Rhai Script**

## Code Style

Configure Rhai code style settings:
**Settings > Editor > Code Style > Rhai**

Options include:
- Indentation (tabs vs spaces)
- Continuation indent
- Keep line breaks

## Requirements

- IntelliJ IDEA 2023.1+ (or compatible JetBrains IDE)
- For running scripts: Rhai interpreter (`cargo install rhai-repl`)

## Building from Source

```bash
# Clone the repository
git clone https://github.com/example/rhai-intellij-plugin.git
cd rhai-intellij-plugin

# Build the plugin
./gradlew buildPlugin

# The plugin ZIP will be in build/distributions/
```

### Development

```bash
# Run the IDE with the plugin installed
./gradlew runIde

# Run tests
./gradlew test

# Run linter
./gradlew detekt
```

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run `./gradlew detekt` to check code style
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Rhai](https://rhai.rs/) - The embedded scripting language for Rust
- [JetBrains Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html) - Plugin development documentation
