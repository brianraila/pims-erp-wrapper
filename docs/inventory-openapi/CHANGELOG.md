# Inventory OpenAPI changelog

All notable revisions to the canonical **`inventory-api-v1.yaml`** artifact.

## 1.2.0 — unreleased

- Closed the Inventory & Stock reporting field gaps requested against `PIMS-Inventory-Stock-ERPNext.postman_collection.json`:
  - `GET /inventory/reports/stock-overview` (`low_stock`): added `id`, `supplier`, `lead_time_days`, `supplier_unit_cost`, `amount`, `reorder_quantity`.
  - `GET /inventory/reports/stock-overview` (`expiring`): added `id` (batch-scoped).
  - `GET /inventory/reports/stock-analytics`: added `total_transactions`, `avg_order_value`, `comparison`.
  - `GET /inventory/reports/stock-value-by-category`: added `revenue`, `profit`, `units`, `percentage`, `metric`.
  - `GET /inventory/products`: added `reorder_quantity`, `preferred_supplier`, `supplier_unit_cost`, `last_movement_date`, `earliest_expiry_date`; `created_at`/`updated_at` now populated (previously always empty strings).
  - New endpoint **`GET /inventory/reports/slow-moving`**.
- Frappe **`pims`** app: new patch **`create_pims_item_cost_fields`** (v1_13) adds **`Item.custom_pims_lead_time_days`** (Int) and **`Item.custom_pims_supplier_unit_cost`** (Float).
- **Fixed a pre-existing field-naming bug**: `Item.custom_pims_supplier` was never a real ERPNext field — `create_custom_fields()` does not auto-prepend `custom_` to a fieldname (that only happens via the Customize Form UI); the actual column created by `v1_0` is **`pims_supplier`** (no prefix), same as `pims_reorder_level`/`pims_maximum_stock`/etc. Requesting the wrong name is silently dropped by ERPNext's list API rather than erroring, so this went undetected. Corrected the Java-side read (`ErpNextDoc`/`InventoryService.ITEM_FIELDS`) for `pims_supplier` only, since it is the one field with no working fallback (unlike `reorder_level`/`maximum_stock`, which also work via the `ItemExtrasCodec` description-embedded channel). **The same mismatch still affects roughly 18 other `custom_pims_*` reads and writes** in `ErpNextDoc.java` and `ProductInventoryService.java` (ppb_code, ndc_code, controlled-substance flags, cold-chain, photosensitive, generic_name, unit_of_measure, and the corresponding create/update body-building code) — those were left as-is since fixing them is a larger, separate change with its own review, but they should be audited and corrected in a follow-up.
- Frappe **`pims`** app: patch **`create_pims_item_custom_fields`** adds **`Item.custom_pims_*`** columns; Java maps create/update/read alongside the legacy description fragment.
- **Breaking:** Removed legacy REST endpoints **`GET /inventory/items`**, **`GET /inventory/items/{id}`**, **`GET /inventory/batches`**, **`GET|POST /inventory/adjustments`**, **`GET|POST /inventory/transfers`**. Use **`/inventory/products`**, **`/inventory/products/{product_id}/batches`**, **`/inventory/products/{product_id}/adjustments`** per this specification.
- Renamed implementation packages **`inventory.portal.*`** → **`inventory.web.*`**; types **`PortalSchemas`** → **`InventoryApiSchemas`**, **`PortalInventoryService`** → **`ProductInventoryService`**, and HTTP controllers accordingly (no URL path changes).
- Documented **`GET|POST /inventory/purchase-orders`** in this OpenAPI artifact (purchase order JSON uses **camelCase** fields to match the Java DTOs).

## 1.0.0 — 2026-05-18

- Initial published OpenAPI 3.0.3 describing the Inventory HTTP API surface (`/inventory/products`, terminology, manufacturers).
- Paths are anchored under **`servers[].url`** = `/api/v1` (consistent with **`ms-pims-api-gateway`** route prefix); resource paths retain `/inventory/...`, `/terminology/...`, `/manufacturers`.
- Resolved schema polish from the upstream draft:
  - **`ProductDraftRequest.data`** modeled as **`DraftData`** (`type: object` with optional mirrored fields instead of brittle `allOf` on required fields).
  - **`Batch` POST** documents JSON (`Batch`) vs multipart CSV (**`BatchBulkUploadResult`**) without ambiguous `discriminator`.
