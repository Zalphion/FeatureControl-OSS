[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)
[![Reddit](https://img.shields.io/badge/featurecontrol-%23FF4500.svg?style=for-the-badge&logo=Reddit&logoColor=white)](https://reddit.com/r/featurecontrol)

# Feature Control

> [!WARNING]
> **Work in Progress**: This project is still under active development and is not yet ready for production use.

## Modules

- **core**: Core web interface, SDK protocol, and plugin system
- **emails**: A plugin to send email notifications over the configured transport
- **hosted**: The official self-hosted distribution of Feature Control OSS
- **storage**: Bring your own storage backend with a wide variety of integrations
  - **postgres**: The ubiquitous relational database
  - **mariadb**: The FOSS continuation of MySQL; retaining high compatibility with its proprietary parent
  - **h2**: An embedded database stored on disk; suitable for single-node deployments or testing
  - **couchdb**: A highly scalable document database for NoSQL aficionados (smallest artifact size)