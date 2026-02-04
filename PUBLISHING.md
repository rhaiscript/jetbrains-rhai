# Руководство по публикации плагина в JetBrains Marketplace

## Содержание
1. [Подготовка к публикации](#1-подготовка-к-публикации)
2. [Регистрация в JetBrains Marketplace](#2-регистрация-в-jetbrains-marketplace)
3. [Получение токена для публикации](#3-получение-токена-для-публикации)
4. [Ручная публикация](#4-ручная-публикация)
5. [Автоматическая публикация через GitHub Actions](#5-автоматическая-публикация-через-github-actions)
6. [Создание релиза](#6-создание-релиза)

---

## 1. Подготовка к публикации

### 1.1. Проверьте plugin.xml

Убедитесь, что файл `src/main/resources/META-INF/plugin.xml` содержит корректную информацию:

```xml
<idea-plugin>
    <id>org.rhai.rhai-highlight-plugin</id>
    <name>Rhai Language Support</name>
    <vendor email="your@email.com" url="https://github.com/yourusername">Your Name</vendor>
    <!-- ... -->
</idea-plugin>
```

### 1.2. Обновите версию в build.gradle.kts

```kotlin
version = "1.0.0"  // Используйте семантическое версионирование
```

### 1.3. Соберите плагин

```bash
./gradlew buildPlugin
```

Плагин будет создан в `build/distributions/rhai-highlight-plugin-1.0.0.zip`

---

## 2. Регистрация в JetBrains Marketplace

1. Перейдите на [JetBrains Marketplace](https://plugins.jetbrains.com/)
2. Нажмите **Sign In** в правом верхнем углу
3. Войдите через JetBrains Account (или создайте новый)
4. После входа перейдите в [Developer Portal](https://plugins.jetbrains.com/author/me)

---

## 3. Получение токена для публикации

### 3.1. Создание токена

1. Перейдите на https://plugins.jetbrains.com/author/me/tokens
2. Нажмите **Generate Token**
3. Введите имя токена (например, `github-actions`)
4. Скопируйте токен (он показывается только один раз!)

### 3.2. Сохранение токена

**Для локальной публикации:**
```bash
export PUBLISH_TOKEN="perm:xxxxxxxx..."
```

**Для GitHub Actions:**
1. Перейдите в репозиторий → Settings → Secrets and variables → Actions
2. Нажмите **New repository secret**
3. Name: `PUBLISH_TOKEN`
4. Value: ваш токен
5. Нажмите **Add secret**

---

## 4. Ручная публикация

### Первая публикация (через веб-интерфейс)

1. Перейдите на https://plugins.jetbrains.com/plugin/add
2. Загрузите ZIP-файл плагина из `build/distributions/`
3. Заполните информацию:
   - **License**: MIT
   - **Tags**: Rhai, Scripting, Language Support
   - **Source code URL**: ссылка на GitHub
4. Нажмите **Upload**
5. Дождитесь модерации (обычно 1-2 рабочих дня)

### Последующие обновления (через командную строку)

```bash
export PUBLISH_TOKEN="perm:xxxxxxxx..."
./gradlew publishPlugin
```

---

## 5. Автоматическая публикация через GitHub Actions

### 5.1. Создайте файл workflow

Создайте файл `.github/workflows/release.yml`:

```yaml
name: Release

on:
  release:
    types: [published]

jobs:
  release:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Extract version from tag
        id: version
        run: echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_OUTPUT

      - name: Update version in build.gradle.kts
        run: |
          sed -i "s/version = \".*\"/version = \"${{ steps.version.outputs.VERSION }}\"/" build.gradle.kts

      - name: Generate Lexer and Parser
        run: ./gradlew generateLexer generateParser

      - name: Build plugin
        run: ./gradlew buildPlugin

      - name: Run Tests
        run: ./gradlew test

      - name: Verify plugin
        run: ./gradlew verifyPlugin

      - name: Publish to JetBrains Marketplace
        env:
          PUBLISH_TOKEN: ${{ secrets.PUBLISH_TOKEN }}
        run: ./gradlew publishPlugin

      - name: Upload artifact to release
        uses: softprops/action-gh-release@v1
        with:
          files: build/distributions/*.zip
```

### 5.2. Создайте workflow для CI (проверка PR)

Создайте файл `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Generate Lexer and Parser
        run: ./gradlew generateLexer generateParser

      - name: Build
        run: ./gradlew buildPlugin

      - name: Run Tests
        run: ./gradlew test

      - name: Run Detekt
        run: ./gradlew detekt

      - name: Verify Plugin
        run: ./gradlew verifyPlugin

      - name: Upload build artifact
        uses: actions/upload-artifact@v4
        with:
          name: plugin-zip
          path: build/distributions/*.zip
```

---

## 6. Создание релиза

### 6.1. Через GitHub UI

1. Перейдите в репозиторий → Releases → **Create a new release**
2. Нажмите **Choose a tag**
3. Введите новый тег (например, `v1.0.0`) и нажмите **Create new tag**
4. **Release title**: `v1.0.0`
5. **Description**: опишите изменения
6. Нажмите **Publish release**

GitHub Actions автоматически:
- Соберёт плагин с новой версией
- Опубликует его в JetBrains Marketplace
- Прикрепит ZIP-файл к релизу

### 6.2. Через командную строку

```bash
# Убедитесь, что все изменения закоммичены
git add .
git commit -m "Release v1.0.0"

# Создайте тег
git tag v1.0.0

# Отправьте тег на GitHub
git push origin v1.0.0

# Создайте релиз через GitHub CLI
gh release create v1.0.0 --title "v1.0.0" --notes "Release notes here"
```

---

## Полезные команды

```bash
# Генерация лексера и парсера
./gradlew generateLexer generateParser

# Сборка плагина
./gradlew buildPlugin

# Запуск тестов
./gradlew test

# Проверка плагина
./gradlew verifyPlugin

# Запуск IDE с плагином (для тестирования)
./gradlew runIde

# Публикация (требует PUBLISH_TOKEN)
./gradlew publishPlugin

# Запуск линтера
./gradlew detekt
```

---

## Troubleshooting

### Ошибка "Plugin ID already exists"
Убедитесь, что `<id>` в plugin.xml уникален.

### Ошибка "Compatibility range"
Проверьте `sinceBuild` и `untilBuild` в `build.gradle.kts`:
```kotlin
patchPluginXml {
    sinceBuild.set("231")    // Минимальная версия IDE
    untilBuild.set("243.*")  // Максимальная версия IDE
}
```

### Модерация не проходит
- Убедитесь, что плагин работает корректно
- Проверьте, что нет вредоносного кода
- Добавьте подробное описание функционала

---

## Ссылки

- [JetBrains Marketplace](https://plugins.jetbrains.com/)
- [Plugin Development Guide](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Publishing Plugins](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html)
- [GitHub Actions for IntelliJ](https://github.com/JetBrains/intellij-platform-plugin-template)
