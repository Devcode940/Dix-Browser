# Contributing to Dix Browser

Thank you for your interest in contributing to **Dix Browser**! We welcome contributions from the community.

---

## How to Contribute

### 1. Reporting Issues

Before creating a new issue, please:

- Search existing issues to avoid duplicates
- Use the issue templates when available
- Provide as much detail as possible (steps to reproduce, expected vs actual behavior, device info, logs)

### 2. Suggesting Features

We love new ideas! Please open a feature request with:

- Clear description of the feature
- Why it would be useful
- Any related issues or references

### 3. Code Contributions

#### Getting Started

1. **Fork** the repository
2. **Clone** your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/DixBrowser.git
   cd DixBrowser
   ```
3. Create a new branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```

#### Development Guidelines

- Follow the existing **Kotlin code style**
- Use **ViewBinding** instead of `findViewById`
- Prefer **Kotlin** over Java for new code
- Write **unit tests** for new features when possible
- Keep commits small and focused

#### Commit Messages

Use clear and descriptive commit messages:

```
feat: add incognito mode toggle
fix: resolve download progress not updating
docs: update README with new features
```

#### Pull Request Process

1. Push your branch to your fork
2. Open a **Pull Request** against the `master` branch
3. Ensure all checks pass
4. Request a review if needed
5. Be responsive to feedback

---

## Project Structure

Please follow the existing folder structure:

```
ui/
├── address/       # Address bar logic
├── browser/       # Main browser components
├── download/      # Download manager
├── history/       # History features
├── incognito/     # Private browsing
├── settings/      # Settings
├── theme/         # Theming (Dark Mode)
└── adblock/       # Ad blocking
```

---

## Coding Standards

- Use **Kotlin** for new features
- Follow **MVVM** architecture
- Use **StateFlow** / **LiveData** for state
- Add **documentation** for public methods
- Keep methods small and focused

---

## Testing

- Add tests for new functionality
- Run `./gradlew test` before submitting PRs
- Ensure no regressions in existing features

---

## Questions?

Feel free to open a discussion or reach out via GitHub Issues.

---

Thank you for helping make **Dix Browser** better! 🚀