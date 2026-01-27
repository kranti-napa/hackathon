# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
[KRANTI:]
- Key challenges: data fragmentation across WMS/ERP/HR/payroll/carrier systems, inconsistent cost definitions, timing differences (accrual vs cash), and shared-cost allocation (overhead, management, shared equipment).
- Cost model: define a clear cost taxonomy (labor, inventory holding, transportation, overhead) and cost objects (warehouse, store, order, SKU, lane). Establish allocation drivers (labor hours, storage cubic feet, picks, throughput, distance, weight/volume).
- Data quality: ensure clean master data (warehouse/store IDs, business unit codes), consistent time granularity, and unified chart of accounts mapping.
- Granularity: decide the minimum level needed for decisions (daily vs weekly, SKU vs category) to avoid noise and manage compute cost.
- Governance: align finance and ops on ownership, approvals, and auditability; maintain traceability from source transactions to allocated costs.
- Past experience: common root causes are mismatched identifiers and late carrier invoices; mitigations include reference data management and periodic reconciliation.
- Questions to ask: What is the primary decision the cost model supports? Which costs are controllable by ops? What SLAs for data freshness? What tolerance for allocation error?

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
[KRANTI:]
- Strategies: labor productivity improvements (slotting, pick-path optimization, labor standards), inventory optimization (safety stock tuning, slow-mover reduction), transportation optimization (mode mix, consolidation, carrier contracts), facility optimization (energy, space utilization), and automation where ROI is proven.
- Identification: baseline costs by cost object, run Pareto (top 20% drivers), correlate with KPIs (OTIF, cycle time), and simulate impacts.
- Prioritization: rank by ROI, payback, operational risk, and dependency complexity. Separate quick wins (process changes) from capex initiatives.
- Implementation: pilot in one warehouse, define success metrics, change management, then scale with standardized playbooks.
- Expected outcomes: lower cost per order, reduced variance, improved predictability, and maintained or improved service metrics.
- Questions: What constraints are non-negotiable (service level, regulatory, labor agreements)? What data is required to validate benefits?

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
[KRANTI:]
- Importance: finance is the system of record; integration ensures cost accuracy, timely close, and compliance while enabling operational decisions.
- Benefits: real-time visibility, fewer manual reconciliations, consistent reporting, and faster variance analysis.
- Integration approach: event-driven ingestion for operational costs (WMS, TMS), batch for accounting close, and a canonical cost schema mapped to the chart of accounts.
- Data synchronization: use idempotent events, late-arriving adjustments handling, and reconciliation reports. Implement data contracts and versioning.
- Security/compliance: least privilege, audit trails, and segregation of duties.
- Questions: What systems are authoritative for which cost elements? Required latency? How are corrections handled?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
[KRANTI:]
- Importance: supports capacity planning, staffing, inventory investment, and budget accountability.
- Design considerations: historical cost trends, seasonality, demand forecasts, growth plans, and known one-off events.
- Modeling: driver-based forecasting (orders, lines, cubic volume, labor hours) with scenario planning (best/base/worst).
- Accuracy: align forecast intervals with planning cadence, include variance tracking and feedback loops.
- Governance: approval workflows, threshold alerts, and clear ownership for forecast inputs.
- Questions: What is the planning horizon? Which drivers are most predictive? What tolerance for variance is acceptable?

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
[KRANTI:]
- Preserve cost history to maintain continuity in reporting, enable trend analysis, and support audits and future benchmarking.
- Avoid cost distortion: new warehouse startup costs should not overwrite legacy operational baselines.
- Approach: archive old warehouse with immutable history; create new warehouse entity with a new operational lifecycle while reusing the business unit code with a versioned effective date.
- Budget impact: separate transition costs and track them against a migration budget; define guardrails for the new warehouse’s steady-state cost targets.
- Questions: How will reporting distinguish legacy vs new periods? What transition costs must be capitalized vs expensed?

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.

**Key information to gather (solution architect view):**
- Business goals: primary decisions the tool must enable (cost per order, warehouse profitability, service-level tradeoffs).
- Stakeholders and governance: finance, ops, IT ownership, and approval workflows.
- Data landscape: systems of record, data quality issues, identifiers, and integration constraints.
- Reporting requirements: KPIs, required granularity, SLAs, and regulatory/audit needs.
- Change impact: process changes needed, training, and operating model updates.
- Technical constraints: existing platforms, security/compliance, and integration patterns.
- Success criteria: measurable targets, adoption milestones, and timeline.
