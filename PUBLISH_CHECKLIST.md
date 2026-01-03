# Quick Publish Checklist

Follow these steps to publish your library to JitPack.

## Pre-Publish Setup

- [ ] **Update `radialbuttons/build.gradle.kts`**
  - Line 70: Replace `yourusername` with your GitHub username
  ```kotlin
  groupId = "com.github.YOURUSERNAME"
  ```

- [ ] **Update `README.md`**
  - Replace all instances of `yourusername` with your GitHub username
  - Update copyright name: "Copyright (c) 2026 [Your Name]"
  - Verify badge URLs point to your repository

- [ ] **Test local build**
  ```bash
  ./gradlew clean
  ./gradlew :radialbuttons:assembleRelease
  ```
  Should complete successfully ✓

## GitHub Repository Setup

- [ ] **Create GitHub repository**
  - Name: `ComposeRadialButtons` (or your choice)
  - Visibility: **Public** (required for free JitPack)
  - Do NOT initialize with README/License

- [ ] **Connect local project**
  ```bash
  git remote add origin https://github.com/YOURUSERNAME/ComposeRadialButtons.git
  git push -u origin main
  ```

- [ ] **Create release tag**
  ```bash
  git tag -a v1.0.0 -m "Initial release - Compose Radial Buttons"
  git push origin v1.0.0
  ```

## JitPack Publication

- [ ] **Visit JitPack**
  - Go to: https://jitpack.io
  - Enter: `https://github.com/YOURUSERNAME/ComposeRadialButtons`
  - Click "Look up"

- [ ] **Build release**
  - Find `v1.0.0` in the list
  - Click "Get it"
  - Wait for green checkmark ✓
  - If red X, click "Log" to see errors

## Verification

- [ ] **Test in another project**

  **settings.gradle.kts:**
  ```kotlin
  repositories {
      google()
      mavenCentral()
      maven { url = uri("https://jitpack.io") }
  }
  ```

  **build.gradle.kts:**
  ```kotlin
  dependencies {
      implementation("com.github.YOURUSERNAME:ComposeRadialButtons:v1.0.0")
  }
  ```

- [ ] **Sync and import**
  ```kotlin
  import com.radialbuttons.circularbuttonlayout.*
  ```

- [ ] **Use the library**
  ```kotlin
  EnhancedCircularButtonLayout(
      leftButtons = listOf(/* ... */),
      rightButtons = listOf(/* ... */),
      centerLabel = "MENU"
  )
  ```

## Post-Publication

- [ ] **Update repository badges** in README
- [ ] **Share your library** 🎉
- [ ] **Monitor issues** on GitHub

## Quick Commands Reference

```bash
# Verify build
./gradlew :radialbuttons:assembleRelease

# Push to GitHub
git push

# Create new release
git tag -a v1.0.1 -m "Version 1.0.1 description"
git push origin v1.0.1

# Delete tag (if needed)
git tag -d v1.0.1
git push origin :refs/tags/v1.0.1
```

## Need Help?

- See `DEPLOYMENT.md` for detailed instructions
- Check JitPack logs if build fails
- Verify all placeholders are updated

---

**Ready?** Start with the Pre-Publish Setup section! ✅
