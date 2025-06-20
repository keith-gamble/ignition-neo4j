#!/bin/bash

# Migration script to update package structure from dev.bwdesigngroup.neo4j to dev.bwdesigngroup.neo4j
# This script will update all Java files, create new directory structure, and move files

set -e

echo "🚀 Starting Neo4J Module Migration..."
echo "📦 Updating package structure: dev.bwdesigngroup.neo4j → dev.bwdesigngroup.neo4j"

# Create new directory structure
create_directory_structure() {
    echo "📁 Creating new directory structure..."
    
    for module in common client designer gateway; do
        if [ -d "$module/src/main/java/com/bwdesigngroup" ]; then
            echo "  Creating $module/src/main/java/dev/bwdesigngroup/neo4j/"
            mkdir -p "$module/src/main/java/dev/bwdesigngroup/neo4j"
        fi
    done
}

# Update package declarations and imports in Java files
update_java_files() {
    echo "🔄 Updating Java package declarations and imports..."
    
    # Find all Java files and update package declarations
    find . -name "*.java" -type f | while read -r file; do
        echo "  Updating $file"
        
        # Update package declarations
        sed -i.bak 's/package com\.bwdesigngroup\.neo4j/package dev.bwdesigngroup.neo4j/g' "$file"
        
        # Update import statements
        sed -i.bak 's/import com\.bwdesigngroup\.neo4j/import dev.bwdesigngroup.neo4j/g' "$file"
        
        # Remove backup files
        rm -f "${file}.bak"
    done
}

# Move Java files to new directory structure
move_java_files() {
    echo "📦 Moving Java files to new directory structure..."
    
    for module in common client designer gateway; do
        old_path="$module/src/main/java/com/bwdesigngroup"
        new_path="$module/src/main/java/dev/bwdesigngroup"
        
        if [ -d "$old_path" ]; then
            echo "  Moving files from $old_path to $new_path"
            
            # Copy files to new location
            cp -r "$old_path/"* "$new_path/"
            
            # Remove old directory structure
            rm -rf "$old_path"
            
            # Clean up empty parent directories
            rmdir "$module/src/main/java/com" 2>/dev/null || true
        fi
    done
}

# Update resource files and property bundles
update_resource_files() {
    echo "🔧 Updating resource files and property bundles..."
    
    # Find and update properties files
    find . -name "*.properties" -type f | while read -r file; do
        if grep -q "com\.bwdesigngroup\.neo4j" "$file" 2>/dev/null; then
            echo "  Updating $file"
            sed -i.bak 's/com\.bwdesigngroup\.neo4j/dev.bwdesigngroup.neo4j/g' "$file"
            rm -f "${file}.bak"
        fi
    done
    
    # Update XML files if any
    find . -name "*.xml" -type f | while read -r file; do
        if grep -q "com\.bwdesigngroup\.neo4j" "$file" 2>/dev/null; then
            echo "  Updating $file"
            sed -i.bak 's/com\.bwdesigngroup\.neo4j/dev.bwdesigngroup.neo4j/g' "$file"
            rm -f "${file}.bak"
        fi
    done
}

# Update build files (if any old ones exist)
update_build_files() {
    echo "🏗️  Checking for old build files to update..."
    
    # Check for old Groovy build files
    if [ -f "build.gradle" ]; then
        echo "  ⚠️  Found old build.gradle - please replace with new build.gradle.kts"
    fi
    
    for module in common client designer gateway; do
        if [ -f "$module/build.gradle" ]; then
            echo "  ⚠️  Found old $module/build.gradle - please replace with new build.gradle.kts"
        fi
    done
}

# Update documentation and README files
update_documentation() {
    echo "📚 Updating documentation references..."
    
    find . -name "*.md" -o -name "*.txt" -o -name "*.rst" | while read -r file; do
        if grep -q "com\.bwdesigngroup\.neo4j" "$file" 2>/dev/null; then
            echo "  Updating $file"
            sed -i.bak 's/com\.bwdesigngroup\.neo4j/dev.bwdesigngroup.neo4j/g' "$file"
            rm -f "${file}.bak"
        fi
    done
}

# Validate the migration
validate_migration() {
    echo "🔍 Validating migration..."
    
    # Check if any old package references remain
    old_refs=$(find . -name "*.java" -exec grep -l "com\.bwdesigngroup\.neo4j" {} \; 2>/dev/null || true)
    if [ -n "$old_refs" ]; then
        echo "  ⚠️  Warning: Found remaining old package references in:"
        echo "$old_refs"
    else
        echo "  ✅ No old package references found in Java files"
    fi
    
    # Check if new directory structure exists
    for module in common client designer gateway; do
        new_path="$module/src/main/java/dev/bwdesigngroup/neo4j"
        if [ -d "$new_path" ]; then
            echo "  ✅ $new_path directory structure created"
        fi
    done
    
    # Check if old directories are removed
    old_dirs_exist=false
    for module in common client designer gateway; do
        old_path="$module/src/main/java/com"
        if [ -d "$old_path" ]; then
            echo "  ⚠️  Old directory still exists: $old_path"
            old_dirs_exist=true
        fi
    done
    
    if [ "$old_dirs_exist" = false ]; then
        echo "  ✅ Old directory structure cleaned up"
    fi
}

# Main execution
main() {
    echo "Starting migration process..."
    
    # Check if we're in the right directory
    if [ ! -f "settings.gradle" ] && [ ! -f "settings.gradle.kts" ]; then
        echo "❌ Error: This doesn't appear to be the root of a Gradle project"
        echo "Please run this script from the root directory of your Neo4J module project"
        exit 1
    fi
    
    # Create backup
    echo "📋 Creating backup..."
    backup_dir="migration-backup-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$backup_dir"
    
    # Backup Java source directories
    for module in common client designer gateway; do
        if [ -d "$module/src" ]; then
            cp -r "$module/src" "$backup_dir/$module-src-backup"
        fi
    done
    echo "  ✅ Backup created in $backup_dir"
    
    # Execute migration steps
    create_directory_structure
    update_java_files
    move_java_files
    update_resource_files
    update_build_files
    update_documentation
    validate_migration
    
    echo ""
    echo "🎉 Migration completed successfully!"
    echo ""
    echo "📋 Next Steps:"
    echo "1. Replace old build.gradle files with new build.gradle.kts files"
    echo "2. Update gradle wrapper: ./gradlew wrapper --gradle-version=8.7"
    echo "3. Test build: ./gradlew build"
    echo "4. Update any IDE project configurations"
    echo "5. Update CI/CD configurations if applicable"
    echo ""
    echo "📁 Backup created in: $backup_dir"
    echo "🔧 If issues occur, restore from backup and report problems"
}

# Run the migration
main "$@"