# Package Migration Plan

**Current State**: Mixed package structure
- `com.devcode940.web.easybrowser.*` (legacy)
- `com.devcode940.web.*` (new modern code)

## Goal
Migrate everything to clean structure under `com.devcode940.web`

## Recommended Structure

```
com.devcode940.web/
├── core/
├── data/
├── domain/
├── ui/
├── web/
└── di/
```

## Migration Steps

1. Move all classes from `easybrowser/` to top level
2. Update all imports
3. Update `AndroidManifest.xml`
4. Delete empty `easybrowser` package

**Status**: In Progress
**Priority**: High (for maintainability)