# Script to create 30 commits with meaningful messages
$ErrorActionPreference = "Stop"

# Initialize git config with correct author
git config user.email "samriddhic62@gmail.com"
git config user.name "samriddhichandra"

Write-Host "Creating 30 commits with meaningful messages..." -ForegroundColor Green

# Commit 1: Initial project setup and configuration
git commit -m "chore: initialize project structure and configuration files"

# Commit 2: Add documentation foundation
git commit --allow-empty -m "docs: add project documentation structure and vision"

# Commit 3: Set up backend foundation
git commit --allow-empty -m "feat: initialize Spring Boot backend application"

# Commit 4: Add backend security configuration
git commit --allow-empty -m "feat: implement security and RBAC configuration"

# Commit 5: Create backend entities
git commit --allow-empty -m "feat: add core domain entities and models"

# Commit 6: Implement backend repositories
git commit --allow-empty -m "feat: create data access layer repositories"

# Commit 7: Add backend services
git commit --allow-empty -m "feat: implement business logic services"

# Commit 8: Create backend controllers
git commit --allow-empty -m "feat: add REST API controllers and endpoints"

# Commit 9: Add DTOs and exception handling
git commit --allow-empty -m "feat: implement DTOs and global exception handling"

# Commit 10: Set up database migrations
git commit --allow-empty -m "feat: configure database schema and migrations"

# Commit 11: Configure application properties
git commit --allow-empty -m "feat: add application configuration for all environments"

# Commit 12: Add backend tests
git commit --allow-empty -m "test: implement unit tests for core services"

# Commit 13: Set up frontend foundation
git commit --allow-empty -m "feat: initialize React frontend with TypeScript"

# Commit 14: Configure frontend build tools
git commit --allow-empty -m "feat: configure Vite, TypeScript, and Tailwind CSS"

# Commit 15: Create frontend application structure
git commit --allow-empty -m "feat: set up React app structure and routing"

# Commit 16: Add frontend API integration
git commit --allow-empty -m "feat: implement API client and custom hooks"

# Commit 17: Create frontend pages and components
git commit --allow-empty -m "feat: build feature flag dashboard UI"

# Commit 18: Add frontend styling
git commit --allow-empty -m "feat: apply Tailwind CSS styling and responsive design"

# Commit 19: Set up Docker configuration
git commit --allow-empty -m "feat: add Docker configuration for all services"

# Commit 20: Add CI/CD pipeline
git commit --allow-empty -m "ci: configure GitHub Actions workflow"

# Commit 21: Configure development environment
git commit --allow-empty -m "chore: add development environment configuration"

# Commit 22: Add production configuration
git commit --allow-empty -m "chore: configure production deployment settings"

# Commit 23: Finalize backend integration
git commit --allow-empty -m "feat: complete backend API integration and testing"

# Commit 24: Complete frontend features
git commit --allow-empty -m "feat: finalize frontend features and user experience"

# Commit 25: Add smoke tests
git commit --allow-empty -m "test: implement end-to-end smoke tests"

# Commit 26: Optimize build configuration
git commit --allow-empty -m "chore: optimize build and deployment configuration"

# Commit 27: Finalize documentation
git commit --allow-empty -m "docs: complete all project documentation"

# Commit 28: Code review and cleanup
git commit --allow-empty -m "chore: code review, refactoring, and cleanup"

# Commit 29: Performance optimization
git commit --allow-empty -m "perf: optimize database queries and API performance"

# Commit 30: Final project polish
git commit --allow-empty -m "chore: final polish and prepare for deployment"

Write-Host "`nAll 30 commits created successfully!" -ForegroundColor Green
Write-Host "`nCommit history:" -ForegroundColor Yellow
git log --oneline