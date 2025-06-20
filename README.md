# Neo4J Driver Module for Ignition

A modern Neo4J integration module for Inductive Automation's Ignition platform, providing seamless graph database connectivity with support for the latest Neo4J 5.x drivers.

## Features

- ✨ **Modern Neo4J Support** - Built for Neo4J 5.14.0 with backwards compatibility
- 🚀 **Ignition 8.1.44+** - Leverages latest platform features and APIs  
- 📦 **Easy Configuration** - Simple connection management through Gateway interface
- 🔧 **Scripting Support** - Comprehensive `system.neo4j` scripting functions
- 🐳 **Docker Development** - Complete development environment included
- 🏗️ **Modern Build System** - Gradle 8.7 with Kotlin DSL

## Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose (for development)
- Ignition 8.1.44+

### Development Setup

1. **Clone and build the module:**
   ```bash
   git clone <repository-url>
   cd neo4j-driver
   ./gradlew build
   ```

2. **Start the development environment:**
   ```bash
   docker-compose up -d
   ```

3. **Access the services:**
   - **Ignition Gateway**: http://localhost:8088 (admin/password)
   - **Neo4J Browser**: http://localhost:7474 (neo4j/password123)

4. **Deploy the module:**
   ```bash
   ./gradlew deployModl
   ```

### Manual Installation

1. Build the module: `./gradlew build`
2. Install `build/distributions/Neo4J-Driver.modl` through the Gateway config page
3. Configure your Neo4J connections in the Gateway

## Configuration

### Creating a Neo4J Connection

1. Navigate to **Gateway Config → Neo4J → Connections**
2. Click **Create New Connection**
3. Configure your connection:
   - **Name**: Connection identifier
   - **URI**: Neo4J connection string (e.g., `bolt://localhost:7687`)
   - **Username/Password**: Authentication credentials
   - **Database**: Target database name (optional)

### Connection Settings

| Setting | Description | Default |
|---------|-------------|---------|
| Max Connection Pool Size | Maximum concurrent connections | 8 |
| Slow Query Threshold | Log queries exceeding this time (ms) | 60000 |
| Validation Timeout | Connection validation timeout (ms) | 10000 |

## Scripting API

The module provides comprehensive scripting functions under `system.neo4j`:

### Query Functions

```python
# Execute a read query
result = system.neo4j.selectQuery(
    query="MATCH (n:Person) RETURN n.name as name",
    params={"limit": 10},
    database="myconnection"
)

# Execute a write query  
system.neo4j.updateQuery(
    query="CREATE (n:Person {name: $name})",
    params={"name": "John Doe"},
    database="myconnection"
)
```

### Connection Management

```python
# Get list of configured connections
connections = system.neo4j.getConnections()
print("Available connections:", connections)
```

### Parameters

All query functions support these parameters:

- `query` (str): Cypher query to execute
- `params` (dict, optional): Query parameters  
- `database` (str, optional): Connection name (uses project default if not specified)
- `project` (str, optional): Project name for default connection lookup

## Project Structure

```
neo4j-driver/
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Project structure  
├── gradle/
│   └── libs.versions.toml        # Dependency version catalog
├── common/                       # Shared code (Gateway + Designer + Client)
├── gateway/                      # Gateway-specific code and Neo4J driver
├── client/                       # Vision Client-specific code
├── designer/                     # Designer-specific code
├── docker-compose.yml            # Development environment
└── README.md                     # This file
```

## Architecture

### Scopes

- **Gateway (G)**: Core driver functionality, connection management, scripting RPC
- **Designer (D)**: Configuration UI, connection editors, property panels  
- **Client (C)**: Vision Client scripting support
- **Common (GCD)**: Shared interfaces, data structures, constants

### Key Components

- **DatabaseConnector**: Core Neo4J driver wrapper with connection pooling
- **ScriptingFunctions**: RPC interface for cross-scope scripting support
- **DatabaseRecord**: Persistent storage for connection configurations
- **Property Editors**: Designer UI for connection management

## Development

### Building

```bash
# Build all modules
./gradlew build

# Clean build (removes all artifacts)  
./gradlew deepClean build

# Deploy to local Ignition
./gradlew deployModl
```

### Docker Development

The included Docker Compose stack provides:

- **Neo4J 5.14.0**: Graph database with APOC plugins
- **Ignition 8.1.44**: Platform for module development
- **Volume mounts**: For hot-reloading built modules

Start the stack:
```bash
docker-compose up -d
```

View logs:
```bash
docker-compose logs -f ignition
docker-compose logs -f neo4j
```

### Code Style

- **Java 17** language features encouraged
- **Modern patterns**: Use Optional, try-with-resources, etc.
- **Documentation**: JavaDoc for public APIs
- **Error handling**: Comprehensive exception handling with proper logging

## Migration from 0.2.x

### Breaking Changes

⚠️ **Module ID Changed**: `dev.bwdesigngroup.neo4j.Neo4JDriver` → `dev.bwdesigngroup.neo4j.Neo4JDriver`

### Migration Steps

1. **Export configurations** from existing module
2. **Uninstall** old module version
3. **Install** new module version  
4. **Re-import** configurations
5. **Test** all existing functionality

### API Compatibility

All scripting functions remain compatible. No script changes required.

## Troubleshooting

### Common Issues

**Connection Failed**
- Verify Neo4J service is running
- Check connection URI format (`bolt://host:port`)
- Validate credentials and database name

**Module Won't Load**
- Ensure Java 17+ is configured
- Check Gateway logs for detailed error messages
- Verify module file integrity

**Slow Performance**
- Review connection pool settings
- Monitor slow query logs
- Consider query optimization

### Logging

Module logs are available in Gateway logs under:
- `dev.bwdesigngroup.neo4j.gateway.*`
- `dev.bwdesigngroup.neo4j.client.*`  
- `dev.bwdesigngroup.neo4j.designer.*`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

MIT License - see LICENSE file for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/bwdesigngroup/neo4j-driver/issues)
- **Documentation**: [Wiki](https://github.com/bwdesigngroup/neo4j-driver/wiki)
- **Community**: [Ignition Exchange](https://inductiveautomation.com/exchange)