# AI Implementation Guide

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines how AI coding assistants should contribute to the Project Analytics codebase.

Its objective is to ensure that all AI-generated code remains consistent with the architecture, coding standards, and long-term vision of the project.

This document complements `AI_INSTRUCTIONS.md` and serves as the implementation reference for any AI agent working on the repository.

---

# 2. General Principles

Every AI implementation must be:

- Correct
- Maintainable
- Modular
- Tested
- Documented
- Consistent

The AI should prioritize correctness over speed.

---

# 3. Mandatory Reading Order

Before writing any code, the AI must read the documentation in the following order:

1. AI_INSTRUCTIONS.md
2. README.md
3. 00_Project_Vision.md
4. 01_Product_Requirements.md
5. 02_System_Architecture.md
6. 03_Domain_Model.md
7. 04_Backend_Architecture.md
8. 05_Frontend_Architecture.md
9. 06_Database_Design.md
10. 07_API_Specification.md
11. 08_Analytics_Engine.md
12. 09_Security.md
13. 10_UI_UX_Guidelines.md
14. 11_Development_Roadmap.md
15. 12_AI_Implementation_Guide.md
16. 13_Project_State.md

Only after reading these documents should implementation begin.

---

# 4. Repository Analysis

Before modifying the project, the AI shall:

- Analyze the repository structure.
- Detect existing modules.
- Search for reusable services.
- Search for reusable DTOs.
- Search for reusable entities.
- Search for existing APIs.
- Identify dependencies.
- Understand the current implementation status.

Never create duplicate functionality.

---

# 5. Implementation Workflow

Every implementation should follow this sequence:

1. Understand the requirement.
2. Identify affected modules.
3. Reuse existing code whenever possible.
4. Implement the feature.
5. Write tests.
6. Verify compilation.
7. Update documentation.
8. Update `Project_State.md`.

---

# 6. Coding Standards

The AI shall:

- Follow SOLID principles.
- Use meaningful names.
- Prefer composition over inheritance.
- Keep methods short.
- Keep classes focused.
- Eliminate duplicated code.

---

# 7. Backend Rules

Backend implementations shall:

- Keep controllers thin.
- Place business logic in services and domain classes.
- Use DTOs for API communication.
- Never expose JPA entities.
- Use constructor injection.
- Validate inputs.
- Throw meaningful exceptions.

---

# 8. Frontend Rules

Frontend implementations shall:

- Use Standalone Components.
- Use Angular Signals.
- Organize by feature.
- Prefer Reactive Forms.
- Consume backend DTOs directly.
- Avoid business calculations.
- Reuse shared components.

---

# 9. Database Rules

The AI shall:

- Use Flyway migrations.
- Preserve referential integrity.
- Avoid unnecessary schema changes.
- Create indexes only when justified.
- Never modify production data manually.

---

# 10. API Rules

REST APIs shall:

- Follow documented endpoints.
- Use appropriate HTTP verbs.
- Return consistent response structures.
- Validate requests.
- Document changes with OpenAPI.

---

# 11. Testing Requirements

Every feature must include:

- Unit tests
- Integration tests (when applicable)
- API tests
- Frontend tests (where applicable)

Business logic must not be merged without automated tests.

---

# 12. Documentation Updates

Whenever a feature changes:

- Update relevant documentation.
- Update diagrams if necessary.
- Update API documentation.
- Update `Project_State.md`.

Documentation and implementation must remain synchronized.

---

# 13. Refactoring Guidelines

Refactoring is allowed only when it:

- Improves readability.
- Removes duplication.
- Improves performance.
- Improves maintainability.

Refactoring must not change observable behavior unless explicitly requested.

---

# 14. Performance Guidelines

The AI should:

- Avoid unnecessary database queries.
- Minimize API calls.
- Reuse cached data when appropriate.
- Keep frontend rendering efficient.
- Avoid premature optimization.

---

# 15. Security Guidelines

The AI must:

- Validate all inputs.
- Protect sensitive endpoints.
- Never hardcode secrets.
- Respect authentication and authorization rules.
- Follow the security architecture document.

---

# 16. Error Handling

Errors should:

- Be meaningful.
- Be logged appropriately.
- Never expose internal implementation details.
- Use consistent exception types.

---

# 17. Git Practices

Each implementation should:

- Focus on a single logical change.
- Avoid unrelated modifications.
- Preserve existing formatting where practical.
- Keep commits small and reviewable (when working in a Git workflow).

---

# 18. Quality Checklist

Before considering a task complete, verify:

- Code compiles.
- Tests pass.
- Documentation updated.
- No duplicated logic.
- Architecture respected.
- Naming consistent.
- No obvious security issues.
- No unnecessary complexity.

---

# 19. Anti-Patterns to Avoid

The AI should avoid:

- God classes
- Massive controllers
- Business logic in the frontend
- Copy-paste implementations
- Circular dependencies
- Hardcoded configuration
- Unused code
- Premature abstraction

---

# 20. Completion Procedure

At the end of every implementation:

1. Verify functionality.
2. Run relevant tests.
3. Update documentation.
4. Update `Project_State.md`.
5. Summarize completed work.

---

# End of Document