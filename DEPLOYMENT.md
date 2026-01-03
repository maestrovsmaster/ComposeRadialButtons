# Deployment Guide - Publishing to JitPack

This guide explains how to publish the **Compose Radial Buttons** library to JitPack and use it in other projects.

## Prerequisites

- GitHub account
- Git installed on your machine
- This project ready to be pushed

## Step 1: Prepare Your Repository

### 1.1 Update Configuration Files

Before publishing, update the following placeholders in your code:

**In `radialbuttons/build.gradle.kts`:**
```kotlin
groupId = "com.github.YOURUSERNAME"  // Replace YOURUSERNAME with your GitHub username
```

**In `README.md`:**
- Replace `yourusername` with your actual GitHub username in all badge links and installation instructions
- Update the screenshot/demo image path if needed
- Update copyright year and name in the License section

### 1.2 Verify Build

Test that everything builds correctly:

```bash
./gradlew clean
./gradlew :radialbuttons:assembleRelease
./gradlew :app:assembleDebug
```

All builds should complete successfully.

## Step 2: Create GitHub Repository

### 2.1 Create New Repository

1. Go to [GitHub](https://github.com)
2. Click "New Repository" or use the "+" menu
3. Repository name: `ComposeRadialButtons` (or your preferred name)
4. Description: "A circular/radial button layout library for Jetpack Compose"
5. Choose: **Public** (required for free JitPack usage)
6. **Do NOT** initialize with README, .gitignore, or license (we already have these)
7. Click "Create repository"

### 2.2 Connect Local Project to GitHub

```bash
# Initialize git if not already done (already done in your project)
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: Compose Radial Buttons library"

# Add remote origin (replace YOURUSERNAME and REPO_NAME)
git remote add origin https://github.com/YOURUSERNAME/ComposeRadialButtons.git

# Push to GitHub
git branch -M main
git push -u origin main
```

## Step 3: Create a Release Tag

JitPack builds your library based on Git tags. Create your first release:

```bash
# Create and push a version tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

**Version Naming Conventions:**
- Use semantic versioning: `vMAJOR.MINOR.PATCH`
- Examples: `v1.0.0`, `v1.0.1`, `v1.1.0`, `v2.0.0`
- Always prefix with `v`

## Step 4: Trigger JitPack Build

### 4.1 Visit JitPack

1. Go to [https://jitpack.io](https://jitpack.io)
2. Enter your repository URL: `https://github.com/YOURUSERNAME/ComposeRadialButtons`
3. Click "Look up"

### 4.2 Build the Release

1. Find your `v1.0.0` tag in the list
2. Click "Get it" button next to the version
3. JitPack will start building your library
4. Wait for the build to complete (green checkmark ✓)
5. Build logs are available if you need to debug

### 4.3 Check Build Status

- **Green checkmark (✓)**: Build successful - ready to use!
- **Red X (✗)**: Build failed - click "Log" to see error details
- **Clock icon**: Build in progress - wait a few minutes

## Step 5: Use in Other Projects

### 5.1 Add JitPack Repository

In your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Add this
    }
}
```

### 5.2 Add Dependency

In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.YOURUSERNAME:ComposeRadialButtons:v1.0.0")
}
```

### 5.3 Sync and Use

1. Click "Sync Now" in Android Studio
2. Wait for sync to complete
3. Start using the library:

```kotlin
import com.radialbuttons.circularbuttonlayout.*
import com.radialbuttons.circularbuttonlayout.data.*

@Composable
fun MyApp() {
    EnhancedCircularButtonLayout(
        leftButtons = listOf(/* ... */),
        rightButtons = listOf(/* ... */),
        centerLabel = "MENU"
    )
}
```

## Step 6: Publishing New Versions

When you make changes and want to release a new version:

```bash
# 1. Commit your changes
git add .
git commit -m "Add new feature X"
git push

# 2. Create a new version tag
git tag -a v1.0.1 -m "Release version 1.0.1 - Bug fixes"
git push origin v1.0.1

# 3. JitPack will automatically build the new version
# Visit jitpack.io to verify the build succeeded
```

## Troubleshooting

### Build Fails on JitPack

**Check the build log:**
1. Click "Log" next to the failed version on JitPack
2. Look for error messages
3. Common issues:
   - Missing dependencies
   - Incorrect Gradle configuration
   - JDK version mismatch

**Solutions:**
- Ensure `jitpack.yml` is in the root directory
- Verify `build.gradle.kts` has correct maven-publish configuration
- Check that the library builds locally: `./gradlew :radialbuttons:assembleRelease`

### Dependency Not Resolving

1. Check JitPack build status (should be green ✓)
2. Verify you added `maven { url = uri("https://jitpack.io") }` to repositories
3. Ensure version tag matches exactly (including the `v` prefix)
4. Try invalidating caches: File → Invalidate Caches → Invalidate and Restart

### Wrong GitHub Username

If you used the wrong username:
1. Update `radialbuttons/build.gradle.kts` with correct `groupId`
2. Commit and push changes
3. Create a new tag: `git tag -a v1.0.1 -m "Fix groupId"`
4. Push tag: `git push origin v1.0.1`

## Best Practices

### Version Numbers
- **Patch** (v1.0.1): Bug fixes, minor changes
- **Minor** (v1.1.0): New features, backward compatible
- **Major** (v2.0.0): Breaking changes

### Commit Messages
Use clear, descriptive commit messages:
```bash
git commit -m "Add ripple animation to center button"
git commit -m "Fix touch detection on side buttons"
git commit -m "Update README with new examples"
```

### Testing Before Release
Always test locally before creating a release tag:
```bash
./gradlew clean
./gradlew :radialbuttons:assembleRelease
./gradlew :radialbuttons:publishToMavenLocal

# Then test in another project using mavenLocal()
```

## Additional Resources

- [JitPack Documentation](https://jitpack.io/docs/)
- [Semantic Versioning](https://semver.org/)
- [GitHub Releases Guide](https://docs.github.com/en/repositories/releasing-projects-on-github)

## Quick Reference

### Common Commands

```bash
# Build library
./gradlew :radialbuttons:assembleRelease

# Create and push tag
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0

# Delete tag (if needed)
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0

# List all tags
git tag -l
```

### JitPack Badge

Add this to your README.md:
```markdown
[![](https://jitpack.io/v/YOURUSERNAME/ComposeRadialButtons.svg)](https://jitpack.io/#YOURUSERNAME/ComposeRadialButtons)
```

---

**Ready to publish?** Follow the steps above and your library will be available on JitPack in minutes! 🚀
