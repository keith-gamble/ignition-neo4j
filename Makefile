# Makefile for Neo4J Driver Ignition Module
# Generated for multi-module Gradle project with Docker support

# Variables
GRADLE := ./gradlew
MODULE_NAME := Neo4J-Driver
VERSION := 0.3.1
MODL_FILE := build/$(MODULE_NAME).modl
DOCKER_COMPOSE := docker-compose -f docker/docker-compose.yaml

# Default target
.PHONY: all
all: build

# Build all modules
.PHONY: build
build:
	$(GRADLE) build

# Build the module file (.modl)
.PHONY: module
module:
	$(GRADLE) assemble
	@echo "Module file generated at: $(MODL_FILE)"

# Run tests
.PHONY: test
test:
	$(GRADLE) test

# Clean build artifacts
.PHONY: clean
clean:
	$(GRADLE) clean eclipse
	rm -rf build

# Deep clean (including Gradle caches and node_modules)
.PHONY: deep-clean
deep-clean:
	$(GRADLE) deepClean eclipse
	rm -rf .gradle node_modules

# Install dependencies for web module
.PHONY: install-web
install-web:
	$(GRADLE) :web:installDependencies

# Build web resources
.PHONY: build-web
build-web: install-web
	$(GRADLE) :web:webpack

# Deploy module to Ignition gateway
.PHONY: deploy
deploy:
	$(GRADLE) deployModl

# Check if Docker containers are running
.PHONY: check-containers
check-containers:
	@echo "Checking Docker container status..."
	@if $(DOCKER_COMPOSE) ps | grep -q "Up"; then \
		echo "Containers are running"; \
		exit 0; \
	else \
		echo "Containers are not running"; \
		exit 1; \
	fi

# Start Docker services (Neo4J and Ignition), build and deploy if already running
.PHONY: docker-up
docker-up:
	@echo "Attempting to start Docker services..."
	@if $(MAKE) check-containers > /dev/null 2>&1; then \
		echo "Containers already running, building and deploying module..."; \
		$(MAKE) module deploy; \
	else \
		$(DOCKER_COMPOSE) up -d; \
		echo "Docker services started. Neo4J available at http://localhost:7474"; \
		echo "Ignition gateway available at https://neo4j-dev.localtest.me"; \
	fi

# Stop Docker services
.PHONY: docker-down
docker-down:
	$(DOCKER_COMPOSE) down
	@echo "Docker services stopped"

# View Docker logs
.PHONY: docker-logs
docker-logs:
	$(DOCKER_COMPOSE) logs

# Build and start Docker services
.PHONY: docker-build
docker-build: module
	$(DOCKER_COMPOSE) up -d --build
	@echo "Docker services built and started"

# Stop and remove Docker services and volumes
.PHONY: docker-clean
docker-clean:
	$(DOCKER_COMPOSE) down -v
	@echo "Docker services and volumes removed"

# Run a full build, test, and deploy cycle
.PHONY: full-cycle
full-cycle: clean build test deploy
	@echo "Full build cycle completed"

# Display help
.PHONY: help
help:
	@echo "Neo4J Driver Module Makefile"
	@echo ""
	@echo "Available targets:"
	@echo "  all          - Build all modules (default)"
	@echo "  build        - Build all modules"
	@echo "  module       - Build the .modl file"
	@echo "  test         - Run tests"
	@echo "  clean        - Clean build artifacts"
	@echo "  deep-clean   - Clean build artifacts, Gradle caches, and node_modules"
	@echo "  install-web  - Install web module dependencies"
	@echo "  build-web    - Build web module resources"
	@echo "  deploy       - Deploy module to Ignition gateway"
	@echo "  docker-up    - Start Docker services, build and deploy if already running"
	@echo "  docker-down  - Stop Docker services"
	@echo "  docker-logs  - View Docker logs"
	@echo "  docker-build - Build module and start Docker services"
	@echo "  docker-clean - Stop and remove Docker services and volumes"
	@echo "  full-cycle   - Run clean, build, test, and deploy"
	@echo "  help         - Display this help message"