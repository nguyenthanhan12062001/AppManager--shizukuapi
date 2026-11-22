# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.0.0   | :white_check_mark: |

## Reporting a Vulnerability

We take the security of Shizuku Manager seriously. If you discover a security
vulnerability, please report it responsibly.

### How to Report

1. **Do not** create a public GitHub issue for security vulnerabilities.
2. Send details to the project maintainer through GitHub's private vulnerability
   reporting feature (if available), or contact us directly.
3. Include as much detail as possible:
   - Description of the vulnerability
   - Steps to reproduce
   - Affected versions
   - Potential impact
   - Suggested fix (if any)

### What to Expect

- We will acknowledge receipt of your report within 7 days.
- We will provide an initial assessment within 14 days.
- We will keep you informed of progress toward a fix.
- Public disclosure will be coordinated with you after a fix is available.

## Security Practices

- No private credentials, keystore files, or signing passwords are stored in
  this repository.
- Release signing uses `signing.properties`, which is listed in `.gitignore`.
- Debug builds use the default Android debug keystore.
- The app only executes `pm`, `am`, and `cmd` commands through Shizuku's
  shell service, with a 15-second timeout and disallowed command filtering.
- Critical system packages (Android, Settings, PackageInstaller, Shizuku, etc.)
  are protected by `PackageSafetyGuard` and cannot be suspended or disabled.

## Disclosure Policy

We follow responsible disclosure. Security vulnerabilities will not be publicly
disclosed until a fix has been released and users have had reasonable time to
update.
