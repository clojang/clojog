# Publishing clojog to Maven Central

This guide walks you through the process of publishing clojog to Maven Central.

## Prerequisites

Before you can publish to Maven Central, you need to set up several accounts and tools.

### 1. Sonatype Account

1. **Create an account** at [Central Portal](https://central.sonatype.com/register)
2. **Request namespace** for `io.github.clojang`:
   - Go to [Namespaces](https://central.sonatype.com/namespaces)
   - Click "Add Namespace"
   - Enter `io.github.clojang`
   - Verify GitHub ownership by creating a repository at `https://github.com/clojang/OSSRH-XXXXX` (they'll provide the ticket number)

### 2. GPG Key Setup

```bash
# Generate a new GPG key (if you don't have one)
gpg --gen-key

# List your keys to get the key ID
gpg --list-secret-keys --keyid-format=long

# Upload your public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export your private key (for GitHub Actions)
gpg --armor --export-secret-keys YOUR_KEY_ID
```

### 3. GitHub Repository Secrets

Add these secrets to your GitHub repository at `Settings` > `Secrets and variables` > `Actions`:

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `MAVEN_CENTRAL_USERNAME` | Your Sonatype username | From Central Portal account |
| `MAVEN_CENTRAL_TOKEN` | Your Sonatype token | Generate in Central Portal profile |
| `GPG_PRIVATE_KEY` | Your GPG private key | Output of `gpg --armor --export-secret-keys YOUR_KEY_ID` |
| `GPG_PASSPHRASE` | Your GPG key passphrase | The passphrase you used when creating the key |

Optional secrets for additional integrations:

| Secret Name | Description |
|-------------|-------------|
| `SONAR_TOKEN` | For SonarCloud code quality analysis |
| `CODECOV_TOKEN` | For code coverage reporting |

## Publishing Process

### Automated Publishing (Recommended)

The project is configured for automated publishing via GitHub Actions.

#### 1. Release via Git Tag

```bash
# Make sure you're on main branch and everything is committed
git checkout main
git pull origin main

# Create and push a version tag
git tag v0.1.1
git push origin v0.1.1
```

This will automatically:
- ✅ Run all tests
- ✅ Build artifacts (JAR, sources, javadoc)
- ✅ Sign artifacts with GPG
- ✅ Publish to Maven Central
- ✅ Create a GitHub Release
- ✅ Update documentation

#### 2. Manual Release via GitHub UI

1. Go to **Actions** tab in your GitHub repository
2. Select **Release** workflow
3. Click **Run workflow**
4. Enter the version number (e.g., `0.1.1`)
5. Click **Run workflow**

### Manual Publishing (Advanced)

If you need to publish manually from your local machine:

#### 1. Set up Maven Settings

Create or update `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_TOKEN</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>gpg</id>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.keyname>YOUR_GPG_KEY_ID</gpg.keyname>
      </properties>
    </profile>
  </profiles>
</settings>
```

#### 2. Update Version

```bash
# Update version in pom.xml
mvn versions:set -DnewVersion=0.1.1 -DgenerateBackupPoms=false

# Commit the version change
git add pom.xml
git commit -m "Release version 0.1.1"
git tag v0.1.1
```

#### 3. Deploy to Maven Central

```bash
# Build and deploy with GPG signing
mvn clean deploy -Dgpg.sign=true

# Push the tag
git push origin v0.1.1
```

#### 4. Update to Next SNAPSHOT

```bash
# Prepare for next development iteration
mvn versions:set -DnewVersion=0.1.2-SNAPSHOT -DgenerateBackupPoms=false
git add pom.xml
git commit -m "Prepare for next development iteration"
git push origin main
```

## Version Management

### Semantic Versioning

clojog follows [Semantic Versioning](https://semver.org/):

- **MAJOR** version: incompatible API changes
- **MINOR** version: backwards-compatible functionality additions
- **PATCH** version: backwards-compatible bug fixes

Examples:
- `0.1.0` → `0.1.1` (bug fix)
- `0.1.1` → `0.2.0` (new feature)
- `0.2.0` → `1.0.0` (breaking change)

### Release Types

| Version Pattern | Description | Example |
|----------------|-------------|---------|
| `x.y.z` | Stable release | `0.1.0` |
| `x.y.z-SNAPSHOT` | Development version | `0.1.1-SNAPSHOT` |
| `x.y.z-alpha` | Alpha release | `0.2.0-alpha` |
| `x.y.z-beta` | Beta release | `0.2.0-beta` |
| `x.y.z-rc` | Release candidate | `0.2.0-rc` |

## Verification

### After Publishing

1. **Check Maven Central**: Your artifact should appear at:
   - https://central.sonatype.com/artifact/io.github.clojang/clojog

2. **Test the published artifact**:
   ```xml
   <dependency>
       <groupId>io.github.clojang</groupId>
       <artifactId>clojog</artifactId>
       <version>YOUR_VERSION</version>
   </dependency>
   ```

3. **Verify signatures**:
   ```bash
   # Download and verify the signature
   gpg --verify clojog-0.1.0.jar.asc clojog-0.1.0.jar
   ```

### Troubleshooting

#### Common Issues

1. **GPG Signing Fails**
   ```bash
   # Make sure GPG agent is running
   gpgconf --kill gpg-agent
   gpg-agent --daemon
   
   # Test signing
   echo "test" | gpg --clearsign
   ```

2. **Sonatype Authentication Issues**
   - Verify your token is correct
   - Check if your namespace is approved
   - Ensure your account has publishing permissions

3. **Build Failures**
   ```bash
   # Clean build
   mvn clean install
   
   # Check for dependency issues
   mvn dependency:tree
   ```

4. **Missing Required Metadata**
   - Ensure POM has all required fields (name, description, url, licenses, developers, scm)
   - Check that sources and javadoc JARs are generated

## Release Checklist

Before each release:

- [ ] All tests pass: `mvn clean test`
- [ ] Code quality checks pass: `mvn clean verify`
- [ ] Version updated in `pom.xml`
- [ ] `CHANGELOG.md` updated (if you create one)
- [ ] Documentation is current
- [ ] GitHub repository secrets are configured
- [ ] GPG key is valid and uploaded to keyserver

After release:

- [ ] Verify artifact appears on Maven Central
- [ ] Test artifact in a separate project
- [ ] GitHub Release created with release notes
- [ ] Documentation website updated (if applicable)
- [ ] Announce release (if applicable)

## Contact

If you encounter issues with publishing:

1. Check the [Central Portal Documentation](https://central.sonatype.org/publish/)
2. Review [GitHub Actions logs](https://github.com/clojang/clojog/actions)
3. Open an issue in the [clojog repository](https://github.com/clojang/clojog/issues)

## References

- [Maven Central Publisher Documentation](https://central.sonatype.org/publish/)
- [GPG Documentation](https://gnupg.org/documentation/)
- [Semantic Versioning](https://semver.org/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)