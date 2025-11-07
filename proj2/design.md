# BoozeBuddies – Architecture & Domain Design (Draft)

Status: Draft (created from inferred requirements; please reconcile with Google Docs content and update)

---

## 1. Vision / Scope

Platform for on-demand alcohol ordering & delivery: users browse products from merchants, place orders, drivers pick up & deliver, with compliance (age verification), payment processing, tracking, and ratings.

## 2. High-Level Architecture

We will start as a modular monolith (single deployable Maven project) with clear, decoupled packages enabling future extraction to microservices.

Layers (inside each bounded context):

1. API (web/controllers, DTO mappers)
2. Application (use cases / orchestration, transactional boundaries)
3. Domain (entities, value objects, domain services, domain events interfaces)
4. Infrastructure (persistence adapters, messaging, external service clients, configuration)
5. Shared / Common (cross-cutting concerns: error model, security utilities, event bus abstraction)

## 3. Proposed Package Layout

```text
src/main/java/com/boozebuddies/
  common/
    config/ (Datalnitializer.java, PasswordEncoderConfig.java, SecurityConfig.java)
    error/ (ProblemDetails, exceptions)
    event/ (DomainEvent interface, dispatcher abstraction)
    util/
  user/
    api/  (UserController, DTOs)
    application/ (RegisterUserUseCase, VerifyAgeUseCase)
    domain/ (User, UserId, Role, AgeVerificationStatus, repository interfaces)
    infrastructure/ (JpaUserRepository, AgeVerificationGateway)
  merchant/
    api/
    application/
    domain/ (Merchant, ProductCatalog aggregate root?)
    infrastructure/
  product/
    api/
    application/
    domain/ (Product, SKU, Category, InventoryItem)
    infrastructure/
  order/
    api/
    application/ (PlaceOrder, CancelOrder, UpdateOrderStatus)
    domain/ (Order, OrderId, OrderItem, OrderStatus enum, PaymentState)
    infrastructure/ (repository impls)
  delivery/
    api/
    application/ (AssignDriver, UpdateDeliveryStatus)
    domain/ (Delivery, DeliveryStatus, TrackingEvent)
    infrastructure/
  driver/
    api/
    application/
    domain/ (Driver, DriverAvailability, VehicleInfo)
    infrastructure/
  payment/
    api/
    application/ (Authorize, Capture, Refund)
    domain/ (Payment, PaymentId, PaymentStatus, Money VO)
    infrastructure/ (PaymentGatewayAdapter)
  notification/
    application/ (SendOrderStatusNotification)
    infrastructure/ (EmailSender, SmsSender, PushAdapter)
  rating/
    api/
    domain/ (Rating, Review, Score)
  security/
    auth/ (JWT utilities, filters)
    acl/ (authorization rules)

resources/
  application-docker.properties
  application.properties
  flyway.conf
  db/migration/
    V1_baseline_schema.sql
    V2_seed_demo_data.sql
    V3__add_mock_locations.sql
    V4_seed_test_drivers_and_orders.sql
```text

Tests mirror structure under `src/test/java/...` with `*Test` and integration tests possibly under `src/test/java/.../it/` or using Failsafe with `src/it/java` later.

## 4. Core Domain Entities (Initial)

| Entity | Summary | Key Fields / Value Objects |
|--------|---------|----------------------------|
| User | Customer account | id, name, dob, roles, ageVerificationStatus |
| Merchant | Storefront | id, name, licenseNumber, address, hours, status |
| Product | Alcohol product SKU | id, merchantId, name, category, abv, price, inventoryRef |
| InventoryItem | Stock tracking | productId, quantityAvailable, reservedQty |
| Order | Purchase aggregate | id, userId, merchantId, items, status, total, paymentState, deliveryId? |
| OrderItem | Line item | productId, quantity, unitPrice, subtotal |
| Delivery | Physical dispatch | id, orderId, driverId, status, pickupTime, dropoffTime |
| Driver | Delivery agent | id, name, license, availability, rating |
| Payment | Transaction record | id, orderId, amount, status, providerRef |
| Rating/Review | Feedback | id, raterUserId, targetType (driver/merchant/product), targetId, score, comment |
| Address (VO) | Location | line1, line2, city, state, postalCode, lat/long |
| Money (VO) | Currency-safe | amount, currency |
| AuditEvent | Compliance log | id, type, refId, payload, timestamp |

## 5. Key Use Cases (Sample)

User:

- Register / login
- Age verification submission & approval
- Browse/search products (by merchant, category, keywords)
Order:

- Add item to cart (in-memory or persistent draft order)
- Place order (reserve inventory, authorize payment, emit OrderPlaced)
- Cancel order (release inventory, refund if captured)
Delivery:

- Assign driver (manual or auto-match by proximity & availability)
- Update status: CREATED → ACCEPTED → PICKED_UP → IN_TRANSIT → DELIVERED / FAILED / CANCELED
Driver:

- Set availability
- Accept / decline assignment
Payment:

- Authorize on place
- Capture on delivery confirmation
Notification:

- Push/email/SMS on status transitions
Rating:

- Rate merchant & driver after delivery

## 6. Domain Events (Initial List)

```text
UserRegistered
UserAgeVerified
OrderPlaced
OrderCancelled
InventoryReserved
InventoryReleased
PaymentAuthorized
PaymentCaptureRequested
PaymentCaptured
PaymentFailed
DeliveryDriverAssigned
DeliveryStatusChanged
OrderDelivered (terminal event)
ReviewSubmitted
```text

Events allow loose coupling (e.g., notification & analytics subscribe without changing order service code).

## 7. State Machines

OrderStatus: DRAFT → PLACED → ACCEPTED → IN_DELIVERY → COMPLETED | CANCELLED | FAILED

DeliveryStatus: CREATED → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED | FAILED | CANCELLED

PaymentStatus: INIT → AUTHORIZED → CAPTURED | REFUNDED | FAILED

## 8. Persistence Strategy

- Start with a single relational DB (e.g., Postgres/MySQL) schemas separated logically by table name prefixes or packages.
- Tables: users, merchants, products, inventory, orders, order_items, deliveries, payments, ratings, audit_events.
- Use optimistic locking (`version` column) for aggregates like Order & InventoryItem to avoid lost updates.

## 9. Transaction & Consistency Approach

- Place Order: single transaction (create Order, reserve inventory, record Payment authorization stub). Emits events after commit.
- Delivery updates: small transactions updating status & timestamps.
- Saga (future): If payment capture fails at delivery, mark Order for manual intervention.

## 10. Security & Compliance

- JWT-based user auth (future `security` module).
- Age verification required before allowing placement of alcohol orders.
- Role-based access: USER, MERCHANT_ADMIN, DRIVER, SYSTEM.
- PII handling: Avoid logging full addresses or payment tokens.

## 11. Validation Strategy

- API layer: syntactic validation (Bean Validation / manual) → 400 on failure.
- Domain layer: invariants (e.g., cannot add item to non-DRAFT order) throw domain exceptions converted to 409/422.

## 12. Error Handling (Unified Model)

Return Problem Details JSON shape: `{ type, title, status, detail, traceId }`.

## 13. Observability

- Structured logging with correlation IDs (orderId, deliveryId).
- Metrics: order_placed_total, delivery_duration_seconds, payment_failure_total.
- Tracing: potential later with OpenTelemetry.

## 14. Performance & Scaling Notes

- Inventory reservation hot path: index on (product_id, merchant_id).
- Read-heavy product catalog: potential caching layer (in-memory or Redis) later.
- Event fan-out via simple in-process publisher initially → message broker (Kafka) later.

## 15. Build & Module Evolution

Phase 1: Single Maven module.
Phase 2: Split into multi-module (`common`, `order`, `delivery`, etc.) if complexity grows.

## 16. Open Questions (Need Confirmation)

1. Is age verification synchronous (manual approval) or external API? (Assumed external adapter.)
2. Do we support partial order fulfillment? (Assumed no: all-or-nothing.)
3. Payment gateway(s)? (Assumed single provider abstraction.)
4. Real-time tracking granularity? (Assumed coarse status + optional lat/long events.)
5. Promotion/discount engine needed now? (Deferred.)

## 17. Immediate Next Implementation Steps

1. Add complete Maven `pom.xml` (groupId, artifactId, dependencies) + Maven Wrapper.
2. Scaffold package tree with placeholder classes & README per module.
3. Implement basic domain models for User, Product, Order with unit tests.
4. Introduce in-memory repositories for early iteration.
5. Add event dispatcher interface + simple synchronous implementation.

## 18. Contribution Alignment

Cross-team boundaries (per CODEOWNERS):

- Team A: order, product, merchant.
- Team B: user, driver, delivery.
Shared: payment, notification, common, security.

---
This document is a starting point; update after syncing with the authoritative Google Docs content.

---

## 19. Consolidated Use Case Coverage (Imported Offline Specs)

The provided offline specification enumerates 29+ detailed use cases (browse, order lifecycle, driver ops, payments/refunds, ratings, admin, promotions, loyalty, support, group ordering, tracking, reporting, payouts, cancellations, scheduling, address management, etc.). Below is a condensed mapping to architectural components:

| Use Case Cluster | Representative UCs | Primary Packages | Notes |
|------------------|--------------------|------------------|-------|
| Discovery & Browse | UC-01 Browse/Search | `product`, `merchant` | Search service may later index with Lucene/OpenSearch. |
| Ordering & Cart | UC-02 Place Order, UC-25 Group Order | `order`, `product`, `promotion` | Group orders introduce composite Order or GroupOrder aggregate referencing child Orders. |
| Real-Time Tracking | UC-03 Track, UC-27 Real-Time Tracking | `delivery`, `notification` | Start with polling, upgrade to SSE/WebSocket. |
| Restaurant Fulfillment | UC-04 Accept/Fulfill, UC-09 Menu Mgmt | `merchant`, `product`, `order` | Kitchen display workflow: ACCEPTED→PREPARING→READY. |
| Driver Dispatch & Execution | UC-05 Assign, UC-06 Pickup/Deliver, UC-15 Availability, UC-16 Navigation | `driver`, `delivery`, `dispatch` (future) | MVP: deterministic simple selection; future: scoring algorithm. |
| Payments & Refunds | UC-07 Payments, UC-21 Refunds | `payment`, `order` | Refactor refunds into separate Refund aggregate if complexity grows. |
| Ratings & Reviews | UC-08 Rate/Review, UC-22 Reviews | `rating` | Support dual target types (restaurant, driver). |
| Promotions & Loyalty | UC-09 Promotions, UC-19 Campaigns, UC-26 Loyalty | `promotion`, `loyalty` | Promotion validation service; loyalty points accrual via domain events. |
| Admin & Governance | UC-10 Administer, UC-29 Role Mgmt, UC-20 Reporting | `admin`, `common`, `security`, `reporting` | Reporting initially SQL views + DTOs. |
| Registration & Auth | UC-11 Register, UC-12 Log In | `user`, `security` | Add age-verification extension for alcohol. |
| Profile & Addresses | UC-13 Update Profile, UC-18 Addresses | `user` | Separate Address VO + repository. |
| Support & Disputes | UC-14 Support Interaction | `support` | Ticket entity w/ state machine (OPEN→IN_PROGRESS→RESOLVED/CLOSED). |
| Driver Earnings/Payouts | UC-23 Earnings & Payouts | `driver`, `payment` | Extend Payment flow or add Payout aggregate. |
| Cancellation & Policy | UC-24 Cancellation Mgmt | `order`, `policy` | Policy service evaluates cancellation/refund rules. |
| Scheduling / Group / Multi | UC-25 Group Ordering | `order` | Option: GroupOrder parent holds participant sub-orders. |
| Loyalty & Rewards | UC-26 Loyalty | `loyalty` | Points ledger with accrual and redemption entries. |

### 19.1 Additional Packages Introduced

```text
com.boozebuddies.policy/          (cancellation, refund, legal constraint evaluation)
com.boozebuddies.promotion/       (codes, validation, application rules)
com.boozebuddies.loyalty/         (points ledger, tier calculations)
com.boozebuddies.support/         (support tickets, chat transcript stub)
com.boozebuddies.reporting/       (ad-hoc report services, aggregators)
com.boozebuddies.compliance/      (age verification, dry zones, legal hours)
com.boozebuddies.recommendation/  (food–drink pairing, hydration tips)
```

## 20. Extended Domain Model Adjustments

| New/Refined Concept | Reason | Modeling Decision |
|---------------------|--------|-------------------|
| GroupOrder | UC-25 group ordering | Aggregate with participants list + shared constraints; may spawn child Orders for persistence simplicity. |
| LoyaltyPointLedger | UC-26 loyalty | Event-sourced or append-only entries (EARN, REDEEM, EXPIRE). |
| Promotion / Campaign | UC-09/19 promotions | Promotion entity + Rule set; apply via PromotionService returning discount lines. |
| Refund | UC-21 refunds | Separate aggregate if partial/multi-stage; else embedded value in Order with status. |
| PolicyEngine | UC-24 cancellation + legal | Service evaluating cancellation windows, dry zones, legal hours, intoxication/age gating. |
| ComplianceCheck | Alcohol restrictions | Records each gate (ID scan result, zone check) for audit. |
| DriverCertification | Alcohol delivery specialization | Value object or separate entity with status & expiry. |
| SupportTicket | UC-14 | Simple state machine; references Order/Delivery optionally. |
| Payout | UC-23 earnings & payouts | Ties driver completed deliveries to disbursement batch. |
| Recommendation | Pairing/hydration suggestions | Stateless service first; future ML candidate. |

## 21. Expanded Domain Events

Additional events beyond earlier list:

```text
GroupOrderCreated
GroupOrderFinalized
PromotionApplied
LoyaltyPointsAccrued
LoyaltyPointsRedeemed
RefundRequested
RefundApproved
RefundRejected
DriverAvailabilityChanged
DriverCertified
ComplianceCheckPassed
ComplianceCheckFailed
PolicyViolationDetected
SupportTicketOpened
SupportTicketResolved
PayoutRequested
PayoutCompleted
RecommendationGenerated (optional, async)
```

These events enable decoupling (e.g., loyalty accrues on OrderDelivered without tight coupling to OrderService logic) and facilitate reporting/analytics.

## 22. Policy & Compliance Layer

Introduce a `PolicyEngine` interface with strategy implementations:

```java
interface PolicyEngine {
  PolicyDecision evaluate(PolicyContext ctx);
}

record PolicyDecision(boolean allowed, List<String> violations) {}
```

Contexts include cancellation requests, alcohol delivery initiation, driver assignment (certification check), operational time window validation, and delivery zone geofence validation.

## 23. Revised Order State (With Group & Refund Nuances)

```
CREATED → AUTHORIZED → ACCEPTED → PREPARING → READY → PICKED_UP → EN_ROUTE → DELIVERED → CLOSED
                     ↘ CANCELLED (policy pass)
                     ↘ REFUND_PENDING → REFUNDED
```

Group Order nuance: individual participant additions keep GroupOrder in COLLECTING state until host finalizes → spawns or transitions to CREATED + AUTHORIZED.

## 24. Minimal API Surface (Refined)

| Area | Endpoint (REST) | Verb | Purpose |
|------|------------------|------|---------|
| Browse | `/api/merchants/{id}/products` | GET | List products for merchant |
| Order | `/api/orders` | POST | Create (single or group host) |
| Order | `/api/orders/{id}` | GET | Retrieve status details |
| Order | `/api/orders/{id}/accept` | POST | Restaurant accept |
| Order | `/api/orders/{id}/cancel` | POST | Cancellation (policy evaluated) |
| Group | `/api/group-orders/{id}/participants` | POST | Add participant items |
| Delivery | `/api/deliveries/assign` | POST | Assign driver |
| Delivery | `/api/deliveries/{id}/pickup` | POST | Mark pickup |
| Delivery | `/api/deliveries/{id}/delivered` | POST | Complete + proof |
| Driver | `/api/drivers/{id}/availability` | POST | Availability toggle |
| Loyalty | `/api/loyalty/{userId}` | GET | Fetch points & tier |
| Promotion | `/api/promotions/apply` | POST | Validate code for draft order |
| Refund | `/api/orders/{id}/refunds` | POST | Request refund |
| Support | `/api/support/tickets` | POST | Open support ticket |
| Support | `/api/support/tickets/{id}` | PATCH | Update/resolve ticket |

## 25. Data Model (Initial Table List)

```
users, user_addresses, drivers, driver_certifications, payouts
merchants, products, product_categories
orders, order_items, group_orders, group_order_participants, refunds
deliveries
promotions, promotion_usages
loyalty_ledger
support_tickets
policy_audit_log, compliance_checks
notifications (optional), recommendation_cache (optional)
```

## 26. Incremental Implementation Strategy (Updated)

Phase 0 (Week 1 existing): core order happy path, minimal payment mock, driver assignment, delivery completion.

Phase 1: promotions + cancellation policy + basic refunds.

Phase 2: loyalty ledger + group ordering + support tickets.

Phase 3: compliance (age, dry zones), policy engine integration, driver certifications.

Phase 4: payouts + reporting + recommendations (rule-based).

## 27. Risk & Mitigation Snapshot

| Risk | Impact | Mitigation |
|------|--------|------------|
| Scope creep from many UCs | Delivery delay | Strict phased backlog & DoR gating |
| Complex state transitions | Logic bugs | Centralized state machine + test matrix |
| Refund edge cases | Financial inaccuracies | Explicit Refund aggregate + idempotent operations |
| Group ordering concurrency | Overwrites / race | Optimistic locking + participant-level granularity |
| Policy/legal variance | Non-compliance | Externalize rules in config/policy tables |
| Event sprawl | Observability noise | Naming conventions + event versioning |

## 28. Next Author Actions

1. Team review: validate mapping vs. offline use cases provided.
2. Approve or trim initial table list to MVP scope.
3. Lock Week 1 backlog (only core order + delivery + minimal payment + basic browse).
4. Add `policy`, `promotion`, `loyalty` packages only as placeholders (avoid premature implementation).
5. Decide on persistence naming conventions (snake_case vs camelCase) and enforce via Flyway baseline.

---

End of extended draft.
