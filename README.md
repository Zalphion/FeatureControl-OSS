[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)
[![Reddit](https://img.shields.io/badge/featurecontrol-%23FF4500.svg?style=for-the-badge&logo=Reddit&logoColor=white)](https://reddit.com/r/featurecontrol)

# Feature Control - OSS

This is the fully FOSS (Free and Open Source Software) core of the Feature Control project.

> [!WARNING]
> **Work in Progress**: This project is still under active development and is not yet ready for production use.

## Modules

- **core**: Core business logic, repository interfaces, and HTTP routes (no HTTP server or database included)
- **emails**: Plugin: Intercept events to send emails over the given transport (not included)
- **hosted**: A fully deployable version of Feature Control using Undertow, Postgres, and Javax Mail
