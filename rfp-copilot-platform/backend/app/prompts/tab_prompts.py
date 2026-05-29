TAB_DEFINITIONS = [
    {
        "key": "current_system_challenges",
        "index": 0,
        "title": "Understanding of Current System, Challenges & Modernization Levers",
    },
    {
        "key": "ai_dlc_solution",
        "index": 1,
        "title": "AI-DLC (AI-Driven Development Life Cycle) Solution Approach",
    },
    {
        "key": "estimation",
        "index": 2,
        "title": "Estimation Approach & Estimations",
    },
    {
        "key": "target_architecture",
        "index": 3,
        "title": "Target Architecture",
    },
    {
        "key": "testing_approach",
        "index": 4,
        "title": "Testing Approach & Efforts",
    },
    {
        "key": "devops_approach",
        "index": 5,
        "title": "DevOps Approach & Efforts",
    },
    {
        "key": "db_rearchitecture",
        "index": 6,
        "title": "Database Re-architecture / Modernization",
    },
    {
        "key": "db_migration",
        "index": 7,
        "title": "Database Migration Approach & Estimates",
    },
    {
        "key": "user_onboarding",
        "index": 8,
        "title": "User Onboarding Approach",
    },
    {
        "key": "scope_assumptions_risks",
        "index": 9,
        "title": "Scope, Assumptions & Risks",
    },
]


SYSTEM_PROMPT_TEMPLATE = """You are an expert IT consulting proposal writer specializing in {section_domain}.

Given the following RFP context:
{extracted_rfp_content}

And the following organizational knowledge:
{rag_retrieved_context}

Generate a comprehensive {section_name} section for the proposal response.

Requirements:
- Be specific to the client's context (industry: {industry}, scale: {scale})
- Use tables and structured formats where applicable
- Cite specific RFP requirements when addressing them (use [RFP-REF: page X, paragraph Y] format)
- Maintain professional consulting tone
- Include quantitative data where possible
- Flag areas where assumptions are being made with [ASSUMPTION] tags
- Provide a confidence score (0-100) at the end in the format: CONFIDENCE_SCORE: XX

{additional_context}

Output format: {section_specific_format}"""


TAB_PROMPTS: dict[str, dict[str, str]] = {
    "current_system_challenges": {
        "section_domain": "IT modernization assessment and cloud migration strategy",
        "section_specific_format": """Generate the following in Markdown format:

## Executive Summary
Brief overview of the client's current state.

## Current-State Architecture Assessment
Description of the client's existing architecture as described or inferred from the RFP.

## Identified Pain Points & Challenges
### Scalability Issues
### Technical Debt
### Legacy Dependencies
### Compliance Gaps

## Business Levers
- Cost Reduction
- Time-to-Market
- Customer Experience
- Regulatory Compliance

## Technology Levers
- Cloud-Native Adoption
- Containerization
- API-First Design
- Data Modernization

## Maturity Assessment
Where the client sits on the cloud/modernization maturity curve.

## Challenge-Impact-Lever Matrix
| Challenge | Impact | Proposed Lever | Priority |
|-----------|--------|----------------|----------|
| ... | ... | ... | ... |

CONFIDENCE_SCORE: XX""",
    },
    "ai_dlc_solution": {
        "section_domain": "AI-augmented software development lifecycle and DevOps automation",
        "section_specific_format": """Generate the following in Markdown format:

## AI-Driven Development Life Cycle Overview

## Phase-by-Phase AI Integration

### 1. Discovery & Analysis
- AI-powered code scanning, dependency mapping, complexity analysis
- Tools and approaches

### 2. Design
- AI-assisted architecture recommendations
- Pattern matching from similar engagements

### 3. Development
- Copilot-assisted code generation
- Automated refactoring
- Code translation capabilities (e.g., COBOL to Java)

### 4. Testing
- AI-generated test cases
- Intelligent test prioritization
- Synthetic data generation

### 5. Deployment
- AI-driven release risk scoring
- Canary analysis

### 6. Operations
- AIOps for monitoring
- Anomaly detection
- Self-healing capabilities

## Toolchain Recommendations
| Phase | Tools | Purpose |
|-------|-------|---------|
| ... | ... | ... |

## Governance Model
Human-in-the-loop checkpoints for AI-generated artifacts.

## AI-DLC Lifecycle Diagram Description
Describe the AI-DLC lifecycle wheel with tools mapped to each phase.

CONFIDENCE_SCORE: XX""",
    },
    "estimation": {
        "section_domain": "IT project estimation, effort analysis, and resource planning",
        "section_specific_format": """Generate the following in Markdown format:

## Estimation Methodology
Description of approach: Function Point Analysis, Story Points, T-shirt sizing, or hybrid.

## Phase-Wise Effort Breakdown
| Phase | Effort (person-days) | Duration (weeks) |
|-------|---------------------|-------------------|
| Discovery | ... | ... |
| Build | ... | ... |
| Test | ... | ... |
| Deploy | ... | ... |
| Hypercare | ... | ... |

## Role-Wise Effort Breakdown
| Role | Count | Effort (person-days) |
|------|-------|---------------------|
| Solution Architect | ... | ... |
| Tech Lead | ... | ... |
| Senior Developer | ... | ... |
| Developer | ... | ... |
| QA Engineer | ... | ... |
| DevOps Engineer | ... | ... |
| Project Manager | ... | ... |

## Module/Workstream-Wise Effort
| Module | Complexity | Effort (person-days) | Duration (weeks) | Team Composition |
|--------|-----------|---------------------|-------------------|------------------|
| ... | ... | ... | ... | ... |

## Assumptions
Key assumptions driving the estimates.

## Contingency Buffer
Rationale for contingency (typically 10-20%).

## Summary
- Total Effort: X person-days
- Total Duration: X weeks
- Cost Range: (if pricing info available)

CONFIDENCE_SCORE: XX""",
    },
    "target_architecture": {
        "section_domain": "cloud-native architecture design, microservices, and distributed systems",
        "section_specific_format": """Generate the following in Markdown format:

## Architecture Vision

## Recommended Architecture Patterns
For each pattern, explain when and why to use it:
- **Microservices Architecture**
- **Event-Driven Architecture**
- **Event Streaming** (Kafka/Pulsar)
- **Strangler Fig Pattern**
- **Circuit Breaker**
- **CQRS/Event Sourcing**
- **API Gateway / BFF**
- **Service Mesh**

## Technology Stack Recommendations
| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Cloud Provider | ... | ... |
| Runtime | ... | ... |
| Messaging | ... | ... |
| Storage | ... | ... |
| Observability | ... | ... |

## Architecture Decision Records (ADRs)
### ADR-1: [Decision Title]
- **Context**: ...
- **Decision**: ...
- **Consequences**: ...

## High-Level Architecture (C4 Model)
### Context Diagram
### Container Diagram
### Component Diagram

## NFR Mapping
| NFR Requirement | Architecture Response | Implementation |
|----------------|----------------------|----------------|
| Scalability | ... | ... |
| Availability | ... | ... |
| Security | ... | ... |
| Performance | ... | ... |

CONFIDENCE_SCORE: XX""",
    },
    "testing_approach": {
        "section_domain": "software testing strategy, test automation, and quality assurance",
        "section_specific_format": """Generate the following in Markdown format:

## Testing Strategy Overview

## Test Pyramid
### Unit Testing
### Integration Testing
### Contract Testing
### End-to-End Testing
### Performance Testing
### Security Testing

## AI-Augmented Testing
- Auto-generated test cases from requirements
- Visual regression testing
- Chaos engineering / resilience testing
- AI-based test impact analysis

## Test Environment Strategy
- Ephemeral environments
- Test data management approach

## Testing Tools
| Category | Tool | Purpose |
|----------|------|---------|
| Unit | Jest/JUnit | ... |
| E2E | Cypress/Playwright | ... |
| Performance | K6/Gatling | ... |
| Security | OWASP ZAP | ... |
| Contract | Pact | ... |

## Effort Breakdown
| Test Type | Scope | Tools | Effort (%) | Automation Target (%) |
|-----------|-------|-------|-----------|----------------------|
| ... | ... | ... | ... | ... |

## Defect Prediction & Quality Gates

CONFIDENCE_SCORE: XX""",
    },
    "devops_approach": {
        "section_domain": "DevOps engineering, CI/CD pipelines, and platform engineering",
        "section_specific_format": """Generate the following in Markdown format:

## DevOps Strategy Overview

## CI/CD Pipeline Design
Build -> Test -> Scan -> Deploy -> Verify

## GitOps Workflow
ArgoCD/Flux implementation approach.

## Infrastructure as Code
Terraform/Pulumi/CloudFormation strategy.

## Container Orchestration
Kubernetes/ECS setup and management.

## Environment Strategy
| Environment | Purpose | Promotion Criteria |
|------------|---------|-------------------|
| Dev | ... | ... |
| QA | ... | ... |
| Staging | ... | ... |
| Production | ... | ... |

## Observability Stack
- **Logging**: ELK/Loki
- **Metrics**: Prometheus/Datadog
- **Tracing**: Jaeger/OpenTelemetry

## Security Integration
SAST, DAST, SCA, secrets management, policy-as-code (OPA).

## Release Strategy
Blue-Green, Canary, Feature Flags.

## Effort Estimates
| Activity | Setup Effort | Ongoing Effort | Team |
|----------|-------------|---------------|------|
| ... | ... | ... | ... |

## Platform Engineering Approach

CONFIDENCE_SCORE: XX""",
    },
    "db_rearchitecture": {
        "section_domain": "database architecture, data modernization, and polyglot persistence",
        "section_specific_format": """Generate the following in Markdown format:

## Current Database Landscape Assessment

## Target Database Strategy
### Polyglot Persistence Approach
### Relational Databases
- Current -> Managed relational (RDS/Aurora/Cloud SQL)
### NoSQL Options
- DynamoDB, MongoDB Atlas, Cosmos DB
### Caching Layer
- Redis/ElastiCache
### Data Warehouse / Lakehouse
- Snowflake, BigQuery, Databricks

## Schema Modernization
Monolithic schema -> Domain-driven bounded context schemas.

## Data Decomposition Patterns
| Pattern | Use Case | Pros | Cons |
|---------|----------|------|------|
| Database-per-service | ... | ... | ... |
| Shared database (interim) | ... | ... | ... |
| CQRS | ... | ... | ... |

## Data Governance, Lineage & Cataloging

CONFIDENCE_SCORE: XX""",
    },
    "db_migration": {
        "section_domain": "database migration strategy, data validation, and cutover planning",
        "section_specific_format": """Generate the following in Markdown format:

## Migration Methodology
Assess -> Plan -> Migrate -> Validate -> Cutover

## Migration Patterns
| Pattern | Description | When to Use |
|---------|------------|-------------|
| Lift-and-shift (Rehost) | ... | ... |
| Re-platform | ... | ... |
| Re-architect | ... | ... |

## Migration Tools
AWS DMS, Azure Database Migration Service, GCP Datastream, ora2pg, pgloader.

## Data Validation Strategy
Row counts, checksums, reconciliation queries.

## Downtime Strategy
Zero-downtime (CDC-based) vs. maintenance window.

## Rollback Plan

## Effort Estimates
| Database | Size | Complexity | Migration Type | Effort (person-days) | Duration (weeks) |
|----------|------|-----------|---------------|---------------------|-------------------|
| ... | ... | ... | ... | ... | ... |

## Risk Factors
Data volume, stored procedure migration, application coupling.

CONFIDENCE_SCORE: XX""",
    },
    "user_onboarding": {
        "section_domain": "user onboarding strategy, change management, and organizational readiness",
        "section_specific_format": """Generate the following in Markdown format:

## Onboarding Strategy Options
### Big Bang
### Phased/Wave-Based
### Pilot -> Expand (Recommended)

## Platform Enablement Steps
1. Pilot users with feature flags
2. Training and documentation delivery
3. Feedback loop and iteration
4. Progressive rollout with monitoring
5. Full production cutover

## Change Management & Communication Plan

## Training Approach
- Self-service documentation
- Instructor-led training
- In-app guidance (WalkMe/Pendo)

## Success Metrics
| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Adoption Rate | ... | ... |
| Support Ticket Volume | ... | ... |
| User Satisfaction (NPS/CSAT) | ... | ... |

## Rollback/Fallback Plan Per Wave

CONFIDENCE_SCORE: XX""",
    },
    "scope_assumptions_risks": {
        "section_domain": "project scope definition, risk management, and stakeholder alignment",
        "section_specific_format": """Generate the following in Markdown format:

## In-Scope
Extracted from RFP requirements, mapped to deliverables.

## Out-of-Scope
Explicitly called out exclusions with rationale.

## Assumptions
### Client-Side Assumptions
- Access, resources, decisions, environments

### Technical Assumptions
- APIs available, data quality, infrastructure readiness

### Commercial Assumptions
- Rate card, billing model, change request process

## Risks & Mitigations
| Risk | Probability | Impact | Mitigation Strategy | Owner |
|------|------------|--------|-------------------|-------|
| ... | ... | ... | ... | ... |

## Dependencies
External systems, third-party vendors, client decisions.

## Constraints
Timeline, budget, regulatory, technology mandates.

CONFIDENCE_SCORE: XX""",
    },
}
