[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Feature Control - OSS

This is the fully FOSS (Free and Open Source Software) repository for the Feature Control project. It contains only open-source components and adheres to the principles of FOSS.

## Modules

- **core**: Core business logic, repository interfaces, and HTTP routes (no HTTP server or database included)
- **emails**: Plugin: Intercept events to send emails over the given transport (not included)
- **hosted**: A fully deployable version of Feature Control using Undertow, Postgres, and Javax Mail
