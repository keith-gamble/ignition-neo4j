.
├── client
│   ├── src
│   │   └── main
│   │       └── java
│   │           └── dev
│   │               └── bwdesigngroup
│   │                   └── neo4j
│   │                       └── client
│   │                           ├── Neo4JClientScriptModule.java
│   │                           └── Neo4JDriverClientHook.java
│   └── build.gradle.kts
├── common
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── dev
│   │       │       └── bwdesigngroup
│   │       │           └── neo4j
│   │       │               └── common
│   │       │                   ├── projectresources
│   │       │                   │   └── Neo4JProperties.java
│   │       │                   ├── scripting
│   │       │                   │   ├── ScriptingFunctions.java
│   │       │                   │   └── ScriptModule.java
│   │       │                   └── Neo4JDriverModule.java
│   │       └── resources
│   │           └── dev
│   │               └── bwdesigngroup
│   │                   └── neo4j
│   │                       └── common
│   │                           └── scripting
│   │                               └── ScriptModule.properties
│   └── build.gradle.kts
├── designer
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── dev
│   │       │       └── bwdesigngroup
│   │       │           └── neo4j
│   │       │               └── designer
│   │       │                   ├── components
│   │       │                   │   ├── DatabaseDropdown.java
│   │       │                   │   └── EditorCategory.java
│   │       │                   ├── editors
│   │       │                   │   └── GeneralPropertyEditor.java
│   │       │                   └── Neo4JDriverDesignerHook.java
│   │       └── resources
│   │           └── dev
│   │               └── bwdesigngroup
│   │                   └── neo4j
│   │                       └── designer
│   │                           └── editors
│   │                               └── GeneralPropertyEditor.properties
│   └── build.gradle.kts
├── docker
│   ├── backups
│   │   └── gateway.gwbk
│   └── docker-compose.yaml
├── gateway
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── dev
│   │       │       └── bwdesigngroup
│   │       │           └── neo4j
│   │       │               └── gateway
│   │       │                   ├── components
│   │       │                   │   ├── DatabaseConnector.java
│   │       │                   │   └── Neo4JConnectionManager.java
│   │       │                   ├── instances
│   │       │                   │   ├── Extendable.java
│   │       │                   │   └── RemoteDatabaseInstance.java
│   │       │                   ├── records
│   │       │                   │   ├── DatabaseRecord.java
│   │       │                   │   └── RemoteDatabaseRecord.java
│   │       │                   ├── web
│   │       │                   │   ├── Neo4JExtensionPage.java
│   │       │                   │   ├── Neo4JOverviewContributor.java
│   │       │                   │   └── Neo4JStatusRoutes.java
│   │       │                   ├── DatabaseRecordType.java
│   │       │                   ├── GatewayScriptModule.java
│   │       │                   └── Neo4JDriverGatewayHook.java
│   │       └── resources
│   │           └── dev
│   │               └── bwdesigngroup
│   │                   └── neo4j
│   │                       └── gateway
│   │                           ├── records
│   │                           │   ├── DatabaseRecord.properties
│   │                           │   └── RemoteDatabaseRecord.properties
│   │                           └── Neo4JDriverGatewayHook.properties
│   └── build.gradle.kts
├── gradle
│   ├── wrapper
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml
├── web
│   ├── src
│   │   ├── components
│   │   │   └── Neo4JStatus.tsx
│   │   ├── model
│   │   │   └── model.ts
│   │   ├── types
│   │   │   └── ignition-lib.d.ts
│   │   └── index.tsx
│   ├── build.gradle.kts
│   ├── eslint.config.mjs
│   ├── package.json
│   ├── tsconfig.json
│   └── webpack.config.ts
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── LICENSE.txt
├── README.md
├── settings.gradle.kts
└── temp.md
