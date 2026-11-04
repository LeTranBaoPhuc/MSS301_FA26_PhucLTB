# Saga Design – Watch Management

> [TODO-4.1] Tài liệu thiết kế Saga (sinh viên tự điền trước khi code).

## Participants

| Service | Vai trò | Database | Dữ liệu tham gia Saga |
|---|---|---|---|
| MSWatch | Orchestrator | DBWatch | `Watches`, `SagaStates`, `SagaLogs` |
| MSBrand | Participant | DBBrand | `BrandWatches` |

## Saga CREATE_WATCH

| # | Local transaction | Service / DB | Compensation | Idempotent? |
|---|---|---|---|---|
| 1 | CREATE_WATCH – INSERT Watches | MSWatch / DBWatch | DELETE_WATCH | ✔ (`deleteIfExists`) |
| 2 | ASSIGN_BRAND – upsert BrandWatches | MSBrand / DBBrand | UNASSIGN_BRAND | ✔ |

## Saga UPDATE_WATCH

| # | Local transaction | Service / DB | Compensation | Idempotent? |
|---|---|---|---|---|
| 1 | UPDATE_WATCH | MSWatch / DBWatch | RESTORE_WATCH (snapshot `before`) | ✔ |
| 2 | ASSIGN_BRAND (brand mới) | MSBrand / DBBrand | ASSIGN_BRAND_BACK (brand cũ) | ✔ |

## Saga DELETE_WATCH

| # | Local transaction | Service / DB | Compensation | Idempotent? |
|---|---|---|---|---|
| 1 | UNASSIGN_BRAND | MSBrand / DBBrand | ASSIGN_BRAND_BACK | ✔ |
| 2 | DELETE_WATCH | MSWatch / DBWatch | — **PIVOT** | — |

## State machine

```
PENDING → EXECUTING → COMPLETED
               └→ FAILED → COMPENSATING → COMPENSATED
                                 └→ (compensation lỗi) dừng ở COMPENSATING
```

## Luồng thất bại điển hình (CREATE_WATCH với brandId không tồn tại)

```
MSWatch (Orchestrator)                 DBWatch                 MSBrand / DBBrand
   │ [1] CREATE_WATCH ───────────────► INSERT Watches (id=11) ✔ COMMIT
   │ [2] ASSIGN_BRAND ──── PUT /internal/brands/999/watches/11 ──► 404 Brand not found
   │      → FAILED
   │      → COMPENSATING
   │ [C1] DELETE_WATCH ──────────────► DELETE Watches id=11 ✔
   │      → COMPENSATED
   ▼  HTTP 400 {"sagaStatus":"COMPENSATED","failedStep":"ASSIGN_BRAND"}
```

## Quyết định thiết kế

1. **Orchestration** (không dùng Choreography): Luồng phân tán chỉ giữa 2 service (MSWatch và MSBrand), chuỗi xử lý ngắn, điều phối tập trung tại MSWatch giúp dễ kiểm soát trạng thái, log và kích hoạt bù trừ.
2. **Orchestrator không có `@Transactional`**: Mỗi bước là 1 local transaction độc lập đã commit. Nếu dùng `@Transactional` ở Orchestrator, khi lỗi DBWatch sẽ rollback âm thầm cả bảng log/state, làm mất dấu vết Saga.
3. **Compensation chạy LIFO, retry tối đa 3 lần**: Khi có bước thất bại, các bước đã commit thành công trước đó được bù trừ theo thứ tự ngược lại (LIFO). Mọi compensating transaction phải **Idempotent** (có thể gọi lại nhiều lần mà không sinh lỗi hoặc sai lệch dữ liệu).
4. **Pivot transaction**: Bước `DELETE_WATCH` trong Saga xóa là bước không thể bù trừ (do khóa chính `IDENTITY` không thể tái tạo lại nguyên vẹn id cũ khi rollback), nên được đặt làm **Pivot Step** ở cuối cùng, sau khi đã gỡ thành công quan hệ ở MSBrand.
5. **Validation rẻ chạy trước**: Ràng buộc dữ liệu (tên > 10 ký tự, giá ≥ 100, ngày tạo, category hợp lệ) được validate ngay tại tầng Controller/Service trước khi khởi tạo Saga, tránh lãng phí chi phí điều phối phân tán.
