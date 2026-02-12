# Rhai Language Support for JetBrains IDEs

[![Build Status](https://github.com/GlebMikhailov/rhai-jetbrains-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/GlebMikhailov/rhai-jetbrains-plugin/actions)
[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange)](https://plugins.jetbrains.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Full-featured language support for the [Rhai](https://rhai.rs/) scripting language in JetBrains IDEs (IntelliJ IDEA, CLion, RustRover, etc.).

## Features

### Syntax Highlighting
- Full syntax highlighting for Rhai scripts
- Customizable colors via **Settings > Editor > Color Scheme > Rhai**
- Semantic highlighting for functions, variables, parameters, and more
- Support for all Rhai constructs: functions, variables, strings, numbers, operators, etc.

### Code Intelligence
- **Code completion** for:
  - Keywords with smart insertion (automatic spaces after modifiers)
  - 40+ built-in functions with documentation
  - Array methods (push, pop, map, filter, reduce, etc.)
  - String methods (trim, split, to_upper, etc.)
  - User-defined functions and variables
  - Cross-file symbols with automatic import generation
  - Registered functions from Rust (via registry system)
- **Go to definition** (Ctrl+Click or Ctrl+B)
- **Find usages** (Alt+F7)
- **Parameter info** for function calls
- **Documentation on hover** for built-in and user-defined symbols

### Refactoring
- **Rename refactoring** (Shift+F6) for:
  - Variables and constants
  - Functions
  - Function parameters
  - Closure parameters
  - For loop variables
- Smart scope detection (local vs global)
- Automatic update of all usages

### Code Navigation
- **Structure view** with filtering (Alt+7)
  - Functions (public/private/test indicators)
  - Constants
  - Variables
  - Modules
- **Breadcrumb navigation**
- **Go to Symbol** (Ctrl+Alt+Shift+N)
- **Line markers** for function definitions and usages

### Code Editing
- **Code folding** for functions, blocks, comments, and imports
- **Brace matching** and auto-closing
- **Smart Enter** completion
- **Comment/uncomment** (Ctrl+/)
- **Surround with** templates (Ctrl+Alt+T):
  - if, if-else, while, for, loop
  - try-catch, block
  - Parentheses
- **Live templates** for common patterns

### Code Formatting
- **Reformat code** (Ctrl+Alt+L)
- Configurable spacing rules for:
  - Assignment, logical, equality, relational operators
  - Additive and multiplicative operators
  - Range and arrow operators
  - Commas, colons, semicolons
  - Braces, parentheses, brackets
  - Closure pipes
- Automatic indentation for blocks
- Alignment of multiline parameters and arguments

### Code Quality
- **Inspections:**
  - Unused variable detection
  - Unresolved reference detection
  - Duplicate function definition detection
- **Quick fixes (Alt+Enter):**
  - **Create function** - creates a new function stub for unresolved function calls
  - **Create variable** - creates a variable declaration for unresolved references
  - **Auto-import symbols** from other files
  - **Import and prefix** with module name (e.g., `module::symbol`)
  - **Rename to `_name`** - marks unused variable as intentionally unused
  - **Remove unused declaration** - deletes unused variable
  - **Delete duplicate function** - removes duplicate function definition
  - **Rename duplicate function** - renames to `funcName_2`
- **Spell checking** in comments and strings

### Run Configuration
- **Run Rhai scripts** directly from the IDE
- **Right-click to run** any `.rhai` file
- **Run gutter icon** for quick execution
- Configurable interpreter path and arguments

### Rust Integration
- **Auto-detect Rhai registrations** from Rust source files
- Support for `register_fn`, `register_type`, `register_get/set`, and module functions
- Separate **global** and **project-specific** registries
- **Right-click action** to manually add registrations from Rust code

## Installation

### From JetBrains Marketplace
1. Open **Settings/Preferences > Plugins**
2. Search for "Rhai Language Support"
3. Click **Install**
4. Restart the IDE

### Manual Installation
1. Download the plugin `.zip` from [Releases](https://github.com/GlebMikhailov/rhai-jetbrains-plugin/releases)
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

## Rust Integration (Auto-Registry)

The plugin automatically detects Rhai function registrations in Rust source files and provides code completion and reference resolution for them.

### Supported Patterns

The plugin parses Rust files for these registration patterns:

```rust
// Function registrations
engine.register_fn("my_function", my_rust_fn);
engine.register_fn(CONST_NAME, my_rust_fn);  // Constant-based names

// Type registrations
engine.register_type::<MyType>();
engine.register_type_with_name::<MyType>("CustomName");

// Property getters/setters
engine.register_get("prop", getter_fn);
engine.register_set("prop", setter_fn);
engine.register_get_set("prop", getter_fn, setter_fn);

// Module registrations
module.set_fn("function_name", func);
module.set_var("variable_name", value);
```

### Registry Settings

Configure the registry at **Settings > Languages & Frameworks > Rhai Custom Registry**:

#### Project Registry Tab
- Add project-specific functions, variables, and types manually
- These are only available in the current project

#### Global Registry Tab
- Add functions, variables, and types available in ALL projects
- Useful for common Rhai engine extensions you use everywhere

#### Auto Registry Tab
- Enable/disable automatic Rust file scanning
- **Destination setting**: Choose where to save auto-detected registrations:
  - **Project registry** - Only available in the current project (default)
  - **Global registry** - Available across all projects
- Configure include/exclude patterns for file scanning
- View statistics of auto-detected registrations
- Click "Rescan Now" to refresh the registry

### Adding Registrations Manually

1. Open a Rust file with Rhai registrations
2. Right-click on a line with a registration (e.g., `engine.register_fn(...)`)
3. Select **Add to Rhai Registry**
4. Choose scope: **Global** (all projects) or **Project** (current project only)

## Live Templates

Type the following abbreviations and press Tab to expand:

| Abbreviation | Expands to |
|-------------|-----------|
| `fn` | Function definition |
| `pubfn` | Public function definition |
| `let` | Variable declaration |
| `const` | Constant declaration |
| `pubconst` | Public constant declaration |
| `if` | If statement |
| `ife` | If-else statement |
| `for` | For loop |
| `fori` | For loop with index (0..n) |
| `forr` | For loop with range |
| `while` | While loop |
| `loop` | Infinite loop |
| `match` | Switch expression |
| `trye` | Try-catch block |
| `closure` | Closure expression |
| `mape` | Map expression |
| `filtere` | Filter expression |
| `reducee` | Reduce expression |
| `guard` | Guard clause (if-return pattern) |
| `doc` | Documentation comment |

## File Templates

Create new Rhai files with predefined templates:
1. Right-click on a folder in Project view
2. Select **New > Rhai Script**

## Code Style

Configure Rhai code style settings:
**Settings > Editor > Code Style > Rhai**

### Spacing Options
- Spaces around operators (assignment, logical, equality, relational, etc.)
- Spaces around range and arrow operators
- Spaces after/before commas, colons, semicolons
- Spaces within parentheses, brackets, braces
- Spaces within closure pipes

### Brace & Wrapping Options
- Space before function/if/loop/switch left brace
- Align multiline parameters and arguments
- Align multiline array elements and object fields
- Keep trailing commas

### Indentation Options
- Indent size (default: 4)
- Tab size (default: 4)
- Continuation indent size (default: 4)
- Use tabs or spaces

### Blank Lines
- Blank lines after imports
- Blank lines around functions
- Blank lines around modules

## Requirements

- IntelliJ IDEA 2023.1+ (or compatible JetBrains IDE)
- Java 17+
- For running scripts: Rhai interpreter (`cargo install rhai-repl`)

## Building from Source

```bash
# Clone the repository
git clone https://github.com/GlebMikhailov/rhai-jetbrains-plugin.git
cd rhai-highlight-plugin

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

# Verify plugin compatibility
./gradlew verifyPlugin
```

### Project Structure

```
src/
├── main/
│   ├── grammars/           # BNF grammar and JFlex lexer
│   ├── kotlin/org/rhai/
│   │   ├── features/       # Completion, folding, structure view, etc.
│   │   ├── highlighting/   # Syntax highlighter
│   │   ├── inspections/    # Code inspections and quick fixes
│   │   ├── intentions/     # Intention actions
│   │   ├── lang/           # Language and file type definitions
│   │   ├── navigation/     # Go to declaration
│   │   ├── parser/         # Parser definition
│   │   ├── refactoring/    # Rename refactoring
│   │   ├── registry/       # Rust integration registry
│   │   ├── run/            # Run configuration
│   │   ├── settings/       # Settings pages
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── META-INF/       # Plugin descriptor
│       ├── icons/          # Plugin icons
│       └── liveTemplates/  # Live template definitions
└── test/
    └── kotlin/org/rhai/    # Unit tests
```

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run `./gradlew test` to ensure tests pass
5. Run `./gradlew detekt` to check code style
6. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Rhai](https://rhai.rs/) - The embedded scripting language for Rust
- [JetBrains Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html) - Plugin development documentation
